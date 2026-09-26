package com.example.tutora.data

import com.example.tutora.domain.GeoPoint
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RadiusMatchingTest {

    @Test
    fun `test distance calculation within 3km`() {
        val center = GeoLocation(23.8103, 90.4125) // Dhaka center
        val withinRadius = GeoLocation(23.8203, 90.4125) // approx 1.1km away
        val outsideRadius = GeoLocation(23.8503, 90.4125) // approx 4.4km away

        val distWithin = GeoFireUtils.getDistanceBetween(center, withinRadius)
        val distOutside = GeoFireUtils.getDistanceBetween(center, outsideRadius)

        assertTrue("Should be within 3000m", distWithin <= 3000.0)
        assertTrue("Should be outside 3000m", distOutside > 3000.0)
    }

    @Test
    fun `test geohash consistency`() {
        val loc = GeoLocation(23.8103, 90.4125)
        val hash = GeoFireUtils.getGeoHashForLocation(loc)
        
        assertEquals("wh0r3", hash.substring(0, 5)) // Corrected geohash prefix for Dhaka
    }
}
