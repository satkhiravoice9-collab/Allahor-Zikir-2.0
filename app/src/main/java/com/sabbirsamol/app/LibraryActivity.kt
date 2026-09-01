package com.sabbirsamol.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

data class PdfBookItem(val fileName: String, val title: String, val id: String)

class LibraryActivity : ComponentActivity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun bg(color: Int, radius: Int = 18) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private val quran = PdfBookItem(
        "quran_full.pdf",
        "পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা)",
        "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q"
    )

    private val bukhariVolList = listOf(
        PdfBookItem("bukhari_vol_1.pdf", "সহীহ বুখারী ১ম খণ্ড", "1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"),
        PdfBookItem("bukhari_vol_2.pdf", "সহীহ বুখারী ২য় খণ্ড", "1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"),
        PdfBookItem("bukhari_vol_3.pdf", "সহীহ বুখারী ৩য় খণ্ড", "14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR"),
        PdfBookItem("bukhari_vol_4.pdf", "সহীহ বুখারী ৪র্থ খণ্ড", "1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd"),
        PdfBookItem("bukhari_vol_5.pdf", "সহীহ বুখারী ৫ম খণ্ড", "1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX"),
        PdfBookItem("bukhari_vol_6.pdf", "সহীহ বুখারী ৬ষ্ঠ খণ্ড", "1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb"),
        PdfBookItem("bukhari_vol_7.pdf", "সহীহ বুখারী ৭ম খণ্ড", "1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9"),
        PdfBookItem("bukhari_vol_8.pdf", "সহীহ বুখারী ৮ম খণ্ড", "1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA"),
        PdfBookItem("bukhari_vol_9.pdf", "সহীহ বুখারী ৯ম খণ্ড", "1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I"),
        PdfBookItem("bukhari_vol_10.pdf", "সহীহ বুখারী ১০ম খণ্ড", "1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8")
    )

    private val muslimVolList = listOf(
        PdfBookItem("muslim_vol_1.pdf", "সহীহ মুসলিম ১ম খণ্ড", "1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"),
        PdfBookItem("muslim_vol_2.pdf", "সহীহ মুসলিম ২য় খণ্ড", "1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG"),
        PdfBookItem("muslim_vol_3.pdf", "সহীহ মুসলিম ৩য় খণ্ড", "11Ul0Laj9YGGV37KYnep73RCUELz0yvU8"),
        PdfBookItem("muslim_vol_4.pdf", "সহীহ মুসলিম ৪র্থ খণ্ড", "16cTetFECwjEPtSHeajv_WTQ11LTATtJp"),
        PdfBookItem("muslim_vol_5.pdf", "সহীহ مسلم ৫ম খণ্ড", "129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z"),
        PdfBookItem("muslim_vol_6.pdf", "সহীহ মুসলিম ৬ষ্ঠ খণ্ড", "1o4mAG-Qx6KSumsohKgUK2k8S7TAy3PKp"),
        PdfBookItem("muslim_vol_7.pdf", "সহীহ মুসলিম ৭ম খণ্ড", "121hx1VQv0HCZnztDFrhJrP4XPBuEzuQW"),
        PdfBookItem("muslim_vol_8.pdf", "সহীহ মুসলিম ৮ম খণ্ড", "1mjTLc_svuuKcUlnQXRu0e3PEAZUzn7_u")
    )

    private val abuDaudVolList = listOf(
        PdfBookItem("abudaud_vol_1.pdf", "সুনান আবু দাউদ ১ম খণ্ড", "15RGRxSsJeKSfITiCU20AOm477jrXf7tX"),
        PdfBookItem("abudaud_vol_2.pdf", "সুনান আবু দাউদ ২য় খণ্ড", "1LLjLCwj-CXLv6RHaTSHpfKK4zPBj6mRn"),
        PdfBookItem("abudaud_vol_3.pdf", "সুনান আবু দাউদ ৩য় খণ্ড", "1KD64uPxeVDliawvEKwOFNaNfH883rR6X"),
        PdfBookItem("abudaud_vol_4.pdf", "সুনান আবু দাউদ ৪র্থ খণ্ড", "1iuOGcHAegMxqJ3VZ7HiLNXTbz8a-iooI")
    )

    private val tirmidhiVolList = listOf(
        PdfBookItem("tirmidhi_vol_1.pdf", "জামে আত-তিরমিযী ১ম খণ্ড", "1tN-N6skALr_G83cXreKmo0mRIqW_siGx"),
        PdfBookItem("tirmidhi_vol_2.pdf", "জামে আত-তিরমিযী ২য় খণ্ড", "1FCwMfqmzNHHrbfQr3ho502obdHAaSGr4"),
        PdfBookItem("tirmidhi_vol_3.pdf", "জামে আত-তিরমিযী ৩য় খণ্ড", "1Z5FxEDR_dcDFVgWTcx5lxfijVUSAxjcN"),
        PdfBookItem("tirmidhi_vol_4.pdf", "জামে আত-তিরমিযী ৪র্থ খণ্ড", "1yIiNsyBpYnJV5y8gnWAor23VwKgpD4N6"),
        PdfBookItem("tirmidhi_vol_5.pdf", "জামে আত-তিরমিযী ৫ম খণ্ড", "18GvNtMs_VK9fb1_oCyHR4nWwz-_6W53h"),
        PdfBookItem("tirmidhi_vol_6.pdf", "জামে আত-তিরমিযী ৬ষ্ঠ খণ্ড", "1D1dnryvH8iOY69ixBtGPQuF3_kmPRgu_")
    )

    private val nasaiVolList = listOf(
        PdfBookItem("nasai_vol_1.pdf", "সুনান আন-নাসায়ী ১ম খণ্ড", "1VH9AkmVv3apCXYPBRu0mI7qmNsU66sZZ"),
        PdfBookItem("nasai_vol_2.pdf", "সুনান আন-নাসায়ী ২য় খণ্ড", "1t_F71oekP3F1Dc2SDY2I-L5eyDgtPcIR"),
        PdfBookItem("nasai_vol_3.pdf", "সুনান আন-নাসায়ী ৩য় খণ্ড", "1mjp-ZSvvBsLzkDRXj87oSrH88ifzLIKF"),
        PdfBookItem("nasai_vol_4.pdf", "সুনান আন-নাসায়ী ৪র্থ খণ্ড", "19yY0S3jLumKpxff3n_Kf6qoX8A6Fmwqi")
    )

    private val ibnMajahVolList = listOf(
        PdfBookItem("ibnmajah_vol_1.pdf", "সুনান ইবনে মাজাহ ১ম খণ্ড", "12nAeV_DjOOCK2WJmrFSSIPyf2fcAyWzd"),
        PdfBookItem("ibnmajah_vol_2.pdf", "সুনান ইবনে মাজাহ ২য় খণ্ড", "1B9ZNJrMmTW1sPXtAX3n2VZj4j4xjG-U6"),
        PdfBookItem("ibnmajah_vol_3.pdf", "সুনান ইবনে মাজাহ ৩য় খণ্ড", "1GVexhALQ3ISCd241Zj7PkgpZG-116Q59")
    )

    private val groups = listOf(
        "সহীহ বুখারী" to bukhariVolList,
        "সহীহ মুসলিম" to muslimVolList,
        "সুনান আবু দাউদ" to abuDaudVolList,
        "জামে তিরমিযী" to tirmidhiVolList,
        "সুনান নাসায়ী" to nasaiVolList,
        "সুনান ইবনে মাজাহ" to ibnMajahVolList
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibrary()
    }

    private fun showLibrary() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 250, 247))
        }
        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = bg(Color.rgb(16, 83, 54), 0)
        }
        top.addView(Button(this).apply {
            text = "←"
            textSize = 20f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(54), dp(50)))
        top.addView(TextView(this).apply {
            text = "📚 ইসলামিক লাইব্রেরি"
            textSize = 20f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, dp(50), 1f))
        root.addView(top)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        list.addView(bookButton("📖 ${quran.title}") { open(quran) })
        groups.forEach { (title, books) ->
            list.addView(bookButton("📚 $title • ${books.size} খণ্ড") { showVolumes(title, books) })
        }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun bookButton(textValue: String, action: () -> Unit): TextView = TextView(this).apply {
        text = textValue
        textSize = 18f
        setTextColor(Color.rgb(20, 55, 40))
        setTypeface(null, Typeface.BOLD)
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(16), dp(14), dp(16), dp(14))
        background = bg(Color.WHITE, 16)
        setOnClickListener { action() }
        layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 0, 0, dp(10)) }
    }

    private fun showVolumes(title: String, books: List<PdfBookItem>) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(248, 250, 247))
            setPadding(dp(10), dp(10), dp(10), dp(10))
        }
        root.addView(TextView(this).apply {
            text = title
            textSize = 22f
            setTypeface(null, Typeface.BOLD)
        })
        root.addView(Button(this).apply {
            text = "← লাইব্রেরিতে ফিরে যান"
            setOnClickListener { showLibrary() }
        })
        books.forEach { root.addView(bookButton(it.title) { open(it) }) }
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun open(book: PdfBookItem) {
        val downloadUrl = "https://drive.google.com/uc?export=download&id=${book.id}"
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title) // ফাইলের নামের জন্য
        intent.putExtra("DOWNLOAD_URL", downloadUrl) // ডাউনলোডের জন্য
        startActivity(intent)
    }
}