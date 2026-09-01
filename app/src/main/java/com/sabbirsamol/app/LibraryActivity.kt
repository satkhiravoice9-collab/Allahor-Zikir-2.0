package com.sabbirsamol.app

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

    // কালার প্যালেট (আপনার ভিডিও অনুযায়ী)
    private val bgMain = Color.parseColor("#091C14")
    private val cardBg = Color.parseColor("#114D3C")
    private val cardStroke = Color.parseColor("#1B785B")
    private val textYellow = Color.parseColor("#FBBF24")
    private val btnYellow = Color.parseColor("#FACC15")
    private val btnDark = Color.parseColor("#0C291F")
    private val btnOrange = Color.parseColor("#D97706")

    // কার্ডের ব্যাকগ্রাউন্ড ডিজাইন
    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg)
        setStroke(dp(1), cardStroke)
        cornerRadius = dp(10).toFloat()
    }

    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(6).toFloat()
    }

    // আপনার বইয়ের ডাটাবেস
    private val quran = PdfBookItem("quran_full.pdf", "পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)", "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")

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
        PdfBookItem("muslim_vol_5.pdf", "সহীহ মুসলিম ৫ম খণ্ড", "129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z"),
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
        "সহীহ বুখারী শরীফ" to bukhariVolList,
        "সহীহ মুসলিম শরীফ" to muslimVolList,
        "সুনান আবু দাউদ" to abuDaudVolList,
        "জামে আত-তিরমিযী" to tirmidhiVolList,
        "সুনান আন-নাসায়ী" to nasaiVolList,
        "সুনান ইবনে মাজাহ" to ibnMajahVolList
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showLibrary()
    }

    override fun onResume() {
        super.onResume()
        showLibrary() // পেজ রিফ্রেশ করে লেটেস্ট পড়া পেজ নম্বর আনার জন্য
    }

    private fun showLibrary() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(16), dp(14), dp(80)) // Bottom padding for Nav Bar
        }

        // Header Title
        content.addView(TextView(this).apply {
            text = "📚 ইসলামিক লাইব্রেরী ও কিতাব ভাণ্ডার"
            setTextColor(textYellow)
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(6))
        })
        content.addView(TextView(this).apply {
            text = "পবিত্র কুরআনুল কারীম ও সিহাহ সিত্তাহ হাদিস কিতাবসমূহ ডাউনলোড করে পড়ুন:"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(0, 0, 0, dp(14))
        })

        // --- আল-কোরআন (PDF) কার্ড ---
        val qCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
        }
        qCard.addView(TextView(this).apply {
            text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা ও ১১৪ সুরা)"
            setTextColor(Color.WHITE)
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
        })
        qCard.addView(TextView(this).apply {
            text = "উসমানের মূল মুদ্রিত ১৫ লাইনের কুরআন শরীফ। ৩০ পারা ও ১১৪ সুরার যেকোনো অংশে সরাসরি যাওয়ার সুবিধা সহ।"
            setTextColor(Color.parseColor("#D1D5DB"))
            textSize = 12f
            setPadding(0, dp(6), 0, dp(12))
        })

        // কোরআন বাটন রো
        val qBtnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f }
        val btn30 = Button(this).apply {
            text = "📖 ৩০ পারা তালিকা"
            isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }
            setOnClickListener { openPdf(quran) }
        }
        val btn114 = Button(this).apply {
            text = "📜 ১১৪ সুরা তালিকা"
            isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnOrange)
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }
            setOnClickListener { openPdf(quran) }
        }
        qBtnRow.addView(btn30); qBtnRow.addView(btn114)
        qCard.addView(qBtnRow)

        // Divider
        qCard.addView(View(this).apply {
            setBackgroundColor(cardStroke)
            layoutParams = LinearLayout.LayoutParams(-1, dp(1)).apply { setMargins(0, dp(12), 0, dp(12)) }
        })

        // Continue Reading
        val prefs = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE)
        val savedPage = prefs.getInt(quran.title, 0)
        
        val continueLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { openPdf(quran) }
        }
        continueLayout.addView(TextView(this).apply {
            text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage)})"
            setTextColor(Color.WHITE)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        })
        continueLayout.addView(TextView(this).apply {
            text = "📕"; textSize = 18f
        })
        qCard.addView(continueLayout)
        content.addView(qCard)

        // --- আল-কোরআন (Text Reader) কার্ড ---
        val textCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(12), dp(12), dp(12), dp(12))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) }
        }
        textCard.addView(TextView(this).apply {
            text = "📗 আল-কোরআনুল কারীম (১১৪ সুরা টেক্সট রিডার)"
            setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD)
        })
        textCard.addView(TextView(this).apply {
            text = "সুরা ভিত্তিক আয়াত ও বাংলা অর্থ আলাদাভাবে পড়ার জন্য চাপুন"
            setTextColor(Color.parseColor("#D1D5DB")); textSize = 12f; setPadding(0, dp(6), 0, dp(12))
        })
        textCard.addView(Button(this).apply {
            text = "সুরা টেক্সট রিডার খুলুন ➔"
            isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
            layoutParams = LinearLayout.LayoutParams(-1, dp(42))
            setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://quran.com/bn"))) }
        })
        content.addView(textCard)

        // --- সিহাহ সিত্তাহ হেডার ---
        content.addView(TextView(this).apply {
            text = "📂 সিহাহ সিত্তাহ হাদিস কিতাবসমূহ:"
            setTextColor(textYellow)
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(6), 0, dp(12))
        })

        // --- হাদিস কার্ডস জেনারেটর ---
        groups.forEachIndexed { index, (title, books) ->
            val hCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                background = getCardDrawable()
                setPadding(dp(12), dp(12), dp(12), dp(12))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            }
            hCard.addView(TextView(this).apply {
                text = "${bn(index + 1)}. $title (${bn(books.size)} খণ্ড)"
                setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD)
            })
            hCard.addView(TextView(this).apply {
                text = "${bn(books.size)}টি খণ্ডের সম্পূর্ণ পিডিএফ ডাউনলোড ও অফলাইন রিডার"
                setTextColor(Color.parseColor("#D1D5DB")); textSize = 12f; setPadding(0, dp(6), 0, dp(12))
            })
            hCard.addView(Button(this).apply {
                text = "খুলুন ➔"
                isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
                layoutParams = LinearLayout.LayoutParams(-1, dp(42))
                setOnClickListener { showVolumes(title, books) }
            })
            content.addView(hCard)
        }

        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    // --- খণ্ডগুলোর লিস্ট দেখার পেজ ---
    private fun showVolumes(title: String, books: List<PdfBookItem>) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgMain)
        }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply { setColor(cardBg) }
        }
        top.addView(Button(this).apply {
            text = "←"; textSize = 20f; setTextColor(Color.WHITE); setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { showLibrary() }
        }, LinearLayout.LayoutParams(dp(54), dp(50)))
        top.addView(TextView(this).apply {
            text = title; textSize = 18f; setTextColor(Color.WHITE); setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(50), 1f))
        root.addView(top)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(14), dp(14), dp(80))
        }

        books.forEachIndexed { idx, book ->
            val vCard = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                background = getCardDrawable()
                setPadding(dp(14), dp(14), dp(14), dp(14))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) }
                setOnClickListener { openPdf(book) }
            }
            vCard.addView(TextView(this).apply {
                text = "📥 ${bn(idx + 1)}. ${book.title}"
                setTextColor(Color.WHITE); textSize = 16f; setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
            })
            list.addView(vCard)
        }
        
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    // --- পিডিএফ রিডার কল করা ---
    private fun openPdf(book: PdfBookItem) {
        val downloadUrl = "https://drive.google.com/uc?export=download&id=${book.id}"
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title)
        intent.putExtra("DOWNLOAD_URL", downloadUrl)
        startActivity(intent)
    }
}
