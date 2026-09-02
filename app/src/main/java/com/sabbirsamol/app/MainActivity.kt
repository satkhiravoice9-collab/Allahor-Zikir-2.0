package com.sabbirsamol.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
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

    private lateinit var tvFajrTime: TextView
    private lateinit var tvZoharTime: TextView
    private lateinit var tvAsrTime: TextView
    private lateinit var tvMaghribTime: TextView
    private lateinit var tvIshaTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupProfessionalUI()
        loadOnlinePrayerTimes()
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
            text = "🔔"
            textSize = 18f
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setOnClickListener { Toast.makeText(this@MainActivity, "কোনো নতুন নোটিফিকেশন নেই", Toast.LENGTH_SHORT).show() }
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

        countdownCard.addView(TextView(this).apply {
            text = "আসর"
            textSize = 22f
            setTextColor(theme.textAccent)
            setTypeface(null, Typeface.BOLD)
        })

        countdownCard.addView(TextView(this).apply {
            text = "শেষ হতে বাকি\n০০:১৬:০৮"
            textSize = 26f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(12))
        })

        val infoRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        infoRow.addView(TextView(this).apply { text = "📍 সাতক্ষীরা ▾"; textSize = 13f; setTextColor(theme.textSub); setPadding(0, 0, dp(16), 0) })
        infoRow.addView(TextView(this).apply { text = "🌅 সূর্যোদয়: ০৫:৪৬"; textSize = 13f; setTextColor(theme.textSub); setPadding(dp(8), 0, dp(8), 0) })
        infoRow.addView(TextView(this).apply { text = "🌇 সূর্যাস্ত: ১৮:২০"; textSize = 13f; setTextColor(theme.textSub); setPadding(dp(16), 0, 0, 0) })
        countdownCard.addView(infoRow)

        content.addView(countdownCard)

        // ================= ৩. ট্যাব (অ্যালরম ও ক্যালেন্ডার) =================
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
        })
        tabRow.addView(Button(this).apply {
            text = "📅 ক্যালেন্ডার"
            isAllCaps = false; textSize = 13f; setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(theme.cardBg); setStroke(dp(1), theme.cardStroke); cornerRadius = dp(8).toFloat() }
            layoutParams = LinearLayout.LayoutParams(0, -1, 1f).apply { leftMargin = dp(6) }
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

        fun createPrayerRow(name: String, timeTextView: TextView, isCurrent: Boolean): LinearLayout {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(8), dp(10), dp(8), dp(10))
                if (isCurrent) {
                    background = GradientDrawable().apply {
                        setColor(Color.parseColor("#1E3A8A"))
                        cornerRadius = dp(8).toFloat()
                    }
                }
            }
            row.addView(TextView(this).apply { text = name; textSize = 15f; setTextColor(if(isCurrent) Color.WHITE else theme.textMain); setTypeface(null, if(isCurrent) Typeface.BOLD else Typeface.NORMAL) }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(timeTextView.apply { textSize = 14f; setTextColor(if(isCurrent) Color.parseColor("#FBBF24") else theme.textSub); setPadding(0, 0, dp(12), 0) })
            row.addView(Switch(this).apply { isChecked = true })
            return row
        }

        tvFajrTime = TextView(this).apply { text = "লোড হচ্ছে..." }
        tvZoharTime = TextView(this).apply { text = "লোড হচ্ছে..." }
        tvAsrTime = TextView(this).apply { text = "লোড হচ্ছে..." }
        tvMaghribTime = TextView(this).apply { text = "লোড হচ্ছে..." }
        tvIshaTime = TextView(this).apply { text = "লোড হচ্ছে..." }

        prayerCard.addView(createPrayerRow("ফজর", tvFajrTime, false))
        prayerCard.addView(createPrayerRow("যোহর", tvZoharTime, false))
        prayerCard.addView(createPrayerRow("আসর", tvAsrTime, true)) // বর্তমান ওয়াক্ত হাইলাইট
        prayerCard.addView(createPrayerRow("মাগরিব", tvMaghribTime, false))
        prayerCard.addView(createPrayerRow("এশা", tvIshaTime, false))

        val divider = View(this).apply {
            background = GradientDrawable().apply { setColor(theme.cardStroke) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(8), 0, dp(8)) }
        }
        prayerCard.addView(divider)
        
        val dummyTahajjud = TextView(this).apply { text = "রাত ০২:৩০ - ০৪:১৫" }
        val dummyIftar = TextView(this).apply { text = "সেহরি: ০৪:১৫ | ইফতার: ১৮:২০" }
        val dummyHaram = TextView(this).apply { text = "সূর্যোদয় ও দ্বিপ্রহর নিষিদ্ধ" }

        prayerCard.addView(createPrayerRow("🌙 তাহাজ্জুদ (শেষ তৃতীয়াংশ)", dummyTahajjud, false))
        prayerCard.addView(createPrayerRow("🍽️ ইফতার ও সেহরি", dummyIftar, false))
        prayerCard.addView(createPrayerRow("⚠️ হারাম ওয়াক্ত (মাকরুহ)", dummyHaram, false))

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
        tasbihCard.addView(TextView(this).apply { text = "০%"; textSize = 16f; setTextColor(theme.textAccent); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER })
        tasbihCard.addView(TextView(this).apply { text = "তাসবিহ"; textSize = 12f; setTextColor(theme.textMain); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
        shortcutsRow.addView(tasbihCard)

        fun addShortcut(title: String, targetActivity: Class<*>) {
            val sCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                background = GradientDrawable().apply { setColor(theme.cardBg); cornerRadius = dp(12).toFloat(); setStroke(dp(1), theme.cardStroke) }
                setPadding(dp(8), dp(12), dp(8), dp(12))
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
                setOnClickListener { startActivity(Intent(this@MainActivity, targetActivity)) }
            }
            sCard.addView(TextView(this).apply { text = "📿"; textSize = 22f; gravity = Gravity.CENTER })
            sCard.addView(TextView(this).apply { text = title; textSize = 12f; setTextColor(theme.textMain); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, 0) })
            shortcutsRow.addView(sCard)
        }

        addShortcut("লাইব্রেরী", LibraryActivity::class.java)
        addShortcut("নোটপ্যাড", NotepadActivity::class.java)
        addShortcut("সেটিংস", ProfileSettingsActivity::class.java)

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
                val zohar = onlineTimes["Dhuhr"] ?: ""
                val asr = onlineTimes["Asr"] ?: ""
                val maghrib = onlineTimes["Maghrib"] ?: ""
                val isha = onlineTimes["Isha"] ?: ""

                // লাইভ সময়গুলো প্রফেশনাল লিস্টে বসিয়ে দেওয়া হলো
                tvFajrTime.text = fajr
                tvZoharTime.text = zohar
                tvAsrTime.text = asr
                tvMaghribTime.text = maghrib
                tvIshaTime.text = isha

                Toast.makeText(context, "সাতক্ষীরার লাইভ সময় আপডেট হয়েছে", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "ইন্টারনেট সংযোগ না থাকায় লাইভ সময় লোড হয়নি", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
