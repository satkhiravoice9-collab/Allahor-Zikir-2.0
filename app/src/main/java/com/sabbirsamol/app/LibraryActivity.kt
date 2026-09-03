package com.sabbirsamol.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class LibraryActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        content.addView(TextView(this).apply {
            text = "📚 ইসলামিক লাইব্রেরী ও কিতাব ভাণ্ডার"
            textSize = 16f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        // লাইব্রেরীর কন্টেন্ট বা কার্ডসমূহ এখানে যুক্ত করতে পারেন
        val sampleCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(theme.cardBg)
                setStroke(dp(1), theme.cardStroke)
                cornerRadius = dp(12).toFloat()
            }
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        sampleCard.addView(TextView(this).apply {
            text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)"
            textSize = 14f
            setTextColor(theme.textMain)
            setTypeface(null, Typeface.BOLD)
        })
        content.addView(sampleCard)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= বটম নেভিগেশন বার =================
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

        navItems.forEach { (label, targetClass) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("লাইব্রেরী")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@LibraryActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@LibraryActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { /* বর্তমান পেজ */ }
                        label.contains("আমল") -> { startActivity(Intent(this@LibraryActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@LibraryActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@LibraryActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@LibraryActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
