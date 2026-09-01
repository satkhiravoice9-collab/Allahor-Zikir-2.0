package com.sabbirsamol.app

import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.activity.ComponentActivity

/**
 * PDF reader entry point. The actual offline download, PDF rendering,
 * bookmarks, page search and Quran navigation are added in the next step.
 */
class PdfReaderActivity : ComponentActivity() {

    companion object {
        const val EXTRA_BOOK_ID = "book_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookId = intent.getStringExtra(EXTRA_BOOK_ID).orEmpty()
        val book = LibraryBooks.all.firstOrNull { it.id == bookId }

        val text = TextView(this).apply {
            text = if (book == null) {
                "বইটি পাওয়া যায়নি"
            } else {
                "${book.title}\n${book.volume.orEmpty()}\n\nPDF Reader প্রস্তুত হচ্ছে..."
            }
            textSize = 19f
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
        }

        setContentView(text)
    }
}
