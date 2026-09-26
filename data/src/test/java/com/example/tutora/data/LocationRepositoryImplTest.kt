package com.example.tutora.data

import android.location.Geocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.gms.tasks.Tasks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import android.location.LocationManager
import android.util.Log
import io.mockk.mockkStatic

class LocationRepositoryImplTest {

    private val fusedLocationClient = mockk<FusedLocationProviderClient>()
    private val locationManager = mockk<LocationManager>()
    private val placesClient = mockk<PlacesClient>()
    private val geocoder = mockk<Geocoder>()

    private lateinit var repository: LocationRepositoryImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>(), any()) } returns 0
        every { Log.w(any<String>(), any<String>()) } returns 0

        repository = LocationRepositoryImpl(
            fusedLocationClient,
            locationManager,
            placesClient,
            geocoder
        )
    }

    @Test
    fun `geocode calls findAutocompletePredictions on PlacesClient`() = runTest {
        val query = "Paris"
        val response = mockk<FindAutocompletePredictionsResponse>()
        every { response.autocompletePredictions } returns emptyList()
        
        every { 
            placesClient.findAutocompletePredictions(any<FindAutocompletePredictionsRequest>()) 
        } returns Tasks.forResult(response)

        repository.geocode(query)

        verify { 
            placesClient.findAutocompletePredictions(match { 
                it.query == query 
            }) 
        }
    }
}
