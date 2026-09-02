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

    private lateinit var tvTahajjudTime: TextView
    private lateinit var tvIftarTime: TextView
    private lateinit var tvHaramTime: TextView

    private val prayerRows = mutableMapOf<String, LinearLayout>()
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
            text = "🔄 রিফ্রেশ"
            textSize = 13f
            setTextColor(Color.parseColor("#3B82F6"))
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { loadOnlinePrayerTimes() }
        })
        content.addView(topBar)

        // ================= ২. কাউন্টডাউন কার্ড =================
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
        infoRow.addView(TextView(this).apply { text = "📍 সাতক্ষীরা"; textSize = 13f; setTextColor(theme.textSub); setPadding(0, 0, dp(12), 0) })
        
        tvSunriseTime = TextView(this).apply { text = "🌅 সূর্যোদয়: --:--"; textSize = 12f; setTextColor(theme.textSub); setPadding(dp(4), 0, dp(4), 0) }
        infoRow.addView(tvSunriseTime)

        tvSunsetTime = TextView(this).apply { text = "🌇 সূর্যাস্ত: --:--"; textSize = 12f; setTextColor(theme.textSub); setPadding(dp(12), 0, 0, 0) }
        infoRow.addView(tvSunsetTime)

        countdownCard.addView(infoRow)
        content.addView(countdownCard)

        // ================= ৩. বক্স ১: পাঁচ ওয়াক্তের সময়সূচি (একপাশে নাম, অন্যপাশে টাইম) =================
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0F172A")) // ডিপ স্লেট থিম
                setStroke(dp(1), Color.parseColor("#334155"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        prayerCard.addView(TextView(this).apply {
            text = "🕋 পাঁচ ওয়াক্ত নামাজের সময়সূচি"
            textSize = 15f
            setTextColor(Color.parseColor("#38BDF8"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        fun createPrayerRow(key: String, name: String, timeTextView: TextView): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(6), dp(8), dp(6), dp(8))
            }
            // বাম পাশে ওয়াক্তের নাম
            val nameView = TextView(this).apply {
                text = name
                textSize = 14f
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            }
            // ডান পাশে সময় ও সুইচ
            val rightLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            timeTextView.textSize = 12f
            timeTextView.setTextColor(Color.parseColor("#94A3B8"))
            timeTextView.setPadding(0, 0, dp(10), 0)

            rightLayout.addView(timeTextView)
            rightLayout.addView(Switch(this).apply { 
                isChecked = true
                setOnCheckedChangeListener { _, isChecked ->
                    val status = if (isChecked) "চালু" else "বন্ধ"
                    Toast.makeText(this@MainActivity, "$name অ্যালার্ম $status", Toast.LENGTH_SHORT).show()
                }
            })

            row.addView(nameView, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(rightLayout, LinearLayout.LayoutParams(-2, -2))

            prayerRows[key] = row
            return row
        }

        tvFajrTime = TextView(this)
        tvZoharTime = TextView(this)
        tvAsrTime = TextView(this)
        tvMaghribTime = TextView(this)
        tvIshaTime = TextView(this)

        prayerCard.addView(createPrayerRow("Fajr", "ফজর", tvFajrTime))
        prayerCard.addView(createPrayerRow("Dhuhr", "যোহর", tvZoharTime))
        prayerCard.addView(createPrayerRow("Asr", "আসর", tvAsrTime))
        prayerCard.addView(createPrayerRow("Maghrib", "মাগরিব", tvMaghribTime))
        prayerCard.addView(createPrayerRow("Isha", "এশা", tvIshaTime))
        content.addView(prayerCard)

        // ================= ৪. বক্স ২: তাহাজ্জুদ ও ইফতার =================
        val specialCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#065F46")) // ডিপ গ্রিন থিম
                setStroke(dp(1), Color.parseColor("#059669"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        specialCard.addView(TextView(this).apply {
            text = "🌙 তাহাজ্জুদ ও ইফতারের সময়"
            textSize = 15f
            setTextColor(Color.parseColor("#A7F3D0"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        fun createSimpleRow(label: String, timeView: TextView): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, dp(4))
            }
            timeView.textSize = 13f
            timeView.setTextColor(Color.WHITE)
            row.addView(TextView(this).apply { text = label; textSize = 13f; textColor = Color.parseColor("#E2E8F0") }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(timeView)
            return row
        }

        tvTahajjudTime = TextView(this)
        tvIftarTime = TextView(this)

        specialCard.addView(createSimpleRow("তাহাজ্জুদ", tvTahajjudTime))
        specialCard.addView(createSimpleRow("সেহরি ও ইফতার", tvIftarTime))
        content.addView(specialCard)

        // ================= ৫. বক্স ৩: হারাম ওয়াক্ত (নিষিদ্ধ সময়) =================
        val haramCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#7F1D1D")) // ডিপ রেড থিম
                setStroke(dp(1), Color.parseColor("#EF4444"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        haramCard.addView(TextView(this).apply {
            text = "⚠️ হারাম ওয়াক্ত (নামাজ নিষিদ্ধ সময়)"
            textSize = 15f
            setTextColor(Color.parseColor("#FCA5A5"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvHaramTime = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(0, dp(2), 0, dp(2))
        }
        haramCard.addView(tvHaramTime)
        content.addView(haramCard)

        // ================= ৬. টপ ফিচার শর্টকাট =================
        val featureHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, 0, 0, dp(8))
        }
        featureHeader.addView(TextView(this).apply { text = "⭐ টপ ফিচারসমূহ"; textSize = 16f; setTextColor(theme.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(featureHeader)

        val shortcutsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            weightSum = 3f
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        fun addShortcut(title: String, icon: String, targetActivity: Class<*>) {
            val sCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = GradientDrawable().apply { setColor(theme.cardBg); cornerRadius = dp(12).toFloat(); setStroke(dp(1), theme.cardStroke) }
                setPadding(dp(8), dp(12), dp(8), dp(12))
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
                setOnClickListener { startActivity(Intent(this@MainActivity, targetActivity)) }
            }
            sCard.addView(TextView(this).apply { text = icon; textSize = 22f; gravity = Gravity.CENTER })
            sCard.addView(TextView(this).apply { text = title; textSize = 12f; setTextColor(theme.textMain); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
            shortcutsRow.addView(sCard)
        }

        addShortcut("তাসবিহ", "📿", TasbihActivity::class.java)
        addShortcut("লাইব্রেরী", "📚", LibraryActivity::class.java)
        addShortcut("নোটপ্যাড", "📝", NotepadActivity::class.java)

        content.addView(shortcutsRow)
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= ৭. নেভিগেশন বার =================
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(6), dp(2), dp(6))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরী", "📝\nনোট", "⚙️\nসেটিংস")
        navItems.forEach { label ->
            bottomNav.addView(Button(this).apply {
                text = label; textSize = 11f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("হোম")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
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
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun loadCachedPrayerTimes() {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE)
        val keys = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Sunset", "Maghrib", "Isha")
        keys.forEach { key ->
            prefs.getString(key, null)?.let { timingsMap[key] = it }
        }
        applyTimingsToUI()
    }

    private fun saveTimingsToCache(times: Map<String, String>) {
        val prefs = getSharedPreferences("PrayerCache", Context.MODE_PRIVATE).edit()
        times.forEach { (k, v) -> prefs.putString(k, v) }
        prefs.apply()
    }

    private fun convertTo12Hour(time24: String?): String {
        if (time24.isNullOrEmpty()) return "--:--"
        return try {
            val parts = time24.trim().split(":")
            var hour = parts[0].toInt()
            val minute = parts[1]
            val ampm = if (hour >= 12) "PM" else "AM"
            if (hour > 12) hour -= 12
            if (hour == 0) hour = 12
            bn(String.format(Locale.ENGLISH, "%02d:%s %s", hour, minute, ampm))
        } catch (e: Exception) {
            time24
        }
    }

    private fun adjustTime(time24: String?, minutesToAdd: Int): String {
        if (time24.isNullOrEmpty()) return "--:--"
        try {
            val parts = time24.trim().split(":")
            val totalMin = parts[0].toInt() * 60 + parts[1].toInt() + minutesToAdd
            val newHour = (totalMin / 60) % 24
            val newMin = totalMin % 60
            return convertTo12Hour(String.format(Locale.ENGLISH, "%02d:%02d", newHour, newMin))
        } catch (e: Exception) {
            return convertTo12Hour(time24)
        }
    }

    private fun applyTimingsToUI() {
        val fajr = timingsMap["Fajr"] ?: "04:30"
        val sunrise = timingsMap["Sunrise"] ?: "05:46"
        val dhuhr = timingsMap["Dhuhr"] ?: "12:03"
        val asr = timingsMap["Asr"] ?: "15:31"
        val sunset = timingsMap["Sunset"] ?: timingsMap["Maghrib"] ?: "18:20"
        val maghrib = timingsMap["Maghrib"] ?: "18:20"
        val isha = timingsMap["Isha"] ?: "19:37"

        // এক সাইডে ওয়াক্ত, অন্য সাইডে শুরু ও শেষ সময়
        tvFajrTime.text = "${convertTo12Hour(fajr)} - ${convertTo12Hour(sunrise)}"
        tvZoharTime.text = "${convertTo12Hour(dhuhr)} - ${convertTo12Hour(asr)}"
        tvAsrTime.text = "${convertTo12Hour(asr)} - ${convertTo12Hour(sunset)}"
        tvMaghribTime.text = "${convertTo12Hour(maghrib)} - ${convertTo12Hour(isha)}"
        tvIshaTime.text = "${convertTo12Hour(isha)} - ${convertTo12Hour(fajr)}"

        tvSunriseTime.text = "🌅 সূর্যোদয়: ${convertTo12Hour(sunrise)}"
        tvSunsetTime.text = "🌇 সূর্যাস্ত: ${convertTo12Hour(sunset)}"

        tvTahajjudTime.text = "১২:০০ AM - ${convertTo12Hour(fajr)}"
        tvIftarTime.text = "শেষ: ${convertTo12Hour(fajr)} | ইফতার: ${convertTo12Hour(maghrib)}"

        val sunriseEnd = adjustTime(sunrise, 15)
        val zoharStart = convertTo12Hour(dhuhr)
        val sunsetStart = adjustTime(sunset, -15)
        
        tvHaramTime.text = "• সূর্যোদয়: ${convertTo12Hour(sunrise)} - $sunriseEnd\n• দ্বিপ্রহর: $zoharStart এর পূর্বে\n• সূর্যাস্ত: $sunsetStart - ${convertTo12Hour(sunset)}"

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
