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
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private var bookName: String = ""
    private var targetPage: Int = 0

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // আগের পেজ (LibraryActivity) থেকে ডেটা গ্রহণ
        bookName = intent.getStringExtra("BOOK_NAME") ?: "Book"
        val downloadUrl = intent.getStringExtra("DOWNLOAD_URL") ?: ""
        targetPage = intent.getIntExtra("TARGET_PAGE", 0) 

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#091C14"))
        }

        // --- টপ বার (Top Bar) ডিজাইন ---
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.parseColor("#091C14"))
        }

        // ব্যাক বাটন (বের হন)
        topBar.addView(TextView(this).apply {
            text = "← বের হন"
            textSize = 15f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, dp(10), 0)
            setOnClickListener { finish() }
        })

        // বইয়ের নাম
        topBar.addView(TextView(this).apply {
            text = bookName
            textSize = 14f
            setTextColor(Color.WHITE)
            isSingleLine = true
            ellipsize = android.text.TextUtils.TruncateAt.MARQUEE
        }, LinearLayout.LayoutParams(0, -2, 1f))

        // পেজ ইনপুট বক্স (EditText)
        val pageInput = EditText(this).apply {
            hint = "পৃষ্ঠা"
            setHintTextColor(Color.GRAY)
            setTextColor(Color.BLACK)
            textSize = 14f
            inputType = InputType.TYPE_CLASS_NUMBER
            gravity = Gravity.CENTER
            setBackgroundColor(Color.WHITE)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            layoutParams = LinearLayout.LayoutParams(dp(50), -2).apply { setMargins(dp(6), 0, dp(6), 0) }
        }
        topBar.addView(pageInput)

        // যান (Go) বাটন
        topBar.addView(Button(this).apply {
            text = "যান"
            textSize = 14f
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#FACC15"))
            setPadding(dp(8), 0, dp(8), 0)
            layoutParams = LinearLayout.LayoutParams(-2, dp(35))
            setOnClickListener {
                val p = pageInput.text.toString().toIntOrNull()
                if (p != null && p > 0) {
                    // পিডিএফ এর ইনডেক্স ০ থেকে শুরু হয়, তাই p-1 করা হয়েছে
                    pdfView.jumpTo(p - 1) 
                }
            }
        })

        root.addView(topBar)

        // --- পিডিএফ ভিউ ফ্রেম ---
        val pdfContainer = RelativeLayout(this).apply { setBackgroundColor(Color.WHITE) }
        
        pdfView = PDFView(this, null)
        pdfContainer.addView(pdfView, RelativeLayout.LayoutParams(-1, -1))

        // লোডিং স্ক্রিন (ডাউনলোডের সময় দেখাবে)
        val loadingLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#E6FFFFFF"))
        }
        
        progressBar = ProgressBar(this)
        statusText = TextView(this).apply {
            text = "ফাইল চেক করা হচ্ছে..."
            textSize = 16f
            setTextColor(Color.BLACK)
            setPadding(0, dp(20), 0, 0)
        }
        loadingLayout.addView(progressBar)
        loadingLayout.addView(statusText)
        
        pdfContainer.addView(loadingLayout, RelativeLayout.LayoutParams(-1, -1))
        root.addView(pdfContainer, LinearLayout.LayoutParams(-1, 0, 1f))
        
        setContentView(root)

        // ফাইলের নাম থেকে স্পেস ও স্পেশাল ক্যারেক্টার বাদ দিয়ে সেভ করা
        val safeFileName = bookName.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
        val file = File(filesDir, safeFileName)

        if (file.exists()) {
            // ফাইল আগে থেকেই থাকলে সরাসরি ওপেন হবে
            loadingLayout.visibility = View.GONE
            openPdf(file)
        } else {
            // ফাইল না থাকলে ইন্টারনেট থেকে ডাউনলোড হবে
            downloadAndOpenPdf(downloadUrl, file, loadingLayout)
        }
    }

    // ব্যাকগ্রাউন্ডে পিডিএফ ডাউনলোডের ফাংশন
    private fun downloadAndOpenPdf(urlStr: String, file: File, loadingLayout: LinearLayout) {
        statusText.text = "ডাউনলোড হচ্ছে, দয়া করে অপেক্ষা করুন...\n(বড় ফাইলের জন্য সময় লাগতে পারে)"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(urlStr)
                val connection = url.openConnection()
                connection.connect()

                val input = connection.getInputStream()
                val output = FileOutputStream(file)
                val data = ByteArray(4096)
                var count: Int

                while (input.read(data).also { count = it } != -1) {
                    output.write(data, 0, count)
                }

                output.flush()
                output.close()
                input.close()

                withContext(Dispatchers.Main) {
                    loadingLayout.visibility = View.GONE
                    openPdf(file)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "ডাউনলোডে সমস্যা হয়েছে। ইন্টারনেট চেক করে আবার চেষ্টা করুন।"
                    progressBar.visibility = View.GONE
                    // ডাউনলোড ফেইল হলে অসম্পূর্ণ ফাইল ডিলিট করে দেওয়া
                    if (file.exists()) file.delete()
                }
            }
        }
    }

    // পিডিএফ রিডারে ফাইল ওপেন করার ফাংশন
    private fun openPdf(file: File) {
        val sharedPref = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE)
        
        // যদি ডায়ালগ লিস্ট থেকে পেজ নম্বর আসে, তবে সেটা দিয়ে ওপেন হবে। না হলে লাস্ট পড়া পেজ।
        val startPage = if (targetPage > 0) targetPage else sharedPref.getInt(bookName, 0)

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
