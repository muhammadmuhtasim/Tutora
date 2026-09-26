package com.example.tutora.data

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Build
import android.util.Log
import com.example.tutora.domain.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationManager: android.location.LocationManager,
    private val placesClient: PlacesClient,
    private val geocoder: Geocoder,
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): AppResult<GeoPoint> {
        val cts = CancellationTokenSource()
        return try {
            Log.d("LocationRepository", "Starting location detection (Google Fused)...")
            
            val location = withTimeoutOrNull(10000) {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token,
                ).await()
            } ?: fusedLocationClient.lastLocation.await()
            
            val finalLocation = location ?: locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            
            if (finalLocation != null) {
                Log.d("LocationRepository", "Location acquired: ${finalLocation.latitude}, ${finalLocation.longitude}")
                AppResult.Success(GeoPoint(finalLocation.latitude, finalLocation.longitude))
            } else {
                Log.e("LocationRepository", "All location providers returned null")
                AppResult.Error(AppError.Unknown("Location not available. Please ensure GPS is enabled."))
            }
        } catch (_: TimeoutCancellationException) {
            Log.e("LocationRepository", "Location request timed out")
            AppResult.Error(AppError.Unknown("Location request timed out. Please try again."))
        } catch (e: CancellationException) {
            cts.cancel()
            throw e
        } catch (e: Exception) {
            Log.e("LocationRepository", "Location detection exception", e)
            AppResult.Error(AppError.Unknown(e.message ?: "Location error", e))
        }
    }

    override suspend fun geocode(address: String): AppResult<List<LocationResult>> {
        return try {
            Log.d("LocationRepository", "Geocoding address using Places SDK: $address")
            
            val token = AutocompleteSessionToken.newInstance()
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(address)
                .build()

            val predictions = placesClient.findAutocompletePredictions(request).await()
            
            val results = coroutineScope {
                predictions.autocompletePredictions.take(5).map { prediction ->
                    async {
                        val placeFields = listOf(Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS)
                        val fetchRequest = FetchPlaceRequest.builder(prediction.placeId, placeFields).build()
                        
                        try {
                            val placeResponse = placesClient.fetchPlace(fetchRequest).await()
                            val place = placeResponse.place
                            val location = place.location
                            if (location != null) {
                                LocationResult(
                                    address = place.formattedAddress ?: prediction.getFullText(null).toString(),
                                    location = GeoPoint(location.latitude, location.longitude)
                                )
                            } else null
                        } catch (e: Exception) {
                            Log.e("LocationRepository", "Failed to fetch place details for ${prediction.placeId}", e)
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }

            Log.d("LocationRepository", "Found ${results.size} results for $address")
            AppResult.Success(results)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("LocationRepository", "Geocoding failed for $address", e)
            AppResult.Error(AppError.Unknown(e.message ?: "Geocoding error", e))
        }
    }

    override suspend fun reverseGeocode(location: GeoPoint): AppResult<String> {
        return try {
            Log.d("LocationRepository", "Reverse geocoding using Google Geocoder: ${location.latitude}, ${location.longitude}")
            
            val addresses = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) {
                            continuation.resume(it)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1) ?: emptyList()
                }
            }

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val fullAddress = (0..address.maxAddressLineIndex).joinToString(", ") { address.getAddressLine(it) }
                Log.d("LocationRepository", "Resolved full address: $fullAddress")
                AppResult.Success(fullAddress)
            } else {
                Log.w("LocationRepository", "No address found for these coordinates")
                AppResult.Error(AppError.Unknown("No address found for the current location."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("LocationRepository", "Reverse geocoding failed for ${location.latitude}, ${location.longitude}", e)
            AppResult.Error(AppError.Unknown(e.message ?: "Reverse geocoding error", e))
        }
    }
}
