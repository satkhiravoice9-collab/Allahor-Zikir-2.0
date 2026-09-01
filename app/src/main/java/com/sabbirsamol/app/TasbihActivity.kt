package com.sabbirsamol.app

import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity

/** Tasbih-only screen. Home screen code is intentionally untouched. */
class TasbihActivity : ComponentActivity() {
    private var count = 0
    private var target = 33
    private lateinit var countText: TextView
    private lateinit var targetText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun panel(color: Int, radius: Int = 22) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("count", 0)
        target = prefs.getInt("target", 33)
        buildTasbih()
    }

    private fun buildTasbih() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(12, 48, 34))
        }

        // Kaaba area: kept as a dedicated top visual area for the Tasbih screen.
        root.addView(TextView(this).apply {
            text = "🕋"
            textSize = 76f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }, LinearLayout.LayoutParams(-1, dp(120)))

        root.addView(TextView(this).apply {
            text = "তাসবিহ"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }, LinearLayout.LayoutParams(-1, dp(45)))

        targetText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(232, 205, 122))
        }
        root.addView(targetText, LinearLayout.LayoutParams(-1, dp(35)))

        countText = TextView(this).apply {
            textSize = 82f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        root.addView(countText, LinearLayout.LayoutParams(-1, dp(150)))

        val tapArea = FrameLayout(this).apply {
            background = panel(Color.rgb(20, 91, 62), 28)
            isClickable = true
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    count++
                    saveAndRefresh()
                    performClick()
                    true
                } else true
            }
            setOnClickListener { }
            addView(TextView(this@TasbihActivity).apply {
                text = "ট্যাপ করে তাসবিহ গণনা করুন"
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
            })
        }
        root.addView(tapArea, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            bottomMargin = dp(12)
        })

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        controls.addView(Button(this).apply {
            text = "টার্গেট"
            setOnClickListener { chooseTarget() }
        }, LinearLayout.LayoutParams(0, dp(52), 1f))
        controls.addView(Button(this).apply {
            text = "রিসেট"
            setOnClickListener { confirmReset() }
        }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(8) })
        root.addView(controls, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
        refresh()
    }

    private fun refresh() {
        countText.text = count.toString()
        targetText.text = "লক্ষ্য: $target    •    বাকি: ${maxOf(target - count, 0)}"
    }

    private fun saveAndRefresh() {
        prefs.edit().putInt("count", count).putInt("target", target).apply()
        refresh()
    }

    private fun chooseTarget() {
        val values = arrayOf("33", "99", "100", "313", "1000")
        AlertDialog.Builder(this)
            .setTitle("তাসবিহের লক্ষ্য")
            .setItems(values) { _, which ->
                target = values[which].toInt()
                saveAndRefresh()
            }
            .show()
    }

    private fun confirmReset() {
        AlertDialog.Builder(this)
            .setTitle("কাউন্ট রিসেট করবেন?")
            .setMessage("বর্তমান তাসবিহ গণনা শূন্য হয়ে যাবে।")
            .setNegativeButton("না", null)
            .setPositiveButton("হ্যাঁ") { _, _ ->
                count = 0
                saveAndRefresh()
            }
            .show()
    }
}
