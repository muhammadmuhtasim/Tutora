package com.example.tutora.di

import android.content.Context
import com.example.tutora.data.*
import com.example.tutora.domain.*
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(impl: BookingRepositoryImpl): BookingRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideLocationManager(@ApplicationContext context: Context): android.location.LocationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context) =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun providePlacesClient(@ApplicationContext context: Context): PlacesClient =
        Places.createClient(context)

    @Provides
    @Singleton
    fun provideGeocoder(@ApplicationContext context: Context): android.location.Geocoder =
        android.location.Geocoder(context)
}
