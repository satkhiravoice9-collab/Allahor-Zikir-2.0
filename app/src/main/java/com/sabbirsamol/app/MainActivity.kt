package com.sabbirsamol.app

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // অ্যাপ চালু হওয়ার সাথে সাথে অনলাইন থেকে সময় এনে লোড করার ফাংশন
        loadOnlinePrayerTimes()
    }

    private fun loadOnlinePrayerTimes() {
        lifecycleScope.launch {
            // ইন্টারনেট কানেকশন চেক করা
            if (OnlinePrayerFetcher.isNetworkAvailable(this@MainActivity)) {
                // আল-আজান এপিআই থেকে সাতক্ষীরার লাইভ সময় নিয়ে আসা
                val onlineTimes = OnlinePrayerFetcher.fetchSatkhiraTimings()
                
                if (onlineTimes != null) {
                    val fajr = onlineTimes["Fajr"] ?: ""
                    val sunrise = onlineTimes["Sunrise"] ?: ""
                    val zohar = onlineTimes["Dhuhr"] ?: ""
                    val asr = onlineTimes["Asr"] ?: ""
                    val maghrib = onlineTimes["Maghrib"] ?: ""
                    val isha = onlineTimes["Isha"] ?: ""

                    // লেআউটের টেক্সটভিউগুলোতে সময় বসানোর কোড (আপনার আইডি অনুযায়ী uncomment করে নিন)
                    // findViewById<TextView>(R.id.txtFajrTime)?.text = fajr
                    // findViewById<TextView>(R.id.txtZoharTime)?.text = zohar
                    // findViewById<TextView>(R.id.txtAsrTime)?.text = asr
                    // findViewById<TextView>(R.id.txtMaghribTime)?.text = maghrib
                    // findViewById<TextView>(R.id.txtIshaTime)?.text = isha

                    Toast.makeText(this@MainActivity, "সাতক্ষীরার লাইভ সময় সফলভাবে আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "সার্ভার থেকে সময় পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this@MainActivity, "ইন্টারনেট সংযোগ নেই, দয়া করে নেট চালু করুন", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
