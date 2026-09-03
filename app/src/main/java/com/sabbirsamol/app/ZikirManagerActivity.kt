package com.sabbirsamol.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ZikirManagerActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    private var bgMain: Int = Color.BLACK
    private var cardBg: Int = Color.WHITE
    private var cardStroke: Int = Color.GRAY
    private var textMain: Int = Color.WHITE
    private var textSub: Int = Color.GRAY

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val themeColors = ThemeManager.getTheme(this)
        bgMain = themeColors.bgMain
        cardBg = themeColors.cardBg
        cardStroke = themeColors.cardStroke
        textMain = themeColors.textMain
        textSub = themeColors.textSub

        buildUI()
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        top.addView(TextView(this).apply {
            text = "📋 জিকির তালিকা ও টার্গেট"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        top.addView(Button(this).apply {
            text = "+ নতুন জিকির"
            isAllCaps = false
            textSize = 12f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(dp(100), dp(38))
            setOnClickListener { showAddEditDialog(null, -1) }
        })
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(75))
        }
        scroll.addView(listContainer)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

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

        navItems.forEach { (label, _) ->
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
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@ZikirManagerActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@ZikirManagerActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@ZikirManagerActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@ZikirManagerActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@ZikirManagerActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@ZikirManagerActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@ZikirManagerActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
        loadZikirList()
    }

    private fun loadZikirList() {
        listContainer.removeAllViews()
        val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")

        if (jsonArray.length() == 0) {
            listContainer.addView(TextView(this).apply {
                text = "কোনো কাস্টম জিকির যোগ করা হয়নি। ওপরে '+ নতুন জিকির' বাটনে ক্লিক করে যোগ করুন।"
                textSize = 14f
                setTextColor(textSub)
                gravity = Gravity.CENTER
                setPadding(0, dp(40), 0, 0)
            })
            return
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getString("id")
            val name = obj.getString("name")
            val target = obj.getInt("target")
            val read = obj.getInt("read")

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    setColor(cardBg)
                    setStroke(dp(1), cardStroke)
                    cornerRadius = dp(12).toFloat()
                }
                setPadding(dp(14), dp(14), dp(14), dp(14))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
            }

            card.addView(TextView(this).apply {
                text = name
                textSize = 16f
                setTextColor(textMain)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            })

            card.addView(TextView(this).apply {
                text = "পড়া হয়েছে: ${bn(read)} / ${bn(target)} বার"
                textSize = 13f
                setTextColor(textSub)
                setPadding(0, 0, 0, dp(8))
            })

            val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 3f }

            btnRow.addView(Button(this).apply {
                text = "পড়ুন"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
                layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply { rightMargin = dp(4) }
                setOnClickListener {
                    val intent = Intent(this@ZikirManagerActivity, TasbihActivity::class.java).apply {
                        putExtra("ZIKIR_ID", id)
                        putExtra("ZIKIR_NAME", name)
                        putExtra("ZIKIR_TARGET", target)
                        putExtra("ZIKIR_READ", read)
                    }
                    startActivity(intent)
                    finish()
                }
            })

            btnRow.addView(Button(this).apply {
                text = "এডিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = GradientDrawable().apply { setColor(Color.parseColor("#2563EB")); cornerRadius = dp(6).toFloat() }
                layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply { setMargins(dp(2), 0, dp(2), 0) }
                setOnClickListener { showAddEditDialog(obj, i) }
            })

            btnRow.addView(Button(this).apply {
                text = "ডিলিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = GradientDrawable().apply { setColor(Color.parseColor("#DC2626")); cornerRadius = dp(6).toFloat() }
                layoutParams = LinearLayout.LayoutParams(0, dp(38), 1f).apply { leftMargin = dp(4) }
                setOnClickListener { deleteZikir(i) }
            })

            card.addView(btnRow)
            listContainer.addView(card)
        }
    }

    private fun showAddEditDialog(existingObj: JSONObject?, index: Int) {
        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        dialogLayout.addView(TextView(this).apply {
            text = if (existingObj == null) "নতুন জিকির যোগ করুন" else "জিকির এডিট করুন"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        })

        val inputName = EditText(this).apply {
            hint = "জিকিরের নাম (যেমন: সুবহানাল্লাহ)"
            setText(existingObj?.optString("name") ?: "")
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }
        dialogLayout.addView(inputName)

        val inputTarget = EditText(this).apply {
            hint = "টার্গেট সংখ্যা (যেমন: ১০০)"
            setText(if (existingObj != null) existingObj.optInt("target").toString() else "")
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
        }
        dialogLayout.addView(inputTarget)

        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()

        val saveBtn = Button(this).apply {
            text = "সংরক্ষণ করুন"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val name = inputName.text.toString().trim()
                val targetStr = inputTarget.text.toString().trim()

                if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                    val target = targetStr.toIntOrNull() ?: 100
                    saveZikirToPrefs(existingObj?.optString("id") ?: UUID.randomUUID().toString(), name, target, existingObj?.optInt("read") ?: 0, index)
                    dialog.dismiss()
                    loadZikirList()
                } else {
                    Toast.makeText(this@ZikirManagerActivity, "সব তথ্য সঠিকভাবে পূরণ করুন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialogLayout.addView(saveBtn)
        dialog.show()
    }

    private fun saveZikirToPrefs(id: String, name: String, target: Int, read: Int, index: Int) {
        val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")
        val obj = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("target", target)
            put("read", read)
        }

        if (index >= 0 && index < jsonArray.length()) {
            jsonArray.put(index, obj)
        } else {
            jsonArray.put(obj)
        }

        prefs.edit().putString("zikir_list", jsonArray.toString()).apply()
    }

    private fun deleteZikir(index: Int) {
        val prefs = getSharedPreferences("ZikirManager", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("zikir_list", "[]") ?: "[]")
        val newArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            if (i != index) newArray.put(jsonArray.get(i))
        }
        prefs.edit().putString("zikir_list", newArray.toString()).apply()
        loadZikirList()
    }
}
