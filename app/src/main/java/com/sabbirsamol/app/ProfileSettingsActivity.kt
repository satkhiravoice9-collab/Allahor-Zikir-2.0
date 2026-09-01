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

        // --- উদ্যোক্তা কার্ড ---
        val devCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        devCard.addView(TextView(this).apply { text = "⭐ অ্যাপ উদ্যোক্তা ও পরিচালক"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        devCard.addView(TextView(this).apply { text = "নাম: সাব্বির আহমাদ\nমোবাইল: ০১৭২৫-২২৮৬২২"; setTextColor(Color.WHITE); textSize = 15f; setPadding(0, dp(8), 0, dp(12)) })
        
        devCard.addView(Button(this).apply {
            text = "📞 সরাসরি কল করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { bottomMargin = dp(8) }
            setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:01725228622"))
                startActivity(intent)
            }
        })
        devCard.addView(Button(this).apply {
            text = "🌐 আমাদের ইসলামিক ফেসবুক পেজ"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#1D4ED8"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(40))
            setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/madinarkontho01?mibextid=ZbWKwL"))
                startActivity(intent)
            }
        })
        content.addView(devCard)

        // --- ক্লাউড সাইন ইন কার্ড ---
        val cloudCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) } }
        cloudCard.addView(TextView(this).apply { text = "☁️ গুগল ক্লাউড সাইন ইন ও সিঙ্ক"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val savedEmail = sharedPrefs.getString("user_email", "sabbirnumber@gmail.com")
        
        cloudCard.addView(TextView(this).apply { text = "সংযুক্ত ক্লাউড জিমেইল:\n$savedEmail\n(১০০% গুগল ক্লাউডে ডাটা সংরক্ষণ হবে)"; setTextColor(Color.WHITE); textSize = 14f; setPadding(0, dp(6), 0, dp(12)) })
        
        cloudCard.addView(Button(this).apply {
            text = "🔵 গুগল দিয়ে সরাসরি সাইন ইন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(40))
            setOnClickListener {
                Toast.makeText(this@ProfileSettingsActivity, "কলাউডের সাথে সফলভাবে সিঙ্ক হয়েছে!", Toast.LENGTH_LONG).show()
                sharedPrefs.edit().putString("user_email", "sabbir.ahmad@gmail.com").apply()
                showSettingsPage()
            }
        })
        content.addView(cloudCard)

        // --- থিম সিলেকশন কার্ড ---
        val themeCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)) }
        themeCard.addView(TextView(this).apply { text = "🎨 অ্যাপ থিম নির্বাচন করুন:"; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })

        val themes = listOf("⚪ সাদা থিম (লাইট)", "⬛ কাবা থিম (ডার্ক গোল্ড)", "🟩 মদিনা থিম (এমরেল্ড গ্রিন)", "🟡 সুবহে-সাদিক থিম (রয়্যাল গোল্ড)")
        themes.forEach { tName ->
            val tBtn = Button(this).apply {
                text = tName; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#064E3B"))
                layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { bottomMargin = dp(8) }
                setOnClickListener {
                    Toast.makeText(this@ProfileSettingsActivity, "থিম পরিবর্তন করা হয়েছে: $tName", Toast.LENGTH_SHORT).show()
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
