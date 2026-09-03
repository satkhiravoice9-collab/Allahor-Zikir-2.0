package com.sabbirsamol.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

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

        // ================= কাবা শরীফের ছবি (তাসবিহ কাউন্টারের ওপরে) =================
        val kaabaCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(8), dp(8), dp(8), dp(8))
            layoutParams = LinearLayout.LayoutParams(-1, dp(180)).apply { bottomMargin = dp(14) }
            gravity = Gravity.CENTER
        }
        
        val kaabaImageView = ImageView(this).apply {
            // res/drawable/kaaba_img.png ছবিটিকে এখানে লোড করা হচ্ছে
            setImageResource(resources.getIdentifier("kaaba_img", "drawable", packageName))
            scaleType = ImageView.ScaleType.CENTER_CROP
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }
        kaabaCard.addView(kaabaImageView)
        content.addView(kaabaCard)

        // ================= তাসবিহ কাউন্টার শর্টকাট কার্ড =================
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

        // ================= অন্যান্য ফিচার কার্ডসমূহ =================
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

        // ================= ৭টি আইটেমের স্থায়ী বটম নেভিগেশন বার =================
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

        navItems.forEach { (label, actClass) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("হোম")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> {}
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@MainActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        else -> { actClass?.let { startActivity(Intent(this@MainActivity, it)) } }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
