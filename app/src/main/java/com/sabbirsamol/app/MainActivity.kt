package com.sabbirsamol.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // সম্পূর্ণ হোম স্ক্রিন ডিজাইন ও লেআউট তৈরি
        setupMainUI()

        // অনলাইন থেকে সাতক্ষীরার লাইভ নামাজের সময় আনা
        loadOnlinePrayerTimes()
    }

    private fun setupMainUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ThemeManager.getTheme(this@MainActivity).bgMain)
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(80))
        }

        // --- হেডার কার্ড (অ্যাপের নাম ও লোগো) ---
        val headerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = ThemeManager.getTheme(this@MainActivity).let { 
                GradientDrawable().apply {
                    setColor(it.cardBg)
                    setStroke(dp(1), it.cardStroke)
                    cornerRadius = dp(12).toFloat()
                }
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }

        headerCard.addView(TextView(this).apply {
            text = "আল্লাহর যিকির"
            textSize = 22f
            setTextColor(ThemeManager.getTheme(this@MainActivity).textAccent)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        })

        content.addView(headerCard)

        // --- নামাজের সময়সূচি কার্ড ---
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ThemeManager.getTheme(this@MainActivity).let { 
                GradientDrawable().apply {
                    setColor(it.cardBg)
                    setStroke(dp(1), it.cardStroke)
                    cornerRadius = dp(12).toFloat()
                }
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }

        prayerCard.addView(TextView(this).apply {
            text = "সাতক্ষীরার নামাজের সময়সূচি"
            textSize = 18f
            setTextColor(ThemeManager.getTheme(this@MainActivity).textAccent)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        })

        // ওয়াক্তের টেক্সটভিউগুলো
        val timeTvParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(6), 0, dp(6)) }
        
        prayerCard.addView(TextView(this).apply { id = R.id.txtFajrTime; text = "ফজর: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })
        prayerCard.addView(TextView(this).apply { id = R.id.txtSunriseTime; text = "সূর্যোদয়: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })
        prayerCard.addView(TextView(this).apply { id = R.id.txtZoharTime; text = "যোহর: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })
        prayerCard.addView(TextView(this).apply { id = R.id.txtAsrTime; text = "আসর: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })
        prayerCard.addView(TextView(this).apply { id = R.id.txtMaghribTime; text = "মাগরিব: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })
        prayerCard.addView(TextView(this).apply { id = R.id.txtIshaTime; text = "এশা: লোড হচ্ছে..."; textSize = 16f; setTextColor(ThemeManager.getTheme(this@MainActivity).textMain); layoutParams = timeTvParams })

        content.addView(prayerCard)

        // --- ফিচার বাটনসমূহ (তাসবিহ, লাইব্রেরি, নোটপ্যাড ইত্যাদি) ---
        val btnParams = LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(10) }
        val theme = ThemeManager.getTheme(this)

        content.addView(Button(this).apply {
            text = "📿 তাসবিহ কাউন্টার"
            isAllCaps = false; textSize = 16f; setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(theme.btnBg); cornerRadius = dp(8).toFloat() }
            layoutParams = btnParams
            setOnClickListener { startActivity(Intent(this@MainActivity, TasbihActivity::class.java)) }
        })

        content.addView(Button(this).apply {
            text = "📚 ইসলামিক লাইব্রেরী ও কিতাব"
            isAllCaps = false; textSize = 16f; setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(theme.btnBg); cornerRadius = dp(8).toFloat() }
            layoutParams = btnParams
            setOnClickListener { startActivity(Intent(this@MainActivity, LibraryActivity::class.java)) }
        })

        content.addView(Button(this).apply {
            text = "📝 কালার নোটপ্যাড"
            isAllCaps = false; textSize = 16f; setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(theme.btnBg); cornerRadius = dp(8).toFloat() }
            layoutParams = btnParams
            setOnClickListener { startActivity(Intent(this@MainActivity, NotepadActivity::class.java)) }
        })

        content.addView(Button(this).apply {
            text = "⚙️ প্রোফাইল ও সেটিংস"
            isAllCaps = false; textSize = 16f; setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(theme.btnBg); cornerRadius = dp(8).toFloat() }
            layoutParams = btnParams
            setOnClickListener { startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java)) }
        })

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // --- বটম মেনু বার ---
        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val menuItems = listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরী", "📝\nনোট", "⚙️\nসেটিংস")
        menuItems.forEach { label ->
            menu.addView(Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("হোম")) Color.parseColor("#FBBF24") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("তাসবিহ") -> startActivity(Intent(this@MainActivity, TasbihActivity::class.java))
                        label.contains("লাইব্রেরী") -> startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@MainActivity, NotepadActivity::class.java))
                        label.contains("সেটিংস") -> startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun loadOnlinePrayerTimes() {
        CoroutineScope(Dispatchers.Main).launch {
            val context = this@MainActivity
            
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
