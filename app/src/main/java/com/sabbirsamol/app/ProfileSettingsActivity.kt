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
import androidx.activity.ComponentActivity

class ProfileSettingsActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSettingsPage()
    }

    private fun showSettingsPage() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }

        // Top Bar
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(themeColors.textMain); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "👤 প্রোফাইল ও সেটিংস"; textSize = 17f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // 1. Mobile & Password Setup Card (Replacing Gmail/OTP)
        val securityCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        securityCard.addView(TextView(this).apply { text = "📱 মোবাইল নম্বর (ফোল্ডার আইডি):"; textSize = 15f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        
        val currentMobile = sharedPrefs.getString("user_mobile", "01725228622") ?: "01725228622"
        val currentPassword = sharedPrefs.getString("user_password", "") ?: ""

        val inputMobile = EditText(this).apply {
            hint = "মোবাইল নম্বর লিখুন"
            setText(currentMobile)
            textSize = 14f
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }
        }
        securityCard.addView(inputMobile)

        securityCard.addView(TextView(this).apply { text = "আপনার নিজস্ব পাসওয়ার্ড:"; textSize = 14f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        
        val inputPassword = EditText(this).apply {
            hint = "পাসওয়ার্ড লিখুন"
            setText(currentPassword)
            textSize = 14f
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.parseColor("#FFFFFF"))
            setTextColor(Color.BLACK)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(12) }
        }
        securityCard.addView(inputPassword)

        val saveBtn = Button(this).apply {
            text = "তথ্য সংরক্ষণ করুন"
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                val mobile = inputMobile.text.toString().trim()
                val password = inputPassword.text.toString().trim()
                if (mobile.isNotEmpty() && password.isNotEmpty()) {
                    sharedPrefs.edit()
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
        content.addView(securityCard)

        // 2. Theme Settings Card
        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })
        val savedTheme = sharedPrefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")
        val themes = listOf(Pair("⚪ সাদা থিম (লাইট)", "#F3F4F6"), Pair("⬛ কাবা থিম (ডার্ক গোল্ড)", "#1F2937"), Pair("🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "#064E3B"), Pair("🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)", "#B45309"))
        themes.forEach { (tName, tColor) ->
            val isSelected = savedTheme == tName
            themeCard.addView(Button(this).apply {
                text = if (isSelected) "✅ $tName" else tName
                isAllCaps = false; setTextColor(if(tName.contains("সাদা")) Color.BLACK else Color.WHITE)
                background = GradientDrawable().apply { setColor(Color.parseColor(tColor)); cornerRadius = dp(8).toFloat(); if (isSelected) setStroke(dp(3), themeColors.btnBg) else setStroke(dp(1), Color.GRAY) }
                layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(10) }
                setOnClickListener { sharedPrefs.edit().putString("app_theme", tName).apply(); Toast.makeText(this@ProfileSettingsActivity, "থিম আপডেট হয়েছে!", Toast.LENGTH_SHORT).show(); startActivity(intent); finish(); overridePendingTransition(0, 0) }
            })
        }
        content.addView(themeCard)

        // 3. Developer & Facebook Page Card
        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(themeColors.textAccent); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(themeColors.textMain); textSize = 15f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(4).toFloat(), 1f) })
        devCard.addView(Button(this).apply { text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(themeColors.btnBg); layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }; setOnClickListener { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))) } })
        devCard.addView(Button(this).apply { text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8")); layoutParams = LinearLayout.LayoutParams(-1, dp(42)); setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))) } })
        content.addView(devCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // Bottom Navigation
        val bottomNav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(Color.parseColor("#0F172A")); setPadding(dp(2), dp(4), dp(2), dp(4)); elevation = dp(8).toFloat() }
        val navItems = listOf(Pair("🏠\nহোম", MainActivity::class.java), Pair("📿\nতাসবিহ", TasbihActivity::class.java), Pair("📚\nলাইব্রেরী", LibraryActivity::class.java), Pair("📖\nআমল", MasnunAmolActivity::class.java), Pair("📝\nনোটপ্যাড", NotepadActivity::class.java), Pair("🔄\nসিঙ্ক", null), Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java))
        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0); gravity = Gravity.CENTER
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
