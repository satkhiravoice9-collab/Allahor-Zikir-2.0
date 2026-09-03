package com.sabbirsamol.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class ProfileSettingsActivity : Activity() {

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
            text = "👤 প্রোফাইল ও সেটিংস"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        })

        // ইউপ্রোফাইল কার্ড
        val profileCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBg)
                setStroke(dp(1), cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        profileCard.addView(TextView(this).apply {
            text = "অ্যাপ ব্যবহারকারী"
            textSize = 16f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(4))
        })
        profileCard.addView(TextView(this).apply {
            text = "সংস্করণ: 2.0 (আল্লাহর জিকির)"
            textSize = 13f
            setTextColor(textSub)
        })
        content.addView(profileCard)

        // থেম সেটিংস কার্ড
        val themeCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBg)
                setStroke(dp(1), cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        themeCard.addView(TextView(this).apply {
            text = "🎨 অ্যাপ থেম নির্বাচন"
            textSize = 16f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        val btnDark = Button(this).apply {
            text = "ডার্ক থেম (ডিফল্ট)"
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply { setColor(Color.parseColor("#111827")); cornerRadius = dp(6).toFloat(); setStroke(dp(1), Color.parseColor("#374151")) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(8) }
            setOnClickListener {
                getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("app_theme", "গাঢ় থেম (ডিফল্ট)").apply()
                Toast.makeText(this@ProfileSettingsActivity, "ডার্ক থেম সেট করা হয়েছে। রিস্টার্ট করুন।", Toast.LENGTH_SHORT).show()
            }
        }
        themeCard.addView(btnDark)

        val btnLight = Button(this).apply {
            text = "লাইট থেম (সাদা ব্যাকগ্রাউন্ড)"
            setTextColor(Color.BLACK)
            isAllCaps = false
            background = GradientDrawable().apply { setColor(Color.parseColor("#F1F5F9")); cornerRadius = dp(6).toFloat(); setStroke(dp(1), Color.parseColor("#CBD5E1")) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit().putString("app_theme", "সাদা থেম").apply()
                Toast.makeText(this@ProfileSettingsActivity, "লাইট থেম সেট করা হয়েছে। রিস্টার্ট করুন।", Toast.LENGTH_SHORT).show()
            }
        }
        themeCard.addView(btnLight)
        content.addView(themeCard)

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
                setTextColor(if (label.contains("প্রোফাইল")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@ProfileSettingsActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@ProfileSettingsActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@ProfileSettingsActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@ProfileSettingsActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@ProfileSettingsActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@ProfileSettingsActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> {}
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
