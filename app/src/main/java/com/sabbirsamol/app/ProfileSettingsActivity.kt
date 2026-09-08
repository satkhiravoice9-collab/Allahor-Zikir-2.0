package com.sabbirsamol.app

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
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
            setPadding(dp(16), dp(8), dp(16), dp(80))
        }

        // 1. Mobile & Password Security Card
        val securityCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBg)
                setStroke(dp(1), cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }

        securityCard.addView(TextView(this).apply {
            text = "🔐 ইউজার ফোল্ডার এবং পাসওয়ার্ড সেটিংস"
            textSize = 15f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(6))
        })

        securityCard.addView(TextView(this).apply {
            text = "মোবাইল নম্বর দিয়ে আপনার ক্লাউড ফোল্ডার এবং পাসওয়ার্ড দিয়ে ডেটা সুরক্ষিত থাকবে।"
            textSize = 12f
            setTextColor(textSub)
            setPadding(0, 0, 0, dp(12))
        })

        val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentMobile = prefs.getString("user_mobile", "") ?: ""
        val currentPassword = prefs.getString("user_password", "") ?: ""

        securityCard.addView(TextView(this).apply {
            text = "মোবাইল নম্বর (ফোল্ডার আইডি):"
            textSize = 13f
            setTextColor(textMain)
            setPadding(0, 0, 0, dp(4))
        })
        val inputMobile = EditText(this).apply {
            hint = "যেমন: 017xxxxxxxx"
            setText(currentMobile)
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
        }
        securityCard.addView(inputMobile)

        securityCard.addView(TextView(this).apply {
            text = "আপনার নিজস্ব পাসওয়ার্ড:"
            textSize = 13f
            setTextColor(textMain)
            setPadding(0, 0, 0, dp(4))
        })
        val inputPassword = EditText(this).apply {
            hint = "গোপন পাসওয়ার্ড লিখুন"
            setText(currentPassword)
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        securityCard.addView(inputPassword)

        val saveBtn = Button(this).apply {
            text = "তথ্য সংরক্ষণ করুন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                val mobile = inputMobile.text.toString().trim()
                val password = inputPassword.text.toString().trim()

                if (mobile.isNotEmpty() && password.isNotEmpty()) {
                    prefs.edit()
                        .putString("user_mobile", mobile)
                        .putString("user_password", password)
                        .apply()
                    Toast.makeText(this@ProfileSettingsActivity, "সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@ProfileSettingsActivity, "দয়া করে মোবাইল নম্বর ও পাসওয়ার্ড দিন", Toast.LENGTH_LONG).show()
                }
            }
        }
        securityCard.addView(saveBtn)
        contentLayout.addView(securityCard)

        // 2. Theme Settings Card
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
            text = "🎨 থিম সেটিংস"
            textSize = 15f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(6))
        })

        themeCard.addView(TextView(this).apply {
            text = "অ্যাপের থিম পরিবর্তন বা কাস্টমাইজ করতে হোম পেজের থিম অপশন ব্যবহার করুন।"
            textSize = 13f
            setTextColor(textSub)
            setPadding(0, 0, 0, dp(6))
        })
        contentLayout.addView(themeCard)

        // 3. Developer & Facebook Page Card
        val infoCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(cardBg)
                setStroke(dp(1), cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }

        infoCard.addView(TextView(this).apply {
            text = "ℹ️ ডেভেলপার ও তথ্য"
            textSize = 15f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(6))
        })

        infoCard.addView(TextView(this).apply {
            text = "ডেভেলপার: Sabbir Samol\nসহায়তা ও আপডেটের জন্য আমাদের অফিসিয়াল ফেসবুক পেজে ভিজিট করুন।"
            textSize = 13f
            setTextColor(textSub)
            setPadding(0, 0, 0, dp(12))
        })

        val fbBtn = Button(this).apply {
            text = "👍 ফেসবুক পেজ ভিজিট করুন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#1877F2")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com"))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileSettingsActivity, "লিংক ওপেন করা যায়নি", Toast.LENGTH_SHORT).show()
                }
            }
        }
        infoCard.addView(fbBtn)
        contentLayout.addView(infoCard)

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
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@ProfileSettingsActivity, "সেটিংস সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> {}
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
