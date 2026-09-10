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
    private var isInsideVolume = false

    private val themeColors by lazy { ThemeManager.getTheme(this) }

    private val bgMain get() = themeColors.bgMain
    private val cardBg get() = themeColors.cardBg
    private val cardStroke get() = themeColors.cardStroke
    private val textYellow get() = themeColors.textAccent
    private val btnYellow get() = themeColors.btnBg
    private val textMain get() = themeColors.textMain
    private val textSub get() = themeColors.textSub
    private val btnDark get() = Color.parseColor("#0C291F")
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

    private val paraList = listOf(
        Pair("পারা ১: আলিফ লাম মীম (الم) (পৃষ্ঠা ৩)", 3),
        Pair("পারা ২: সায়াকুল (سَيَقُولُ) (পৃষ্ঠা ২৩)", 23),
        Pair("পারা ৩: তিলকার রুসুল (تِلْكَ الرُّسُلُ) (পৃষ্ঠা ৪৩)", 43),
        Pair("পারা ৪: লান তানালু (لَنْ تَنَالُوا) (পৃষ্ঠা ৬৩)", 63),
        Pair("পারা ৫: ওয়াল মুহসানাতু (وَالْمُحْصَنَاتُ) (পৃষ্ঠা ৮৩)", 83),
        Pair("পারা ৬: লা ইয়ুহিব্বুল্লাহ (لَا يُحِبُّ اللَّهُ) (পৃষ্ঠা ১০৩)", 103),
        Pair("পারা ৭: ওয়া ইযা সামিউ (وَإِذَا سَمِعُوا) (পৃষ্ঠা ১২৩)", 123),
        Pair("পারা ৮: ওয়া লাউ আন্নানা (وَلَوْ أَنَّنَا) (পৃষ্ঠা ১৪৩)", 143),
        Pair("পারা ৯: ক্বালাল মালাউ (قَالَ الْمَلَأُ) (পৃষ্ঠা ১৬৩)", 163),
        Pair("পারা ১০: ওয়া'লামু (وَاعْلَمُوا) (পৃষ্ঠা ১৮৩)", 183),
        Pair("পারা ১১: ইয়া'তাযিরুন (يَعْتَذِرُونَ) (পৃষ্ঠা ২০৩)", 203),
        Pair("পারা ১২: ওয়া মা মিন দা-ব্বাহ (وَمَا مِنْ دَابَّةٍ) (পৃষ্ঠা ২২২)", 222),
        Pair("পারা ১৩: ওয়া মা উবাররিউ (وَمَا أُبَرِّئُ) (পৃষ্ঠা ২৪৩)", 243),
        Pair("পারা ১৪: রুবামা (رُبَمَا) (পৃষ্ঠা ২৬২)", 262),
        Pair("পারা ১৫: সুবহানাল্লাযী (سُبْحَانَ الَّذِي) (পৃষ্ঠা ২৮৩)", 283),
        Pair("পারা ১৬: ক্বালা আলাম (قَالَ أَلَمْ) (পৃষ্ঠা ৩০৩)", 303),
        Pair("পারা ১৭: ইক্বতারা বা লিন্নাস (اقْتَرَبَ لِلنَّاسِ) (পৃষ্ঠা ৩২৩)", 323),
        Pair("পারা ১৮: ক্বাদ আফলাহা (قَدْ أَفْلَحَ) (পৃষ্ঠা ৩৪৩)", 343),
        Pair("পারা ১৯: ওয়া ক্বাল্লাযীনা (وَقَالَ الَّذِينَ) (পৃষ্ঠা ৩৬৩)", 363),
        Pair("পারা ২০: আম্মান খালাক্বা (أَمَّنْ خَلَقَ) (পৃষ্ঠা ৩৮৩)", 383),
        Pair("পারা ২১: উতলু মা উহিয়া (اتْلُ مَا أُوحِيَ) (পৃষ্ঠা ৪০৩)", 403),
        Pair("পারা ২২: ওয়া মাই-য়াক্বনুত (وَمَنْ يَقْنُتْ) (পৃষ্ঠা ৪২৩)", 423),
        Pair("পারা ২৩: ওয়া মালি-য়া (وَمَا لِيَ) (পৃষ্ঠা ৪৪৩)", 443),
        Pair("পারা ২৪: ফামান আজলামু (فَمَنْ أَظْلَمُ) (পৃষ্ঠা ৪৬৩)", 463),
        Pair("পারা ২৫: ইলাইহি ইউরাদ্দু (إِلَيْهِ يُرَدُّ) (পৃষ্ঠা ৪৮৩)", 483),
        Pair("পারা ২৬: হা-মীম (حم) (পৃষ্ঠা ৫০৩)", 503),
        Pair("পারা ২৭: ক্বালা ফামা খাতবুকুম (قَالَ فَمَا خَطْبُكُمْ) (পৃষ্ঠা ৫২৩)", 523),
        Pair("পারা ২৮: ক্বাদ সামিআল্লাহ (قَدْ سَمِعَ اللَّهُ) (পৃষ্ঠা ৫৪৩)", 543),
        Pair("পারা ২৯: তাবারাকাল্লাযী (تَبَارَكَ الَّذِي) (পৃষ্ঠা ৫৬৩)", 563),
        Pair("পারা ৩০: আম্মা ইয়াতাসায়ালুন (عَمَّ يَتَسَاءَلُونَ) (পৃষ্ঠা ৫৮৭)", 587)
    )

    private val surahList = listOf(
        Pair("১. সূরা আল-ফাতিহা (পৃষ্ঠা ২)", 2),
        Pair("২. সূরা আল-বাকারাহ (পৃষ্ঠা ৩)", 3),
        Pair("৩. সূরা আলে-ইমরান (পৃষ্ঠা ৫১)", 51),
        Pair("৪. সূরা আন-নিসা (পৃষ্ঠা ৭৮)", 78),
        Pair("৫. সূরা আল-মায়িদাহ (পৃষ্ঠা ১০৭)", 107),
        Pair("৬. সূরা আল-আনআম (পৃষ্ঠা ১২৯)", 129),
        Pair("৭. সূরা আল-আরাফ (পৃষ্ঠা ১৫২)", 152),
        Pair("৮. সূরা আল-আনফাল (পৃষ্ঠা ১৭৮)", 178),
        Pair("৯. সূরা আত-তাওবাহ (পৃষ্ঠা ১৮৮)", 188),
        Pair("১০. সূরা ইউনুস (পৃষ্ঠা ২০৯)", 209),
        Pair("১১. সূরা হুদ (পৃষ্ঠা ২২২)", 222),
        Pair("১২. সূরা ইউসুফ (পৃষ্ঠা ২৩৬)", 236),
        Pair("১৩. সূরা আর-রাদ (পৃষ্ঠা ২৫০)", 250),
        Pair("১৪. সূরা ইব্রাহীম (পৃষ্ঠা ২৫৬)", 256),
        Pair("১৫. সূরা আল-হিজর (পৃষ্ঠা ২৬২)", 262),
        Pair("১৬. সূরা আন-নাহল (পৃষ্ঠা ২৬৮)", 268),
        Pair("১৭. সূরা বনী ইসরাঈল (পৃষ্ঠা ২৮৩)", 283),
        Pair("১৮. সূরা আল-কাহফ (পৃষ্ঠা ২৯৪)", 294),
        Pair("১৯. সূরা মারইয়াম (পৃষ্ঠা ৩০৬)", 306),
        Pair("২০. সূরা ত্বা-হা (পৃষ্ঠা ৩১৩)", 313),
        Pair("২১. সূরা আল-আম্বিয়া (পৃষ্ঠা ৩২৩)", 323),
        Pair("২২. সূরা আল-হাজ্জ (পৃষ্ঠা ৩৩২)", 332),
        Pair("২৩. সূরা আল-মুমিনুন (পৃষ্ঠা ৩৪৩)", 343),
        Pair("২৪. সূরা আন-নূর (পৃষ্ঠা ৩৫১)", 351),
        Pair("২৫. সূরা আল-ফুরকান (পৃষ্ঠা ৩৬০)", 360),
        Pair("২৬. সূরা আশ-শুয়ারা (পৃষ্ঠা ৩৬৭)", 367),
        Pair("২৭. সূরা আন-নামল (পৃষ্ঠা ৩৭৭)", 377),
        Pair("২৮. সূরা আল-কাসাস (পৃষ্ঠা ৩৮৬)", 386),
        Pair("২৯. সূরা আল-আনকাবুত (পৃষ্ঠা ৩৯৭)", 397),
        Pair("৩০. সূরা আর-রূম (পৃষ্ঠা ৪০৫)", 405),
        Pair("৩১. সূরা লুকমান (পৃষ্ঠা ৪১২)", 412),
        Pair("৩২. সূরা আস-সাজদাহ (পৃষ্ঠা ৪১৬)", 416),
        Pair("৩৩. সূরা আল-আহযাব (পৃষ্ঠা ৪১৯)", 419),
        Pair("৩৪. সূরা সাবা (পৃষ্ঠা ৪২৯)", 429),
        Pair("৩৫. সূরা ফাতির (পৃষ্ঠা ৪৩৫)", 435),
        Pair("৩৬. সূরা ইয়াসীন (পৃষ্ঠা ৪৪১)", 441),
        Pair("৩৭. সূরা আস-সাফফাত (পৃষ্ঠা ৪৪৬)", 446),
        Pair("৩৮. সূরা সোয়াদ (পৃষ্ঠা ৪৫৩)", 453),
        Pair("৩৯. সূরা আজ-জুমার (পৃষ্ঠা ৪৫৯)", 459),
        Pair("৪০. সূরা গাফির / আল-মুমিন (পৃষ্ঠা ৬৮)", 468),
        Pair("৪১. সূরা ফুসসিলাত (পৃষ্ঠা ৪৭৮)", 478),
        Pair("৪২. সূরা আশ-শূরা (পৃষ্ঠা ৪৮৪)", 484),
        Pair("৪৩. সূরা আজ-জুখরুফ (পৃষ্ঠা ৪৯০)", 490),
        Pair("৪৪. সূরা আদ-দুখান (পৃষ্ঠা ৪৯৬)", 496),
        Pair("৪৫. সূরা আল-জাসিয়াহ (পৃষ্ঠা ৪৯৯)", 499),
        Pair("৪৬. সূরা আল-আহকাফ (পৃষ্ঠা ৫০৩)", 503),
        Pair("৪৭. সূরা মুহাম্মদ (পৃষ্ঠা ৫০৭)", 507),
        Pair("৪৮. সূরা আল-ফাতহ (পৃষ্ঠা ৫১২)", 512),
        Pair("৪৯. সূরা আল-হুজুরাত (পৃষ্ঠা ৫১৬)", 516),
        Pair("৫০. সূরা কাফ (পৃষ্ঠা ৫১৯)", 519),
        Pair("৫১. সূরা আজ-যারিয়াত (পৃষ্ঠা ৫২১)", 521),
        Pair("৫২. সূরা আত্ব-তূর (পৃষ্ঠা ৫২৪)", 524),
        Pair("৫৩. সূরা আন-নাজম (পৃষ্ঠা ৫২৭)", 527),
        Pair("৫৪. সূরা আল-কামার (পৃষ্ঠা ৫২৯)", 529),
        Pair("৫৫. সূরা আর-রহমান (পৃষ্ঠা ৫৩২)", 532),
        Pair("৫৬. সূরা আল-ওয়াকিয়াহ (পৃষ্ঠা ৫৩৫)", 535),
        Pair("৫৭. সূরা আল-হাদীদ (পৃষ্ঠা ৫৩৮)", 538),
        Pair("৫৮. সূরা আল-মুজাদালাহ (পৃষ্ঠা ৫৪৩)", 543),
        Pair("৫৯. সূরা আল-হাশর (পৃষ্ঠা ৫৪৬)", 546),
        Pair("৬০. সূরা আল-মুমতাহিনাহ (পৃষ্ঠা ৫৫০)", 550),
        Pair("৬১. সূরা আস-সাফ (পৃষ্ঠা ৫৫২)", 552),
        Pair("৬২. সূরা আল-জুমুআহ (পৃষ্ঠা ৫৫৪)", 554),
        Pair("৬৩. সূরা আল-মুনাফিকুন (পৃষ্ঠা ৫৫৫)", 555),
        Pair("৬৪. সূরা আত-তাগাবুন (পৃষ্ঠা ৫৫৭)", 557),
        Pair("৬৫. সূরা আত-তালাক (পৃষ্ঠা ৫৯)", 559),
        Pair("৬৬. সূরা আত-তাহরিম (পৃষ্ঠা ৫৬১)", 561),
        Pair("৬৭. সূরা আল-মুলক (পৃষ্ঠা ৫৬৩)", 563),
        Pair("৬৮. সূরা আল-কলম (পৃষ্ঠা ১৯৬৫)", 565),
        Pair("৬৯. সূরা আল-হাক্কাহ (পৃষ্ঠা ৫৬৮)", 568),
        Pair("৭০. সূরা আল-মাআরিজ (পৃষ্ঠা ৫৭০)", 570),
        Pair("৭১. সূরা নূহ (পৃষ্ঠা ৫৭০২)", 572),
        Pair("৭২. সূরা আল-জিন (পৃষ্ঠা ৫৭১৪)", 574),
        Pair("৭৩. সূরা আল-মুযযাম্মিল (পৃষ্ঠা ৫৭৭)", 577),
        Pair("৭৪. সূরা আল-মুদ্দাসসির (পৃষ্ঠা ৫ ১৯৭)", 579),
        Pair("৭৫. সূরা আল-কিয়ামাহ (পৃষ্ঠা ৫৮১)", 581),
        Pair("৭৬. সূরা আল-ইনসান / আদ-দাহর (পৃষ্ঠা ৫ ১৯৮)", 583),
        Pair("৭৭. সূরা আল-মুরসালাত (পৃষ্ঠা ৫৮৫)", 585),
        Pair("৭৮. সূরা আন-নাবা (পৃষ্ঠা ৫৯৪)", 587),
        Pair("৭৯. সূরা আন-নাযিআত (পৃষ্ঠা ৫ ১৯৮)", 588),
        Pair("৮০. সূরা আবাসা (পৃষ্ঠা ৫৯০)", 590),
        Pair("৮১. সূরা আত-তাকভির (পৃষ্ঠা ৫৯১)", 591),
        Pair("৮২. সূরা আল-ইনফিতার (পৃষ্ঠা ৫৯২)", 592),
        Pair("৮৩. সূরা আল-মুতাফফিফিন (পৃষ্ঠা ৫৯৩)", 593),
        Pair("৮৪. সূরা আল-ইনশিকাক (পৃষ্ঠা ৫৯৫)", 595),
        Pair("৮৫. সূরা আল-বুরুজ (পৃষ্ঠা ৫৯৬)", 596),
        Pair("৮৬. সূরা আত-তারিক (পৃষ্ঠা ৫৯৭)", 597),
        Pair("৮৭. সূরা আল-আলা (পৃষ্ঠা ৫৯৮)", 598),
        Pair("৮৮. সূরা আল-গাশিয়াহ (পৃষ্ঠা ৫৯৯)", 599),
        Pair("৮৯. সূরা আল-ফজর (পৃষ্ঠা ৬০০)", 600),
        Pair("৯০. সূরা আল-বালাদ (পৃষ্ঠা ৬০১)", 601),
        Pair("৯১. সূরা আশ-শামস (পৃষ্ঠা ৬০২)", 602),
        Pair("৯২. সূরা আল-লাইল (পৃষ্ঠা ৬০৩)", 603),
        Pair("৯৩. সূরা আদ-দুহা (পৃষ্ঠা ৬০৩)", 603),
        Pair("৯৪. সূরা আশ-শারহ (পৃষ্ঠা ৬০৩)", 603),
        Pair("৯৫. সূরা আত-তিন (পৃষ্ঠা ৬০৪)", 604),
        Pair("৯৬. সূরা আল-আলাক (পৃষ্ঠা ৬০৫)", 605),
        Pair("৯৭. সূরা আল-কদর (পৃষ্ঠা ৬০৫)", 605),
        Pair("৯৮. সূরা আল-বাইয়্যিনাহ (পৃষ্ঠা ৬০৬)", 606),
        Pair("৯৯. সূরা আয-যিলযাল (পৃষ্ঠা ৬০৬)", 606),
        Pair("১০০. সূরা আল-আদিয়াত (পৃষ্ঠা ৬০৭)", 607),
        Pair("১০১. সূরা আল-কারিয়াহ (পৃষ্ঠা ৬০৭)", 607),
        Pair("১০২. সূরা আত-তাকাসুর (পৃষ্ঠা ৬০৭)", 607),
        Pair("১০৩. সূরা আল-আসর (পৃষ্ঠা ৬০৮)", 608),
        Pair("১০৪. সূরা আল-হুমাযাহ (পৃষ্ঠা ৬০৮)", 608),
        Pair("১০৫. সূরা আল-ফিল (পৃষ্ঠা ৬০৮)", 608),
        Pair("১০৬. সূরা কুরাইশ (পৃষ্ঠা ৬০৯)", 609),
        Pair("১০৭. সূরা আল-মাউন (পৃষ্ঠা ৬০৯)", 609),
        Pair("১০৮. সূরা আল-কাউসার (পৃষ্ঠা ৬০৯)", 609),
        Pair("১০৯. সূরা আল-কাফিরুন (পৃষ্ঠা ৬১০)", 610),
        Pair("১১০. সূরা আন-নাসর (পৃষ্ঠা ৬১০)", 610),
        Pair("১১১. সূরা আল-মাসাদ / লাহাব (পৃষ্ঠা ৬১০)", 610),
        Pair("১১২. সূরা আল-ইখলাস (পৃষ্ঠা ৬১০)", 610),
        Pair("১১৩. সূরা আল-ফালাক (পৃষ্ঠা ৬১১)", 611),
        Pair("১টো. সূরা আন-নাস (পৃষ্ঠা ৬১১)", 611)
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showLibrary() }
    
    override fun onBackPressed() { if (isInsideVolume) showLibrary() else super.onBackPressed() }

    private fun showLibrary() {
        isInsideVolume = false 
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← হোম"; textSize = 16f; setTextColor(textMain); setPadding(0, 0, dp(12), 0); setOnClickListener { finish() } })
        top.addView(TextView(this).apply { text = "📚 ইসলামিক লাইব্রেরী"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(80)) }

        val qCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
        qCard.addView(TextView(this).apply { text = "📖 পবিত্র কুরআন শরীফ (সম্পূর্ণ)"; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD) })
        
        if (isFileExists(quran.title)) {
            val qBtnRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; weightSum = 2f; setPadding(0, dp(10), 0, dp(10)) }
            qBtnRow.addView(Button(this).apply { text = "📖 ৩০ পারা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(btnDark); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }; setOnClickListener { showListDialog("৩০ পারা সূচিপত্র", paraList, quran) } })
            qBtnRow.addView(Button(this).apply { text = "📜 ১১৪ সুরা"; isAllCaps = false; setTextColor(Color.WHITE); background = getBtnDrawable(Color.parseColor("#D97706")); layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }; setOnClickListener { showListDialog("১১৪ সুরা সূচিপত্র", surahList, quran) } })
            qCard.addView(qBtnRow)
            
            val continueLayout = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            val savedPage = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE).getInt(quran.title, 0)
            continueLayout.addView(TextView(this).apply { text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage + 1)})"; setTextColor(textSub); textSize = 14f; layoutParams = LinearLayout.LayoutParams(0, -2, 1f); setOnClickListener { openPdf(quran, 0) } })
            continueLayout.addView(Button(this).apply { text = "🗑️"; setTextColor(Color.WHITE); background = getBtnDrawable(btnRed); layoutParams = LinearLayout.LayoutParams(dp(40), dp(35)); setOnClickListener { deleteFileSilent(quran.title); showLibrary() } })
            qCard.addView(continueLayout)
        } else {
            val dlBtn = Button(this).apply { text = "📥 কোরআন ডাউনলোড করুন"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(10) } }
            dlBtn.setOnClickListener { startDownload(quran, dlBtn) { showLibrary() } }
            qCard.addView(dlBtn)
        }
        content.addView(qCard)

        content.addView(TextView(this).apply { text = "📂 সিহাহ সিত্তাহ হাদিস কিতাবসমূহ:"; setTextColor(textYellow); textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(0, dp(6), 0, dp(12)) })

        groups.forEachIndexed { index, (title, books) ->
            val hCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(12), dp(12), dp(12), dp(12)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) } }
            hCard.addView(TextView(this).apply { text = "${bn(index + 1)}. $title (${bn(books.size)} খণ্ড)"; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD) })
            hCard.addView(Button(this).apply { text = "খুলুন ➔"; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow); layoutParams = LinearLayout.LayoutParams(-1, dp(40)).apply { topMargin = dp(10) }; setOnClickListener { showVolumes(title, books, index + 1) } })
            content.addView(hCard)
        }

        scroll.addView(content); root.addView(scroll); setContentView(root)
    }

    private fun showVolumes(title: String, books: List<PdfBookItem>, idxGroup: Int) {
        isInsideVolume = true 
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12)); background = getCardDrawable() }
        top.addView(TextView(this).apply { text = "← ফিরে যান"; textSize = 16f; setTextColor(textMain); setPadding(dp(10),0,dp(10),0); setOnClickListener { showLibrary() } })
        top.addView(TextView(this).apply { text = "📂 ${bn(idxGroup)}. $title"; textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        books.forEach { book ->
            val vCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; background = getCardDrawable(); setPadding(dp(14), dp(14), dp(14), dp(14)); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) } }
            vCard.addView(TextView(this).apply { text = book.title; setTextColor(textMain); textSize = 16f; setTypeface(null, Typeface.BOLD); setPadding(0, 0, 0, dp(10)) })
            
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

    private fun startDownload(book: PdfBookItem, btn: Button, onComplete: () -> Unit) {
        btn.text = "⏳ ডাউনলোড হচ্ছে..."
        btn.isEnabled = false
        val file = File(filesDir, getSafeFileName(book.title))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL("https://drive.google.com/uc?export=download&id=${book.id}")
                val connection = url.openConnection(); connection.connect()
                val input = connection.getInputStream(); val output = FileOutputStream(file)
                val data = ByteArray(4096); var count: Int
                while (input.read(data).also { count = it } != -1) output.write(data, 0, count)
                output.flush(); output.close(); input.close()
                withContext(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { btn.text = "❌ ব্যর্থ হয়েছে!"; btn.isEnabled = true; if (file.exists()) file.delete() }
            }
        }
    }

    private fun showListDialog(titleText: String, items: List<Pair<String, Int>>, book: PdfBookItem) {
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(themeColors.bgMain) }
        dialogLayout.addView(TextView(this).apply { text = titleText; textSize = 18f; setTextColor(themeColors.textAccent); setTypeface(null, Typeface.BOLD); setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(themeColors.cardBg) })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        items.forEach { item ->
            list.addView(TextView(this).apply { text = item.first; textSize = 18f; setPadding(0, dp(12), 0, dp(12)); setTextColor(themeColors.textMain); setOnClickListener { openPdf(book, item.second); dialogLayout.parent?.let { (it as? AlertDialog)?.dismiss() } } })
            list.addView(View(this).apply { setBackgroundColor(themeColors.cardStroke); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) })
        }
        scroll.addView(list); dialogLayout.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()
        dialogLayout.addView(Button(this).apply { text = "বন্ধ করুন"; setTextColor(Color.BLACK); setBackgroundColor(themeColors.btnBg); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(16)) }; setOnClickListener { dialog.dismiss() } })
        dialog.show()
    }

    private fun openPdf(book: PdfBookItem, page: Int) {
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title); intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }
}
