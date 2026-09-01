package com.sabbirsamol.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

class LibraryActivity : ComponentActivity() {
    private val green = Color.rgb(20, 83, 45)
    private val lightGreen = Color.rgb(245, 250, 247)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibrary()
    }

    private fun showLibrary() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(lightGreen)
            setPadding(20, 24, 20, 24)
        }

        root.addView(TextView(this).apply {
            text = "📚 ইসলামিক লাইব্রেরি"
            textSize = 25f
            setTextColor(green)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "কুরআন ও হাদিসের কিতাব"
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 18)
        })

        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        addQuranSection(list)
        addSectionTitle(list, "সহিহ বুখারী শরীফ — ১০ খণ্ড")
        addBooks(list, "bukhari-")
        addSectionTitle(list, "সহিহ মুসলিম শরীফ — ৮ খণ্ড")
        addBooks(list, "muslim-")
        addSectionTitle(list, "আবু দাউদ শরীফ — ৪ খণ্ড")
        addBooks(list, "abu-dawud-")
        addSectionTitle(list, "তিরমিজি শরীফ — ৬ খণ্ড")
        addBooks(list, "tirmidhi-")
        addSectionTitle(list, "নাসাঈ শরীফ — ৪ খণ্ড")
        addBooks(list, "nasai-")
        addSectionTitle(list, "ইবনে মাজাহ শরীফ — ৩ খণ্ড")
        addBooks(list, "ibn-majah-")

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun addQuranSection(parent: LinearLayout) {
        addBookCard(parent, LibraryBooks.quran, true)
        addSectionTitle(parent, "কুরআন শরীফ — সূরা ও পারা")

        addNavigationGroup(parent, "সূরা তালিকা", QuranNavigation.surahs)
        addNavigationGroup(parent, "পারা তালিকা", QuranNavigation.paras)
    }

    private fun addNavigationGroup(
        parent: LinearLayout,
        title: String,
        items: List<QuranNavigationItem>
    ) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(green)
            setPadding(8, 8, 8, 6)
        })
        items.forEach { item ->
            parent.addView(Button(this).apply {
                text = "${item.title} — পৃষ্ঠা ${item.page}"
                setOnClickListener {
                    startActivity(Intent(this@LibraryActivity, PdfReaderActivity::class.java)
                        .putExtra(PdfReaderActivity.EXTRA_BOOK_ID, LibraryBooks.quran.id)
                        .putExtra(PdfReaderActivity.EXTRA_PAGE, item.page))
                }
            })
        }
    }

    private fun addBooks(parent: LinearLayout, prefix: String) {
        LibraryBooks.hadithBooks.filter { it.id.startsWith(prefix) }
            .forEach { addBookCard(parent, it, false) }
    }

    private fun addSectionTitle(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title
            textSize = 18f
            setTextColor(green)
            setPadding(4, 22, 4, 10)
        })
    }

    private fun addBookCard(parent: LinearLayout, book: LibraryBook, quran: Boolean) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(18, 14, 12, 14)
            setBackgroundColor(Color.WHITE)
        }
        val text = TextView(this).apply {
            text = if (quran) "📖 ${book.title}" else "📕 ${book.title}\n${book.volume.orEmpty()}"
            textSize = if (quran) 19f else 17f
            setTextColor(Color.rgb(30, 30, 30))
        }
        val button = Button(this).apply {
            text = "পড়ুন"
            setOnClickListener {
                startActivity(Intent(this@LibraryActivity, PdfReaderActivity::class.java)
                    .putExtra(PdfReaderActivity.EXTRA_BOOK_ID, book.id))
            }
        }
        card.addView(text, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(button, LinearLayout.LayoutParams(-2, -2))
        parent.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 8 })
    }
}
