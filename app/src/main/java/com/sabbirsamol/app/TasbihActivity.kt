package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray

class TasbihActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    // থিম ম্যানেজারের সাথে কানেকশন
    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val textAccent get() = themeColors.textAccent
    private val btnBg get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain

    private var currentCount = 0
    private var isCustomMode = false
    private var customZikirId = ""
    private var customZikirName = ""
    private var customTarget = 0
    private var hasShownPopup = false

    private lateinit var countTextView: TextView

    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        customZikirId = intent.getStringExtra("ZIKIR_ID") ?: ""
        if (customZikirId.isNotEmpty()) {
            isCustomMode = true
            customZikirName = intent.getStringExtra("ZIKIR_NAME") ?: ""
            customTarget = intent.getIntExtra("ZIKIR_TARGET", 0)
            currentCount = intent.getIntExtra("ZIKIR_READ", 0)
        } else {
            currentCount = getSharedPreferences("TasbihData", Context.MODE_PRIVATE).getInt("main_count", 0)
        }

        buildUI()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain)
            setOnClickListener { incrementCount() } // স্ক্রিনে ক্লিক করলেই কাউন্ট হবে
        }

        // ================= ১. টপ বার =================
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = GradientDrawable().apply { setColor(cardBg) } }
        top.addView(TextView(this).apply { text = "←"; textSize = 22f; setTextColor(textMain); setPadding(0, 0, dp(12), 0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = if (isCustomMode) "📿 $customZikirName" else "📿 সাধারণ তাসবিহ কাউন্টার"; textSize = 18f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        
        top.addView(Button(this).apply {
            text = "রিসেট (০)"; isAllCaps = false; textSize = 12f; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#475569"))
            layoutParams = LinearLayout.LayoutParams(dp(80), dp(35))
            setOnClickListener { currentCount = 0; hasShownPopup = false; updateDisplay(); saveProgress() }
        })
        root.addView(top)

        // ================= ২. কাউন্টার ও কাবার ডিজাইন সেকশন =================
        val centerLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(20), dp(20), dp(20), dp(20)) }

        // --- সম্পূর্ণ কোডিং করা কাবা শরীফ এবং কালিমার বক্স ---
        val kaabaBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0F172A")) // ভিডিওর মতো ডার্ক ব্লু
                setStroke(dp(2), Color.parseColor("#FBBF24")) // সোনালী বর্ডার
                cornerRadius = dp(12).toFloat()
            }
            layoutParams = LinearLayout.LayoutParams(dp(230), dp(230)).apply { bottomMargin = dp(30) }
            setPadding(dp(10), dp(20), dp(10), dp(15))
        }

        // কোডিং দিয়ে বানানো কাবা আইকন
        val codedKaaba = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(85), dp(95)).apply { bottomMargin = dp(25) }
            
            // মূল কালো গিলাফ
            addView(View(this@TasbihActivity).apply { background = GradientDrawable().apply { setColor(Color.parseColor("#000000")); cornerRadius = dp(2).toFloat() }; layoutParams = FrameLayout.LayoutParams(-1, -1) })
            
            // ছাদের ওপরের সোনালী বর্ডার
            addView(View(this@TasbihActivity).apply { background = GradientDrawable().apply { setColor(Color.parseColor("#FBBF24")) }; layoutParams = FrameLayout.LayoutParams(-1, dp(4)) })
            
            // মাঝখানের সোনালী বেল্ট (কিসওয়াহ)
            addView(LinearLayout(this@TasbihActivity).apply {
                orientation = LinearLayout.HORIZONTAL; background = GradientDrawable().apply { setColor(Color.parseColor("#FBBF24")) }
                layoutParams = FrameLayout.LayoutParams(-1, dp(12)).apply { topMargin = dp(20) }
                // বেল্টের ভেতরের ডিজাইন (কালো দাগ)
                addView(View(this@TasbihActivity).apply { layoutParams = LinearLayout.LayoutParams(dp(15), -1).apply { setMargins(dp(5),0,dp(5),0) }; setBackgroundColor(Color.BLACK) })
                addView(View(this@TasbihActivity).apply { layoutParams = LinearLayout.LayoutParams(dp(15), -1).apply { setMargins(dp(5),0,dp(5),0) }; setBackgroundColor(Color.BLACK) })
                addView(View(this@TasbihActivity).apply { layoutParams = LinearLayout.LayoutParams(dp(15), -1).apply { setMargins(dp(5),0,dp(5),0) }; setBackgroundColor(Color.BLACK) })
            })
            
            // কাবা শরীফের দরজা (Al-Multazam)
            addView(FrameLayout(this@TasbihActivity).apply {
                layoutParams = FrameLayout.LayoutParams(dp(28), dp(45)).apply { gravity = Gravity.BOTTOM or Gravity.END; rightMargin = dp(12) }
                // আউটার গোল্ডেন ফ্রেম
                addView(View(this@TasbihActivity).apply { background = GradientDrawable().apply { setColor(Color.parseColor("#FBBF24")); cornerRadius = dp(2).toFloat() }; layoutParams = FrameLayout.LayoutParams(-1, -1) })
                // ইনার ব্ল্যাক ফ্রেম
                addView(View(this@TasbihActivity).apply { background = GradientDrawable().apply { setColor(Color.parseColor("#000000")); cornerRadius = dp(1).toFloat() }; layoutParams = FrameLayout.LayoutParams(dp(22), dp(41)).apply { gravity = Gravity.CENTER } })
                // ইনার গোল্ডেন ডিজাইন
                addView(View(this@TasbihActivity).apply { background = GradientDrawable().apply { setColor(Color.parseColor("#FBBF24")) }; layoutParams = FrameLayout.LayoutParams(dp(16), dp(36)).apply { gravity = Gravity.CENTER } })
            })
        }
        kaabaBox.addView(codedKaaba) // কোডিং করা কাবা বসানো হলো

        // কালিমা লেখা
        kaabaBox.addView(TextView(this).apply {
            text = "لَا إِلٰهَ إِلَّا اللّٰهُ مُحَمَّدٌ رَسُولُ اللّٰهِ"
            textSize = 19f; setTextColor(Color.parseColor("#FBBF24"))
            setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER
        })
        centerLayout.addView(kaabaBox)

        // কাউন্টের বিশাল সংখ্যা
        countTextView = TextView(this).apply {
            textSize = 90f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
        }
        centerLayout.addView(countTextView)

        // ইনস্ট্রাকশন টেক্সট
        if (isCustomMode) {
            centerLayout.addView(TextView(this).apply { text = "টার্গেট: ${bn(customTarget)} বার"; textSize = 18f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
        } else {
            centerLayout.addView(TextView(this).apply { text = "মুক্ত গণনা (প্রতি ১০০ পূর্ণে ভাইব্রেশন)"; textSize = 16f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
        }
        centerLayout.addView(TextView(this).apply { text = "👇 স্ক্রিনের যেকোনো জায়গায় ট্যাপ করে গণনা করুন"; textSize = 14f; setTextColor(Color.parseColor("#D1D5DB")) })

        root.addView(centerLayout, LinearLayout.LayoutParams(-1, 0, 1f))

        // ================= ৩. অ্যাকশন বাটনসমূহ =================
        val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(dp(12), dp(10), dp(12), dp(10)) }
        if (isCustomMode) {
            actionRow.addView(Button(this).apply {
                text = "সাধারণ তাসবিহে ফিরে যান"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 12f; background = getBtnDrawable(Color.parseColor("#374151"))
                layoutParams = LinearLayout.LayoutParams(0, dp(45), 1f).apply { rightMargin = dp(5) }
                setOnClickListener { startActivity(Intent(this@TasbihActivity, TasbihActivity::class.java)); finish() }
            })
        }
        actionRow.addView(Button(this).apply {
            text = "📄 জিকির তালিকা ও টার্গেট"; isAllCaps = false; setTextColor(Color.BLACK); textSize = 12f; background = getBtnDrawable(Color.parseColor("#10B981"))
            layoutParams = LinearLayout.LayoutParams(0, dp(45), if(isCustomMode) 1f else 2f).apply { leftMargin = if(isCustomMode) dp(5) else 0 }
            setOnClickListener { startActivity(Intent(this@TasbihActivity, ZikirManagerActivity::class.java)); if(isCustomMode) finish() }
        })
        root.addView(actionRow)

        // ================= ৪. বটম মেনু =================
        val menu = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setBackgroundColor(cardBg); setPadding(dp(2), dp(2), dp(2), dp(2)); elevation = dp(8).toFloat() }
        listOf("🏠\nহোম", "📿\nতাসবিহ", "📚\nলাইব্রেরি", "🤲\nমাসনুন", "📝\nনোট", "🔄\nরিফ্রেশ", "ℹ️\nএবাউট").forEach { label ->
            menu.addView(Button(this).apply {
                text = label; textSize = 9f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER; setTextColor(textMain); background = GradientDrawable().apply { setColor(bgMain); cornerRadius = dp(8).toFloat() }
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@TasbihActivity, MainActivity::class.java)); finishAffinity() }
                        label.contains("লাইব্রেরি") -> startActivity(Intent(this@TasbihActivity, LibraryActivity::class.java))
                        label.contains("মাসনুন") -> startActivity(Intent(this@TasbihActivity, MasnunAmolActivity::class.java))
                        label.contains("নোট") -> startActivity(Intent(this@TasbihActivity, NotepadActivity::class.java))
                        label.contains("এবাউট") -> startActivity(Intent(this@TasbihActivity, ProfileSettingsActivity::class.java))
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(menu, LinearLayout.LayoutParams(-1, dp(58)))

        setContentView(root)
        updateDisplay()
    }

    private fun incrementCount() {
        currentCount++
        updateDisplay()
        saveProgress()

        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (isCustomMode) {
            if (currentCount == customTarget && !hasShownPopup) {
                vibratePhone(v, 500) // টার্গেট পূরণে ভাইব্রেশন
                showTargetPopup()
                hasShownPopup = true
            } else {
                vibratePhone(v, 50)
            }
        } else {
            if (currentCount > 0 && currentCount % 100 == 0) {
                vibratePhone(v, 500) // ১০০ বার হলে ভাইব্রেশন
            } else {
                vibratePhone(v, 50)
            }
        }
    }

    private fun vibratePhone(vibrator: Vibrator, duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") vibrator.vibrate(duration)
            }
        } catch (e: Exception) {}
    }

    private fun updateDisplay() { countTextView.text = bn(currentCount) }

    private fun saveProgress() {
        if (isCustomMode) {
            val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
            val jsonArray = JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                if (obj.getString("id") == customZikirId) { obj.put("read", currentCount); break }
            }
            prefs.edit().putString("zikir_list", jsonArray.toString()).apply()
        } else {
            getSharedPreferences("TasbihData", Context.MODE_PRIVATE).edit().putInt("main_count", currentCount).apply()
        }
    }

    private fun showTargetPopup() {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain); setPadding(dp(20), dp(20), dp(20), dp(20)) }
        dialogLayout.addView(TextView(this).apply { text = "মাশাআল্লাহ!"; textSize = 22f; setTextColor(textAccent); setTypeface(null, Typeface.BOLD); gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(10)) })
        dialogLayout.addView(TextView(this).apply { text = "আপনার নির্ধারিত টার্গেট (${bn(customTarget)} বার) পূর্ণ হয়েছে।"; textSize = 16f; setTextColor(textMain); gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(20)) })
        
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).setCancelable(false).create()
        dialogLayout.addView(Button(this).apply {
            text = "ঠিক আছে"; setTextColor(Color.BLACK); background = getBtnDrawable(btnBg); layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener { dialog.dismiss() }
        })
        dialog.show()
    }
}
