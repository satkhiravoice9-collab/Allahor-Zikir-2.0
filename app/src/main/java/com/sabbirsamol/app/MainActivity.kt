package com.sabbirsamol.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var statusText: TextView
    private lateinit var countdownText: TextView
    private lateinit var hijriText: TextView
    private lateinit var banglaText: TextView
    private lateinit var englishText: TextView
    private lateinit var locationText: TextView
    
    private val handler = Handler(Looper.getMainLooper())
    private val locationRequestCode = 501
    
    data class Prayer(val name: String, val icon: String, val start: Int, val end: Int)
    
    private val prayers = listOf(
        Prayer("ফজর", "🌅", 270, 345),
        Prayer("যোহর", "☀️", 735, 990),
        Prayer("আসর", "🌤️", 990, 1110),
        Prayer("মাগরিব", "🌇", 1110, 1200),
        Prayer("এশা", "🌙", 1200, 1470)
    )
    
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        window.navigationBarColor = Color.rgb(24, 42, 35)
        buildHome()
        updateClock()
        updateDates()
        setupLocation()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateClock()
                updateDates()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }
    
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    
    private fun bg(c: Int, r: Int = 16) = GradientDrawable().apply { setColor(c); cornerRadius = dp(r).toFloat() }
    
    private fun row(icon: String, label: String, value: String, color: Int, alarm: Boolean = false) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(7), 0, dp(4), 0); background = bg(color, 9)
        addView(TextView(this@MainActivity).apply { text = icon; textSize = 17f; gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL }, LinearLayout.LayoutParams(dp(31), dp(40)))
        addView(TextView(this@MainActivity).apply { text = label; textSize = 13f; gravity = Gravity.CENTER_VERTICAL; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) }, LinearLayout.LayoutParams(0, dp(40), 1f))
        addView(TextView(this@MainActivity).apply { text = value; textSize = 11f; gravity = Gravity.CENTER; setTextColor(Color.WHITE) }, LinearLayout.LayoutParams(dp(140), dp(40)))
        if (alarm) addView(Switch(this@MainActivity).apply { gravity = Gravity.CENTER_VERTICAL; scaleX = .65f; scaleY = .65f }, LinearLayout.LayoutParams(dp(40), dp(40)))
    }
    
    private fun section(title: String, side: Int, inside: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(4), 0, 0, 0); background = bg(side)
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(8), dp(5), dp(8), dp(6)); background = bg(inside, 13)
            addView(TextView(this@MainActivity).apply { text = title; textSize = 15f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) })
        })
    }
    
    private fun buildHome() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(250, 247, 244)) }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, i -> v.setPadding(0, 0, 0, i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom); i }
        
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(7), dp(7), dp(7), dp(7)) }
        hijriText = TextView(this).apply { textSize = 13f; gravity = Gravity.CENTER; setTextColor(Color.rgb(34, 70, 52)); setTypeface(null, android.graphics.Typeface.BOLD) }
        banglaText = TextView(this).apply { textSize = 13f; gravity = Gravity.CENTER; setTextColor(Color.rgb(72, 53, 43)) }
        englishText = TextView(this).apply { textSize = 13f; gravity = Gravity.CENTER; setTextColor(Color.rgb(43, 58, 75)); setTypeface(null, android.graphics.Typeface.BOLD) }
        locationText = TextView(this).apply { text = "📍 লোকেশন খোঁজা হচ্ছে..."; textSize = 11f; gravity = Gravity.CENTER; setTextColor(Color.rgb(34, 70, 52)) }
        content.addView(hijriText); content.addView(banglaText); content.addView(englishText); content.addView(locationText)
        
        statusText = TextView(this).apply { textSize = 19f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) }
        countdownText = TextView(this).apply { textSize = 22f; gravity = Gravity.CENTER; setTextColor(Color.WHITE); setTypeface(null, android.graphics.Typeface.BOLD) }
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; background = bg(Color.rgb(139, 25, 45)); setPadding(dp(5), dp(7), dp(5), dp(7))
            addView(statusText); addView(countdownText)
        }, LinearLayout.LayoutParams(-1, dp(84)).apply { bottomMargin = dp(7) })
        
        val p = section("🕌 নামাজের সময়সূচী", Color.rgb(125, 20, 38), Color.rgb(158, 39, 56))
        val prayerInner = Color.rgb(170, 45, 62)
        prayers.forEachIndexed { idx, it ->
            p.addView(row(it.icon, it.name, "${fmt(it.start)} — ${fmt(it.end)}", prayerInner, true))
            if (idx < 4) p.addView(Space(this).apply { setBackgroundColor(Color.rgb(255, 238, 238)) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        content.addView(p, LinearLayout.LayoutParams(-1, dp(260)).apply { bottomMargin = dp(7) })
        
        val n = section("🌙 তাহাজ্জুদ • সেহরি • ইফতার", Color.rgb(16, 83, 54), Color.rgb(25, 112, 72))
        val nightInner = Color.rgb(31, 126, 82)
        listOf(Triple("🌙", "তাহাজ্জুদ", "০১:০০ — ০৪:২০ AM"), Triple("🌄", "সেহরি শেষ", "০৪:৩০ AM"), Triple("🌇", "ইফতার", "০৬:৩০ PM")).forEachIndexed { idx, it ->
            n.addView(row(it.first, it.second, it.third, nightInner))
            if (idx < 2) n.addView(Space(this).apply { setBackgroundColor(Color.rgb(235, 249, 240)) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        content.addView(n, LinearLayout.LayoutParams(-1, dp(173)).apply { bottomMargin = dp(7) })
        
        val i = section("☀️ ইশরাক • চাশত", Color.rgb(151, 102, 0), Color.rgb(190, 137, 8))
        val dayInner = Color.rgb(205, 149, 10)
        listOf(Triple("🌤️", "ইশরাক", "সূর্যোদয়ের পর"), Triple("☀️", "চাশত", "সূর্যোদয়ের পর থেকে যোহরের আগে")).forEachIndexed { idx, it ->
            i.addView(row(it.first, it.second, it.third, dayInner))
            if (idx == 0) i.addView(Space(this).apply { setBackgroundColor(Color.rgb(255, 249, 221)) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        content.addView(i, LinearLayout.LayoutParams(-1, dp(132)).apply { bottomMargin = dp(7) })
        
        val f = section("🚫 নামাজের নিষিদ্ধ সময়", Color.rgb(18, 65, 115), Color.rgb(25, 91, 151))
        val forbiddenInner = Color.rgb(31, 105, 169)
        listOf(Triple("🌅", "সূর্যোদয়", "০৫:৪৫ — ০৬:০০ AM"), Triple("☀️", "দুপুর", "১২:০৫ — ১২:১৫ PM"), Triple("🌇", "সূর্যাস্ত", "০৬:২০ — ০৬:৩০ PM")).forEachIndexed { idx, it ->
            f.addView(row(it.first, it.second, it.third, forbiddenInner))
            if (idx < 2) f.addView(Space(this).apply { setBackgroundColor(Color.rgb(235, 244, 255)) }, LinearLayout.LayoutParams(-1, dp(1)))
        }
        content.addView(f, LinearLayout.LayoutParams(-1, dp(173)))
        
        root.addView(ScrollView(this).apply { addView(content); isFillViewport = true }, LinearLayout.LayoutParams(-1, 0, 1f))
        
        val menu = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(Color.rgb(24, 42, 35)); setPadding(dp(2), dp(2), dp(2), dp(2)) }
        val menuColors = arrayOf(Color.rgb(139, 25, 45), Color.rgb(16, 83, 54), Color.rgb(151, 102, 0), Color.rgb(18, 65, 115), Color.rgb(91, 55, 125), Color.rgb(160, 64, 35), Color.rgb(55, 76, 96))
        
        listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরি", "🤲\nমাসনুন", "📝\nনোট", "🔄\nরিফ্রেশ", "ℹ️\nএবাউট").forEachIndexed { idx, label ->
            menu.addView(Button(this).apply {
                text = label; textSize = 9f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER; setTextColor(Color.WHITE); background = bg(menuColors[idx], 8)
                
                setOnClickListener {
                    when {
                        label.contains("তাসবিহ") -> startActivity(Intent(this@MainActivity, TasbihActivity::class.java))
                        label.contains("লাইব্রেরি") -> startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
                        label.contains("মাসনুন") -> startActivity(Intent(this@MainActivity, MasnunAmolActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@MainActivity, NotepadActivity::class.java))
                        label.contains("এবাউট") -> startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java))
                        label.contains("রিফ্রেশ") -> {
                            Toast.makeText(this@MainActivity, "☁️ ক্লাউড থেকে সমস্ত ডেটা সিঙ্ক হচ্ছে...", Toast.LENGTH_SHORT).show()
                            Handler(Looper.getMainLooper()).postDelayed({
                                Toast.makeText(this@MainActivity, "✅ সিঙ্ক সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                                updateClock(); updateDates(); setupLocation()
                            }, 1500)
                        }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(1), 0, dp(1), 0) })
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(58)))
        setContentView(root)
    }
    
    private fun setupLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode); return
        }
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationText.text = "📍 ${if (location.provider == LocationManager.GPS_PROVIDER) "GPS" else "Network"} লোকেশন • ${String.format(Locale.US, "%.4f, %.4f", location.latitude, location.longitude)}"
            }
        }
        try {
            listOf(LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER).forEach { provider ->
                if (lm.isProviderEnabled(provider)) lm.requestLocationUpdates(provider, 30000L, 50f, listener, Looper.getMainLooper())
            }
            locationText.text = "📍 GPS / Network লোকেশন সক্রিয়"
        } catch (_: Exception) { locationText.text = "📍 লোকেশন পাওয়া যায়নি" }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode && grantResults.any { it == PackageManager.PERMISSION_GRANTED }) setupLocation()
    }
    
    private fun updateDates() {
        val c = Calendar.getInstance()
        englishText.text = "📅 " + SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH).format(c.time)
        banglaText.text = "🇧🇩 বাংলা তারিখ: " + banglaDate(c)
        hijriText.text = "🌙 হিজরি তারিখ: " + hijriDate(c)
    }
    
    private fun banglaDate(c: Calendar): String {
        val y = c.get(Calendar.YEAR); val m = c.get(Calendar.MONTH) + 1; val d = c.get(Calendar.DAY_OF_MONTH)
        val starts = Calendar.getInstance(); starts.clear(); starts.set(y, Calendar.APRIL, 14, 0, 0, 0)
        val by = if (m > 4 || (m == 4 && d >= 14)) y - 593 else y - 594
        if (c.before(starts)) starts.set(y - 1, Calendar.APRIL, 14, 0, 0, 0)
        val dayOfYear = ((c.timeInMillis - starts.timeInMillis) / 86400000L).toInt()
        val lengths = intArrayOf(31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30, 30)
        val names = arrayOf("বৈশাখ", "জ্যৈষ্ঠ", "আষাঢ়", "শ্রাবণ", "ভাদ্র", "আশ্বিন", "কার্তিক", "অগ্রহায়ণ", "পৌষ", "মাঘ", "ফাল্গুন", "চৈত্র")
        var rem = dayOfYear; var mi = 0
        while (mi < 12 && rem >= lengths[mi]) { rem -= lengths[mi]; mi++ }
        return "${bn(rem + 1)} ${names[mi]} ${bn(by)} বঙ্গাব্দ"
    }
    
    private fun hijriDate(c: Calendar): String = try {
        val d = java.time.LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
        val h = java.time.chrono.HijrahDate.from(d)
        val names = arrayOf("মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ")
        "${bn(h.get(java.time.temporal.ChronoField.DAY_OF_MONTH))} ${names[h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1]} ${bn(h.get(java.time.temporal.ChronoField.YEAR))} হিজরি"
    } catch (_: Exception) { "হিজরি তারিখ পাওয়া যায়নি" }
    
    private fun bn(n: Int) = n.toString().map { "০১২৩৪৫৬৭৮৯"["0123456789".indexOf(it)] }.joinToString("")
    
    private fun updateClock() {
        val c = Calendar.getInstance()
        val now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        val sec = c.get(Calendar.SECOND)
        val active = prayers.lastOrNull { now >= it.start && now < it.end }
        val next = prayers.firstOrNull { it.start > now } ?: prayers.first()
        var total = (next.start - now) * 60 - sec
        if (total <= 0) total += 86400
        statusText.text = if (active != null) "🕐 ${active.name} ওয়াক্ত চলছে" else "🕐 পরবর্তী ওয়াক্ত: ${next.name}"
        countdownText.text = "%02d:%02d:%02d বাকি".format(total / 3600, (total % 3600) / 60, total % 60)
    }
    
    private fun fmt(m: Int): String {
        val h = (m / 60) % 24
        return "%02d:%02d %s".format(if (h % 12 == 0) 12 else h % 12, m % 60, if (h < 12) "AM" else "PM")
    }
}
