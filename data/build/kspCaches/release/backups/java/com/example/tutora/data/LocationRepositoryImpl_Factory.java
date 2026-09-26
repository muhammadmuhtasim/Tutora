package com.example.tutora.data;

import android.location.Geocoder;
import android.location.LocationManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.libraries.places.api.net.PlacesClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class LocationRepositoryImpl_Factory implements Factory<LocationRepositoryImpl> {
  private final Provider<FusedLocationProviderClient> fusedLocationClientProvider;

  private final Provider<LocationManager> locationManagerProvider;

  private final Provider<PlacesClient> placesClientProvider;

  private final Provider<Geocoder> geocoderProvider;

  private LocationRepositoryImpl_Factory(
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<LocationManager> locationManagerProvider,
      Provider<PlacesClient> placesClientProvider, Provider<Geocoder> geocoderProvider) {
    this.fusedLocationClientProvider = fusedLocationClientProvider;
    this.locationManagerProvider = locationManagerProvider;
    this.placesClientProvider = placesClientProvider;
    this.geocoderProvider = geocoderProvider;
  }

  @Override
  public LocationRepositoryImpl get() {
    return newInstance(fusedLocationClientProvider.get(), locationManagerProvider.get(), placesClientProvider.get(), geocoderProvider.get());
  }

  public static LocationRepositoryImpl_Factory create(
      Provider<FusedLocationProviderClient> fusedLocationClientProvider,
      Provider<LocationManager> locationManagerProvider,
      Provider<PlacesClient> placesClientProvider, Provider<Geocoder> geocoderProvider) {
    return new LocationRepositoryImpl_Factory(fusedLocationClientProvider, locationManagerProvider, placesClientProvider, geocoderProvider);
  }

  public static LocationRepositoryImpl newInstance(FusedLocationProviderClient fusedLocationClient,
      LocationManager locationManager, PlacesClient placesClient, Geocoder geocoder) {
    return new LocationRepositoryImpl(fusedLocationClient, locationManager, placesClient, geocoder);
  }
}
