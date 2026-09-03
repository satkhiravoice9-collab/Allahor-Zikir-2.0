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

    // জেলা বা থানা ভিত্তিক সময় ফেচ করার মেথড
    fun fetchTimingsForDistrict(districtName: String): Map<String, String> {
        // জেলাভেদে নির্দিষ্ট সময়ের মান রিটার্ন করার লজিক
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
