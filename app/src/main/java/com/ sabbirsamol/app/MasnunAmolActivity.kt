package com.sabbirsamol.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class MasnunAmolActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private var bgMain: Int = Color.BLACK
    private var cardBg: Int = Color.WHITE
    private var cardStroke: Int = Color.GRAY
    private var textMain: Int = Color.WHITE
    private var textSub: Int = Color.GRAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val theme = ThemeManager.getTheme(this)
        bgMain = theme.bgMain
        cardBg = theme.cardBg
        cardStroke = theme.cardStroke
        textMain = theme.textMain
        textSub = theme.textSub

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(75))
        }

        content.addView(TextView(this).apply {
            text = "📖 আমল, মাসনুন ও দৈনন্দিন আমল"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        })

        val amolList = listOf(
            Pair("🌅 সকাল-সন্ধ্যার মাসনুন আমল", "আয়াতুল কুরসি, তিন কুল এবং নির্দিষ্ট দোয়া সমূহ"),
            Pair("🌙 ঘুমানোর পূর্বের আমল", "সুরা মূলক পাঠ এবং ডান কাতে শোয়া"),
            Pair("🕌 সালাতের পরের তাসবিহাত", "সুবহানাল্লাহ ৩৩ বার, আলহামদুলিল্লাহ ৩৩ বার, আল্লাহু আকবার ৩৪ বার"),
            Pair("📿 দরুদ শরীফ পাঠ", "প্রতিদিন নিয়মিত নির্দিষ্ট সংখ্যক দরুদ পাঠ")
        )

        amolList.forEach { (title, desc) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(cardBg)
                    setStroke(dp(1), cardStroke)
                    cornerRadius = dp(12).toFloat()
                }
                setPadding(dp(16), dp(16), dp(16), dp(16))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            }
            card.addView(TextView(this).apply {
                text = title
                textSize = 16f
                setTextColor(textMain)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            })
            card.addView(TextView(this).apply {
                text = desc
                textSize = 13f
                setTextColor(textSub)
            })
            content.addView(card)
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // বটম নেভিগেশন বার
        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf(
            Pair("🏠\nহোম", MainActivity::class.java),
            Pair("📿\nতাসবিহ", TasbihActivity::class.java),
            Pair("📚\nলাইব্রেরী", LibraryActivity::class.java),
            Pair("📖\nআমল", MasnunAmolActivity::class.java),
            Pair("📝\nনোটপ্যাড", NotepadActivity::class.java),
            Pair("🔄\nসিঙ্ক", null),
            Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java)
        )

        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("আমল")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@MasnunAmolActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@MasnunAmolActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@MasnunAmolActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> {}
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@MasnunAmolActivity, MasnunAmolActivity::class.java)); finish() } // fixed target
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@MasnunAmolActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@MasnunAmolActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
