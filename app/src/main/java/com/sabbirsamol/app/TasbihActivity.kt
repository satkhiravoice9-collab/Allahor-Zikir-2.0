package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
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

/** Tasbih only. Home screen remains untouched. */
class TasbihActivity : ComponentActivity() {
    private var count = 0
    private var selected = "সুবহানাল্লাহ"
    private lateinit var countText: TextView
    private lateinit var nameText: TextView
    private val prefs by lazy { getSharedPreferences("tasbih_only", Context.MODE_PRIVATE) }
    private val listPrefs by lazy { getSharedPreferences("tasbih_list", Context.MODE_PRIVATE) }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(c: Int, r: Int) = GradientDrawable().apply {
        setColor(c)
        cornerRadius = dp(r).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        count = prefs.getInt("count", 0)
        selected = prefs.getString("selected", "সুবহানাল্লাহ") ?: "সুবহানাল্লাহ"
        build()
    }

    private fun zikirs(): MutableList<String> {
        val defaults = linkedSetOf(
            "সুবহানাল্লাহ",
            "আলহামদুলিল্লাহ",
            "আল্লাহু আকবার",
            "আস্তাগফিরুল্লাহ",
            "লা ইলাহা ইল্লাল্লাহ"
        )
        return listPrefs.getStringSet("items", defaults)?.toMutableList() ?: defaults.toMutableList()
    }

    private fun saveList(items: List<String>) {
        listPrefs.edit().putStringSet("items", items.toSet()).apply()
    }

    private fun build() {
        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.rgb(7, 45, 31))
        }

        val kaaba = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            background = bg(Color.rgb(11, 72, 48), 26)
            addView(TextView(this@TasbihActivity).apply {
                text = "🕋"
                textSize = 66f
                gravity = Gravity.CENTER
            })
            addView(TextView(this@TasbihActivity).apply {
                text = "الْكَعْبَةُ الْمُشَرَّفَةُ"
                textSize = 12f
                gravity = Gravity.CENTER
                setTextColor(Color.rgb(238, 210, 130))
                setTypeface(null, Typeface.BOLD)
            })
        }
        root.addView(kaaba, FrameLayout.LayoutParams(-1, dp(130)).apply {
            leftMargin = dp(8)
            rightMargin = dp(8)
            topMargin = dp(8)
        })

        nameText = TextView(this).apply {
            text = selected
            textSize = 22f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(4), 0)
        }
        root.addView(nameText, FrameLayout.LayoutParams(dp(230), dp(55)).apply {
            leftMargin = dp(8)
            topMargin = dp(143)
        })

        root.addView(Button(this).apply {
            text = "↻"
            textSize = 25f
            contentDescription = "রিসেট"
            setOnClickListener { resetCount() }
        }, FrameLayout.LayoutParams(dp(62), dp(52)).apply {
            gravity = Gravity.TOP or Gravity.END
            rightMargin = dp(8)
            topMargin = dp(143)
        })

        countText = TextView(this).apply {
            textSize = 92f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(countText, FrameLayout.LayoutParams(-1, dp(150)).apply {
            topMargin = dp(205)
        })

        // The entire middle display is the counter. There is no visible tap button or instruction.
        val counterArea = FrameLayout(this).apply {
            isClickable = true
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) increment()
                true
            }
        }
        root.addView(counterArea, FrameLayout.LayoutParams(-1, dp(0)).apply {
            topMargin = dp(195)
            bottomMargin = dp(112)
            height = dp(0)
        })

        val listBar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = bg(Color.rgb(15, 104, 69), 24)
            setPadding(dp(8), dp(5), dp(8), dp(5))
        }
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        titleRow.addView(TextView(this).apply {
            text = "জিকির লিস্ট"
            textSize = 16f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(38), 1f))
        titleRow.addView(Button(this).apply {
            text = "⚙️"
            setOnClickListener { customize() }
        }, LinearLayout.LayoutParams(dp(55), dp(38)))
        listBar.addView(titleRow)

        val horizontal = HorizontalScrollView(this)
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        for (z in zikirs()) {
            row.addView(Button(this).apply {
                text = z
                setOnClickListener {
                    selected = z
                    count = 0
                    prefs.edit().putString("selected", selected).putInt("count", 0).apply()
                    refresh()
                }
            }, LinearLayout.LayoutParams(dp(145), dp(45)).apply {
                rightMargin = dp(6)
            })
        }
        horizontal.addView(row)
        listBar.addView(horizontal, LinearLayout.LayoutParams(-1, dp(48)))
        root.addView(listBar, FrameLayout.LayoutParams(-1, dp(98)).apply {
            gravity = Gravity.BOTTOM
            leftMargin = dp(8)
            rightMargin = dp(8)
            bottomMargin = dp(8)
        })

        // Transparent touch layer fills the whole usable counter region; header and list remain interactive.
        val touchLayer = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isClickable = true
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) increment()
                true
            }
        }
        root.addView(touchLayer, FrameLayout.LayoutParams(-1, 0).apply {
            topMargin = dp(130)
            bottomMargin = dp(106)
            height = dp(0)
        })

        setContentView(root)
        refresh()
    }

    private fun increment() {
        count += 1
        prefs.edit().putInt("count", count).putString("selected", selected).apply()
        refresh()
        if (count % 100 == 0) vibrate()
    }

    private fun refresh() {
        countText.text = count.toString()
        nameText.text = selected
    }

    private fun vibrate() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
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

    private fun customize() {
        val items = zikirs()
        AlertDialog.Builder(this)
            .setTitle("📿 জিকির কাস্টমাইজ")
            .setItems(items.toTypedArray()) { _, which -> editZikir(items, items[which]) }
            .setPositiveButton("➕ নতুন জিকির") { _, _ -> addZikir() }
            .setNegativeButton("বন্ধ", null)
            .show()
    }

    private fun addZikir() {
        val input = EditText(this).apply { hint = "জিকিরের নাম লিখুন" }
        AlertDialog.Builder(this)
            .setTitle("নতুন জিকির")
            .setView(input)
            .setNegativeButton("বাতিল", null)
            .setPositiveButton("যোগ") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) {
                    val items = zikirs()
                    if (!items.contains(value)) {
                        items.add(value)
                        saveList(items)
                    }
                    build()
                }
            }
            .show()
    }

    private fun editZikir(items: MutableList<String>, old: String) {
        val input = EditText(this).apply {
            setText(old)
            selectAll()
        }
        AlertDialog.Builder(this)
            .setTitle("জিকির পরিবর্তন")
            .setView(input)
            .setNegativeButton("🗑 মুছুন") { _, _ ->
                items.remove(old)
                if (items.isEmpty()) items.add("সুবহানাল্লাহ")
                saveList(items)
                if (selected == old) selected = items.first()
                count = 0
                prefs.edit().putString("selected", selected).putInt("count", 0).apply()
                build()
            }
            .setPositiveButton("💾 সংরক্ষণ") { _, _ ->
                val value = input.text.toString().trim()
                if (value.isNotEmpty()) {
                    val index = items.indexOf(old)
                    if (index >= 0) items[index] = value
                    saveList(items)
                    if (selected == old) selected = value
                    prefs.edit().putString("selected", selected).apply()
                    build()
                }
            }
            .show()
    }
}
