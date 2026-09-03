package com.sabbirsamol.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textYellow get() = themeColors.textAccent
    private val btnYellow get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain
    private val textSub get() = themeColors.textSub

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(12).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(8).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHomePage()
    }

    private fun showHomePage() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        // --- টপ বার ---
        val topBar = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = getCardDrawable()
        }
        topBar.addView(TextView(this).apply {
            text = "🕌 আল্লাহর জিকির ও আমল"
            textSize = 18f
            setTextColor(textYellow)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        
        topBar.addView(TextView(this).apply {
            text = "⚙️"
            textSize = 20f
            setPadding(dp(8), 0, 0, 0)
            setOnClickListener { startActivity(Intent(this@MainActivity, ProfileSettingsActivity::class.java)) }
        })
        root.addView(topBar)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(80))
        }

        // ================= তারিখ ও বারের কার্ড (বাংলা, ইংরেজি ও সম্ভাব্য আরবি তারিখ) =================
        val dateCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
            gravity = Gravity.CENTER
        }
        val currentDateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("bn", "BD")).format(Date())
        dateCard.addView(TextView(this).apply {
            text = "📅 আজকের তারিখ: $currentDateStr"
            textSize = 14f
            setTextColor(textYellow)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        })
        dateCard.addView(TextView(this).apply {
            text = "🌙 আরবি ও বাংলা সময়সূচী অনুযায়ী নিয়মিত আমল করুন"
            textSize = 12f
            setTextColor(textSub)
            setPadding(0, dp(4), 0, 0)
            gravity = Gravity.CENTER
        })
        content.addView(dateCard)

        // ================= ৫ ওয়াক্ত নামাজের সময়সূচী কার্ড =================
        val prayerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        prayerCard.addView(TextView(this).apply {
            text = "⏰ পাঁচ ওয়াক্ত নামাজের সময়সূচী"
            textSize = 16f
            setTextColor(textYellow)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })

        val prayers = listOf(
            Pair("ফজর", "০৫:০০ AM"),
            Pair("যোহর", "০১:১৫ PM"),
            Pair("আসর", "০৪:৩০ PM"),
            Pair("মাগরিব", "০৬:২০ PM"),
            Pair("এশা", "০৭:৪০ PM")
        )

        prayers.forEach { (name, time) ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(4), 0, dp(4))
            }
            row.addView(TextView(this).apply {
                text = "• $name ওয়াক্ত"
                textSize = 13f
                setTextColor(textMain)
            }, LinearLayout.LayoutParams(0, -2, 1f))
            row.addView(TextView(this).apply {
                text = time
                textSize = 13f
                setTextColor(textYellow)
                setTypeface(null, Typeface.BOLD)
            })
            prayerCard.addView(row)
        }
        content.addView(prayerCard)

        // ================= কাবা শরীফের ছবি =================
        val imageId = resources.getIdentifier("kaaba_img", "drawable", packageName)
        if (imageId != 0) {
            val kaabaCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getCardDrawable()
                setPadding(dp(4), dp(4), dp(4), dp(4))
                layoutParams = LinearLayout.LayoutParams(-1, dp(170)).apply { bottomMargin = dp(14) }
                gravity = Gravity.CENTER
            }
            val kaabaImageView = ImageView(this).apply {
                setImageResource(imageId)
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = LinearLayout.LayoutParams(-1, -1)
            }
            kaabaCard.addView(kaabaImageView)
            content.addView(kaabaCard)
        }

        // ================= তাসবিহ কাউন্টার কার্ড =================
        val tasbihCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(16), dp(16), dp(16), dp(16))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        tasbihCard.addView(TextView(this).apply {
            text = "📿 ডিজিটাল তাসবিহ কাউন্টার"
            textSize = 16f
            setTextColor(textYellow)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })
        tasbihCard.addView(TextView(this).apply {
            text = "দৈনন্দিন জিকির ও তাসবিহ গণনা করুন সহজে।"
            textSize = 13f
            setTextColor(textSub)
            setPadding(0, 0, 0, dp(12))
        })
        tasbihCard.addView(Button(this).apply {
            text = "তাসবিহ খুলুন ➔"
            isAllCaps = false
            setTextColor(Color.BLACK)
            background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { startActivity(Intent(this@MainActivity, TasbihActivity::class.java)) }
        })
        content.addView(tasbihCard)

        // ================= অন্যান্য ফিচার ও ইসলামিক সেকশনসমূহ =================
        val features = listOf(
            Triple("📚 ইসলামিক লাইব্রেরী ও কিতাব", "পবিত্র কুরআন ও সিহাহ সিত্তাহ হাদিস পড়ুন", LibraryActivity::class.java),
            Triple("📖 মাসনুন আমল ও দোয়া", "সকাল-সন্ধ্যার জিকির ও মানযিল আয়াত", MasnunAmolActivity::class.java),
            Triple("📝 কালার নোটপ্যাড", "আপনার প্রয়োজনীয় নোট ও ফতওয়া সংরক্ষণ করুন", NotepadActivity::class.java)
        )

        features.forEach { (title, desc, targetActivity) ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getCardDrawable()
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
                setPadding(0, 0, 0, dp(10))
            })
            card.addView(Button(this).apply {
                text = "প্রবেশ করুন ➔"
                isAllCaps = false
                setTextColor(Color.BLACK)
                background = getBtnDrawable(btnYellow)
                layoutParams = LinearLayout.LayoutParams(-1, dp(38))
                setOnClickListener { startActivity(Intent(this@MainActivity, targetActivity)) }
            })
            content.addView(card)
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }
}
