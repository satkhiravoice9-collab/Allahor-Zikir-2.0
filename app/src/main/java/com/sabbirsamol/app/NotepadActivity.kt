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

class NotepadActivity : Activity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private var bgMain: Int = Color.BLACK
    private var cardBg: Int = Color.WHITE
    private var cardStroke: Int = Color.GRAY
    private var textMain: Int = Color.WHITE
    private var textSub: Int = Color.GRAY

    private lateinit var listContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val theme = ThemeManager.getTheme(this)
        bgMain = theme.bgMain
        cardBg = theme.cardBg
        cardStroke = theme.cardStroke
        textMain = theme.textMain
        textSub = theme.textSub

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        // টপ বার
        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }
        top.addView(TextView(this).apply {
            text = "📝 কালার নোটপ্যাড"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        top.addView(Button(this).apply {
            text = "+ নতুন নোট"
            isAllCaps = false
            textSize = 12f
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(dp(95), dp(38))
            setOnClickListener { showAddEditDialog(null, -1) }
        })
        root.addView(top)

        // কন্টেন্ট লিস্ট
        val scroll = ScrollView(this).apply { isFillViewport = true }
        listContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(75))
        }
        scroll.addView(listContainer)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // বটম নেভিগেশন বার
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
                setTextColor(if (label.contains("নোটপ্যাড")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@NotepadActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@NotepadActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@NotepadActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@NotepadActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> {}
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@NotepadActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@NotepadActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
        loadNotes()
    }

    private fun loadNotes() {
        listContainer.removeAllViews()
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")

        if (jsonArray.length() == 0) {
            listContainer.addView(TextView(this).apply {
                text = "কোনো নোট সংরক্ষণ করা হয়নি। ওপরে '+ নতুন নোট' বাটনে ক্লিক করে নোট লিখুন।"
                textSize = 14f
                setTextColor(textSub)
                gravity = Gravity.CENTER
                setPadding(0, dp(40), 0, 0)
            })
            return
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val title = obj.optString("title")
            val content = obj.optString("content")

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
                text = title
                textSize = 16f
                setTextColor(textMain)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            })

            card.addView(TextView(this).apply {
                text = content
                textSize = 13f
                setTextColor(textSub)
                setPadding(0, 0, 0, dp(8))
            })

            val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }

            btnRow.addView(Button(this).apply {
                text = "এডিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = GradientDrawable().apply { setColor(Color.parseColor("#2563EB")); cornerRadius = dp(6).toFloat() }
                layoutParams = LinearLayout.LayoutParams(0, dp(35), 1f).apply { rightMargin = dp(4) }
                setOnClickListener { showAddEditDialog(obj, i) }
            })

            btnRow.addView(Button(this).apply {
                text = "ডিলিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = GradientDrawable().apply { setColor(Color.parseColor("#DC2626")); cornerRadius = dp(6).toFloat() }
                layoutParams = LinearLayout.LayoutParams(0, dp(35), 1f).apply { leftMargin = dp(4) }
                setOnClickListener { deleteNote(i) }
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
            text = if (existingObj == null) "নতুন নোট তৈরি করুন" else "নোট এডিট করুন"
            textSize = 18f
            setTextColor(textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        })

        val inputTitle = EditText(this).apply {
            hint = "নোটের শিরোনাম"
            setText(existingObj?.optString("title") ?: "")
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }
        dialogLayout.addView(inputTitle)

        val inputContent = EditText(this).apply {
            hint = "নোটের বিস্তারিত বিবরণ..."
            setText(existingObj?.optString("content") ?: "")
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), cardStroke); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply { bottomMargin = dp(20) }
            gravity = Gravity.TOP
        }
        dialogLayout.addView(inputContent)

        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()

        val saveBtn = Button(this).apply {
            text = "সংরক্ষণ করুন"
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply { setColor(Color.parseColor("#047857")); cornerRadius = dp(6).toFloat() }
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val title = inputTitle.text.toString().trim()
                val content = inputContent.text.toString().trim()

                if (title.isNotEmpty()) {
                    saveNoteToPrefs(title, content, index)
                    dialog.dismiss()
                    loadNotes()
                } else {
                    Toast.makeText(this@NotepadActivity, "শিরোনাম লিখুন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialogLayout.addView(saveBtn)
        dialog.show()
    }

    private fun saveNoteToPrefs(title: String, content: String, index: Int) {
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
        val obj = JSONObject().apply {
            put("title", title)
            put("content", content)
        }

        if (index >= 0 && index < jsonArray.length()) {
            jsonArray.put(index, obj)
        } else {
            jsonArray.put(obj)
        }
        prefs.edit().putString("notes_list", jsonArray.toString()).apply()
    }

    private fun deleteNote(index: Int) {
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
        val newArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            if (i != index) newArray.put(jsonArray.get(i))
        }
        prefs.edit().putString("notes_list", newArray.toString()).apply()
        loadNotes()
    }
}
