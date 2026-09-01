package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

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
    private val btnOrange = Color.parseColor("#D97706")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }

    private val quran = PdfBookItem("quran_full.pdf", "পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)", "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")

    // --- সূচিপত্রের ডেটা (পারা এবং সুরা) ---
    private val paraList = listOf(
        Pair("পারা ১: আলিফ লাম মীম (الم) (পৃষ্ঠা ৩)", 3), Pair("পারা ২: সায়াকুল (سَيَقُولُ) (পৃষ্ঠা ২৪)", 24),
        Pair("পারা ৩: তিলকার রুসুল (تِلْكَ الرُّسُلُ) (পৃষ্ঠা ৪৪)", 44), Pair("পারা ৪: লান তানালু (لَنْ تَنَالُوا) (পৃষ্ঠা ৬৪)", 64),
        Pair("পারা ৫: ওয়াল মুহসানাতু (وَالْمُحْصَنَاتُ) (পৃষ্ঠা ৮৪)", 84), Pair("পারা ৬: লা ইয়ুহিব্বুল্লাহ (لَا يُحِبُّ اللَّهُ) (পৃষ্ঠা ১০৪)", 104),
        Pair("পারা ৭: ওয়া ইযা সামিউ (وَإِذَا سَمِعُوا) (পৃষ্ঠা ১২৪)", 124), Pair("পারা ৮: ওয়া লাউ আন্নানা (وَلَوْ أَنَّنَا) (পৃষ্ঠা ১৪৪)", 144),
        Pair("পারা ৯: ক্বালাল মালাউ (قَالَ الْمَلَأُ) (পৃষ্ঠা ১৬৪)", 164), Pair("পারা ১০: ওয়া'লামু (وَاعْلَمُوا) (পৃষ্ঠা ১৮৪)", 184),
        Pair("পারা ১১: ইয়া'তাযিরুন (يَعْتَذِرُونَ) (পৃষ্ঠা ২০৪)", 204), Pair("পারা ১২: ওয়া মা মিন দা-ব্বাহ (وَمَا مِنْ دَابَّةٍ) (পৃষ্ঠা ২২৪)", 224),
        Pair("পারা ১৩: ওয়া মা উবাররিউ (وَمَا أُبَرِّئُ) (পৃষ্ঠা ২৪৪)", 244), Pair("পারা ১৪: রুবামা (رُبَمَا) (পৃষ্ঠা ২৬৪)", 264),
        Pair("পারা ১৫: সুবহানাল্লাযী (سُبْحَانَ الَّذِي) (পৃষ্ঠা ২৮৪)", 284), Pair("পারা ১৬: ক্বালা আলাম (قَالَ أَلَمْ) (পৃষ্ঠা ৩০৪)", 304),
        Pair("পারা ১৭: ইক্বতারা বা লিন্নাস (اقْتَرَبَ لِلنَّاسِ) (পৃষ্ঠা ৩২৪)", 324), Pair("পারা ১৮: ক্বাদ আফলাহা (قَدْ أَفْلَحَ) (পৃষ্ঠা ৩৪৪)", 344),
        Pair("পারা ১৯: ওয়া ক্বাল্লাযীনা (وَقَالَ الَّذِينَ) (পৃষ্ঠা ৩৬৪)", 364), Pair("পারা ২০: আম্মান খালাক্বা (أَمَّنْ خَلَقَ) (পৃষ্ঠা ৩৮৪)", 384),
        Pair("পারা ২১: উতলু মা উহিয়া (اتْلُ مَا أُوحِيَ) (পৃষ্ঠা ৪০৪)", 404), Pair("পারা ২২: ওয়া মাই-য়াক্বনুত (وَمَنْ يَقْنُتْ) (পৃষ্ঠা ৪২৪)", 424),
        Pair("পারা ২৩: ওয়া মালি-য়া (وَمَا لِيَ) (পৃষ্ঠা ৪৪৪)", 444), Pair("পারা ২৪: ফামান আজলামু (فَمَنْ أَظْلَمُ) (পৃষ্ঠা ৪৬৪)", 464),
        Pair("পারা ২৫: ইলাইহি ইউরাদ্দু (إِلَيْهِ يُرَدُّ) (পৃষ্ঠা ৪৮৪)", 484), Pair("পারা ২৬: হা-মীম (حم) (পৃষ্ঠা ৫০৪)", 504),
        Pair("পারা ২৭: ক্বালা ফামা খাতবুকুম (قَالَ فَمَا خَطْبُكُمْ) (পৃষ্ঠা ৫২৪)", 524), Pair("পারা ২৮: ক্বাদ সামিআল্লাহ (قَدْ سَمِعَ اللَّهُ) (পৃষ্ঠা ৫৪৪)", 544),
        Pair("পারা ২৯: তাবারাকাল্লাযী (تَبَارَكَ الَّذِي) (পৃষ্ঠা ৫৬৪)", 564), Pair("পারা ৩০: আম্মা ইয়াতাসায়ালুন (عَمَّ يَتَسَاءَلُونَ) (পৃষ্ঠা ৫৮৪)", 584)
    )

    private val surahList = listOf(
        Pair("১. সূরা আল-ফাতিহা (পৃষ্ঠা ৩)", 3), Pair("২. সূরা আল-বাকারাহ (পৃষ্ঠা ৪)", 4),
        Pair("৩. সূরা আলে-ইমরান (পৃষ্ঠা ৫২)", 52), Pair("৪. সূরা আন-নিসা (পৃষ্ঠা ৭৯)", 79),
        Pair("৫. সূরা আল-মায়িদাহ (পৃষ্ঠা ১০৮)", 108), Pair("৬. সূরা আল-আনআম (পৃষ্ঠা ১৩০)", 130),
        Pair("৭. সূরা আল-আরাফ (পৃষ্ঠা ১৫৩)", 153), Pair("৮. সূরা আল-আনফাল (পৃষ্ঠা ১৭৯)", 179),
        Pair("৯. সূরা আত-তাওবাহ (পৃষ্ঠা ১৮৯)", 189), Pair("১০. সূরা ইউনুস (পৃষ্ঠা ২১০)", 210),
        Pair("১১. সূরা হুদ (পৃষ্ঠা ২২৩)", 223), Pair("১২. সূরা ইউসুফ (পৃষ্ঠা ২৩৭)", 237),
        Pair("১১৪. সূরা আন-নাস (পৃষ্ঠা ৬৩২)", 632) // আপনি চাইলে বাকিগুলো এই প্যাটার্নে অ্যাড করতে পারেন
    )

    private val bukhariVolList = listOf(
        PdfBookItem("bukhari_vol_1.pdf", "সহীহ বুখারী ১ম খণ্ড", "1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"),
        PdfBookItem("bukhari_vol_2.pdf", "সহীহ বুখারী ২য় খণ্ড", "1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"),
        PdfBookItem("bukhari_vol_3.pdf", "সহীহ বুখারী ৩য় খণ্ড", "14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR")
    )
    private val muslimVolList = listOf(
        PdfBookItem("muslim_vol_1.pdf", "সহীহ মুসলিম ১ম খণ্ড", "1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"),
        PdfBookItem("muslim_vol_2.pdf", "সহীহ মুসলিম ২য় খণ্ড", "1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG")
    )

    private val groups = listOf(
        "সহীহ বুখারী শরীফ" to bukhariVolList,
        "সহীহ মুসলিম শরীফ" to muslimVolList
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showLibrary() }
    override fun onResume() { super.onResume(); showLibrary() }

    private fun showLibrary() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(80)) }

        content.addView(TextView(this).apply { text = "📚 ইসলামিক লাইব্রেরী ও কিতাব ভাণ্ডার"; setTextColor(textYellow); textSize = 20f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(6)) })
        content.addView(TextView(this).apply { text = "পবিত্র কুরআনুল কারীম ও সিহাহ সিত্তাহ হাদিস কিতাবসমূহ ডাউনলোড করে পড়ুন:"; setTextColor(Color.WHITE); textSize = 14f; setPadding(0, 0, 0, dp(14)) })

        // কোরআন কার্ড
        val qCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
        qCard.addView(TextView(this).apply { text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)"; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        qCard.addView(TextView(this).apply { text = "উসমানের মূল মুদ্রিত ১৫ লাইনের কুরআন শরীফ। ৩০ পারা ও ১১৪ সুরার যেকোনো অংশে সরাসরি যাওয়ার সুবিধা সহ।"; setTextColor(Color.parseColor("#D1D5DB")); textSize = 12f; setPadding(0, dp(6), 0, dp(12)) })

        val qBtnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        qBtnRow.addView(Button(this).apply {
            text = "📖 ৩০ পারা তালিকা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }
            setOnClickListener { showListDialog("৩০ পারা সূচিপত্র (সরাসরি যান)", paraList, quran) }
        })
        qBtnRow.addView(Button(this).apply {
            text = "📜 ১১৪ সুরা তালিকা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnOrange)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }
            setOnClickListener { showListDialog("১১৪ সুরা সূচিপত্র (সরাসরি যান)", surahList, quran) }
        })
        qCard.addView(qBtnRow)
        qCard.addView(View(this).apply { setBackgroundColor(cardStroke); layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(12), 0, dp(12)) } })

        val savedPage = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE).getInt(quran.title, 0)
        val continueLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setOnClickListener { openPdf(quran, savedPage) } }
        continueLayout.addView(TextView(this).apply { text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage)})"; setTextColor(Color.WHITE); textSize = 14f; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) })
        continueLayout.addView(TextView(this).apply { text = "📕"; textSize = 18f })
        qCard.addView(continueLayout); content.addView(qCard)

        // হাদিস লিস্ট
        content.addView(TextView(this).apply { text = "📂 সিহাহ সিত্তাহ হাদিস কিতাবসমূহ:"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, dp(6), 0, dp(12)) })

        groups.forEachIndexed { index, (title, books) ->
            val hCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
            hCard.addView(TextView(this).apply { text = "${bn(index + 1)}. $title (${bn(books.size)} খণ্ড)"; setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD) })
            hCard.addView(TextView(this).apply { text = "${bn(books.size)}টি খণ্ডের সম্পূর্ণ পিডিএফ ডাউনলোড ও অফলাইন রিডার"; setTextColor(Color.parseColor("#D1D5DB")); textSize = 12f; setPadding(0, dp(6), 0, dp(12)) })
            hCard.addView(Button(this).apply { text = "খুলুন ➔"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(42)); setOnClickListener { showVolumes(title, books, index + 1) } })
            content.addView(hCard)
        }

        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    // খণ্ড লিস্ট (Volumes Design)
    private fun showVolumes(title: String, books: List<PdfBookItem>, idxGroup: Int) {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12)); background = GradientDrawable().apply { setColor(cardBg) } }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(Color.WHITE); setPadding(dp(10),0,dp(10),0); setOnClickListener { showLibrary() } })
        top.addView(TextView(this).apply { text = "📂 ${bn(idxGroup)}. $title (${bn(books.size)} খণ্ড)"; textSize = 18f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        books.forEach { book ->
            val vCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
            vCard.addView(TextView(this).apply { text = book.title; setTextColor(Color.WHITE); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
            vCard.addView(Button(this).apply {
                text = "📥 ডাউনলোড করুন"
                isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
                layoutParams = LinearLayout.LayoutParams(dp(160), dp(38))
                setOnClickListener { openPdf(book, 0) }
            })
            list.addView(vCard)
        }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    // ডায়ালগ লিস্ট (Para/Surah)
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
        dialogLayout.addView(Button(this).apply {
            text = "বন্ধ করুন"; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#E5E7EB"))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(16)) }
            setOnClickListener { dialog.dismiss() }
        })
        dialog.show()
    }

    private fun openPdf(book: PdfBookItem, page: Int) {
        val downloadUrl = "https://drive.google.com/uc?export=download&id=${book.id}"
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title)
        intent.putExtra("DOWNLOAD_URL", downloadUrl)
        intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }
}
