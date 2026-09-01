package com.sabbirsamol.app

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private lateinit var statusText: TextView
    private lateinit var countdownText: TextView
    private val handler = Handler(Looper.getMainLooper())

    data class Prayer(val name: String, val icon: String, val start: Int, val end: Int)
    private val prayers = listOf(
        Prayer("ফজর", "🌅", 270, 345),
        Prayer("যোহর", "☀️", 735, 990),
        Prayer("আসর", "🌤️", 990, 1110),
        Prayer("মাগরিব", "🌇", 1110, 1200),
        Prayer("এশা", "🌙", 1200, 1470)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = Color.WHITE
        buildHome()
        updateClock()
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateClock()
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun bg(color: Int, radius: Int = 16) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun row(icon: String, label: String, value: String, color: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(7), dp(2), dp(4), dp(2))
        background = bg(color, 9)
        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 17f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(31), dp(34)))
        addView(TextView(this@MainActivity).apply {
            text = label
            textSize = 13f
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(34), 1f))
        addView(TextView(this@MainActivity).apply {
            text = value
            textSize = 11f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(124), dp(34)))
        addView(Switch(this@MainActivity).apply {
            isChecked = false
            scaleX = .7f
            scaleY = .7f
            contentDescription = "$label অ্যালার্ম"
            setOnCheckedChangeListener { _, on ->
                Toast.makeText(
                    this@MainActivity,
                    if (on) "$label অ্যালার্ম চালু" else "$label অ্যালার্ম বন্ধ",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, LinearLayout.LayoutParams(dp(42), dp(34)))
    }

    private fun section(title: String, side: Int, inside: Int) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(4), 0, 0, 0)
        background = bg(side)
        addView(LinearLayout(this@MainActivity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            background = bg(inside, 13)
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 15f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
            })
        })
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(246, 248, 247))
        }
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            view.setPadding(0, 0, 0, insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom)
            insets
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(7), dp(4), dp(7), dp(4))
        }
        content.addView(TextView(this).apply {
            text = "📅 আজকের তারিখ"
            textSize = 15f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(20, 92, 64))
            setTypeface(null, android.graphics.Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "বাংলা তারিখ • ইংরেজি তারিখ • হিজরি তারিখ"
            textSize = 11f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(82, 92, 88))
        })
        statusText = TextView(this).apply {
            textSize = 19f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        countdownText = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(255, 235, 235))
        }
        content.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = bg(Color.rgb(177, 54, 54))
            addView(statusText)
            addView(countdownText)
        }, LinearLayout.LayoutParams(-1, dp(72)).apply { bottomMargin = dp(5) })

        val p = section("🕌  নামাজের সময়সূচী", Color.rgb(8, 75, 54), Color.rgb(30, 126, 86))
        prayers.forEach { prayer ->
            p.addView(row(prayer.icon, prayer.name, "${fmt(prayer.start)} — ${fmt(prayer.end)}", Color.rgb(39, 145, 99)))
        }
        content.addView(p, LinearLayout.LayoutParams(-1, dp(199)).apply { bottomMargin = dp(4) })

        val f = section("🚫  নামাজের নিষিদ্ধ সময়", Color.rgb(125, 48, 43), Color.rgb(181, 71, 61))
        f.addView(simple("🌅", "সূর্যোদয়", "০৫:৪৫ — ০৬:০০ AM"))
        f.addView(simple("☀️", "দুপুর", "১২:০৫ — ১২:১৫ PM"))
        f.addView(simple("🌇", "সূর্যাস্ত", "০৬:২০ — ০৬:৩০ PM"))
        content.addView(f, LinearLayout.LayoutParams(-1, dp(112)).apply { bottomMargin = dp(4) })

        val n = section("🌙  তাহাজ্জুদ • সেহরি • ইফতার", Color.rgb(61, 51, 103), Color.rgb(91, 78, 151))
        n.addView(simple("🌙", "তাহাজ্জুদ", "০১:০০ — ০৪:২০ AM"))
        n.addView(simple("🌄", "সেহরি শেষ", "০৪:৩০ AM"))
        n.addView(simple("🌇", "ইফতার", "০৬:৩০ PM"))
        content.addView(n, LinearLayout.LayoutParams(-1, dp(112)).apply { bottomMargin = dp(4) })

        val ishraq = section("☀️  ইশরাক • চাশত", Color.rgb(137, 91, 25), Color.rgb(198, 135, 42))
        ishraq.addView(simple("🌤️", "ইশরাক", "সূর্যোদয়ের পর"))
        ishraq.addView(simple("☀️", "চাশত", "সূর্যোদয়ের পর থেকে যোহরের আগে"))
        content.addView(ishraq, LinearLayout.LayoutParams(-1, dp(82)))

        root.addView(ScrollView(this).apply {
            addView(content)
            isFillViewport = true
        }, LinearLayout.LayoutParams(-1, 0, 1f))

        val menu = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(1, 2, 1, 2)
        }
        listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরি", "🤲\nমাসনুন", "📝\nনোট", "🔄\nরিফ্রেশ", "ℹ️\nএবাউট").forEach { label ->
            menu.addView(Button(this).apply {
                text = label
                textSize = 9f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setOnClickListener {
                    Toast.makeText(this@MainActivity, label.replace("\n", " "), Toast.LENGTH_SHORT).show()
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f))
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(58)))
        setContentView(root)
    }

    private fun simple(icon: String, label: String, value: String) = TextView(this).apply {
        text = "$icon   $label    $value"
        textSize = 12f
        gravity = Gravity.CENTER
        setTextColor(Color.WHITE)
        setPadding(0, dp(2), 0, dp(2))
    }

    private fun updateClock() {
        val calendar = Calendar.getInstance()
        val now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val activePrayer = prayers.lastOrNull { now >= it.start && now < it.end }
        val nextPrayer = prayers.firstOrNull { it.start > now } ?: prayers.first()
        var totalSeconds = (nextPrayer.start - now) * 60 - second
        if (totalSeconds <= 0) totalSeconds += 24 * 60 * 60
        statusText.text = if (activePrayer != null) {
            "🕐 ${activePrayer.name} ওয়াক্ত চলছে"
        } else {
            "🕐 পরবর্তী ওয়াক্ত: ${nextPrayer.name}"
        }
        countdownText.text = "শুরু হতে বাকি %02d:%02d:%02d".format(
            totalSeconds / 3600,
            (totalSeconds % 3600) / 60,
            totalSeconds % 60
        )
    }

    private fun fmt(minutes: Int): String {
        val hour = (minutes / 60) % 24
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        return "%02d:%02d %s".format(displayHour, minutes % 60, amPm)
    }
}
