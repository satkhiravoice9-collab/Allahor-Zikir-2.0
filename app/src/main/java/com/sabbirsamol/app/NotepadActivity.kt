package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class NotepadActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private val bgMain = Color.parseColor("#091C14")
    private val cardBg = Color.parseColor("#114D3C")
    private val cardStroke = Color.parseColor("#1B785B")
    private val textYellow = Color.parseColor("#FBBF24")
    private val btnYellow = Color.parseColor("#FACC15")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNotesList()
    }

    private fun getNotes(): JSONArray {
        val prefs = getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE)
        return JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
    }

    private fun saveNotes(array: JSONArray) {
        getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE).edit().putString("notes_list", array.toString()).apply()
    }

    private fun showNotesList() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        // Top Bar
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(Color.WHITE); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📝 কালার নোটপ্যাড"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        val notes = getNotes()
        if (notes.length() == 0) {
            listLayout.addView(TextView(this).apply { text = "কোনো নোট নেই। নিচে '+' এ চাপ দিয়ে নতুন নোট তৈরি করুন।"; setTextColor(Color.WHITE); textSize = 15f; gravity = Gravity.CENTER; setPadding(0, dp(50), 0, 0) })
        } else {
            for (i in 0 until notes.length()) {
                val obj = notes.getJSONObject(i)
                val title = obj.getString("title")
                val date = obj.getString("date")

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14))
                    layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                    setOnClickListener { showViewOrEditNoteDialog(i, obj) }
                }
                card.addView(LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                    addView(TextView(this@NotepadActivity).apply { text = title; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD) })
                    addView(TextView(this@NotepadActivity).apply { text = date; setTextColor(Color.parseColor("#9CA3AF")); textSize = 12f; setPadding(0, dp(4), 0, 0) })
                })
                listLayout.addView(card)
            }
        }
        scroll.addView(listLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        // Floating Add Button Style at bottom
        val bottomBar = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(dp(10), dp(10), dp(10), dp(10)); background = getCardDrawable() }
        bottomBar.addView(Button(this).apply {
            text = "＋ নতুন নোট তৈরি করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener { showAddEditNoteDialog(-1, null) }
        })
        root.addView(bottomBar)

        setContentView(root)
    }

    private fun showAddEditNoteDialog(index: Int, existingObj: JSONObject?) {
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(Color.parseColor("#091C14")) }

        val titleInput = EditText(this).apply {
            hint = "নোটের শিরোনাম লিখুন..."; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); setBackgroundColor(Color.parseColor("#114D3C"))
            setPadding(dp(10), dp(10), dp(10), dp(10)); setText(existingObj?.optString("title") ?: "")
        }
        val contentInput = EditText(this).apply {
            hint = "নোটের বিবরণ লিখুন..."; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE); setBackgroundColor(Color.parseColor("#114D3C"))
            minLines = 5; setPadding(dp(10), dp(10), dp(10), dp(10)); setText(existingObj?.optString("content") ?: "")
        }

        dialogView.addView(titleInput, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        dialogView.addView(contentInput, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(15) })

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        btnLayout.addView(Button(this).apply {
            text = "সংরক্ষণ"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(5) }
            setOnClickListener {
                val t = titleInput.text.toString().trim()
                val c = contentInput.text.toString().trim()
                if (t.isNotEmpty() && c.isNotEmpty()) {
                    val notes = getNotes()
                    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val currentDate = sdf.format(Date())

                    val obj = JSONObject().apply {
                        put("title", t)
                        put("content", c)
                        put("date", currentDate)
                    }

                    if (index == -1) notes.put(obj) else notes.put(index, obj)
                    saveNotes(notes)
                    dialog.dismiss()
                    showNotesList()
                } else {
                    Toast.makeText(this@NotepadActivity, "শিরোনাম ও বিবরণ লিখুন", Toast.LENGTH_SHORT).show()
                }
            }
        })
        btnLayout.addView(Button(this).apply {
            text = "বাতিল"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#374151"))
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(5) }
            setOnClickListener { dialog.dismiss() }
        })
        dialogView.addView(btnLayout)

        dialog.show()
    }

    private fun showViewOrEditNoteDialog(index: Int, obj: JSONObject) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(Color.WHITE); setPadding(0,0,dp(12),0); setOnClickListener { showNotesList() } })
        top.addView(TextView(this).apply { text = obj.getString("title"); textSize = 17f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD); isSingleLine = true }, LinearLayout.LayoutParams(0, -2, 1f))
        
        // Edit & Delete Icons
        top.addView(TextView(this).apply { text = "✏️"; textSize = 18f; setPadding(dp(8), 0, dp(8), 0); setOnClickListener { showAddEditNoteDialog(index, obj) } })
        top.addView(TextView(this).apply { text = "🗑️"; textSize = 18f; setPadding(dp(8), 0, 0, 0); setOnClickListener {
            val notes = getNotes()
            notes.remove(index)
            saveNotes(notes)
            showNotesList()
        } })
        root.addView(top)

        val contentScroll = ScrollView(this).apply { setPadding(dp(16), dp(16), dp(16), dp(16)) }
        val contentBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(16), dp(16), dp(16), dp(16)) }
        
        contentBox.addView(TextView(this).apply { text = "তারিখ: ${obj.getString("date")}"; setTextColor(Color.parseColor("#9CA3AF")); textSize = 12f; setPadding(0, 0, 0, dp(12)) })
        contentBox.addView(TextView(this).apply { text = obj.getString("content"); setTextColor(Color.WHITE); textSize = 16f })
        
        contentScroll.addView(contentBox)
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        setContentView(root)
    }
}
