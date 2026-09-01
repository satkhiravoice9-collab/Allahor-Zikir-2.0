package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class PdfBookItem(val fileName: String, val title: String, val id: String)

class LibraryActivity : ComponentActivity() {
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২৩৪৫৬৭৮৯"[it - '0'] }.joinToString("")

    // কালার প্যালেট
    private val bgMain = Color.parseColor("#091C14")
    private val cardBg = Color.parseColor("#114D3C")
    private val cardStroke = Color.parseColor("#1B785B")
    private val textYellow = Color.parseColor("#FBBF24")
    private val btnYellow = Color.parseColor("#FACC15")
    private val btnDark = Color.parseColor("#0C291F")
    private val btnRed = Color.parseColor("#DC2626")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    private fun getSafeFileName(title: String) = title.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
    private fun isFileExists(title: String) = File(filesDir, getSafeFileName(title)).exists()
    private fun deleteFileSilent(title: String) {
        val file = File(filesDir, getSafeFileName(title))
        if (file.exists()) file.delete()
        Toast.makeText(this, "ফাইলটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
    }

    // --- বইয়ের ডাটাবেস (সম্পূর্ণ) ---
    private val quran = PdfBookItem("quran_full.pdf", "পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)", "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")

    private val bukhariVolList = listOf(
        PdfBookItem("bukhari_1.pdf", "সহীহ বুখারী ১ম খণ্ড", "1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"),
        PdfBookItem("bukhari_2.pdf", "সহীহ বুখারী ২য় খণ্ড", "1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"),
        PdfBookItem("bukhari_3.pdf", "সহীহ বুখারী ৩য় খণ্ড", "14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR"),
        PdfBookItem("bukhari_4.pdf", "সহীহ বুখারী ৪র্থ খণ্ড", "1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd"),
        PdfBookItem("bukhari_5.pdf", "সহীহ বুখারী ৫ম খণ্ড", "1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX"),
        PdfBookItem("bukhari_6.pdf", "সহীহ বুখারী ৬ষ্ঠ খণ্ড", "1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb"),
        PdfBookItem("bukhari_7.pdf", "সহীহ বুখারী ৭ম খণ্ড", "1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9"),
        PdfBookItem("bukhari_8.pdf", "সহীহ বুখারী ৮ম খণ্ড", "1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA"),
        PdfBookItem("bukhari_9.pdf", "সহীহ বুখারী ৯ম খণ্ড", "1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I"),
        PdfBookItem("bukhari_10.pdf", "সহীহ বুখারী ১০ম খণ্ড", "1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8")
    )

    private val muslimVolList = listOf(
        PdfBookItem("muslim_1.pdf", "সহীহ মুসলিম ১ম খণ্ড", "1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"),
        PdfBookItem("muslim_2.pdf", "সহীহ মুসলিম ২য় খণ্ড", "1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG"),
        PdfBookItem("muslim_3.pdf", "সহীহ মুসলিম ৩য় খণ্ড", "11Ul0Laj9YGGV37KYnep73RCUELz0yvU8"),
        PdfBookItem("muslim_4.pdf", "সহীহ মুসলিম ৪র্থ খণ্ড", "16cTetFECwjEPtSHeajv_WTQ11LTATtJp"),
        PdfBookItem("muslim_5.pdf", "সহীহ মুসলিম ৫ম খণ্ড", "129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z"),
        PdfBookItem("muslim_6.pdf", "সহীহ মুসলিম ৬ষ্ঠ খণ্ড", "1o4mAG-Qx6KSumsohKgUK2k8S7TAy3PKp"),
        PdfBookItem("muslim_7.pdf", "সহীহ মুসলিম ৭ম খণ্ড", "121hx1VQv0HCZnztDFrhJrP4XPBuEzuQW"),
        PdfBookItem("muslim_8.pdf", "সহীহ মুসলিম ৮ম খণ্ড", "1mjTLc_svuuKcUlnQXRu0e3PEAZUzn7_u")
    )

    private val abuDaudVolList = listOf(
        PdfBookItem("abudaud_1.pdf", "সুনান আবু দাউদ ১ম খণ্ড", "15RGRxSsJeKSfITiCU20AOm477jrXf7tX"),
        PdfBookItem("abudaud_2.pdf", "সুনান আবু দাউদ ২য় খণ্ড", "1LLjLCwj-CXLv6RHaTSHpfKK4zPBj6mRn"),
        PdfBookItem("abudaud_3.pdf", "সুনান আবু দাউদ ৩য় খণ্ড", "1KD64uPxeVDliawvEKwOFNaNfH883rR6X"),
        PdfBookItem("abudaud_4.pdf", "সুনান আবু দাউদ ৪র্থ খণ্ড", "1iuOGcHAegMxqJ3VZ7HiLNXTbz8a-iooI")
    )

    private val tirmidhiVolList = listOf(
        PdfBookItem("tirmidhi_1.pdf", "জামে আত-তিরমিযী ১ম খণ্ড", "1tN-N6skALr_G83cXreKmo0mRIqW_siGx"),
        PdfBookItem("tirmidhi_2.pdf", "জামে আত-তিরমিযী ২য় খণ্ড", "1FCwMfqmzNHHrbfQr3ho502obdHAaSGr4"),
        PdfBookItem("tirmidhi_3.pdf", "জামে আত-তিরমিযী ৩য় খণ্ড", "1Z5FxEDR_dcDFVgWTcx5lxfijVUSAxjcN"),
        PdfBookItem("tirmidhi_4.pdf", "জামে আত-তিরমিযী ৪র্থ খণ্ড", "1yIiNsyBpYnJV5y8gnWAor23VwKgpD4N6"),
        PdfBookItem("tirmidhi_5.pdf", "জামে আত-তিরমিযী ৫ম খণ্ড", "18GvNtMs_VK9fb1_oCyHR4nWwz-_6W53h"),
        PdfBookItem("tirmidhi_6.pdf", "জামে আত-তিরমিযী ৬ষ্ঠ খণ্ড", "1D1dnryvH8iOY69ixBtGPQuF3_kmPRgu_")
    )

    private val nasaiVolList = listOf(
        PdfBookItem("nasai_1.pdf", "সুনান আন-নাসায়ী ১ম খণ্ড", "1VH9AkmVv3apCXYPBRu0mI7qmNsU66sZZ"),
        PdfBookItem("nasai_2.pdf", "সুনান আন-নাসায়ী ২য় খণ্ড", "1t_F71oekP3F1Dc2SDY2I-L5eyDgtPcIR"),
        PdfBookItem("nasai_3.pdf", "সুনান আন-নাসায়ী ৩য় খণ্ড", "1mjp-ZSvvBsLzkDRXj87oSrH88ifzLIKF"),
        PdfBookItem("nasai_4.pdf", "সুনান আন-নাসায়ী ৪র্থ খণ্ড", "19yY0S3jLumKpxff3n_Kf6qoX8A6Fmwqi")
    )

    private val ibnMajahVolList = listOf(
        PdfBookItem("ibnmajah_1.pdf", "সুনান ইবনে মাজাহ ১ম খণ্ড", "12nAeV_DjOOCK2WJmrFSSIPyf2fcAyWzd"),
        PdfBookItem("ibnmajah_2.pdf", "সুনান ইবনে মাজাহ ২য় খণ্ড", "1B9ZNJrMmTW1sPXtAX3n2VZj4j4xjG-U6"),
        PdfBookItem("ibnmajah_3.pdf", "সুনান ইবনে মাজাহ ৩য় খণ্ড", "1GVexhALQ3ISCd241Zj7PkgpZG-116Q59")
    )

    private val groups = listOf(
        "সহীহ বুখারী শরীফ" to bukhariVolList,
        "সহীহ মুসলিম শরীফ" to muslimVolList,
        "সুনান আবু দাউদ" to abuDaudVolList,
        "জামে আত-তিরমিযী" to tirmidhiVolList,
        "সুনান আন-নাসায়ী" to nasaiVolList,
        "সুনান ইবনে মাজাহ" to ibnMajahVolList
    )

    // কোরআন পারা সূচিপত্র
    private val paraList = listOf(
        Pair("পারা ১: আলিফ লাম মীম (الم) (পৃষ্ঠা ২)", 2), Pair("পারা ২: সায়াকুল (سَيَقُولُ) (পৃষ্ঠা ২২)", 22),
        Pair("পারা ৩: তিলকার রুসুল (تِلْكَ الرُّسُلُ) (পৃষ্ঠা ৪২)", 42), Pair("পারা ৪: লান তানালু (لَنْ تَنَالُوا) (পৃষ্ঠা ৬২)", 62),
        Pair("পারা ৫: ওয়াল মুহসানাতু (وَالْمُحْصَنَاتُ) (পৃষ্ঠা ৮২)", 82), Pair("পারা ৬: লা ইয়ুহিব্বুল্লাহ (لَا يُحِبُّ اللَّهُ) (পৃষ্ঠা ১০২)", 102),
        Pair("পারা ৭: ওয়া ইযা সামিউ (وَإِذَا سَمِعُوا) (পৃষ্ঠা ১২২)", 122), Pair("পারা ৮: ওয়া লাউ আন্নানা (وَلَوْ أَنَّنَا) (পৃষ্ঠা ১৪২)", 142),
        Pair("পারা ৯: ক্বালাল মালাউ (قَالَ الْمَلَأُ) (পৃষ্ঠা ১৬২)", 162), Pair("পারা ১০: ওয়া'লামু (وَاعْلَمُوا) (পৃষ্ঠা ১৮২)", 182),
        Pair("পারা ২৯: তাবারাকাল্লাযী (تَبَارَكَ الَّذِي) (পৃষ্ঠা ৫৬২)", 562), Pair("পারা ৩০: আম্মা ইয়াতাসায়ালুন (عَمَّ يَتَسَاءَلُونَ) (পৃষ্ঠা ৫৮২)", 582)
    )

    private val surahList = listOf(
        Pair("১. সূরা আল-ফাতিহা (পৃষ্ঠা ৩)", 3), Pair("২. সূরা আল-বাকারাহ (পৃষ্ঠা ৪)", 4),
        Pair("৩. সূরা আলে-ইমরান (পৃষ্ঠা ৫২)", 52), Pair("৪. সূরা আন-নিসা (পৃষ্ঠা ৭৯)", 79)
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showLibrary() }
    override fun onResume() { super.onResume(); showLibrary() }

    private fun showLibrary() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(80)) }

        content.addView(TextView(this).apply { text = "📚 ইসলামিক লাইব্রেরী ও কিতাব ভাণ্ডার"; setTextColor(textYellow); textSize = 20f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        
        // কোরআন কার্ড
        val qCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
        qCard.addView(TextView(this).apply { text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ)"; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        if (isFileExists(quran.title)) {
            val qBtnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(0, dp(10), 0, dp(10)) }
            qBtnRow.addView(Button(this).apply { text = "📖 ৩০ পারা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }; setOnClickListener { showListDialog("৩০ পারা সূচিপত্র", paraList, quran) } })
            qBtnRow.addView(Button(this).apply { text = "📜 ১১৪ সুরা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#D97706")); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }; setOnClickListener { showListDialog("১১৪ সুরা সূচিপত্র", surahList, quran) } })
            qCard.addView(qBtnRow)
            
            val continueLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val savedPage = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE).getInt(quran.title, 0)
            continueLayout.addView(TextView(this).apply { text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage + 1)})"; setTextColor(Color.WHITE); textSize = 14f; layoutParams = LinearLayout.LayoutParams(0, -2, 1f); setOnClickListener { openPdf(quran, 0) } })
            continueLayout.addView(Button(this).apply { text = "🗑️"; setTextColor(Color.WHITE); background = getBtnDrawable(btnRed); layoutParams = LinearLayout.LayoutParams(dp(40), dp(35)); setOnClickListener { deleteFileSilent(quran.title); showLibrary() } })
            qCard.addView(continueLayout)
        } else {
            val dlBtn = Button(this).apply { text = "📥 কোরআন ডাউনলোড করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(10) } }
            dlBtn.setOnClickListener { startDownload(quran, dlBtn) { showLibrary() } }
            qCard.addView(dlBtn)
        }
        content.addView(qCard)

        // হাদিস লিস্ট
        content.addView(TextView(this).apply { text = "📂 সিহাহ সিত্তাহ হাদিস কিতাবসমূহ:"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, dp(6), 0, dp(12)) })

        groups.forEachIndexed { index, (title, books) ->
            val hCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
            hCard.addView(TextView(this).apply { text = "${bn(index + 1)}. $title (${bn(books.size)} খণ্ড)"; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD) })
            hCard.addView(Button(this).apply { text = "খুলুন ➔"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { topMargin = dp(10) }; setOnClickListener { showVolumes(title, books, index + 1) } })
            content.addView(hCard)
        }

        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    // খণ্ড লিস্ট (ডাউনলোড ও ডিলিট অপশনসহ)
    private fun showVolumes(title: String, books: List<PdfBookItem>, idxGroup: Int) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12)); background = GradientDrawable().apply { setColor(cardBg) } }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(Color.WHITE); setPadding(dp(10),0,dp(10),0); setOnClickListener { showLibrary() } })
        top.addView(TextView(this).apply { text = "📂 ${bn(idxGroup)}. $title"; textSize = 18f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        books.forEach { book ->
            val vCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
            vCard.addView(TextView(this).apply { text = book.title; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
            
            val actionRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            
            if (isFileExists(book.title)) {
                val readBtn = Button(this).apply { text = "📖 পড়ুন"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(8) }; setOnClickListener { openPdf(book, 0) } }
                val delBtn = Button(this).apply { text = "🗑️"; setTextColor(Color.WHITE); background = getBtnDrawable(btnRed); layoutParams = LinearLayout.LayoutParams(dp(45), dp(40)); setOnClickListener { deleteFileSilent(book.title); showVolumes(title, books, idxGroup) } }
                actionRow.addView(readBtn); actionRow.addView(delBtn)
            } else {
                val dlBtn = Button(this).apply { text = "📥 ডাউনলোড করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(40)) }
                dlBtn.setOnClickListener { startDownload(book, dlBtn) { showVolumes(title, books, idxGroup) } }
                actionRow.addView(dlBtn)
            }
            vCard.addView(actionRow)
            list.addView(vCard)
        }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    // ব্যাকগ্রাউন্ড ডাউনলোড ফাংশন
    private fun startDownload(book: PdfBookItem, btn: Button, onComplete: () -> Unit) {
        btn.text = "⏳ ডাউনলোড হচ্ছে... অনুগ্রহ করে অপেক্ষা করুন"
        btn.isEnabled = false
        val file = File(filesDir, getSafeFileName(book.title))
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://drive.google.com/uc?export=download&id=${book.id}")
                val connection = url.openConnection()
                connection.connect()
                val input = connection.getInputStream()
                val output = FileOutputStream(file)
                val data = ByteArray(4096)
                var count: Int
                while (input.read(data).also { count = it } != -1) { output.write(data, 0, count) }
                output.flush(); output.close(); input.close()
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btn.text = "❌ ব্যর্থ হয়েছে! ইন্টারনেট চেক করে আবার চাপুন"
                    btn.isEnabled = true
                    if (file.exists()) file.delete()
                }
            }
        }
    }

    private fun showListDialog(titleText: String, items: List<Pair<String, Int>>, book: PdfBookItem) {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE) }
        dialogLayout.addView(TextView(this).apply { text = titleText; textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(Color.parseColor("#F3F4F6")) })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        items.forEach { item ->
            list.addView(TextView(this).apply {
                text = item.first; textSize = 18f; setPadding(0, dp(12), 0, dp(12)); setTextColor(Color.BLACK)
                setOnClickListener { openPdf(book, item.second) }
            })
            list.addView(View(this).apply { setBackgroundColor(Color.parseColor("#E5E7EB")); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) })
        }
        scroll.addView(list)
        dialogLayout.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()
        dialogLayout.addView(Button(this).apply { text = "বন্ধ করুন"; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#E5E7EB")); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(16)) }; setOnClickListener { dialog.dismiss() } })
        dialog.show()
    }

    private fun openPdf(book: PdfBookItem, page: Int) {
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title)
        intent.putExtra("DOWNLOAD_URL", "https://drive.google.com/uc?export=download&id=${book.id}")
        intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }
}
