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
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class LegacyZikrItem(
    val id: String,
    var name: String,
    var count: Int,
    var target: Int
)

/** Storage format copied from the working legacy MainActivity approach. */
object LegacyTasbihStore {
    private const val PREF = "legacy_working_tasbih"
    private const val KEY_LIST = "zikr_json"
    private const val KEY_FREE = "free_count"
    private const val KEY_MODE = "target_mode"
    private const val KEY_ACTIVE = "active_id"

    private fun prefs(context: Context) = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun items(context: Context): MutableList<LegacyZikrItem> {
        val raw = prefs(context).getString(KEY_LIST, null)
        if (raw.isNullOrBlank()) {
            return mutableListOf(
                LegacyZikrItem("1", "সুবহানাল্লাহ (SubhanAllah)", 0, 33),
                LegacyZikrItem("2", "আলহামদুলিল্লাহ (Alhamdulillah)", 0, 33),
                LegacyZikrItem("3", "আল্লাহু আকবার (Allahu Akbar)", 0, 34),
                LegacyZikrItem("4", "আস্তাগফিরুল্লাহ (Astaghfirullah)", 0, 100),
                LegacyZikrItem("5", "আয়াতুল কুরসি (Ayatul Kursi)", 0, 7)
            ).also { saveItems(context, it) }
        }
        val array = JSONArray(raw)
        val result = mutableListOf<LegacyZikrItem>()
        for (i in 0 until array.length()) {
            val o = array.getJSONObject(i)
            result.add(LegacyZikrItem(o.getString("id"), o.getString("name"), o.optInt("count", 0), o.optInt("target", 0)))
        }
        return result
    }

    fun saveItems(context: Context, items: List<LegacyZikrItem>) {
        val a = JSONArray()
        items.forEach { z ->
            a.put(JSONObject().apply {
                put("id", z.id); put("name", z.name); put("count", z.count); put("target", z.target)
            })
        }
        prefs(context).edit().putString(KEY_LIST, a.toString()).apply()
    }

    fun freeCount(context: Context) = prefs(context).getInt(KEY_FREE, 0)
    fun setFreeCount(context: Context, value: Int) = prefs(context).edit().putInt(KEY_FREE, value).apply()
    fun targetMode(context: Context) = prefs(context).getBoolean(KEY_MODE, false)
    fun setTargetMode(context: Context, value: Boolean) = prefs(context).edit().putBoolean(KEY_MODE, value).apply()
    fun activeId(context: Context) = prefs(context).getString(KEY_ACTIVE, "") ?: ""
    fun setActiveId(context: Context, id: String) = prefs(context).edit().putString(KEY_ACTIVE, id).apply()
    fun newId() = UUID.randomUUID().toString()
}

class TasbihActivity : ComponentActivity() {
    private lateinit var countDisplay: TextView
    private lateinit var targetInfo: TextView
    private lateinit var titleView: TextView
    private var currentCount = 0
    private var currentTarget = 0
    private var active: LegacyZikrItem? = null
    private var stopped = false

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bangla(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showTasbihScreen()
    }

    override fun onResume() {
        super.onResume()
        if (::countDisplay.isInitialized) showTasbihScreen()
    }

    private fun showTasbihScreen() {
        val targetMode = LegacyTasbihStore.targetMode(this)
        active = if (targetMode) LegacyTasbihStore.items(this).firstOrNull { it.id == LegacyTasbihStore.activeId(this) } else null
        currentCount = active?.count ?: LegacyTasbihStore.freeCount(this)
        currentTarget = active?.target ?: 0
        stopped = active != null && currentTarget > 0 && currentCount >= currentTarget

        val root = FrameLayout(this).apply { setBackgroundColor(Color.parseColor("#1B2A22")) }
        val tapLayer = View(this).apply {
            setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) addOne()
                true
            }
        }
        root.addView(tapLayer, FrameLayout.LayoutParams(-1, -1))

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(6)) }
        titleView = TextView(this).apply {
            text = active?.let { "📿 ${it.name}" } ?: "🕋 সাধারণ তাসবিহ কাউন্টার"
            textSize = 16f; setTextColor(Color.WHITE); setTypeface(Typeface.SERIF, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
        val reset = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_popup_sync); setColorFilter(Color.WHITE); setBackgroundColor(Color.TRANSPARENT)
            contentDescription = "রিসেট"; setOnClickListener { confirmReset() }
        }
        top.addView(titleView); top.addView(reset, LinearLayout.LayoutParams(dp(52), dp(48)))
        root.addView(top, FrameLayout.LayoutParams(-1, dp(64)).apply { gravity = Gravity.TOP })

        val kaaba = TextView(this).apply {
            text = "🕋"; textSize = 82f; gravity = Gravity.CENTER
            setOnClickListener { addOne() }
        }
        root.addView(kaaba, FrameLayout.LayoutParams(-1, dp(150)).apply { topMargin = dp(85) })

        val counterBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        countDisplay = TextView(this).apply {
            textSize = 78f; setTextColor(Color.WHITE); setTypeface(Typeface.SERIF, Typeface.BOLD); gravity = Gravity.CENTER
        }
        targetInfo = TextView(this).apply {
            textSize = 15f; setTextColor(Color.parseColor("#FDE68A")); setTypeface(Typeface.SERIF, Typeface.BOLD); gravity = Gravity.CENTER
        }
        val guide = TextView(this).apply {
            text = "👆 স্ক্রিনের যেকোনো জায়গায় ট্যাপ করে গণনা করুন"
            textSize = 13f; setTextColor(Color.parseColor("#CBD5E1")); gravity = Gravity.CENTER
        }
        counterBox.addView(countDisplay); counterBox.addView(targetInfo); counterBox.addView(guide)
        root.addView(counterBox, FrameLayout.LayoutParams(-1, dp(210)).apply { topMargin = dp(235) })

        val listButton = TextView(this).apply {
            text = "📁  জিকির লিস্ট"; textSize = 18f; setTextColor(Color.WHITE); setTypeface(Typeface.SERIF, Typeface.BOLD); gravity = Gravity.CENTER
            background = GradientDrawable().apply { setColor(Color.parseColor("#7C2D12")); cornerRadius = dp(20).toFloat() }
            setOnClickListener { startActivity(Intent(this@TasbihActivity, ZikirManagerActivity::class.java)) }
        }
        root.addView(listButton, FrameLayout.LayoutParams(-1, dp(62)).apply {
            gravity = Gravity.BOTTOM; leftMargin = dp(12); rightMargin = dp(12); bottomMargin = dp(12)
        })

        setContentView(root)
        refresh()
    }

    private fun addOne() {
        if (stopped) return
        val z = active
        if (z != null) {
            z.count++
            currentCount = z.count
            LegacyTasbihStore.items(this).also { list ->
                list.firstOrNull { it.id == z.id }?.count = z.count
                LegacyTasbihStore.saveItems(this, list)
            }
            if (currentCount % 100 == 0) vibrate(180)
            if (currentTarget > 0 && currentCount >= currentTarget) {
                stopped = true
                vibrate(500)
                AlertDialog.Builder(this).setTitle("মাশাআল্লাহ!").setMessage("আপনার নির্ধারিত টার্গেট (${bangla(currentTarget)} বার) পূর্ণ হয়েছে।").setPositiveButton("ঠিক আছে", null).show()
            }
        } else {
            currentCount++
            LegacyTasbihStore.setFreeCount(this, currentCount)
            if (currentCount % 100 == 0) vibrate(200)
        }
        refresh()
    }

    private fun refresh() {
        countDisplay.text = bangla(currentCount)
        targetInfo.text = if (active == null) {
            "Unlimited"
        } else {
            val left = (currentTarget - currentCount).coerceAtLeast(0)
            "🎯 টার্গেট: ${bangla(currentTarget)}   |   বাকি: ${bangla(left)}${if (stopped) "   ✓ সম্পূর্ণ" else ""}"
        }
    }

    private fun confirmReset() {
        AlertDialog.Builder(this).setTitle("রিসেট").setMessage("বর্তমান গণনা শূন্য করবেন?")
            .setNegativeButton("না", null).setPositiveButton("হ্যাঁ") { _, _ ->
                val z = active
                if (z != null) {
                    val list = LegacyTasbihStore.items(this)
                    list.firstOrNull { it.id == z.id }?.count = 0
                    LegacyTasbihStore.saveItems(this, list)
                } else LegacyTasbihStore.setFreeCount(this, 0)
                currentCount = 0; stopped = false; refresh()
            }.show()
    }

    private fun vibrate(duration: Long) {
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        else @Suppress("DEPRECATION") v.vibrate(duration)
    }
}
