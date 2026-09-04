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

class TasbihActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private val themeColors by lazy { ThemeManager.getTheme(this) }
    
    private var count = 0
    private lateinit var countText: TextView

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(12).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(10).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("TasbihPrefs", Context.MODE_PRIVATE)
        count = prefs.getInt("tasbih_count", 0)

        buildUI()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL; 
            setBackgroundColor(themeColors.bgMain) 
        }

        // টপ বার
        val top = LinearLayout(this).apply { 
            gravity = Gravity.CENTER_VERTICAL; 
            setPadding(dp(12), dp(12), dp(12), dp(12)); 
            background = getCardDrawable() 
        }
        top.addView(TextView(this).apply { 
            text = "← হোম"; textSize = 16f; setTextColor(themeColors.textMain); setPadding(0, 0, dp(12), 0); 
            setOnClickListener { startActivity(Intent(this@TasbihActivity, MainActivity::class.java)); finish() } 
        })
        top.addView(TextView(this).apply { 
            text = "📿 ডিজিটাল তাসবিহ"; textSize = 17f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD) 
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        // স্ক્રોલ ভিউ ও মেইন বডি
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val body = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(20), dp(30), dp(20), dp(30))
        }

        // তাসবিহ কাউন্ট কার্ড ডিজাইন
        val counterCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = getCardDrawable()
            setPadding(dp(24), dp(30), dp(24), dp(30))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { 
                bottomMargin = dp(24) 
            }
        }

        counterCard.addView(TextView(this).apply {
            text = "সুবহানাল্লাহ, আলহামদুলিল্লাহ, আল্লাহু আকবার"
            textSize = 13f
            setTextColor(themeColors.textSub)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(15))
        })

        countText = TextView(this).apply {
            text = count.toString()
            textSize = 75f
            setTextColor(themeColors.textMain)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        }
        counterCard.addView(countText)
        body.addView(counterCard)

        // গণনা করুন (Click) বড় বাটন
        val countBtn = Button(this).apply {
            text = "🤲 গণনা করুন (Tap Here)"
            setTextColor(Color.BLACK)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            background = getBtnDrawable(themeColors.btnBg)
            layoutParams = LinearLayout.LayoutParams(-1, dp(60)).apply { 
                bottomMargin = dp(16) 
            }
            setOnClickListener {
                count++
                countText.text = count.toString()
                saveCount()
            }
        }
        body.addView(countBtn)

        // রিসেট বাটন
        val resetBtn = Button(this).apply {
            text = "🔄 রিসেট করুন"
            setTextColor(Color.WHITE)
            textSize = 14f
            background = getBtnDrawable(Color.parseColor("#DC2626"))
            layoutParams = LinearLayout.LayoutParams(dp(160), dp(45))
            setOnClickListener {
                count = 0
                countText.text = count.toString()
                saveCount()
            }
        }
        body.addView(resetBtn)

        scroll.addView(body)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // নিচের ফিক্সড ন্যাভিগেশন বার
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
                setTextColor(if (label.contains("তাসবিহ")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    if (targetClass != null && !label.contains("তাসবিহ")) {
                        startActivity(Intent(this@TasbihActivity, targetClass))
                        finish()
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun saveCount() {
        getSharedPreferences("TasbihPrefs", Context.MODE_PRIVATE).edit().putInt("tasbih_count", count).apply()
    }
}
