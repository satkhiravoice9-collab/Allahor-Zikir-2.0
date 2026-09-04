package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NotepadActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private val themeColors by lazy { ThemeManager.getTheme(this) }
    private lateinit var notesContainer: LinearLayout

    private val databaseRef = FirebaseDatabase.getInstance().reference

    // এনক্রিপশনের জন্য একটি ফিক্সড সিক্রেট কি (১৬ বাইট)
    private val encryptionKey = "SabbirSamolAppKey"

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(themeColors.cardBg); setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkPinLock {
            buildUI()
            fetchNotesFromFirebase()
        }
    }

    private fun getSafeUserId(): String {
        val sharedPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val activeEmail = sharedPrefs.getString("user_email", "default_user") ?: "default_user"
        return activeEmail.replace(".", "_").replace("@", "_")
    }

    // AES এনক্রিপশন ফাংশন
    private fun encrypt(data: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            data
        }
    }

    // AES ডিক্রিপশন ফাংশন
    private fun decrypt(encryptedData: String): String {
        return try {
            val keySpec = SecretKeySpec(encryptionKey.toByteArray(Charsets.UTF_8), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val decodedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedData
        }
    }

    private fun checkPinLock(onSuccess: () -> Unit) {
        val prefs = getSharedPreferences("NotepadSecurity", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("notepad_pin", null)

        if (savedPin == null) {
            showSetPinDialog { onSuccess() }
        } else {
            showUnlockDialog(savedPin) { onSuccess() }
        }
    }

    private fun showSetPinDialog(onSuccess: () -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(themeColors.bgMain)
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        layout.addView(TextView(this).apply {
            text = "🔒 নোটপ্যাড সিকিউরিটি পিন সেট করুন"
            textSize = 18f
            setTextColor(themeColors.textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })

        layout.addView(TextView(this).apply {
            text = "আপনার নোট সুরক্ষিত রাখতে ৪ বা তার বেশি সংখ্যার একটি পিন দিন।"
            textSize = 13f
            setTextColor(themeColors.textSub)
            setPadding(0, 0, 0, dp(16))
        })

        val inputPin = EditText(this).apply {
            hint = "নতুন পিন দিন (যেমন: 1234)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setTextColor(themeColors.textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply { setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(6).toFloat(); setColor(themeColors.cardBg) }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
        }
        layout.addView(inputPin)

        val dialog = AlertDialog.Builder(this).setView(layout).setCancelable(false).create()

        val saveBtn = Button(this).apply {
            text = "পিন সেট করুন"
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val pin = inputPin.text.toString().trim()
                if (pin.length >= 4) {
                    getSharedPreferences("NotepadSecurity", Context.MODE_PRIVATE).edit().putString("notepad_pin", pin).apply()
                    Toast.makeText(this@NotepadActivity, "পিন সফলভাবে সেট হয়েছে", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    onSuccess()
                } else {
                    Toast.makeText(this@NotepadActivity, "কমপক্ষে ৪ সংখ্যার পিন দিন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(saveBtn)
        dialog.show()
    }

    private fun showUnlockDialog(savedPin: String, onSuccess: () -> Unit) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(themeColors.bgMain)
            setPadding(dp(24), dp(24), dp(24), dp(24))
        }

        layout.addView(TextView(this).apply {
            text = "🔐 নোটপ্যাড লক করা আছে"
            textSize = 18f
            setTextColor(themeColors.textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(12))
        })

        layout.addView(TextView(this).apply {
            text = "নোট দেখতে আপনার সিকিউরিটি পিনটি লিখুন।"
            textSize = 13f
            setTextColor(themeColors.textSub)
            setPadding(0, 0, 0, dp(16))
        })

        val inputPin = EditText(this).apply {
            hint = "পিন কোড লিখুন"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setTextColor(themeColors.textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply { setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(6).toFloat(); setColor(themeColors.cardBg) }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(20) }
        }
        layout.addView(inputPin)

        val dialog = AlertDialog.Builder(this).setView(layout).setCancelable(false).create()

        val unlockBtn = Button(this).apply {
            text = "আনলক করুন"
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val enteredPin = inputPin.text.toString().trim()
                if (enteredPin == savedPin) {
                    dialog.dismiss()
                    onSuccess()
                } else {
                    Toast.makeText(this@NotepadActivity, "ভুল পিন! আবার চেষ্টা করুন।", Toast.LENGTH_SHORT).show()
                }
            }
        }
        layout.addView(unlockBtn)
        dialog.show()
    }

    private fun fetchNotesFromFirebase() {
        val safeUserId = getSafeUserId()
        databaseRef.child("users").child(safeUserId).child("notepad_data").get().addOnSuccessListener { snapshot: DataSnapshot ->
            val cloudNotes = snapshot.value as? String
            if (!cloudNotes.isNullOrEmpty()) {
                getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE).edit().putString("notes_list", cloudNotes).apply()
                loadNotesList()
            }
        }
    }

    private fun buildUI() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(themeColors.textMain); setPadding(0, 0, dp(12), 0); setOnClickListener { startActivity(Intent(this@NotepadActivity, MainActivity::class.java)); finish() } })
        top.addView(TextView(this).apply { text = "📝 জরুরি নোটপ্যাড"; textSize = 17f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        
        top.addView(Button(this).apply {
            text = "+ নতুন নোট"; isAllCaps = false; setTextColor(Color.WHITE); textSize = 12f
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(dp(90), dp(36))
            setOnClickListener { showAddEditDialog(null, -1) }
        })
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        notesContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }
        scroll.addView(notesContainer)
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
                setTextColor(if (label.contains("নোটপ্যাড")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@NotepadActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@NotepadActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@NotepadActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { startActivity(Intent(this@NotepadActivity, MasnunAmolActivity::class.java)); finish() }
                        label.contains("নোটপ্যাড") -> {}
                        label.contains("সিঙ্ক") -> { fetchNotesFromFirebase(); Toast.makeText(this@NotepadActivity, "নোটপ্যাড ডেটা সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@NotepadActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
        loadNotesList()
    }

    private fun loadNotesList() {
        notesContainer.removeAllViews()
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")

        if (jsonArray.length() == 0) {
            notesContainer.addView(TextView(this).apply {
                text = "কোনো নোট সংরক্ষিত নেই। ওপরে '+ নতুন নোট' বাটনে ক্লিক করে তৈরি করুন।"
                textSize = 14f
                setTextColor(themeColors.textSub)
                gravity = Gravity.CENTER
                setPadding(0, dp(40), 0, 0)
            })
            return
        }

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val id = obj.getString("id")
            
            // ফায়ারবেস বা লোকাল থেকে আনা এনক্রিপ্টেড ডেটা ডিক্রিপ্ট করে স্ক্রিনে দেখানো হচ্ছে
            val title = decrypt(obj.getString("title"))
            val content = decrypt(obj.getString("content"))

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getCardDrawable()
                setPadding(dp(14), dp(14), dp(14), dp(14))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            }

            card.addView(TextView(this).apply {
                text = title
                textSize = 16f
                setTextColor(themeColors.textMain)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 0, 0, dp(4))
            })

            card.addView(TextView(this).apply {
                text = content
                textSize = 14f
                setTextColor(themeColors.textSub)
                setPadding(0, 0, 0, dp(10))
            })

            val btnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }

            btnRow.addView(Button(this).apply {
                text = "এডিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = getBtnDrawable(Color.parseColor("#2563EB"))
                layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f).apply { rightMargin = dp(4) }
                setOnClickListener { showAddEditDialog(obj, i) }
            })

            btnRow.addView(Button(this).apply {
                text = "ডিলিট"
                isAllCaps = false
                setTextColor(Color.WHITE)
                textSize = 12f
                background = getBtnDrawable(Color.parseColor("#DC2626"))
                layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f).apply { leftMargin = dp(4) }
                setOnClickListener { deleteNote(i) }
            })

            card.addView(btnRow)
            notesContainer.addView(card)
        }
    }

    private fun showAddEditDialog(existingObj: JSONObject?, index: Int) {
        // এডিট করার সময় এনক্রিপ্টেড ডেটা ডিক্রিপ্ট করে বক্সে দেখানো হবে
        val rawTitle = if (existingObj != null) decrypt(existingObj.optString("title")) else ""
        val rawContent = if (existingObj != null) decrypt(existingObj.optString("content")) else ""

        val dialogLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(themeColors.bgMain)
            setPadding(dp(20), dp(20), dp(20), dp(20))
        }

        dialogLayout.addView(TextView(this).apply {
            text = if (existingObj == null) "নতুন নোট তৈরি করুন" else "নোট এডিট করুন"
            textSize = 18f
            setTextColor(themeColors.textMain)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(15))
        })

        val inputTitle = EditText(this).apply {
            hint = "নোটের শিরোনাম"
            setText(rawTitle)
            setTextColor(themeColors.textMain)
            setHintTextColor(Color.GRAY)
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(6).toFloat(); setColor(themeColors.cardBg) }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }
        dialogLayout.addView(inputTitle)

        val inputContent = EditText(this).apply {
            hint = "নোটের বিস্তারিত বিবরণ..."
            setText(rawContent)
            setTextColor(themeColors.textMain)
            setHintTextColor(Color.GRAY)
            gravity = Gravity.TOP
            minLines = 4
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = GradientDrawable().apply { setStroke(dp(1), themeColors.cardStroke); cornerRadius = dp(6).toFloat(); setColor(themeColors.cardBg) }
            layoutParams = LinearLayout.LayoutParams(-1, dp(120)).apply { bottomMargin = dp(20) }
        }
        dialogLayout.addView(inputContent)

        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()

        val saveBtn = Button(this).apply {
            text = "সংরক্ষণ করুন"
            setTextColor(Color.WHITE)
            background = getBtnDrawable(Color.parseColor("#047857"))
            layoutParams = LinearLayout.LayoutParams(-1, dp(45))
            setOnClickListener {
                val title = inputTitle.text.toString().trim()
                val content = inputContent.text.toString().trim()

                if (title.isNotEmpty() && content.isNotEmpty()) {
                    saveNoteToPrefs(existingObj?.optString("id") ?: UUID.randomUUID().toString(), title, content, index)
                    dialog.dismiss()
                    loadNotesList()
                } else {
                    Toast.makeText(this@NotepadActivity, "শিরোনাম ও বিবরণ উভয়ই পূরণ করুন", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialogLayout.addView(saveBtn)
        dialog.show()
    }

    private fun saveNoteToPrefs(id: String, title: String, content: String, index: Int) {
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")

        // ফায়ারবেসে পাঠানোর আগে শিরোনাম ও বিবরণ এনক্রিপ্ট করা হচ্ছে
        val encryptedTitle = encrypt(title)
        val encryptedContent = encrypt(content)

        val obj = JSONObject().apply {
            put("id", id)
            put("title", encryptedTitle)
            put("content", encryptedContent)
        }

        if (index >= 0 && index < jsonArray.length()) {
            jsonArray.put(index, obj)
        } else {
            jsonArray.put(obj)
        }

        prefs.edit().putString("notes_list", jsonArray.toString()).apply()

        val safeUserId = getSafeUserId()
        databaseRef.child("users").child(safeUserId).child("notepad_data").setValue(jsonArray.toString())
    }

    private fun deleteNote(index: Int) {
        val prefs = getSharedPreferences("NotepadPrefs", Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString("notes_list", "[]") ?: "[]")
        val newArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            if (i != index) newArray.put(jsonArray.get(i))
        }
        prefs.edit().putString("notes_list", newArray.toString()).apply()
        loadNotesList()

        val safeUserId = getSafeUserId()
        databaseRef.child("users").child(safeUserId).child("notepad_data").setValue(newArray.toString())
    }
}
