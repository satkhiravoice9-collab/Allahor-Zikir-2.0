package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
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
    private val cardStroke = Color.parseColor("#1B785B")
    private val textYellow = Color.parseColor("#FBBF24")
    private val btnYellow = Color.parseColor("#FACC15")

    // নোটের ব্যাকগ্রাউন্ড কালার লিস্ট
    private val noteBgColors = arrayOf(
        "#FFFFFF", // সাদা
        "#FDF6E3", // অফ-হোয়াইট / হালকা হলুদ
        "#DCFCE7", // হালকা সবুজ
        "#DBEAFE", // হালকা নীল
        "#FCE7F3", // হালকা গোলাপি
        "#FEF2F2", // লালচে সাদা
        "#114D3C", // ডার্ক গ্রিন
        "#1F2937"  // ডার্ক গ্রে
    )

    // লেখার (Text) কালার লিস্ট
    private val textColors = arrayOf(
        Color.RED,
        Color.parseColor("#10B981"), // Green
        Color.parseColor("#3B82F6"), // Blue
        Color.parseColor("#F59E0B"), // Yellow/Orange
        Color.parseColor("#8B5CF6"), // Purple
        Color.BLACK,
        Color.WHITE
    )

    private fun getCardDrawable(bgColor: Int = Color.parseColor("#114D3C")) = GradientDrawable().apply {
        setColor(bgColor); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    private fun getCircleColorDrawable(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
        setStroke(dp(1), Color.GRAY)
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
                val bgColorStr = obj.optString("bgColor", "#114D3C")
                val bgColor = Color.parseColor(bgColorStr)

                // ডার্ক ব্যাকগ্রাউন্ড হলে টেক্সট সাদা হবে, লাইট হলে কালো হবে
                val titleColor = if (bgColorStr == "#FFFFFF" || bgColorStr == "#FDF6E3" || bgColorStr == "#DCFCE7" || bgColorStr == "#DBEAFE" || bgColorStr == "#FCE7F3" || bgColorStr == "#FEF2F2") Color.BLACK else Color.WHITE

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                    background = getCardDrawable(bgColor); setPadding(dp(14), dp(14), dp(14), dp(14))
                    layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                    setOnClickListener { showAddEditNoteDialog(i, obj) }
                }
                card.addView(LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                    addView(TextView(this@NotepadActivity).apply { text = title; setTextColor(titleColor); textSize = 16f; setTypeface(null, Typeface.BOLD) })
                    addView(TextView(this@NotepadActivity).apply { text = date; setTextColor(Color.GRAY); textSize = 12f; setPadding(0, dp(4), 0, 0) })
                })
                
                // ডিলিট বাটন লিস্টেই দেওয়া হলো
                card.addView(TextView(this).apply {
                    text = "🗑️"; textSize = 20f; setPadding(dp(10), 0, 0, 0)
                    setOnClickListener {
                        notes.remove(i); saveNotes(notes); showNotesList()
                    }
                })
                
                listLayout.addView(card)
            }
        }
        scroll.addView(listLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

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
        val dialogView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(Color.parseColor("#1F2937")) }
        
        var currentBgColor = existingObj?.optString("bgColor", "#FFFFFF") ?: "#FFFFFF"

        val titleInput = EditText(this).apply {
            hint = "নোটের শিরোনাম লিখুন"; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE); setPadding(dp(10), dp(10), dp(10), dp(10))
            setText(existingObj?.optString("title") ?: "")
        }
        
        val contentInput = EditText(this).apply {
            hint = "নোটের বিবরণ লিখুন..."; setHintTextColor(Color.GRAY); setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor(currentBgColor)); minLines = 8; gravity = Gravity.TOP
            setPadding(dp(10), dp(10), dp(10), dp(10))
            
            // HTML থেকে লোড করা (যাতে বোল্ড, কালার ঠিক থাকে)
            if (existingObj != null) {
                setText(Html.fromHtml(existingObj.getString("content"), Html.FROM_HTML_MODE_COMPACT))
            }
        }

        // ================= টেক্সট ফরম্যাটিং টুলবার =================
        val formatToolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        
        formatToolbar.addView(TextView(this).apply { text = "টেক্সট ফরম্যাটিং ও কালার টুলস (লেখা সিলেক্ট করে চাপুন):"; setTextColor(Color.LTGRAY); textSize = 11f; layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(4) } })
        
        val formatRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        
        // Bold Button
        formatRow.addView(Button(this).apply {
            text = "B"; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK); background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { rightMargin = dp(4) }
            setOnClickListener {
                val start = contentInput.selectionStart; val end = contentInput.selectionEnd
                if (start < end) contentInput.text.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        })

        // Underline Button
        formatRow.addView(Button(this).apply {
            text = "U"; paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG; setTextColor(Color.BLACK); background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40)).apply { rightMargin = dp(8) }
            setOnClickListener {
                val start = contentInput.selectionStart; val end = contentInput.selectionEnd
                if (start < end) contentInput.text.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        })

        // Text Color Buttons
        textColors.forEach { color ->
            formatRow.addView(View(this).apply {
                background = getCircleColorDrawable(color)
                layoutParams = LinearLayout.LayoutParams(dp(30), dp(30)).apply { rightMargin = dp(6); gravity = Gravity.CENTER_VERTICAL }
                setOnClickListener {
                    val start = contentInput.selectionStart; val end = contentInput.selectionEnd
                    if (start < end) contentInput.text.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            })
        }

        // ================= নোট ব্যাকগ্রাউন্ড কালার টুলবার =================
        val bgToolbar = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, dp(10), 0, dp(10)) }
        bgToolbar.addView(TextView(this).apply { text = "নোটের ব্যাকগ্রাউন্ড থিম (সাদা, লাল, সবুজ ইত্যাদি):"; setTextColor(Color.LTGRAY); textSize = 11f; setPadding(0, 0, 0, dp(4)) })
        
        val bgColorsRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        noteBgColors.forEach { hexColor ->
            bgColorsRow.addView(View(this).apply {
                background = getCircleColorDrawable(Color.parseColor(hexColor))
                layoutParams = LinearLayout.LayoutParams(dp(35), dp(35)).apply { rightMargin = dp(8) }
                setOnClickListener {
                    currentBgColor = hexColor
                    contentInput.setBackgroundColor(Color.parseColor(hexColor))
                    // ব্যাকগ্রাউন্ড অনুযায়ী টেক্সট কালার অ্যাডজাস্ট
                    val isDark = hexColor == "#114D3C" || hexColor == "#1F2937"
                    contentInput.setTextColor(if (isDark) Color.WHITE else Color.BLACK)
                    contentInput.setHintTextColor(if (isDark) Color.LTGRAY else Color.GRAY)
                }
            })
        }
        val bgScroll = HorizontalScrollView(this).apply { addView(bgColorsRow); isHorizontalScrollBarEnabled = false }
        bgToolbar.addView(bgScroll)

        // অ্যাডিং ভিউস
        dialogView.addView(TextView(this).apply { text = if (index == -1) "নতুন নোট তৈরি" else "নোট সম্পাদনা"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(12)) })
        dialogView.addView(titleInput, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        dialogView.addView(contentInput, LinearLayout.LayoutParams(-1, dp(200)).apply { bottomMargin = dp(5) })
        
        val formatContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        formatContainer.addView(formatToolbar.getChildAt(0)) // Title
        formatContainer.addView(formatRow)
        dialogView.addView(formatContainer)
        dialogView.addView(bgToolbar)

        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val btnLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        btnLayout.addView(Button(this).apply {
            text = "সংরক্ষণ"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(5) }
            setOnClickListener {
                val t = titleInput.text.toString().trim()
                // লেখাকে HTML এ কনভার্ট করা হচ্ছে (যাতে কালার, বোল্ড সেভ থাকে)
                val htmlContent = Html.toHtml(contentInput.text, Html.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE).trim()
                
                if (t.isNotEmpty() && contentInput.text.toString().trim().isNotEmpty()) {
                    val notes = getNotes()
                    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    val currentDate = sdf.format(Date())

                    val obj = JSONObject().apply {
                        put("title", t)
                        put("content", htmlContent)
                        put("date", currentDate)
                        put("bgColor", currentBgColor)
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
            text = "বাতিল"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(Color.parseColor("#E5E7EB"))
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(5) }
            setOnClickListener { dialog.dismiss() }
        })
        dialogView.addView(btnLayout)

        dialog.show()
    }
}
