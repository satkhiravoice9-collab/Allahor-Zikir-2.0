package com.sabbirsamol.app

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

/** Legacy working Zikr list behavior, kept separate from the locked Home screen. */
class ZikirManagerActivity : ComponentActivity() {
    private val colors = intArrayOf(
        Color.parseColor("#7F1D1D"), Color.parseColor("#065F46"), Color.parseColor("#92400E"), Color.parseColor("#1E3A8A"),
        Color.parseColor("#581C87"), Color.parseColor("#9A3412"), Color.parseColor("#155E75"), Color.parseColor("#713F12")
    )
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun card(c: Int) = GradientDrawable().apply { setColor(c); cornerRadius = dp(18).toFloat() }
    private fun bn(n: Int) = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showList() }

    private fun showList() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); setBackgroundColor(Color.parseColor("#1B2A22"))
        }
        val head = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val title = TextView(this).apply {
            text = "📁  জিকির লিস্ট"; textSize = 23f; setTextColor(Color.WHITE); setTypeface(null, 1)
            layoutParams = LinearLayout.LayoutParams(0, dp(56), 1f)
        }
        val main = Button(this).apply {
            text = "মূল তাসবিহ"; textSize = 12f
            setOnClickListener {
                LegacyTasbihStore.setTargetMode(this@ZikirManagerActivity, false)
                LegacyTasbihStore.setActiveId(this@ZikirManagerActivity, "")
                finish()
            }
        }
        head.addView(title); head.addView(main, LinearLayout.LayoutParams(dp(105), dp(50)))
        root.addView(head)

        val scroll = ScrollView(this)
        val listBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val items = LegacyTasbihStore.items(this)
        items.forEachIndexed { index, z ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; background = card(colors[index % colors.size]); setPadding(dp(10), dp(5), dp(6), dp(5))
                setOnClickListener { select(z) }
            }
            val info = TextView(this).apply {
                val left = (z.target - z.count).coerceAtLeast(0)
                text = "${z.name}\n🎯 ${if (z.target > 0) "${bn(z.count)}/${bn(z.target)}  |  বাকি ${bn(left)}" else "Unlimited"}"
                textSize = 15f; setTextColor(Color.WHITE); gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, dp(70), 1f)
            }
            val edit = Button(this).apply { text = "✏️"; setOnClickListener { edit(z) } }
            val delete = Button(this).apply { text = "🗑"; setOnClickListener { delete(z) } }
            row.addView(info); row.addView(edit, LinearLayout.LayoutParams(dp(56), dp(54))); row.addView(delete, LinearLayout.LayoutParams(dp(56), dp(54)))
            listBox.addView(row, LinearLayout.LayoutParams(-1, dp(80)).apply { bottomMargin = dp(8) })
        }
        scroll.addView(listBox)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(Button(this).apply { text = "➕  নতুন জিকির যোগ করুন"; textSize = 16f; setOnClickListener { add() } }, LinearLayout.LayoutParams(-1, dp(58)))
        setContentView(root)
    }

    private fun select(z: LegacyZikrItem) {
        LegacyTasbihStore.setTargetMode(this, true)
        LegacyTasbihStore.setActiveId(this, z.id)
        finish()
    }

    private fun add() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(12), 0) }
        val name = EditText(this).apply { hint = "জিকিরের নাম" }
        val target = EditText(this).apply { hint = "Target (0 = Unlimited)"; inputType = InputType.TYPE_CLASS_NUMBER }
        box.addView(name); box.addView(target)
        AlertDialog.Builder(this).setTitle("নতুন জিকির").setView(box).setNegativeButton("বাতিল", null)
            .setPositiveButton("যোগ") { _, _ ->
                val n = name.text.toString().trim()
                if (n.isNotEmpty()) {
                    val list = LegacyTasbihStore.items(this)
                    list.add(LegacyZikrItem(LegacyTasbihStore.newId(), n, 0, target.text.toString().toIntOrNull() ?: 0))
                    LegacyTasbihStore.saveItems(this, list); showList()
                }
            }.show()
    }

    private fun edit(z: LegacyZikrItem) {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, dp(12), 0) }
        val name = EditText(this).apply { setText(z.name) }
        val target = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER; setText(if (z.target > 0) z.target.toString() else "") }
        box.addView(name); box.addView(target)
        AlertDialog.Builder(this).setTitle("জিকির ও Target Edit").setView(box).setNegativeButton("বাতিল", null)
            .setPositiveButton("Save") { _, _ ->
                val n = name.text.toString().trim()
                if (n.isNotEmpty()) {
                    val list = LegacyTasbihStore.items(this)
                    list.firstOrNull { it.id == z.id }?.apply { name = n; this.target = target.text.toString().toIntOrNull() ?: 0 }
                    LegacyTasbihStore.saveItems(this, list); showList()
                }
            }.show()
    }

    private fun delete(z: LegacyZikrItem) {
        AlertDialog.Builder(this).setTitle("জিকির মুছবেন?").setMessage(z.name).setNegativeButton("না", null)
            .setPositiveButton("হ্যাঁ") { _, _ ->
                val list = LegacyTasbihStore.items(this); list.removeAll { it.id == z.id }
                LegacyTasbihStore.saveItems(this, list)
                if (LegacyTasbihStore.activeId(this) == z.id) {
                    LegacyTasbihStore.setTargetMode(this, false); LegacyTasbihStore.setActiveId(this, "")
                }
                showList()
            }.show()
    }
}
