package com.sabbirsamol.app

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.activity.ComponentActivity
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfReaderActivity : ComponentActivity() {

    private lateinit var pdfView: PDFView
    private var bookName: String = ""
    private var targetPage: Int = 0

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookName = intent.getStringExtra("BOOK_NAME") ?: "Book"
        targetPage = intent.getIntExtra("TARGET_PAGE", 0) 

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.parseColor("#091C14")) }

        // --- টপ বার (Top Bar) ও সার্চ অপশন ---
        val topBar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(8), dp(8), dp(8)); setBackgroundColor(Color.parseColor("#091C14")) }

        topBar.addView(TextView(this).apply {
            text = "← বের হন"
            textSize = 15f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD); setPadding(0, 0, dp(10), 0)
            setOnClickListener { finish() }
        })

        topBar.addView(TextView(this).apply {
            text = bookName
            textSize = 14f; setTextColor(Color.WHITE); isSingleLine = true; ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
        }, LinearLayout.LayoutParams(0, -2, 1f))

        val pageInput = EditText(this).apply {
            hint = "পৃষ্ঠা"
            setHintTextColor(Color.GRAY); setTextColor(Color.BLACK); textSize = 14f; inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER; setBackgroundColor(Color.WHITE); setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(dp(50), -2).apply { setMargins(dp(6), 0, dp(6), 0) }
        }
        topBar.addView(pageInput)

        topBar.addView(Button(this).apply {
            text = "যান"
            textSize = 14f; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#FACC15")); setPadding(dp(8), 0, dp(8), 0)
            layoutParams = LinearLayout.LayoutParams(-2, dp(35))
            setOnClickListener {
                val p = pageInput.text.toString().toIntOrNull()
                if (p != null && p > 0) {
                    // পিডিএফ ০ থেকে ইনডেক্স শুরু করে, তাই p - 1 দেওয়া হলো
                    pdfView.jumpTo(p - 1) 
                }
            }
        })
        root.addView(topBar)

        // --- পিডিএফ ভিউ ফ্রেম ---
        val pdfContainer = RelativeLayout(this).apply { setBackgroundColor(Color.WHITE) }
        pdfView = PDFView(this, null)
        pdfContainer.addView(pdfView, RelativeLayout.LayoutParams(-1, -1))
        
        root.addView(pdfContainer, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)

        val safeFileName = bookName.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
        val file = File(filesDir, safeFileName)

        if (file.exists()) {
            openPdf(file)
        } else {
            Toast.makeText(this, "ফাইল পাওয়া যায়নি! লাইব্রেরি থেকে ডাউনলোড করুন।", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun openPdf(file: File) {
        val sharedPref = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE)
        
        // টার্গেট পেজ থাকলে সেখান থেকে ওপেন হবে (p - 1), না হলে সেভ করা জায়গা থেকে
        val startPage = if (targetPage > 0) targetPage - 1 else sharedPref.getInt(bookName, 0)

        pdfView.fromFile(file)
            .defaultPage(startPage)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .onPageChange { page, _ ->
                // পেজ পাল্টালেই অটোমেটিক সেভ হবে
                sharedPref.edit().putInt(bookName, page).apply()
            }
            .enableDoubletap(true)
            .load()
    }
}
