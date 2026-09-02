package com.sabbirsamol.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(s: String): String = s.map { if (it in '0'..'9') "০১২৩৪৫৬৭৮৯"[it - '0'] else it }.joinToString("")

    private lateinit var tvCurrentWaqtName: TextView
    private lateinit var tvRemainingCountdown: TextView
    private lateinit var tvSunriseTime: TextView
    private lateinit var tvSunsetTime: TextView

    private lateinit var tvFajrTime: TextView
    private lateinit var tvZoharTime: TextView
    private lateinit var tvAsrTime: TextView
    private lateinit var tvMaghribTime: TextView
    private lateinit var tvIshaTime: TextView

    private val prayerRows = mutableMapOf<String, LinearLayout>()

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    private val handler = Handler(Looper.getMainLooper())
    private var isTimerRunning = false

    private val timingsMap = mutableMapOf<String, String>()

    private val timerRunnable = object : Runnable {
        override fun run() {
            updateLiveCountdown()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupProfessionalUI()
        loadCachedPrayerTimes()
        loadOnlinePrayerTimes()
    }

    override fun onResume() {
        super.onResume()
        startCountdownTimer()
    }

    override fun onPause() {
        super.onPause()
        stopCountdownTimer()
    }

    private fun startCountdownTimer() {
        if (!isTimerRunning) {
            handler.post(timerRunnable)
            isTimerRunning = true
        }
    }

    private fun stopCountdownTimer() {
        handler.removeCallbacks(timerRunnable)
        isTimerRunning = false
    }

    private fun setupProfessionalUI() {
        val theme = ThemeManager.getTheme(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(theme.bgMain)
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(90))
        }

        // ================= ১. টপ বার =================
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(10))
        }

        val dateFormat = SimpleDateFormat("dd MMMM, EEEE", Locale("bn", "BD"))
        val currentDate = dateFormat.format(Date())

        topBar.addView(TextView(this).apply {
            text = "📅 $currentDate"
            textSize = 14f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        topBar.addView(TextView(this).apply {
            text = "🔄"
            textSize = 18f
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { loadOnlinePrayerTimes() }
        })

        content.addView(topBar)

        // ================= ২. কাউন্টডাউন কার্ড (লাইভ সেকেন্ডসহ রানিং) =================
        val countdownCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(theme.cardBg)
                setStroke(dp(1), theme.cardStroke)
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        tvCurrentWaqtName = TextView(this).apply {
            text = "ওয়াক্ত লোড হচ্ছে..."
            textSize = 22f
            setTextColor(theme.textAccent)
            setTypeface(null, Typeface.BOLD)
        }
        countdownCard.addView(tvCurrentWaqtName)

        tvRemainingCountdown = TextView(this).apply {
            text = "০০:০০:০০"
            textSize = 28f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        }
        countdownCard.addView(tvRemainingCountdown)

        val infoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        infoRow.addView(TextView(this).apply { text = "📍 সাতক্ষীরা ▾"; textSize = 13f; setTextColor(theme.textSub); setPadding(0, 0, dp(14), 0) })
        
        tvSunriseTime = TextView(this).apply { text = "🌅 সূর্যোদয়: --:--"; textSize = 13f; setTextColor(theme.textSub); setPadding(dp(6), 0, dp(6), 0) }
        infoRow.addView(tvSunriseTime)

        tvSunsetTime = TextView(this).apply { text = "🌇 সূর্যাস্ত: --:--"; textSize = 13f; setTextColor(theme.textSub); setPadding(dp(14), 0, 0, 0) }
        infoRow.addView(tvSunsetTime)

        countdownCard.addView(infoRow)
        content.addView(countdownCard)

        // ================= ৩. ট্যাব (অ্যালার্ম ও ক্যালেন্ডার) =================
        val tabRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 2f
            layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { bottomMargin = dp(12) }
        }
        tabRow.addView(Button(this).apply {
            text = "⏰ অ্যালার্ম"
            isAllCaps = false; textSize = 13f; setTextColor(Color.BLACK)
            background = GradientDrawable().apply { setColor(theme.btnBg); cornerRadius = dp(8).toFloat() }
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { rightMargin = dp(6) }
            setOnClickListener { Toast.makeText(this@MainActivity, "ওয়াক্ত অ্যালার্ম চালু আছে", Toast.LENGTH_SHORT).show() }
        })
        tabRow.addView(Button(this).apply {
            text = "📅 ক্যালেন্ডার"
            isAllCaps = false; textSize = 13f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(theme.cardBg); setStroke(dp(1), theme.cardStroke); cornerRadius = dp(8).toFloat() }
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(6) }
            setOnClickListener { Toast.makeText(this@MainActivity, "হিজরি ক্যালেন্ডার", Toast.LENGTH_SHORT).show() }
        })
        content.addView(tabRow)

        // ================= ৪. নামাজের সময়সূচি তালিকা =================
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(theme.cardBg)
                setStroke(dp(1), theme.cardStroke)
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        prayerCard.addView(TextView(this).apply {
            text = "আজকের সময়সূচি ও বিশেষ ওয়াক্ত"
            textSize = 16f
            setTextColor(theme.textAccent)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        fun createPrayerRow(key: String, name: String, timeTextView: TextView): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(10), dp(8), dp(10))
            }
            row.addView(TextView(this).apply { text = name; textSize = 15f; setTextColor(theme.textMain) }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(timeTextView.apply { textSize = 14f; setTextColor(theme.textSub); setPadding(0, 0, dp(12), 0) })
            row.addView(Switch(this).apply { isChecked = true })
            prayerRows[key] = row
            return row
        }

        tvFajrTime = TextView(this).apply { text = "--:--" }
        tvZoharTime = TextView(this).apply { text = "--:--" }
        tvAsrTime = TextView(this).apply { text = "--:--" }
        tvMaghribTime = TextView(this).apply { text = "--:--" }
        tvIshaTime = TextView(this).apply { text = "--:--" }

        prayerCard.addView(createPrayerRow("Fajr", "ফজর", tvFajrTime))
        prayerCard.addView(createPrayerRow("Dhuhr", "যোহর", tvZoharTime))
        prayerCard.addView(createPrayerRow("Asr", "আসর", tvAsrTime))
        prayerCard.addView(createPrayerRow("Maghrib", "মাগরিব", tvMaghribTime))
        prayerCard.addView(createPrayerRow("Isha", "এশা", tvIshaTime))

        val divider = View(this).apply {
            background = GradientDrawable().apply { setColor(theme.cardStroke) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(8), 0, dp(8)) }
        }
        prayerCard.addView(divider)

        val dummyTahajjud = TextView(this).apply { text = "রাত ০২:৩০ - ০৪:১৫" }
        val dummyIftar = TextView(this).apply { text = "সেহরি শেষ | ইফতার মাগরিব" }
        val dummyHaram = TextView(this).apply { text = "সূর্যোদয় ও দ্বিপ্রহরে নিষিদ্ধ" }

        prayerCard.addView(createPrayerRow("Tahajjud", "🌙 তাহাজ্জুদ (শেষ তৃতীয়াংশ)", dummyTahajjud))
        prayerCard.addView(createPrayerRow("Iftar", "🍽️ ইফতার ও সেহরি", dummyIftar))
        prayerCard.addView(createPrayerRow("Haram", "⚠️ হারাম ওয়াক্ত (মাকরুহ)", dummyHaram))

        content.addView(prayerCard)

        // ================= ৫. টপ ফিচার শর্টকাট =================
        val featureHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        featureHeader.addView(TextView(this).apply { text = "⭐ টপ ফিচার"; textSize = 16f; setTextColor(theme.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        featureHeader.addView(TextView(this).apply { text = "আরও দেখুন ➔"; textSize = 13f; setTextColor(Color.parseColor("#3B82F6")) })
        content.addView(featureHeader)

        val shortcutsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 4f
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        val tasbihCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(theme.cardBg); cornerRadius = dp(12).toFloat(); setStroke(dp(1), theme.cardStroke) }
            setPadding(dp(8), dp(12), dp(8), dp(12))
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
            setOnClickListener { startActivity(Intent(this@MainActivity, TasbihActivity::class.java)) }
        }
        tasbihCard.addView(TextView(this).apply { text = "📿"; textSize = 20f; gravity = Gravity.CENTER })
        tasbihCard.addView(TextView(this).apply { text = "তাসবিহ"; textSize = 12f; setTextColor(theme.textMain); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
        shortcutsRow.addView(tasbihCard)

        fun addShortcut(title: String, icon: String, targetActivity: Class<*>) {
            val sCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = GradientDrawable().apply { setColor(theme.cardBg); cornerRadius = dp(12).toFloat(); setStroke(dp(1), theme.cardStroke) }
                setPadding(dp(8), dp(12), dp(8), dp(12))
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
                setOnClickListener { startActivity(Intent(this@MainActivity, targetActivity)) }
            }
            sCard.addView(TextView(this).apply { text = icon; textSize = 20f; gravity = Gravity.CENTER })
            sCard.addView(TextView(this).apply { text = title; textSize = 12f; setTextColor(theme.textMain); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
            shortcutsRow.addView(sCard)
        }

        addShortcut("লাইব্রেরী", "📚", LibraryActivity::class.java)
        addShortcut("নোটপ্যাড", "📝", NotepadActivity::class.java)
        addShortcut("সেটিংস", "⚙️", ProfileSettingsActivity::class.java)

        content.addView(shortcutsRow)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= ৬. বটম নেভিগেশন বার =================
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(6), dp(2), dp(6))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf("🏠\nহোম", "📖\nটিপস", "📚\nকুরআন", "🤲\nদোয়া", "👤\nপ্রোফাইল")
        navItems.forEach { label ->
            bottomNav.addView(Button(this).apply {
                text = label; textSize = 11f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("হোম")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("কুরআন") || label.contains("📚") -> startActivity(Intent(this@MainActivity, LibraryActivity::class.java))
                        label.contains("প্রোফাইল") -> startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun loadCachedPrayerTimes() {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE)
        val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha")
        keys.forEach { key ->
            val cachedVal = prefs.getString(key, null)
            if (cachedVal != null) {
                timingsMap[key] = cachedVal
            }
        }
        applyTimingsToUI()
    }

    private fun saveTimingsToCache(times: Map<String, String>) {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE).edit()
        times.forEach { (k, v) -> prefs.putString(k, v) }
        prefs.apply()
    }

    private fun applyTimingsToUI() {
        tvFajrTime.text = bn(timingsMap["Fajr"] ?: "--:--")
        tvZoharTime.text = bn(timingsMap["Dhuhr"] ?: "--:--")
        tvAsrTime.text = bn(timingsMap["Asr"] ?: "--:--")
        tvMaghribTime.text = bn(timingsMap["Maghrib"] ?: "--:--")
        tvIshaTime.text = bn(timingsMap["Isha"] ?: "--:--")

        tvSunriseTime.text = "🌅 সূর্যোদয়: ${bn(timingsMap["Sunrise"] ?: "--:--")}"
        tvSunsetTime.text = "🌇 সূর্যাস্ত: ${bn(timingsMap["Sunset"] ?: timingsMap["Maghrib"] ?: "--:--")}"
        updateLiveCountdown()
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
                timingsMap.clear()
                timingsMap.putAll(onlineTimes)
                saveTimingsToCache(onlineTimes)
                applyTimingsToUI()
                Toast.makeText(context, "সাতক্ষীরার লাইভ সময় আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else if (timingsMap.isEmpty()) {
                // অফলাইন ডিফল্ট সময়
                val defaultTimes = mapOf(
                    "Fajr" to "04:30", "Sunrise" to "05:46", "Dhuhr" to "12:03",
                    "Asr" to "15:31", "Sunset" to "18:20", "Maghrib" to "18:20", "Isha" to "19:37"
                )
                timingsMap.putAll(defaultTimes)
                applyTimingsToUI()
            }
        }
    }

    private fun updateLiveCountdown() {
        if (timingsMap.isEmpty()) return

        val now = Calendar.getInstance()
        val currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val currentSeconds = now.get(Calendar.SECOND)

        fun parseMinutes(timeStr: String?): Int {
            if (timeStr.isNullOrEmpty()) return -1
            val parts = timeStr.trim().split(":")
            if (parts.size < 2) return -1
            return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
        }

        val fajr = parseMinutes(timingsMap["Fajr"])
        val sunrise = parseMinutes(timingsMap["Sunrise"])
        val dhuhr = parseMinutes(timingsMap["Dhuhr"])
        val asr = parseMinutes(timingsMap["Asr"])
        val maghrib = parseMinutes(timingsMap["Maghrib"])
        val isha = parseMinutes(timingsMap["Isha"])

        var currentWaqtName = "তাহাজ্জুদ"
        var targetEndMinutes = fajr
        var activeKey = "Tahajjud"

        when {
            currentMinutes in fajr until sunrise -> {
                currentWaqtName = "ফজর"
                targetEndMinutes = sunrise
                activeKey = "Fajr"
            }
            currentMinutes in sunrise until dhuhr -> {
                currentWaqtName = "চাশত / নিষিদ্ধ সময়"
                targetEndMinutes = dhuhr
                activeKey = "Haram"
            }
            currentMinutes in dhuhr until asr -> {
                currentWaqtName = "যোহর"
                targetEndMinutes = asr
                activeKey = "Dhuhr"
            }
            currentMinutes in asr until maghrib -> {
                currentWaqtName = "আসর"
                targetEndMinutes = maghrib
                activeKey = "Asr"
            }
            currentMinutes in maghrib until isha -> {
                currentWaqtName = "মাগরিব"
                targetEndMinutes = isha
                activeKey = "Maghrib"
            }
            currentMinutes >= isha || currentMinutes < fajr -> {
                currentWaqtName = "এশা"
                targetEndMinutes = if (currentMinutes >= isha) (24 * 60) + fajr else fajr
                activeKey = "Isha"
            }
        }

        tvCurrentWaqtName.text = currentWaqtName

        val currentTotalSec = (currentMinutes * 60) + currentSeconds
        val targetTotalSec = targetEndMinutes * 60
        var diffSec = targetTotalSec - currentTotalSec
        if (diffSec < 0) diffSec += (24 * 60 * 60)

        val h = diffSec / 3600
        val m = (diffSec % 3600) / 60
        val s = diffSec % 60

        val countdownStr = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
        tvRemainingCountdown.text = "শেষ হতে বাকি\n${bn(countdownStr)}"

        highlightActiveWaqt(activeKey)
    }

    private fun highlightActiveWaqt(activeKey: String) {
        val theme = ThemeManager.getTheme(this)
        prayerRows.forEach { (key, row) ->
            if (key == activeKey) {
                row.background = GradientDrawable().apply {
                    setColor(Color.parseColor("#1E3A8A"))
                    cornerRadius = dp(8).toFloat()
                }
            } else {
                row.background = GradientDrawable().apply {
                    setColor(Color.TRANSPARENT)
                }
            }
        }
    }
}
