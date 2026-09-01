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
import android.view.MotionEvent
import android.widget.*
import androidx.activity.ComponentActivity

/** Main/default Tasbih screen. Home screen remains untouched. */
class TasbihActivity : ComponentActivity() {
    private var count = 0
    private var selected = "সুবহানাল্লাহ"
    private lateinit var countText: TextView
    private lateinit var nameText: TextView
    private lateinit var progressText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }
    private val listPrefs by lazy { getSharedPreferences("tasbih_list", Context.MODE_PRIVATE) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun targetFor(name: String): Int = listPrefs.getInt("target_$name", 0)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        selected = prefs.getString("selected", "সুবহানাল্লাহ") ?: "সুবহানাল্লাহ"
        count = prefs.getInt("count", 0)
        showMain()
    }

    override fun onResume() {
        super.onResume()
        if (::nameText.isInitialized) {
            selected = prefs.getString("selected", selected) ?: selected
            count = prefs.getInt("count", count)
            refresh()
        }
    }

    private fun showMain() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(7, 45, 31))
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) increment()
                true
            }
        }

        nameText = TextView(this).apply {
            text = selected
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(nameText, FrameLayout.LayoutParams(dp(230), dp(50)).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = dp(10)
            topMargin = dp(4)
        })

        val reset = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_popup_sync)
            setColorFilter(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "রিসেট"
            setOnClickListener { resetCount() }
        }
        root.addView(reset, FrameLayout.LayoutParams(dp(54), dp(52)).apply {
            gravity = Gravity.TOP or Gravity.END
            rightMargin = dp(8)
            topMargin = dp(2)
        })

        val kaaba = TextView(this).apply {
            text = "🕋"
            textSize = 78f
            gravity = Gravity.CENTER
            isClickable = true
            setOnClickListener { increment() }
        }
        root.addView(kaaba, FrameLayout.LayoutParams(-1, dp(135)).apply { topMargin = dp(62) })

        countText = TextView(this).apply {
            textSize = 86f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(countText, FrameLayout.LayoutParams(-1, dp(125)).apply { topMargin = dp(190) })

        progressText = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(238, 210, 130))
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(progressText, FrameLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(305) })

        val folder = TextView(this).apply {
            text = "📁  জিকির লিস্ট"
            textSize = 17f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.rgb(145, 55, 25))
                cornerRadius = dp(22).toFloat()
            }
            isClickable = true
            setOnClickListener {
                startActivity(Intent(this@TasbihActivity, ZikirManagerActivity::class.java))
            }
        }
        root.addView(folder, FrameLayout.LayoutParams(-1, dp(62)).apply {
            gravity = Gravity.BOTTOM
            leftMargin = dp(10)
            rightMargin = dp(10)
            bottomMargin = dp(10)
        })

        setContentView(root)
        refresh()
    }

    private fun increment() {
        count++
        prefs.edit().putInt("count", count).putString("selected", selected).apply()
        refresh()
        val target = targetFor(selected)
        if (count % 100 == 0 || (target > 0 && count == target)) vibrate()
    }

    private fun refresh() {
        if (!::countText.isInitialized) return
        countText.text = count.toString()
        nameText.text = selected
        val target = targetFor(selected)
        progressText.text = if (target > 0) {
            "Target: $target    |    বাকি: ${(target - count).coerceAtLeast(0)}"
        } else "Unlimited"
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(120)
        }
    }

    private fun resetCount() {
        AlertDialog.Builder(this)
            .setTitle("রিসেট")
            .setMessage("বর্তমান গণনা শূন্য করবেন?")
            .setNegativeButton("না", null)
            .setPositiveButton("হ্যাঁ") { _, _ ->
                count = 0
                prefs.edit().putInt("count", 0).apply()
                refresh()
            }
            .show()
    }
}
