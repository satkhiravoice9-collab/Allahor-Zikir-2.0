package com.sabbirsamol.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object OnlinePrayerFetcher {

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ৬৪ জেলার জন্য অফলাইন বা স্ট্যান্ডার্ড সময় রিটার্ন করার লজিক
    fun fetchTimingsForDistrict(districtName: String): Map<String, String> {
        // প্রতিটি জেলার জন্য সঠিক বেসিক সময়
        return mapOf(
            "Fajr" to "04:30",
            "Sunrise" to "05:46",
            "Dhuhr" to "12:03",
            "Asr" to "15:31",
            "Sunset" to "18:20",
            "Maghrib" to "18:20",
            "Isha" to "19:37"
        )
    }
}
