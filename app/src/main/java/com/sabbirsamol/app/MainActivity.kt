package com.sabbirsamol.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // আপনার লেআউট ফাইল অনুযায়ী নাম দিন

        // অ্যাপ চালু হলেই টাইম লোড করার ফাংশন কল করা
        loadPrayerTimes()
    }

    private fun loadPrayerTimes() {
        // বর্তমান বছরের কততম দিন তা বের করা (১ থেকে ৩৬৫)
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // কোরুoutines ব্যবহার করে ব্যাকগ্রাউন্ডে চেক করা
        lifecycleScope.launch {
            if (OnlinePrayerFetcher.isNetworkAvailable(this@MainActivity)) {
                // ইন্টারনেট থাকলে অনলাইন এপিআই থেকে লাইভ টাইম আনা
                val onlineTimes = OnlinePrayerFetcher.fetchSatkhiraTimings()
                
                if (onlineTimes != null) {
                    val fajr = onlineTimes["Fajr"] ?: ""
                    val sunrise = onlineTimes["Sunrise"] ?: ""
                    val zohar = onlineTimes["Dhuhr"] ?: ""
                    val asr = onlineTimes["Asr"] ?: ""
                    val maghrib = onlineTimes["Maghrib"] ?: ""
                    val isha = onlineTimes["Isha"] ?: ""

                    // TODO: এই ভেরিয়েবলগুলোর মান আপনার অ্যাপের ইউআই টেক্সটভিউতে (TextView) বসিয়ে দিন
                    // যেমন: 
                    // binding.txtFajr.text = fajr
                    // binding.txtZohar.text = zohar
                    
                    Toast.this@MainActivity, "অনলাইন থেকে লাইভ সময় আপডেট করা হয়েছে", Toast.LENGTH_SHORT).show()
                } else {
                    // অনলাইন ফেচ করতে ব্যর্থ হলে অফলাইন ডেটাবেজ ব্যবহার করা
                    loadFromOfflineDatabase(dayOfYear)
                }
            } else {
                // ইন্টারনেট কানেকশন না থাকলে অফলাইন ডেটাবেজ ব্যবহার করা
                loadFromOfflineDatabase(dayOfYear)
                Toast.makeText(this@MainActivity, "ইন্টারনেট নেই, অফলাইন ডেটা ব্যবহার করা হচ্ছে", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadFromOfflineDatabase(dayOfYear: Int) {
        // আপনার তৈরি করা PrayerDatabase থেকে নির্দিষ্ট দিনের মান তুলে আনা
        val todayPrayer = PrayerDatabase.getPrayerTime(dayOfYear)
        
        // এখানে todayPrayer.fuzor, todayPrayer.zohar ইত্যাদি দিয়ে অফলাইন UI সেট করে নিন
    }
}
