package com.example.tutora

import android.app.Application
import android.content.pm.PackageManager
import androidx.appfunctions.AppFunctionConfiguration
import com.example.tutora.functions.TutoraAppFunctions
import com.google.android.libraries.places.api.Places
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TutoraApp : Application(), AppFunctionConfiguration.Provider {

    @Inject
    lateinit var tutoraAppFunctions: TutoraAppFunctions

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Google Places SDK
        val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        val apiKey = ai.metaData.getString("com.google.android.geo.API_KEY")
        if (apiKey != null) {
            Places.initializeWithNewPlacesApiEnabled(this, apiKey)
        }
    }

    override val appFunctionConfiguration: AppFunctionConfiguration =
        AppFunctionConfiguration.Builder()
            .addEnclosingClassFactory(TutoraAppFunctions::class.java) { tutoraAppFunctions }
            .build()
}
