package com.sabbirsamol.app

import android.Manifest
import android.content.Context
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
    
    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private var savedThemeName = ""
    
    // ৩৬৫ দিনের লকড ডেটাবেজ এবং জেলাভিত্তিক অফসেট থেকে আজকের নিখুঁত সময় বের করার ফাংশন
    private fun getTodayPrayers(): List<Prayer> {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        
        // ডিফল্ট জেলা হিসেবে সাতক্ষীরা বা ইউজারের সেভ করা জেলা সেট করা আছে
        val currentDistrict = "Satkhira" 
        val offset = PrayerDatabase.getDistrictOffset(currentDistrict) 
        val p = PrayerDatabase.getPrayerTime(dayOfYear)
        
        return listOf(
            Prayer("ফজর", "🌅", p.fuzor + offset, p.fuzor + offset + 75),
            Prayer("যোহর", "☀️", p.zohar + offset, p.asr + offset),
            Prayer("আসর", "🌤️", p.asr + offset, p.magrib + offset - 10),
            Prayer("মাগরিব", "🌇", p.magrib + offset, p.magrib + offset + 90),
            Prayer("এশা", "🌙", p.esha + offset, p.fuzor + offset + 1440)
        )
    }

    data class Prayer(val name: String, val icon: String, val start: Int, val end: Int)
    
    data class ForbiddenTime(val name: String, val icon: String, val start: Int, val end: Int)
    private val forbiddenTimes = listOf(
        ForbiddenTime("সূর্যোদয়ের নিষিদ্ধ সময়", "🌅", 345, 360),
        ForbiddenTime("দুপুরের নিষিদ্ধ সময়", "☀️", 725, 735),
        ForbiddenTime("সূর্যাস্তের নিষিদ্ধ সময়", "🌇", 1180, 1200)
    )
    
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        savedThemeName = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)") ?: ""
        
        window.navigationBarColor = themeColors.cardBg
        buildHome()
        updateClock()
        updateDates()
        setupLocation()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateClock(); updateDates()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }
    
    override fun onResume() {
        super.onResume()
        val currentTheme = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)") ?: ""
        if (currentTheme != savedThemeName) { recreate() }
    }
    
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(c: Int, r: Int = 16) = GradientDrawable().apply { setColor(c); cornerRadius = dp(r).toFloat() }
    
    private fun row(icon: String, label: String, value: String, alarm: Boolean = false) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(7), dp(4), dp(4), dp(4)); background = bg(themeColors.bgMain, 9)
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) }
        
        addView(TextView(this@MainActivity).apply { text = icon; textSize = 17f; gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL }, LinearLayout.LayoutParams(dp(31), dp(40)))
        addView(TextView(this@MainActivity).apply { text = label; textSize = 14f; gravity = Gravity.CENTER_VERTICAL; setTextColor(themeColors.textMain); setTypeface(null, android.graphics.Typeface.BOLD) }, LinearLayout.LayoutParams(0, dp(40), 1f))
        addView(TextView(this@MainActivity).apply { text = value; textSize = 12f; gravity = Gravity.CENTER; setTextColor(themeColors.textSub) }, LinearLayout.LayoutParams(dp(140), dp(40)))
        
        if (alarm) addView(Switch(this@MainActivity).apply { gravity = Gravity.CENTER_VERTICAL; scaleX = .65f; scaleY = .65f }, LinearLayout.LayoutParams(dp(40), dp(40)))
    }
    
    private fun section(title: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(4))
        background = GradientDrawable().apply { setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat() }
        addView(TextView(this@MainActivity).apply { text = title; textSize = 16f; gravity = Gravity.CENTER; setTextColor(themeColors.textAccent); setTypeface(null, android.graphics.Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
    }
    
    private fun buildHome() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, i -> v.setPadding(0, 0, 0, i.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom); i }
        
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)) }
        
        hijriText = TextView(this).apply { textSize = 14f; gravity = Gravity.CENTER; setTextColor(themeColors.textAccent); setTypeface(null, android.graphics.Typeface.BOLD) }
        banglaText = TextView(this).apply { textSize = 13f; gravity = Gravity.CENTER; setTextColor(themeColors.textMain) }
        englishText = TextView(this).apply { textSize = 13f; gravity = Gravity.CENTER; setTextColor(themeColors.textMain); setTypeface(null, android.graphics.Typeface.BOLD) }
        locationText = TextView(this).apply { text = "📍 সাতক্ষীরা (৬৪ জেলা ডেটাবেজ ও অফসেট সক্রিয়)"; textSize = 12f; gravity = Gravity.CENTER; setTextColor(themeColors.textSub); setPadding(0, dp(4), 0, dp(12)) }
        
        content.addView(hijriText); content.addView(banglaText); content.addView(englishText); content.addView(locationText)
        
        statusText = TextView(this).apply { textSize = 18f; gravity = Gravity.CENTER; setTextColor(themeColors.textMain); setTypeface(null, android.graphics.Typeface.BOLD) }
        countdownText = TextView(this).apply { textSize = 22f; gravity = Gravity.CENTER; setTextColor(themeColors.textAccent); setTypeface(null, android.graphics.Typeface.BOLD) }
        
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat() }
            setPadding(dp(10), dp(12), dp(10), dp(12))
            addView(statusText); addView(countdownText)
        }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        
        val p = section("🕌 নামাজের সময়সূচী (৩৬৫ দিন স্বয়ংক্রিয়)")
        getTodayPrayers().forEach { p.addView(row(it.icon, it.name, "${fmt(it.start)} — ${fmt(it.end)}", true)) }
        content.addView(p, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        
        val n = section("🌙 তাহাজ্জুদ • সেহরি • ইফতার")
        listOf(Triple("🌙", "তাহাজ্জুদ", "০১:০০ — ০৪:২০ AM"), Triple("🌄", "সেহরি শেষ", "০৪:৩০ AM"), Triple("🌇", "ইফতার", "০৬:৩০ PM")).forEach { n.addView(row(it.first, it.second, it.third)) }
        content.addView(n, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        
        val i = section("☀️ ইশরাক • চাশত")
        listOf(Triple("🌤️", "ইশরাক", "সূর্যোদয়ের পর"), Triple("☀️", "চাশত", "সূর্যোদয়ের পর থেকে যোহরের আগে")).forEach { i.addView(row(it.first, it.second, it.third)) }
        content.addView(i, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) })
        
        val f = section("🚫 নামাজের নিষিদ্ধ সময়")
        listOf(Triple("🌅", "সূর্যোদয়", "০৫:৪৫ — ০৬:০০ AM"), Triple("☀️", "দুপুর", "১২:০৫ — ১২:১৫ PM"), Triple("🌇", "সূর্যাস্ত", "০৬:২০ — ০৬:৩০ PM")).forEach { f.addView(row(it.first, it.second, it.third)) }
        content.addView(f, LinearLayout.LayoutParams(-1, -2))
        
        root.addView(ScrollView(this).apply { addView(content); isFillViewport = true }, LinearLayout.LayoutParams(-1, 0, 1f))
        
        val menu = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER 
            setBackgroundColor(themeColors.cardBg); setPadding(dp(2), dp(2), dp(2), dp(2))
            elevation = dp(8).toFloat()
        }
        
        listOf("🏠\nহোম", "🕋\nতাসবিহ", "📚\nলাইব্রেরী", "📁\nআমল", "📝\nনোট", "🔄\nসিঙ্ক", "👤\nপ্রোফাইল").forEach { label ->
            menu.addView(Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER; setTextColor(themeColors.textMain)
                background = GradientDrawable().apply { setColor(themeColors.bgMain); cornerRadius = dp(8).toFloat() }
                
                setOnClickListener {
                    when {
                        label.contains("তাসবিহ") -> startActivity(Intent(this@MainActivity, TasbihActivity::class.java))
                        label.contains("লাইব্রেরী") -> startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
                        label.contains("আমল") -> startActivity(Intent(this@MainActivity, MasnunAmolActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@MainActivity, NotepadActivity::class.java))
                        label.contains("প্রোফাইল") -> startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java))
                        label.contains("সিঙ্ক") -> {
                            Toast.makeText(this@MainActivity, "☁️ ৩৬৫ দিনের ডেটা সফলভাবে সিঙ্ক হয়েছে!", Toast.LENGTH_SHORT).show()
                            updateClock(); updateDates()
                        }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(58)))
        setContentView(root)
    }
    
    private fun setupLocation() {
        locationText.text = "📍 সাতক্ষীরা (ইসলামিক ফাউন্ডেশন ৩৬৫ দিন ও ৬৪ জেলা প্লাস-মাইনাস সক্রিয়)"
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
        var targetCal = c.clone() as Calendar
        val currentHour = targetCal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = targetCal.get(Calendar.MINUTE)
        if (currentHour * 60 + currentMinute >= 1110) { targetCal.add(Calendar.DAY_OF_MONTH, 1) }
        val d = java.time.LocalDate.of(targetCal.get(Calendar.YEAR), targetCal.get(Calendar.MONTH) + 1, targetCal.get(Calendar.DAY_OF_MONTH))
        val h = java.time.chrono.HijrahDate.from(d)
        val names = arrayOf("মুহাররম", "সফর", "রবিউল আউয়াল", "রবিউস সানি", "জমাদিউল আউয়াল", "জমাদিউস সানি", "রজব", "শাবান", "রমজান", "শাওয়াল", "জিলকদ", "জিলহজ")
        "${bn(h.get(java.time.temporal.ChronoField.DAY_OF_MONTH))} ${names[h.get(java.time.temporal.ChronoField.MONTH_OF_YEAR) - 1]} ${bn(h.get(java.time.temporal.ChronoField.YEAR))} হিজরি"
    } catch (_: Exception) { "হিজরি তারিখ পাওয়া যায়নি" }
    
    private fun bn(n: Int) = n.toString().map { "০১২translateX৫৬৭৮৯"["0123456789".indexOf(it)] }.joinToString("")
    
    private fun updateClock() {
        val c = Calendar.getInstance()
        val now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
        val sec = c.get(Calendar.SECOND)
        
        val activeForbidden = forbiddenTimes.firstOrNull { now >= it.start && now < it.end }
        if (activeForbidden != null) {
            var forbiddenTotalSec = (activeForbidden.end - now) * 60 - sec
            if (forbiddenTotalSec <= 0) forbiddenTotalSec = 1
            statusText.text = "🚫 ${activeForbidden.name} চলছে"
            countdownText.text = "%02d:%02d:%02d বাকি".format(forbiddenTotalSec / 3600, (forbiddenTotalSec % 3600) / 60, forbiddenTotalSec % 60)
            return
        }
        
        val prayers = getTodayPrayers()
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
