package com.sabbirsamol.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // অ্যাপের অন্যান্য ফিচার বা ইনিশিয়ালাইজেশন থাকলে এখানে কাজ করবে

        // অনলাইন থেকে সাতক্ষীরার লাইভ নামাজের সময় আনা
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

                // টেক্সটভিউগুলোতে সময়গুলো বসিয়ে দেওয়া
                findViewById<TextView>(R.id.txtFajrTime)?.text = "ফজর: $fajr"
                findViewById<TextView>(R.id.txtSunriseTime)?.text = "সূর্যোদয়: $sunrise"
                findViewById<TextView>(R.id.txtZoharTime)?.text = "যোহর: $zohar"
                findViewById<TextView>(R.id.txtAsrTime)?.text = "আসর: $asr"
                findViewById<TextView>(R.id.txtMaghribTime)?.text = "মাগরিব: $maghrib"
                findViewById<TextView>(R.id.txtIshaTime)?.text = "এশা: $isha"

                Toast.makeText(context, "সাতক্ষীরার লাইভ সময় সফলভাবে আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ইন্টারনেট সংযোগ নেই অথবা সার্ভার থেকে সময় পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
