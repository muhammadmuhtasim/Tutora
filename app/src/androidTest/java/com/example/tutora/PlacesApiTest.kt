package com.example.tutora

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.android.libraries.places.api.Places
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlacesApiTest {

    @Test
    fun testPlacesApiKeyInManifest() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val ai = appContext.packageManager.getApplicationInfo(
            appContext.packageName,
            PackageManager.GET_META_DATA
        )
        val apiKey = ai.metaData.getString("com.google.android.geo.API_KEY")
        
        assertEquals("AIzaSyAMHpUL5ChWbMd1KoIFEgrhMS096vPs8qk", apiKey)
    }

    @Test
    fun testPlacesIsInitialized() {
        // The Application class (TutoraApp) should have initialized it in onCreate
        assertTrue("Places SDK should be initialized", Places.isInitialized())
    }
}
