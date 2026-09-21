package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.AlignmentSpan
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NotepadActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private var isInsideNote = false

    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textYellow get() = themeColors.textAccent
    private val btnYellow get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain

    private val noteBgColors = arrayOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2", "#114D3C", "#1F2937")
    private val textColors = arrayOf(Color.RED, Color.parseColor("#10B981"), Color.parseColor("#3B82F6"), Color.parseColor("#F59E0B"), Color.parseColor("#8B5CF6"), Color.BLACK, Color.WHITE)

    private val encryptionKey = "SabbirSamolAppKey"
    private val adminMasterPassword = "Sabbir@@ahmad123"
    private val databaseRef = FirebaseDatabase.getInstance().reference

    private fun getUserMobile(): String {
        val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return prefs.getString("user_mobile", "01700000000") ?: "01700000000"
    }

    private fun getCardDrawable(bgColor: Int = cardBg) = GradientDrawable().apply {
        setColor(bgColor); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }
    private fun getCircleColorDrawable(color: Int) = GradientDrawable().apply {
        shape = GradientDrawable.OVAL; setColor(color); setStroke(dp(1), Color.GRAY)
    }

    private fun encrypt(data: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT).trim()
        } catch (e: Exception) { data }
    }

    private fun decrypt(encryptedData: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) { encryptedData }
    }

    private fun spannedToJson(spanned: Spanned): String {
        val json = JSONObject()
        json.put("text", spanned.toString())
        val spanArray = JSONArray()
        
        val spans = spanned.getSpans(0, spanned.length, Any::class.java)
        for (span in spans) {
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            val spanObj = JSONObject()
            spanObj.put("start", start)
            spanObj.put("end", end)
            
            when (span) {
                is StyleSpan -> {
                    if (span.style == Typeface.BOLD) {
                        spanObj.put("type", "BOLD")
                        spanArray.put(spanObj)
                    }
                }
                is UnderlineSpan -> {
                    spanObj.put("type", "UNDERLINE")
                    spanArray.put(spanObj)
                }
                is ForegroundColorSpan -> {
                    spanObj.put("type", "COLOR")
                    spanObj.put("color", span.foregroundColor)
                    spanArray.put(spanObj)
                }
                is RelativeSizeSpan -> {
                    spanObj.put("type", "SIZE")
                    spanObj.put("size", span.sizeChange.toDouble())
                    spanArray.put(spanObj)
                }
                is AlignmentSpan.Standard -> {
                    if (span.alignment == Layout.Alignment.ALIGN_CENTER) {
                        spanObj.put("type", "ALIGN_CENTER")
                        spanArray.put(spanObj)
                    }
                }
            }
        }
        json.put("spans", spanArray)
        return json.toString()
    }

    private fun jsonToSpanned(jsonStr: String): Spanned {
        return try {
            val json = JSONObject(jsonStr)
            val text = json.optString("text", "")
            val spannable = SpannableString(text)
            val spanArray = json.optJSONArray("spans") ?: JSONArray()
            
            for (i in 0 until spanArray.length()) {
                val spanObj = spanArray.getJSONObject(i)
                val start = spanObj.optInt("start", 0)
                val end = spanObj.optInt("end", 0)
                val type = spanObj.optString("type", "")
                
                if (start >= 0 && end <= text.length && start < end) {
                    when (type) {
                        "BOLD" -> spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        "UNDERLINE" -> spannable.setSpan(UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        "COLOR" -> {
                            val color = spanObj.optInt("color", Color.BLACK)
                            spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        "SIZE" -> {
                            val size = spanObj.optDouble("size", 1.3).toFloat()
                            spannable.setSpan(RelativeSizeSpan(size), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                        "ALIGN_CENTER" -> {
                            spannable.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                }
            }
            spannable
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(jsonStr, Html.FROM_HTML_MODE_COMPACT)
            else @Suppress("DEPRECATION") Html.fromHtml(jsonStr)
        }
    }

    private fun parseColorSafe(colorStr: String, defaultColor: Int): Int {
        return try { Color.parseColor(colorStr) } catch (e: Exception) { defaultColor }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNotesList()
        fetchNotesFromFirebase()
    }
    
    override fun onBackPressed() {
        if (isInsideNote) showNotesList() else super.onBackPressed()
    }

    private fun getNotes(): JSONArray {
        return try {
            val prefs = getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE)
            JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
        } catch (e: Exception) { JSONArray() }
    }

    private fun saveNotes(array: JSONArray) {
        getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE).edit().putString("notes_list", array.toString()).apply()

        val mobile = getUserMobile()
        databaseRef.child("users").child(mobile).child("notes_data").setValue(array.toString())
            .addOnSuccessListener {
                Toast.makeText(this, "নোট ক্লাউডে সেভ হয়েছে!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "সেভ ব্যর্থ: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchNotesFromFirebase() {
        val mobile = getUserMobile()
        databaseRef.child("users").child(mobile).child("notes_data").get().addOnSuccessListener { snapshot: DataSnapshot ->
            val cloudNotes = snapshot.value as? String
            if (!cloudNotes.isNullOrEmpty()) {
                getSharedPreferences("ColorNotepad", Context.MODE_PRIVATE).edit().putString("notes_list", cloudNotes).apply()
                showNotesList()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(this, "সিঙ্ক ব্যর্থ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showPasswordOrAdminDialog(onSuccess: () -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(cardBg)
        }
        layout.addView(TextView(this).apply {
            text = "🔒 পাসওয়ার্ড যাচাইকরণ"
            textSize = 16f
            setTextColor(textYellow)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(10))
        })
        val passInput = EditText(this).apply {
            hint = "আপনার পাসওয়ার্ড বা অ্যাডমিন পাসওয়ার্ড দিন"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(textMain)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        layout.addView(passInput, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(15) })

        val dialog = AlertDialog.Builder(this).setView(layout).create()

        val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        btnRow.addView(Button(this).apply {
            text = "নিশ্চিত করুন"
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(5) }
            setOnClickListener {
                val enteredPass = passInput.text.toString()
                val userSavedPass = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).getString("user_password", "")

                if (enteredPass == userSavedPass || enteredPass == adminMasterPassword) {
                    dialog.dismiss()
                    onSuccess()
                } else {
                    Toast.makeText(this@NotepadActivity, "ভুল পাসওয়ার্ড! পাসওয়ার্ড ভুলে গেলে অ্যাডমিনের সাহায্য নিন।", Toast.LENGTH_LONG).show()
                }
            }
        })
        btnRow.addView(Button(this).apply {
            text = "বাতিল"
            setTextColor(Color.BLACK)
            background = getBtnDrawable(Color.parseColor("#E5E7EB"))
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(5) }
            setOnClickListener { dialog.dismiss() }
        })
        layout.addView(btnRow)
        dialog.show()
    }

    private fun showNotesList() {
        isInsideNote = false

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(textMain); setPadding(0,0,dp(12),0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📝 নোটপ্যাড (নং: ${getUserMobile()})"; textSize = 15f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val listLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        val notes = getNotes()
        if (notes.length() == 0) {
            listLayout.addView(TextView(this).apply { text = "কোনো নোট নেই। নিচে '+' এ চাপ দিয়ে নতুন নোট তৈরি করুন।"; setTextColor(textMain); textSize = 16f; gravity = Gravity.CENTER; setPadding(0, dp(50), 0, 0) })
        } else {
            for (i in 0 until notes.length()) {
                try {
                    val obj = notes.getJSONObject(i)
                    val encryptedTitle = obj.optString("title", "শিরোনামহীন")
                    val title = decrypt(encryptedTitle)
                    val date = obj.optString("date", "")
                    val bgColorStr = obj.optString("bgColor", "#114D3C")
                    val bgColor = parseColorSafe(bgColorStr, Color.parseColor("#114D3C"))

                    val isLight = bgColorStr in listOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2")
                    val titleColor = if (isLight) Color.BLACK else Color.WHITE

                    val card = LinearLayout(this).apply {
                        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                        background = getCardDrawable(bgColor); setPadding(dp(14), dp(14), dp(14), dp(14))
                        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                        setOnClickListener { showViewOrEditNoteDialog(i, obj) }
                    }
                    
                    card.addView(LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                        addView(TextView(this@NotepadActivity).apply { text = title; setTextColor(titleColor); textSize = 17f; setTypeface(null, Typeface.BOLD) })
                        addView(TextView(this@NotepadActivity).apply { text = date; setTextColor(if(isLight) Color.DKGRAY else Color.LTGRAY); textSize = 13f; setPadding(0, dp(4), 0, 0) })
                    })

                    val reorderLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
                    
                    val btnUp = TextView(this).apply {
                        text = "⬆️"; textSize = 16f; setPadding(dp(6), dp(6), dp(6), dp(6))
                        setOnClickListener {
                            if (i > 0) {
                                val temp = notes.get(i)
                                notes.put(i, notes.get(i - 1))
                                notes.put(i - 1, temp)
                                saveNotes(notes)
                                showNotesList()
                            }
                        }
                    }
                    val btnDown = TextView(this).apply {
                        text = "⬇️"; textSize = 16f; setPadding(dp(6), dp(6), dp(6), dp(6))
                        setOnClickListener {
                            if (i < notes.length() - 1) {
                                val temp = notes.get(i)
                                notes.put(i, notes.get(i + 1))
                                notes.put(i + 1, temp)
                                saveNotes(notes)
                                showNotesList()
                            }
                        }
                    }
                    reorderLayout.addView(btnUp)
                    reorderLayout.addView(btnDown)

                    val btnDelete = TextView(this).apply { 
                        text = "🗑️"; textSize = 18f; setPadding(dp(8), dp(6), dp(2), dp(6))
                        setOnClickListener { 
                            showPasswordOrAdminDialog {
                                val updatedNotes = JSONArray()
                                for (j in 0 until notes.length()) {
                                    if (j != i) updatedNotes.put(notes.get(j))
                                }
                                saveNotes(updatedNotes)
                                showNotesList()
                                Toast.makeText(this@NotepadActivity, "নোট ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        } 
                    }
                    
                    card.addView(reorderLayout)
                    card.addView(btnDelete)
                    listLayout.addView(card)
                } catch (e: Exception) { continue }
            }
        }
        scroll.addView(listLayout)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomBar = LinearLayout(this).apply { gravity = Gravity.CENTER; setPadding(dp(10), dp(10), dp(10), dp(10)); background = getCardDrawable() }
        bottomBar.addView(Button(this).apply { text = "＋ নতুন নোট তৈরি করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(45)); setOnClickListener { showAddEditNoteScreen(-1, null) } })
        root.addView(bottomBar)

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A")); setPadding(dp(2), dp(4), dp(2), dp(4)); elevation = dp(8).toFloat()
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
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0
                setPadding(0, 0, 0, 0); gravity = Gravity.CENTER
                setTextColor(if (label.contains("নোটপ্যাড")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@NotepadActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@NotepadActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@NotepadActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@NotepadActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> {}
                        label.contains("সিঙ্ক") -> { 
                            fetchNotesFromFirebase() 
                            val toast = Toast.makeText(this@NotepadActivity, "ক্লাউড থেকে নোট সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show() 
                        }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@NotepadActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun showAddEditNoteScreen(index: Int, existingObj: JSONObject?) {
        isInsideNote = true
        var currentBgColor = existingObj?.optString("bgColor", "#FFFFFF") ?: "#FFFFFF"

        val rawTitle = if (existingObj != null) decrypt(existingObj.optString("title", "")) else ""
        val rawContent = if (existingObj != null) decrypt(existingObj.optString("content", "")) else ""

        val root = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(parseColorSafe(currentBgColor, Color.WHITE)) 
        }

        val topBar = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#114D3C"))
        }

        val btnSave = TextView(this).apply {
            text = " ✓ "; textSize = 24f; setTextColor(Color.parseColor("#10B981"))
            setPadding(dp(8), dp(4), dp(12), dp(4))
        }

        val titleInput = EditText(this).apply {
            hint = "নোটের শিরোনাম লিখুন"; setHintTextColor(Color.LTGRAY); setTextColor(Color.WHITE); textSize = 18f
            setBackgroundColor(Color.TRANSPARENT); setPadding(dp(4), dp(4), dp(4), dp(4)); setText(rawTitle)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        topBar.addView(btnSave)
        topBar.addView(titleInput)
        root.addView(topBar)

        val contentScroll = ScrollView(this).apply { isFillViewport = true; setPadding(dp(8), dp(8), dp(8), dp(8)) }
        val editorBox = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        val initialIsDark = currentBgColor == "#114D3C" || currentBgColor == "#1F2937"

        val contentInput = EditText(this).apply {
            hint = "নোটের বিবরণ লিখুন..."; setHintTextColor(Color.GRAY)
            setTextColor(if (initialIsDark) Color.WHITE else Color.BLACK)
            textSize = 17f
            setBackgroundColor(Color.TRANSPARENT)
            minLines = 20; gravity = Gravity.TOP
            if (rawContent.isNotEmpty()) setText(jsonToSpanned(rawContent))
        }

        editorBox.addView(contentInput)
        contentScroll.addView(editorBox)
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomToolsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(cardBg)
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }

        val formatRow = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        formatRow.addView(Button(this).apply { 
            text = "B"
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34)).apply { rightMargin = dp(4) }
            setOnClickListener { 
                val s = contentInput.selectionStart
                val e = contentInput.selectionEnd
                if (s != -1 && e != -1 && s < e) {
                    contentInput.text.setSpan(StyleSpan(Typeface.BOLD), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        })

        formatRow.addView(Button(this).apply { 
            text = "U"
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            setTextColor(Color.BLACK)
            background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34)).apply { rightMargin = dp(4) }
            setOnClickListener { 
                val s = contentInput.selectionStart
                val e = contentInput.selectionEnd
                if (s != -1 && e != -1 && s < e) {
                    contentInput.text.setSpan(UnderlineSpan(), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        })

        formatRow.addView(Button(this).apply { 
            text = "↔"
            setTextColor(Color.BLACK)
            background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34)).apply { rightMargin = dp(4) }
            setOnClickListener { 
                val s = contentInput.selectionStart
                val e = contentInput.selectionEnd
                if (s != -1 && e != -1 && s < e) {
                    contentInput.text.setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        })

        formatRow.addView(Button(this).apply { 
            text = "A+"
            setTextColor(Color.BLACK)
            background = getBtnDrawable(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(dp(34), dp(34)).apply { rightMargin = dp(8) }
            setOnClickListener { 
                val s = contentInput.selectionStart
                val e = contentInput.selectionEnd
                if (s != -1 && e != -1 && s < e) {
                    contentInput.text.setSpan(RelativeSizeSpan(1.3f), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        })

        val textColorsRow = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        textColors.forEach { color ->
            textColorsRow.addView(View(this).apply {
                background = getCircleColorDrawable(color)
                layoutParams = LinearLayout.LayoutParams(dp(26), dp(26)).apply { rightMargin = dp(6) }
                setOnClickListener {
                    val s = contentInput.selectionStart
                    val e = contentInput.selectionEnd
                    if (s != -1 && e != -1 && s < e) {
                        contentInput.text.setSpan(ForegroundColorSpan(color), s, e, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            })
        }

        formatRow.addView(HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(textColorsRow)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        bottomToolsContainer.addView(formatRow)

        val bgColorsRow = LinearLayout(this).apply { 
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        
        noteBgColors.forEach { hexColor -> 
            bgColorsRow.addView(View(this).apply { 
                background = getCircleColorDrawable(Color.parseColor(hexColor))
                layoutParams = LinearLayout.LayoutParams(dp(32), dp(32)).apply { rightMargin = dp(8) }
                setOnClickListener { 
                    currentBgColor = hexColor
                    root.setBackgroundColor(parseColorSafe(hexColor, Color.WHITE))
                    val isDarkBg = hexColor == "#114D3C" || hexColor == "#1F2937"
                    contentInput.setTextColor(if (isDarkBg) Color.WHITE else Color.BLACK)
                } 
            }) 
        }

        val horizontalScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            isFillViewport = false
            addView(bgColorsRow)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            setPadding(0, dp(6), 0, dp(2))
        }
        bottomToolsContainer.addView(horizontalScroll)
        root.addView(bottomToolsContainer)

        btnSave.setOnClickListener {
            val t = titleInput.text.toString().trim()
            val contentJson = spannedToJson(contentInput.text)
            if (t.isNotEmpty() && contentInput.text.toString().trim().isNotEmpty()) { 
                val notes = getNotes()
                val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val obj = JSONObject().apply { 
                    put("title", encrypt(t))
                    put("content", encrypt(contentJson))
                    put("date", sdf.format(Date()))
                    put("bgColor", currentBgColor) 
                }
                
                if (index == -1) {
                    val newNotesArray = JSONArray()
                    newNotesArray.put(obj)
                    for (i in 0 until notes.length()) {
                        newNotesArray.put(notes.get(i))
                    }
                    saveNotes(newNotesArray)
                } else {
                    notes.put(index, obj)
                    saveNotes(notes)
                }
                showNotesList() 
            } else {
                Toast.makeText(this@NotepadActivity, "শিরোনাম ও বিবরণ লিখুন", Toast.LENGTH_SHORT).show()
            }
        }

        setContentView(root)
    }

    private fun exportNoteAsPdf(title: String, contentJson: String, date: String) {
        try {
            val pageWidth = 595
            val pageHeight = 842
            val margin = 50f
            val printableWidth = (pageWidth - (margin * 2)).toInt()
            val printableHeight = pageHeight - (margin * 2)

            val pdfDocument = android.graphics.pdf.PdfDocument()
            val textPaint = android.text.TextPaint().apply {
                textSize = 18f
                color = Color.BLACK
                isAntiAlias = true
            }

            val spannedContent = jsonToSpanned(contentJson)
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(spannedContent, 0, spannedContent.length, textPaint, printableWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(1.0f, 1.25f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(spannedContent, textPaint, printableWidth, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, false)
            }

            val totalHeight = staticLayout.height
            val headerHeight = 90f
            val availableFirstPageHeight = printableHeight - headerHeight

            var pageNum = 1
            var renderedHeight = 0

            while (renderedHeight < totalHeight || pageNum == 1) {
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawColor(Color.WHITE)

                if (pageNum == 1) {
                    val titlePaint = android.text.TextPaint().apply {
                        color = Color.BLACK
                        textSize = 24f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val datePaint = android.text.TextPaint().apply {
                        color = Color.GRAY
                        textSize = 12f
                        isAntiAlias = true
                    }

                    var y = margin
                    canvas.drawText(title, margin, y + 20f, titlePaint)
                    y += 35f
                    canvas.drawText("তারিখ: $date", margin, y, datePaint)
                    y += 20f

                    val linePaint = android.graphics.Paint().apply {
                        color = Color.LTGRAY
                        strokeWidth = 1f
                    }
                    canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)

                    canvas.save()
                    canvas.translate(margin, y + 25f)
                    canvas.clipRect(0, 0, printableWidth, printableHeight.toInt())
                    canvas.translate(0f, -renderedHeight.toFloat())
                    staticLayout.draw(canvas)
                    canvas.restore()

                    renderedHeight += availableFirstPageHeight.toInt()
                } else {
                    canvas.save()
                    canvas.translate(margin, margin)
                    canvas.clipRect(0, 0, printableWidth, printableHeight.toInt())
                    canvas.translate(0f, -renderedHeight.toFloat())
                    staticLayout.draw(canvas)
                    canvas.restore()

                    renderedHeight += printableHeight.toInt()
                }

                pdfDocument.finishPage(page)
                if (renderedHeight >= totalHeight) break
                pageNum++
            }

            val safeTitle = title.replace(Regex("[^\\w\\d\\u0980-\\u09FF_ -]"), "_").trim()
            val fileName = "${if(safeTitle.isEmpty()) "Untitled" else safeTitle}_${System.currentTimeMillis()}.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        pdfDocument.writeTo(outputStream)
                    }
                }
            } else {
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) targetDir.mkdirs()
                val file = File(targetDir, fileName)
                pdfDocument.writeTo(FileOutputStream(file))
            }

            pdfDocument.close()
            Toast.makeText(this, "পিডিএফ সফলভাবে A4 সাইজে Download ফোল্ডারে সেভ হয়েছে!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "পিডিএফ এক্সপোর্ট ব্যর্থ: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showViewOrEditNoteDialog(index: Int, obj: JSONObject) {
        isInsideNote = true 
        val noteBgColor = obj.optString("bgColor", "#FFFFFF")
        val parsedBgColor = parseColorSafe(noteBgColor, Color.WHITE)
        val isLightBg = noteBgColor in listOf("#FFFFFF", "#FDF6E3", "#DCFCE7", "#DBEAFE", "#FCE7F3", "#FEF2F2")
        val contentTextColor = if (isLightBg) Color.BLACK else Color.WHITE

        val root = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(parsedBgColor) 
        }

        val decryptedTitle = decrypt(obj.optString("title", ""))
        val decryptedContent = decrypt(obj.optString("content", ""))
        val noteDate = obj.optString("date", "")

        val top = LinearLayout(this).apply { 
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.parseColor("#114D3C"))
        }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(Color.WHITE); setPadding(0,0,dp(12),0); setOnClickListener { showNotesList() } })
        top.addView(TextView(this).apply { text = decryptedTitle; textSize = 17f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD); isSingleLine = true }, LinearLayout.LayoutParams(0, -2, 1f))
        
        top.addView(TextView(this).apply { 
            text = "📄"; textSize = 18f; setPadding(dp(8), 0, dp(8), 0)
            setOnClickListener { exportNoteAsPdf(decryptedTitle, decryptedContent, noteDate) } 
        })

        top.addView(TextView(this).apply { text = "✏️"; textSize = 18f; setPadding(dp(8), 0, dp(8), 0); setOnClickListener { showAddEditNoteScreen(index, obj) } })
        top.addView(TextView(this).apply { 
            text = "🗑️"; textSize = 18f; setPadding(dp(8), 0, 0, 0)
            setOnClickListener { 
                showPasswordOrAdminDialog {
                    val notes = getNotes()
                    val updatedNotes = JSONArray()
                    for (j in 0 until notes.length()) {
                        if (j != index) updatedNotes.put(notes.get(j))
                    }
                    saveNotes(updatedNotes)
                    showNotesList()
                    Toast.makeText(this@NotepadActivity, "নোট ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
                }
            } 
        })
        root.addView(top)

        val contentScroll = ScrollView(this).apply { setPadding(dp(16), dp(16), dp(16), dp(16)); isFillViewport = true }
        val contentBox = LinearLayout(this).apply { 
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(8), dp(8), dp(8), dp(8)) 
        }
        contentBox.addView(TextView(this).apply { text = "তারিখ: $noteDate"; setTextColor(if(isLightBg) Color.DKGRAY else Color.LTGRAY); textSize = 13f; setPadding(0, 0, 0, dp(12)) })
        
        val renderedSpanned = jsonToSpanned(decryptedContent)
        contentBox.addView(TextView(this).apply { text = renderedSpanned; setTextColor(contentTextColor); textSize = 17f })
        
        contentScroll.addView(contentBox)
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A")); setPadding(dp(2), dp(4), dp(2), dp(4)); elevation = dp(8).toFloat()
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
                text = label; textSize = 10f; isAllCaps = false; minHeight = 0; minWidth = 0; setPadding(0, 0, 0, 0); gravity = Gravity.CENTER
                setTextColor(if (label.contains("নোটপ্যাড")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@NotepadActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@NotepadActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@NotepadActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@NotepadActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> { showNotesList() }
                        label.contains("সিঙ্ক") -> { 
                            fetchNotesFromFirebase() 
                            val toast = Toast.makeText(this@NotepadActivity, "ক্লাউড থেকে নোট সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT)
                            toast.setGravity(Gravity.CENTER, 0, 0)
                            toast.show() 
                        }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@NotepadActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
