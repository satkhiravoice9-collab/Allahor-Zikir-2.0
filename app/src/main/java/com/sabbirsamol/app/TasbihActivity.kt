package com.sabbirsamol.app

import android.os.Bundle
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
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
    private fun panel(color: Int, radius: Int = 22) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("count", 0)
        target = prefs.getInt("target", 33)
        buildTasbih()
    }

    private fun buildTasbih() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            setBackgroundColor(Color.rgb(7, 45, 31))
        }

        val kaabaCard = FrameLayout(this).apply {
            background = panel(Color.rgb(11, 72, 48), 26)
            addView(TextView(this@TasbihActivity).apply {
                text = "🕋"
                textSize = 78f
                gravity = Gravity.CENTER
            })
            addView(TextView(this@TasbihActivity).apply {
                text = "الْكَعْبَةُ الْمُشَرَّفَةُ"
                textSize = 13f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(238, 210, 130))
                setTypeface(null, Typeface.BOLD)
                val lp = FrameLayout.LayoutParams(-1, dp(30), Gravity.BOTTOM)
                lp.bottomMargin = dp(5)
                layoutParams = lp
            })
        }
        root.addView(kaabaCard, LinearLayout.LayoutParams(-1, dp(135)).apply { bottomMargin = dp(8) })

        root.addView(TextView(this).apply {
            text = "📿 তাসবিহ গণনা"
            textSize = 22f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(-1, dp(40)))

        targetText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(238, 210, 130))
        }
        root.addView(targetText, LinearLayout.LayoutParams(-1, dp(34)))

        countText = TextView(this).apply {
            textSize = 76f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(countText, LinearLayout.LayoutParams(-1, dp(120)))

        val tapArea = FrameLayout(this).apply {
            background = panel(Color.rgb(15, 104, 69), 30)
            isClickable = true
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    count++
                    saveAndRefresh()
                    performClick()
                }
                true
            }
            setOnClickListener { }
            addView(TextView(this@TasbihActivity).apply {
                text = "تাসবিহ পড়তে স্ক্রিনে ট্যাপ করুন\n\nযেকোনো জায়গায় ট্যাপ করলেই ১ গণনা"
                textSize = 18f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)
            })
        }
        root.addView(tapArea, LinearLayout.LayoutParams(-1, 0, 1f).apply { bottomMargin = dp(10) })

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
        controls.addView(Button(this).apply { text = "🎯  টার্গেট"; setOnClickListener { chooseTarget() } }, LinearLayout.LayoutParams(0, dp(52), 1f))
        controls.addView(Button(this).apply { text = "🔄  রিসেট"; setOnClickListener { confirmReset() } }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(8) })
        root.addView(controls, LinearLayout.LayoutParams(-1, dp(58)))

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
        AlertDialog.Builder(this).setTitle("তাসবিহের লক্ষ্য").setItems(values) { _, which ->
            target = values[which].toInt()
            saveAndRefresh()
        }.show()
    }

    private fun confirmReset() {
        AlertDialog.Builder(this).setTitle("কাউন্ট রিসেট করবেন?").setMessage("বর্তমান তাসবিহ গণনা শূন্য হয়ে যাবে।").setNegativeButton("না", null).setPositiveButton("হ্যাঁ") { _, _ ->
            count = 0
            saveAndRefresh()
        }.show()
    }
}
