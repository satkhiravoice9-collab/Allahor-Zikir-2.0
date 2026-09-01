package com.sabbirsamol.app

import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.github.barteksc.pdfviewer.PDFView
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class PdfReaderActivity : ComponentActivity() {
    companion object {
        const val EXTRA_BOOK_ID = "book_id"
        const val EXTRA_PAGE = "page"
    }

    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private var book: LibraryBook? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bookId = intent.getStringExtra(EXTRA_BOOK_ID).orEmpty()
        book = LibraryBooks.all.firstOrNull { it.id == bookId }

        if (book == null) {
            showMessage("বইটি পাওয়া যায়নি")
            return
        }

        val initialPage = (intent.getIntExtra(EXTRA_PAGE, 1) - 1).coerceAtLeast(0)
        showLoading()
        openOrDownload(book!!, initialPage)
    }

    private fun showLoading() {
        val root = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }
        root.addView(ProgressBar(this))
        status = TextView(this).apply {
            text = "PDF প্রস্তুত হচ্ছে..."
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 0)
        }
        root.addView(status)
        setContentView(root)
    }

    private fun openOrDownload(book: LibraryBook, page: Int) {
        val file = File(filesDir, "library_${book.id}.pdf")
        if (file.exists() && file.length() > 1024) {
            runOnUiThread { openPdf(file, page) }
            return
        }

        executor.execute {
            try {
                downloadDrivePdf(book.driveUrl, file) { downloaded, total ->
                    runOnUiThread {
                        status.text = if (total > 0) {
                            "ডাউনলোড হচ্ছে: ${downloaded * 100 / total}%"
                        } else {
                            "PDF ডাউনলোড হচ্ছে..."
                        }
                    }
                }
                runOnUiThread { openPdf(file, page) }
            } catch (e: Exception) {
                file.delete()
                runOnUiThread {
                    status.text = "PDF ডাউনলোড করা যায়নি"
                    AlertDialog.Builder(this)
                        .setTitle("ডাউনলোড ব্যর্থ")
                        .setMessage("Google Drive ফাইলটি সরাসরি ডাউনলোড করা যাচ্ছে না। ফাইলটি Public/Anyone with the link হিসেবে শেয়ার করা আছে কি না পরীক্ষা করুন।")
                        .setPositiveButton("ঠিক আছে", null)
                        .show()
                }
            }
        }
    }

    private fun downloadDrivePdf(
        driveUrl: String,
        destination: File,
        progress: (Long, Long) -> Unit
    ) {
        val id = Regex("/d/([^/]+)").find(driveUrl)?.groupValues?.get(1)
            ?: throw IllegalArgumentException("Invalid Google Drive URL")

        val url = URL("https://drive.usercontent.google.com/download?id=$id&export=download&confirm=t")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 30_000
            readTimeout = 60_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
        }

        try {
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("HTTP ${connection.responseCode}")
            }
            val total = connection.contentLengthLong
            BufferedInputStream(connection.inputStream).use { input ->
                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var downloaded = 0L
                    while (true) {
                        val count = input.read(buffer)
                        if (count < 0) break
                        output.write(buffer, 0, count)
                        downloaded += count
                        if (downloaded % (256 * 1024) < count) progress(downloaded, total)
                    }
                    output.flush()
                    if (destination.length() < 1024) throw IllegalStateException("Downloaded file is too small")
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun openPdf(file: File, page: Int) {
        val pdfView = PDFView(this, null)
        setContentView(pdfView)
        pdfView.fromFile(file)
            .defaultPage(page)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .enableAnnotationRendering(true)
            .spacing(4)
            .onError { error -> showMessage("PDF খোলা যায়নি: ${error.message.orEmpty()}") }
            .onPageError { pageNumber, error -> showMessage("পৃষ্ঠা ${pageNumber + 1} খোলা যায়নি") }
            .load()
    }

    private fun showMessage(message: String) {
        val text = TextView(this).apply {
            this.text = message
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }
        setContentView(text)
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
