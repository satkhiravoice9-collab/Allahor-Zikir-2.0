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

        val themeColors = ThemeManager.getTheme(this)
        bgMain = themeColors.bgMain
        cardBg = themeColors.cardBg
        cardStroke = themeColors.cardStroke
        textMain = themeColors.textMain
        textSub = themeColors.textSub

        buildUI()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        // Top bar
        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        top.addView(TextView(this).apply {
            text = "👤 প্রোফাইল ও সেটিংস"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(80))
        }

        // Mobile number setup card
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBg)
                setStroke(dp(1), cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(15) }
        }

        card.addView(TextView(this).apply {
            text = "আপনার মোবাইল নম্বর বা ফোল্ডার আইডি"
            textSize = 15f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        card.addView(TextView(this).apply {
            text = "এই নম্বরটি দিয়ে ক্লাউডে আপনার আলাদা ফোল্ডার তৈরি হবে। অ্যাপ রিইন্সটল করলেও এই নম্বর দিয়ে আপনার সব ডেটা ফিরে পাবেন।"
            textSize = 13f
            setTextColor(textSub)
            setPadding(0, 0, 0, dp(12))
        })

        val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentMobile = prefs.getString("user_mobile", "01700000000") ?: "01700000000"

        val inputMobile = EditText(this).apply {
            hint = "মোবাইল নম্বর লিখুন (যেমন: 017xxxxxxxx)"
            setText(currentMobile)
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        card.addView(inputMobile)

        val saveBtn = Button(this).apply {
            text = "সংরক্ষণ করুন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val mobile = inputMobile.text.toString().trim()
                if (mobile.isNotEmpty()) {
                    prefs.edit().putString("user_mobile", mobile).apply()
                    Toast.makeText(this@ProfileSettingsActivity, "সফলভাবে সংরক্ষণ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileSettingsActivity, "দয়া করে সঠিক নম্বর দিন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        card.addView(saveBtn)
        contentLayout.addView(card)

        scroll.addView(contentLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // Bottom Navigation
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
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@ProfileSettingsActivity, "প্রোফাইল সেটিংস সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> {}
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
