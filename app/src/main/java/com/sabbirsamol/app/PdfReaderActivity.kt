package com.sabbirsamol.app

import android.content.Context
import android.graphics.Color
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookName = intent.getStringExtra("BOOK_NAME") ?: "Book"
        val downloadUrl = intent.getStringExtra("DOWNLOAD_URL") ?: ""

        val root = RelativeLayout(this).apply { setBackgroundColor(Color.WHITE) }
        
        pdfView = PDFView(this, null)
        root.addView(pdfView, RelativeLayout.LayoutParams(-1, -1))

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
            setPadding(0, 20, 0, 0)
        }
        loadingLayout.addView(progressBar)
        loadingLayout.addView(statusText)
        
        root.addView(loadingLayout, RelativeLayout.LayoutParams(-1, -1))
        setContentView(root)

        val fileName = "$bookName.pdf"
        val file = File(filesDir, fileName)

        if (file.exists()) {
            loadingLayout.visibility = View.GONE
            openPdf(file)
        } else {
            downloadAndOpenPdf(downloadUrl, file, loadingLayout)
        }
    }

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
                }
            }
        }
    }

    private fun openPdf(file: File) {
        val sharedPref = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE)
        val lastSavedPage = sharedPref.getInt(bookName, 0)

        pdfView.fromFile(file)
            .defaultPage(lastSavedPage)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .onPageChange { page, _ ->
                sharedPref.edit().putInt(bookName, page).apply()
            }
            .enableDoubletap(true)
            .load()
    }
}
