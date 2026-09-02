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
    private lateinit var tvDateDisplay: TextView

    private lateinit var tvFajrTime: TextView
    private lateinit var tvZoharTime: TextView
    private lateinit var tvAsrTime: TextView
    private lateinit var tvMaghribTime: TextView
    private lateinit var tvIshaTime: TextView

    private lateinit var tvTahajjudTime: TextView
    private lateinit var tvSehriTime: TextView
    private lateinit var tvIftarTime: TextView

    private lateinit var tvHaramSunrise: TextView
    private lateinit var tvHaramZohar: TextView
    private lateinit var tvHaramSunset: TextView

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
            setPadding(dp(12), dp(12), dp(12), dp(75))
        }

        // ================= ১. টপ বার (ইংরেজি, বাংলা ও সন্ধ্যার পর অটো আপডেট আরবি তারিখ) =================
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(10))
        }

        tvDateDisplay = TextView(this).apply {
            text = "তারিখ লোড হচ্ছে..."
            textSize = 13f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
        }
        updateDynamicDates()

        topBar.addView(tvDateDisplay, LinearLayout.LayoutParams(0, -2, 1f))

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

        // ================= ৩. হেল্পার ফাংশন =================
        fun createUniformRow(key: String, name: String, timeTextView: TextView, isSwitchNeeded: Boolean, textColor: Int, subColor: Int): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(6), dp(8), dp(6), dp(8))
            }
            val nameView = TextView(this).apply {
                text = name
                textSize = 14f
                setTextColor(textColor)
                setTypeface(null, Typeface.BOLD)
            }
            val rightLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            timeTextView.textSize = 12f
            timeTextView.setTextColor(subColor)
            timeTextView.setPadding(0, 0, if(isSwitchNeeded) dp(10) else 0, 0)

            rightLayout.addView(timeTextView)
            if (isSwitchNeeded) {
                rightLayout.addView(Switch(this).apply { 
                    isChecked = true
                    setOnCheckedChangeListener { _, isChecked ->
                        val status = if (isChecked) "চালু" else "বন্ধ"
                        Toast.makeText(this@MainActivity, "$name অ্যালার্ম $status", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            row.addView(nameView, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(rightLayout, LinearLayout.LayoutParams(-2, -2))

            prayerRows[key] = row
            return row
        }

        // ================= ৪. বক্স ১: পাঁচ ওয়াক্তের সময়সূচি (হলুদ ব্যাকগ্রাউন্ড) =================
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#FACC15"))
                setStroke(dp(1), Color.parseColor("#EAB308"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        prayerCard.addView(TextView(this).apply {
            text = "🕋 পাঁচ ওয়াক্ত নামাজের সময়সূচি (হানাফি)"
            textSize = 15f
            setTextColor(Color.parseColor("#78350F"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        tvFajrTime = TextView(this)
        tvZoharTime = TextView(this)
        tvAsrTime = TextView(this)
        tvMaghribTime = TextView(this)
        tvIshaTime = TextView(this)

        prayerCard.addView(createUniformRow("Fajr", "ফজর", tvFajrTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createUniformRow("Dhuhr", "যোহর", tvZoharTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createUniformRow("Asr", "আসর", tvAsrTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createUniformRow("Maghrib", "মাগরিব", tvMaghribTime, true, Color.BLACK, Color.parseColor("#334155")))
        prayerCard.addView(createUniformRow("Isha", "এশা", tvIshaTime, true, Color.BLACK, Color.parseColor("#334155")))
        content.addView(prayerCard)

        // ================= ৫. বক্স ২: তাহাজ্জুদ ও সেহরি-ইফতার (সাদা ব্যাকগ্রাউন্ড) =================
        val specialCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.WHITE) // সাদা কালার
                setStroke(dp(1), Color.parseColor("#CBD5E1"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        specialCard.addView(TextView(this).apply {
            text = "🌙 তাহাজ্জুদ, সেহরি ও ইফতার"
            textSize = 15f
            setTextColor(Color.parseColor("#0F172A"))
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvTahajjudTime = TextView(this)
        tvSehriTime = TextView(this)
        tvIftarTime = TextView(this)

        specialCard.addView(createUniformRow("Tahajjud", "তাহাজ্জুদ", tvTahajjudTime, false, Color.BLACK, Color.parseColor("#475569")))
        specialCard.addView(createUniformRow("Sehri", "সেহরির শেষ সময়", tvSehriTime, false, Color.BLACK, Color.parseColor("#475569")))
        specialCard.addView(createUniformRow("Iftar", "ইফতারের সময়", tvIftarTime, false, Color.BLACK, Color.parseColor("#475569")))
        content.addView(specialCard)

        // ================= ৬. বক্স ৩: হারাম ওয়াক্ত (লাল ব্যাকগ্রাউন্ড) =================
        val haramCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#EF4444")) // লাল কালার
                setStroke(dp(1), Color.parseColor("#DC2626"))
                cornerRadius = dp(16).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }

        haramCard.addView(TextView(this).apply {
            text = "⚠️ হারাম ওয়াক্ত (নামাজ নিষিদ্ধ সময়)"
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        tvHaramSunrise = TextView(this)
        tvHaramZohar = TextView(this)
        tvHaramSunset = TextView(this)

        haramCard.addView(createUniformRow("HaramSunrise", "সূর্যোদয় নিষিদ্ধ সময়", tvHaramSunrise, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        haramCard.addView(createUniformRow("HaramZohar", "দ্বিপ্রহর নিষিদ্ধ সময়", tvHaramZohar, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        haramCard.addView(createUniformRow("HaramSunset", "সূর্যাস্ত নিষিদ্ধ সময়", tvHaramSunset, false, Color.WHITE, Color.parseColor("#FEE2E2")))
        content.addView(haramCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= ৭. ফিক্সড বটম নেভিগেশন বার =================
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
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

    private fun updateDynamicDates() {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        // সন্ধ্যার সময় (সন্ধ্যা ৬টা বা ১৮:০০ টার পর আরবি তারিখ পরবর্তী দিনে পরিবর্তিত হওয়ার লজিক)
        if (hour >= 18) {
            now.add(Calendar.DAY_OF_MONTH, 1)
        }

        // ইংরেজি, বাংলা ও আরবি তারিখ ফরম্যাট
        val engFormat = SimpleDateFormat("dd MMM, EEEE", Locale("bn", "BD"))
        val hijriFormat = SimpleDateFormat("dd MMMM", ULocale("ar", "SA", "@calendar=islamic-umalqura"))
        
        val engStr = engFormat.format(Date())
        val hijriStr = hijriFormat.format(now.time)

        tvDateDisplay.text = "📅 $engStr | আরবি: $hijriStr হি."
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
        val sunset = timingsMap["Sunset"] ?: timingsMap["Maghrib"] ?: "18:20"
        val maghrib = timingsMap["Maghrib"] ?: "18:20"
        val isha = timingsMap["Isha"] ?: "19:37"

        // পাঁচ ওয়াক্ত
        tvFajrTime.text = "${convertTo12Hour(fajr)} - ${convertTo12Hour(sunrise)}"
        tvZoharTime.text = "${convertTo12Hour(dhuhr)} - ০৪:৩৩ PM"
        tvAsrTime.text = "০৪:৩৩ PM - ${convertTo12Hour(sunset)}"
        tvMaghribTime.text = "${convertTo12Hour(maghrib)} - ${convertTo12Hour(isha)}"
        tvIshaTime.text = "${convertTo12Hour(isha)} - ${convertTo12Hour(fajr)}"

        tvSunriseTime.text = "🌅 সূর্যোদয়: ${convertTo12Hour(sunrise)}"
        tvSunsetTime.text = "🌇 সূর্যাস্ত: ${convertTo12Hour(sunset)}"

        // তাহাজ্জুদ ও সেহরি-ইফতার (সাদা বক্সের জন্য)
        tvTahajjudTime.text = "১২:০০ AM - ${convertTo12Hour(fajr)}"
        tvSehriTime.text = "${convertTo12Hour(fajr)} এর পূর্বে"
        tvIftarTime.text = "${convertTo12Hour(maghrib)}"

        // হারাম ওয়াক্ত (লাল বক্সের জন্য)
        val sunriseEnd = adjustTime(sunrise, 15)
        val sunsetStart = adjustTime(sunset, -15)

        tvHaramSunrise.text = "${convertTo12Hour(sunrise)} - $sunriseEnd"
        tvHaramZohar.text = "০৪:২৮ PM - ০৪:৩৩ PM"
        tvHaramSunset.text = "$sunsetStart - ${convertTo12Hour(sunset)}"

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
        val asr = 16 * 60 + 33 // ৪:৩৩ PM
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
