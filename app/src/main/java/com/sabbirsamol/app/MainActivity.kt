package com.sabbirsamol.app

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // অ্যাপ চালু হওয়ার সাথে সাথে অনলাইন থেকে সময় আনা
        loadOnlinePrayerTimes()
    }

    private fun loadOnlinePrayerTimes() {
        CoroutineScope(Dispatchers.Main).launch {
            val context = this@MainActivity
            
            // ব্যাকগ্রাউন্ডে নেটওয়ার্ক চেক ও এপিআই কল করা
            val onlineTimes = withContext(Dispatchers.IO) {
                if (OnlinePrayerFetcher.isNetworkAvailable(context)) {
                    OnlinePrayerFetcher.fetchSatkhiraTimings()
                } else {
                    null
                }
            }

            if (onlineTimes != null) {
                val fajr = onlineTimes["Fajr"] ?: ""
                val sunrise = onlineTimes["Sunrise"] ?: ""
                val zohar = onlineTimes["Dhuhr"] ?: ""
                val asr = onlineTimes["Asr"] ?: ""
                val maghrib = onlineTimes["Maghrib"] ?: ""
                val isha = onlineTimes["Isha"] ?: ""

                Toast.makeText(context, "সাতক্ষীরার লাইভ সময় সফলভাবে আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ইন্টারনেট সংযোগ নেই অথবা সার্ভার থেকে সময় পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
