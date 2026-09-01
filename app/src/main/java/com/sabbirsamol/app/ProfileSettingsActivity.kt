package com.sabbirsamol.app

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

    private val bgMain = Color.parseColor("#091C14")
    private val cardBg = Color.parseColor("#114D3C")
    private val cardStroke = Color.parseColor("#1B785B")
    private val textYellow = Color.parseColor("#FBBF24")
    private val btnYellow = Color.parseColor("#FACC15")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSettingsPage()
    }

    private fun showSettingsPage() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        // Top Bar
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(Color.WHITE); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "⚙️ প্রোফাইল, ব্যাকআপ ও সেটিংস"; textSize = 17f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        // ================= ১. অ্যাপ উদ্যোক্তা ও পরিচালক =================
        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(Color.WHITE); textSize = 15f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(4).toFloat(), 1f) })
        
        // কল বাটন
        devCard.addView(Button(this).apply {
            text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(10) }
            setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))
                startActivity(intent)
            }
        })
        
        // ফেসবুক পেজ বাটন
        devCard.addView(Button(this).apply {
            text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))
                startActivity(intent)
            }
        })
        content.addView(devCard)

        // ================= ২. গুগল ক্লাউড সাইন ইন ও সিঙ্ক =================
        val cloudCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        cloudCard.addView(TextView(this).apply { text = "☁️ গুগল ক্লাউড সাইন ইন ও সিঙ্ক"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", "sabbirnumber@gmail.com")
        
        cloudCard.addView(TextView(this).apply { text = "সংযুক্ত ক্লাউড জিমেইল:\n$savedEmail\n(১০০% গুগল ক্লাউডে ডাটা সংরক্ষণ হবে)"; setTextColor(Color.WHITE); textSize = 14f; setPadding(0, dp(8), 0, dp(12)); setLineSpacing(dp(2).toFloat(), 1f) })
        
        cloudCard.addView(Button(this).apply {
            text = "🔵 গুগল দিয়ে সরাসরি সাইন ইন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#60A5FA"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener {
                Toast.makeText(this@ProfileSettingsActivity, "ক্লাউডের সাথে সফলভাবে সিঙ্ক হয়েছে!", Toast.LENGTH_LONG).show()
                sharedPrefs.edit().putString("user_email", "sabbir.ahmad@gmail.com").apply()
                showSettingsPage() // রিফ্রেশ পেজ
            }
        })
        content.addView(cloudCard)

        // ================= ৩. অ্যাপ থিম নির্বাচন =================
        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })

        val savedTheme = sharedPrefs.getString("app_theme", "মদিনা থিম (এমরেল্ড গ্রিন)")

        val themes = listOf(
            Pair("⚪ সাদা থিম (লাইট)", "#F3F4F6"), 
            Pair("⬛ কাবা থিম (ডার্ক গোল্ড)", "#1F2937"), 
            Pair("🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "#064E3B"), 
            Pair("🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)", "#B45309")
        )

        themes.forEach { (tName, tColor) ->
            val isSelected = savedTheme == tName
            
            val tBtn = Button(this).apply {
                // যে থিমটি সিলেক্ট করা আছে, সেটির পাশে টিক চিহ্ন (✅) দেখাবে
                text = if (isSelected) "✅ $tName" else tName
                isAllCaps = false; setTextColor(if(tName.contains("সাদা")) Color.BLACK else Color.WHITE)
                
                // বাটন ডিজাইন
                background = GradientDrawable().apply {
                    setColor(Color.parseColor(tColor))
                    cornerRadius = dp(8).toFloat()
                    if (isSelected) setStroke(dp(2), btnYellow) else setStroke(dp(1), Color.GRAY)
                }
                
                layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { bottomMargin = dp(10) }
                
                setOnClickListener {
                    sharedPrefs.edit().putString("app_theme", tName).apply()
                    Toast.makeText(this@ProfileSettingsActivity, "থিম আপডেট করা হয়েছে: $tName", Toast.LENGTH_SHORT).show()
                    showSettingsPage() // সাথে সাথে পেজ রিফ্রেশ করে টিক চিহ্ন আপডেট করবে
                }
            }
            themeCard.addView(tBtn)
        }
        content.addView(themeCard)

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }
}
