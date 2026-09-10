package com.sabbirsamol.app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.URL

data class DoaItem(val title: String, val arabic: String, val pron: String? = null, val meaning: String? = null)
data class DoaFolder(val id: Int, val title: String, val subtitle: String, val btnText: String, val doasList: List<DoaItem>)
data class AmolPdfItem(val fileName: String, val title: String, val id: String, val subtitle: String)

class MasnunAmolActivity : ComponentActivity() {

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bn(n: Int): String = n.toString().map { "০১২translateX৫৬৭৮৯"[it - '0'] }.joinToString("")
    private fun bnStr(s: String): String = s.map { if (it in '0'..'9') "০১২৩৪৫৬৭৮৯"[it - '0'] else it }.joinToString("")
    
    private var isInsideFolder = false

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

    private val textPron = Color.parseColor("#A7F3D0")
    private val textMeaning = Color.parseColor("#9CA3AF")

    private fun getCardDrawable() = GradientDrawable().apply {
        setColor(cardBg); setStroke(dp(1), cardStroke); cornerRadius = dp(10).toFloat()
    }
    private fun getBtnDrawable(color: Int) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(6).toFloat()
    }
    private fun getTitleBoxDrawable() = GradientDrawable().apply {
        setColor(cardStroke); cornerRadius = dp(6).toFloat()
    }

    private fun getSafeFileName(title: String) = title.replace(Regex("[^A-Za-z0-9]"), "_") + ".pdf"
    private fun isFileExists(title: String) = File(filesDir, getSafeFileName(title)).exists()
    private fun deleteFileSilent(title: String) {
        val file = File(filesDir, getSafeFileName(title))
        if (file.exists()) file.delete()
        Toast.makeText(this, "ফাইলটি ডিলিট করা হয়েছে", Toast.LENGTH_SHORT).show()
        showMainFolders()
    }

    private val masnunPdf = AmolPdfItem("masnun_amol.pdf", "মাসনূন আমল সংগ্রহ", "1HO1U_cA0LvHtblEFFxbAXX6y7D-QyFkY", "সকাল ও সন্ধ্যায় পঠিত গুরুত্বপূর্ণ আমল ও দোয়া সংকলন।")
    private val manzilPdf = AmolPdfItem("manzil_amol.pdf", "মানযিল আয়াত (সম্পূর্ণ)", "1gaNkUuBFczLhe5FHw2leGz9qdbqGzzvb", "কুরআনুল কারীমের রোগ-বালাই ও অনিষ্ট থেকে বাঁচার মানযিল বুকলেট।")

    private val folder3 = DoaFolder(
        id = 3, title = "📁 ফোল্ডার ৩: দৈনন্দিন জীবনের গুরুত্বপূর্ণ দোয়া",
        subtitle = "ঘুম, খাবার, মসজিদ, রিজিক, যানবাহন, সহবাস ও বিভিন্ন কাজের সহীহ দোয়া।", btnText = "📖 দৈনন্দিন দোয়া পড়ুন ➔",
        doasList = listOf(
            DoaItem("১। ঘুম থেকে ওঠার দোয়া", "الحمد لله الذي أحيانا بعدما أماتنا وإليه النشور", "আলহামদু লিল্লাহিল্লাযী আহইয়ানা বা'দা মা আমাতানা ওয়া ইলাইহিন নুশূর।", "সব প্রশংসা আল্লাহর, যিনি আমাদের মৃত্যুর মতো ঘুমের পর জীবিত করলেন এবং তাঁর কাছেই ফিরে যেতে হবে।"),
            DoaItem("২। ঘুমানোর দোয়া", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "বিসমিকা আল্লাহুম্মা আমূতু ওয়া আহইয়া।", "হে আল্লাহ! আপনার নামেই আমি মরি এবং বেঁচে থাকি।"),
            DoaItem("৩। পোশাক পরার দোয়া", "الْحَمْدُ لِلَّهِ الَّذِي كَسَانِي هَذَا الثَّوْبَ وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ", "আলহামদু লিল্লাহিল্লাযী কাসানী হাযাস সাওবা ওয়া রাযাকানীহি মিন গাইরি হাওলিন মিন্নী ওয়ালা কুওয়াহ।", "সব প্রশংসা আল্লাহর, যিনি আমাকে এই পোশাক পরিয়েছেন এবং তা আমাকে দান করেছেন।"),
            DoaItem("৪। বাথরুমে প্রবেশের দোয়া", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْخُبُثِ وَالْخَبَائِثِ", "আল্লাহুম্মা ইন্নী আউযু বিকা মিনাল খুবুছি ওয়াল খাবায়িছ।", "হে আল্লাহ! আমি আপনার কাছে অপবিত্র শয়তানদের থেকে আশ্রয় চাই।"),
            DoaItem("৫। বাথরুম থেকে বের হওয়ার দোয়া", "غُفْرَانَكَ", "গুফরানাকা।", "হে আল্লাহ! আমি আপনার ক্ষমা চাই।"),
            DoaItem("৬। ওযু শুরু করার সময়", "بِسْمِ اللَّهِ", "বিসমিল্লাহ।", "আল্লাহর নামে।"),
            DoaItem("৭। ওযুর পরের দোয়া", "أَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، وَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "আশহাদু আল্লা ইলাহা ইল্লাল্লাহু ওয়াহদাহু লা শারীকা লাহু, ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসূলুহু।", "আমি সাক্ষ্য দিচ্ছি, আল্লাহ ছাড়া কোনো সত্য ইলাহ নেই এবং মুহাম্মদ ﷺ তাঁর বান্দা ও রাসূল।"),
            DoaItem("৮। ঘর থেকে বের হওয়ার দোয়া", "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "বিসমিল্লাহি তাওয়াক্কালতু আলাল্লাহ, লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ।", "আল্লাহর নামে, আমি আল্লাহর ওপর ভরসা করলাম। আল্লাহ ছাড়া কোনো শক্তি নেই।"),
            DoaItem("৯। ঘরে প্রবেশের দোয়া", "بِسْمِ اللَّهِ وَلَجْنَا، وَبِسْمِ اللَّهِ خَرَجْنَا، وَعَلَى رَبِّنَا تَوَكَّلْنَا", "বিসমিল্লাহি ওয়ালাজনা, ওয়া বিসমিল্লাহি খারাজনা, ওয়া আলা রাব্বিনা তাওয়াক্কালনা।", "আল্লাহর নামেই আমরা প্রবেশ করেছি, আল্লাহর নামেই বের হয়েছি এবং আমাদের রবের ওপর ভরসা করেছি।"),
            DoaItem("১০। মসজিদে প্রবেশের দোয়া", "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ", "আল্লাহুম্মাফতাহ লী আবওয়াবা রাহমাতিক।", "হে আল্লাহ! আমার জন্য আপনার রহমতের দরজাগুলো খুলে দিন।"),
            DoaItem("১১। মসজিদ থেকে বের হওয়ার দোয়া", "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ", "আল্লাহুম্মা ইন্নী আসআলুকা মিন ফাদলিক।", "হে আল্লাহ! আমি আপনার অনুগ্রহ চাই।"),
            DoaItem("১২। খাবার শুরু করার দোয়া", "بِسْمِ اللَّهِ", "বিসমিল্লাহ।", "আল্লাহর নামে।"),
            DoaItem("১৩। খাবার শেষে দোয়া", "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ", "আলহামদু লিল্লাহিল্লাযী আতআমানী হাযা ওয়া রাযাকানীহি মিন গাইরি হাওলিন মিন্নী ওয়ালা কুওয়াহ।", "সব প্রশংসা আল্লাহর, যিনি আমাকে এই খাবার দান করেছেন।"),
            DoaItem("১৪। বিসমিল্লাহ বলতে ভুলে গেলে", "بِسْمِ اللَّهِ أَوَّلَهُ وَآخِرَهُ", "বিসমিল্লাহি আওয়ালাহু ওয়া আখিরাহু।", "শুরু ও শেষ আল্লাহর নামে।"),
            DoaItem("১৫। যানবাহনে ওঠার দোয়া", "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ", "সুবহানাল্লাযী সাখ্খারা লানা হাযা, ওয়া মা কুন্না লাহু মুকরিনীন, ওয়া ইন্না ইলা রাব্বিনা লামুনকালিবূন।", "পবিত্র তিনি, যিনি এটিকে আমাদের অধীন করেছেন। নিশ্চয়ই আমরা আমাদের রবের কাছেই ফিরে যাব।"),
            DoaItem("১৬। বৃষ্টি হলে দোয়া", "اللَّهُمَّ صَيِّبًا نَافِعًا", "আল্লাহুম্মা সাইয়্যিবান নাফিআ।", "হে আল্লাহ! এটিকে উপকারী বৃষ্টি করুন।"),
            DoaItem("১৭। হাঁচি দিলে", "الْحَمْدُ لِلَّهِ", "আলহামদুলিল্লাহ।", "সব প্রশংসা আল্লাহর।"),
            DoaItem("১৮। হাঁচির জবাব", "يَرْحَمُكَ اللَّهُ", "ইয়ারহামুকাল্লাহ।", "আল্লাহ আপনার প্রতি রহম করুন।"),
            DoaItem("১৯। রাগের সময়ের দোয়া", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ", "আউযু বিল্লাহি মিনাশ শাইতানির রাজীম।", "আমি বিতাড়িত শয়তান থেকে আল্লাহর আশ্রয় চাই।"),
            DoaItem("২০। স্ত্রী সহবাসের পূর্বের দোয়া", "بِسْمِ اللَّهِ، اللَّهُمَّ جَنِّبْنَا الشَّيْطَانَ وَجَنِّبِ الشَّيْطَانَ مَا رَزَقْتَنَا", "বিসমিল্লাহ, আল্লাহুম্মা জান্নিবনাশ শাইতানা ওয়া জান্নিবিশ শাইতানা মা রাযাকতানা।", "হে আল্লাহ! আমাদের এবং আমাদেরকে যে সন্তান দান করবেন, তাকে শয়তান থেকে দূরে রাখুন।"),
            DoaItem("২১। বিপদের সময়", "حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ", "হাসبুনাল্লাহু ওয়া নি'মাল ওয়াকীল।", "আল্লাহই আমাদের জন্য যথেষ্ট এবং তিনিই উত্তম অভিভাবক।"),
            DoaItem("২২। দুশ্চিন্তার সময়", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "লা হাওলা ওয়ালা কুওয়াতা ইল্লা বিল্লাহ।", "আল্লাহ ছাড়া কোনো শক্তি ও ক্ষমতা নেই।"),
            DoaItem("২৩। অসুস্থ ব্যক্তিকে দেখতে গেলে", "لَا بَأْسَ طَهُورٌ إِنْ شَاءَ اللَّهُ", "লা বাসা, তহূরুন ইন শা আল্লাহ।", "কোনো সমস্যা নেই, ইনশাআল্লাহ এটি পবিত্রতার কারণ হবে।"),
            DoaItem("২৪। ভালো কিছু দেখে", "مَا شَاءَ اللَّهُ لَا قُوَّةَ إِلَّا بِاللَّهِ", "মা শা আল্লাহ, লা কুওয়াতা ইল্লা বিল্লাহ।", "আল্লাহ যা চেয়েছেন তাই হয়েছে, আল্লাহ ছাড়া কোনো শক্তি নেই।"),
            DoaItem("২৫। সকাল-সন্ধ্যার গুরুত্বপূর্ণ আমল", "آية الكرسي، سورة الإخلاص، سورة الفلق، سورة الناس", "আয়াতুল কুরসি, সূরা ইখলাস, সূরা ফালাক ও সূরা নাস পড়া।", "সকাল ও সন্ধ্যায় এগুলো পড়লে আল্লাহ সকল বিপদ থেকে রক্ষা করেন।"),
            DoaItem("২৬। বেশি বেশি ইস্তিগফার", "أَسْتَغْفِرُ اللَّهَ", "আস্তাগফিরুল্লাহ।", "আমি আল্লাহর কাছে ক্ষমা চাই।"),
            DoaItem("২৭। দরুদ শরিফ", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ", "আল্লাহুম্মা সাল্লি আলা মুহাম্মাদ।", "হে আল্লাহ! মুহাম্মদ ﷺ-এর ওপর রহমত বর্ষণ করুন।"),
            DoaItem("২৮। আল্লাহর ওপর ভরসা", "حَسْبِيَ اللَّهُ لَا إِلٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ", "হাসবিয়াল্লাহু লা ইলাহা ইল্লা হুয়া আলাইহি তাওয়াক্কালতু।", "আল্লাহই আমার জন্য যথেষ্ট। তিনি ছাড়া কোনো ইলাহ নেই। আমি তাঁর ওপর ভরসা করি।"),
            DoaItem("২৯। তওবা ও ক্ষমা প্রার্থনা", "رَبِّ اغْفِرْ لِي وَتُبْ عَلَيَّ إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ", "রাব্বিগফির লী ওয়া তুব আলাইয়া, ইন্নাকা আন্তাত তাওয়াবুর রাহীম।", "হে আমাদের রব! আমাকে ক্ষমা করুন এবং আমাদের তওবা কবুল করুন। নিশ্চয়ই আপনি তওবা কবুলকারী, পরম দয়ালু।"),
            DoaItem("৩০। দুনিয়া ও আখিরাতের কল্যাণের দোয়া", "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ", "রাব্বāna آtina fiদ্দুনইয়া হাসানাতাও ওয়া ফিল আখিরাতি হাসানাতাও ওয়া কিনা আযাবান্নার।", "হে আমাদের রব! আমাদের দুনিয়াতে কল্যাণ দিন, আখিরাতে কল্যাণ দিন এবং জাহান্নামের শাস্তি থেকে রক্ষা করুন।")
        )
    )

    private val folder4 = DoaFolder(
        id = 4, title = "📁 ফোল্ডার ৪: নামাজের দোয়া, যিকির ও জানাজা",
        subtitle = "সানা, রুকু-সিজদাহ, তাশাহহুদ, দরুদ ও জানাজার নামাজের দোয়া।", btnText = "📖 নামাজের দোয়া পড়ুন ➔",
        doasList = listOf(
            DoaItem("১। সানা", "سُبْحَانَكَ اللَّهُمَّ وَبِحَمْدِكَ، وَتَبَارَكَ اسْمُكَ، وَتَعَالَى جَدُّكَ، وَلَا إِلٰهَ غَيْرُكَ", "সুবহানাকা আল্লাহুম্মা ওয়া বিহামদিকা, ওয়া তাবারাকাসমুকা, ওয়া তা'আলা জাদ্দুকা, ওয়া লা ইলাহা গাইরুক।", "হে আল্লাহ! আপনি পবিত্র এবং আপনারই প্রশংসা। আপনার নাম বরকতময়, আপনার মর্যাদা অতি উচ্চ এবং আপনি ছাড়া কোনো সত্য ইলাহ নেই।"),
            DoaItem("২। সূরা ফাতিহা পড়ার আগে", "أَعُوذُ بِاللَّهِ مِنَ الشَّيْطَانِ الرَّجِيمِ\nبِسْمِ اللَّهِ الرَّحْمٰنِ الرَّحِيمِ", "আউযু বিল্লাহি মিনাশ শাইত্বানির রাজীম।\nবিসমিল্লাহির রাহমানির রাহীম।", "আমি বিতাড়িত শয়তান থেকে আল্লাহর আশ্রয় চাই।\nপরম করুণাময়, অতি দয়ালু আল্লাহর নামে।"),
            DoaItem("৩। রুকুর তাসবিহ", "سُبْحَانَ رَبِّيَ الْعَظِيمِ", "সুবহানা রব্বিয়াল আযীম।", "আমার মহান রব পবিত্র।"),
            DoaItem("৪। রুকু থেকে ওঠার সময়", "سَمِعَ اللَّهُ لِمَنْ حَمِدَهُ\nرَبَّنَا وَلَكَ الْحَمْدُ", "সামিআল্লাহু লিমান হামিদাহ।\nরাব্বানা ওয়া লাকাল হামদ।", "যে আল্লাহর প্রশংসা করে, আল্লাহ তার প্রশংসা শুনেন।\nহে আমাদের রব! সমস্ত প্রশংসা আপনারই জন্য।"),
            DoaItem("৫। সিজদার তাসবিহ", "سُبْحَانَ رَبِّيَ الْأَعْلَى", "সুবহানা রব্বিয়াল আ'লা।", "আমার সর্বোচ্চ রব পবিত্র।"),
            DoaItem("৬। দুই সিজদার মাঝখানের দোয়া", "رَبِّ اغْفِرْ لِي", "রাব্বিগফির লী।", "হে আমাদের রব! আমাকে ক্ষমা করুন।"),
            DoaItem("৭। আত্তাহিয়্যাতু", "التَّحِيَّاتُ لِلَّهِ وَالصَّلَوَاتُ وَالطَّيِّبَاتُ،\n\nالسَّلَامُ عَلَيْكَ أَيُّهَا النَّبِيُّ وَرَحْمَةُ اللَّهِ وَبَرَكَاتُهُ،\n\nالسَّلَامُ عَلَيْنَا وَعَلَى عِبَادِ اللَّهِ الصَّالِحِينَ،\n\nأَشْهَدُ أَنْ لَا إِلٰهَ إِلَّا اللَّهُ،\n\nوَأَشْهَدُ أَنَّ مُحَمَّدًا عَبْدُهُ وَرَسُولُهُ", "আত্তাহিয়্যাতু লিল্লাহি ওয়াস সালাওয়াতু ওয়াত তায়্যিবাত। আসসালামু আলাইকা আইয়ুহান নাবিয়্যু ওয়া রাহমাতুল্লাহি ওয়া বারাকাতুহ। আসসালামু আলাইনা ওয়া আলা ইবাদিল্লাহিস সালিহীন। আশহাদু আল্লা ইলাহা ইল্লাল্লাহু, ওয়া আশহাদু আন্না মুহাম্মাদান আবদুহু ওয়া রাসূলুহ।", "সকল সম্মান, ইবাদত ও পবিত্র বিষয় আল্লাহর জন্য। হে নবী! আপনার ওপর শান্তি, আল্লাহর রহমত ও বরকত বর্ষিত হোক। আমাদের এবং আল্লাহর নেক বান্দাদের ওপর শান্তি বর্ষিত হোক। আমি সাক্ষ্য দিচ্ছি, আল্লাহ ছাড়া কোনো সত্য ইলাহ নেই এবং আমি সাক্ষ্য দিচ্ছি যে, মুহাম্মদ ﷺ তাঁর বান্দা ও রাসূল।"),
            DoaItem("৮। দরুদে ইবরাহিম", "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا صَلَّيْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ\n\nاللَّهُمَّ بَارِكْ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ، كَمَا بَارَكْتَ عَلَى إِبْرَاهِيمَ وَعَلَى آلِ إِبْرَاهِيمَ، إِنَّكَ حَمِيدٌ مَجِيدٌ", "আল্লাহুম্মা সাল্লি আলা মুহাম্মadiও ওয়া আলা আলি মুহাম্মাদ, কামা সাল্লাইতা আলা ইবরাহীما ওয়া আলা আলি ইবরাহীما, ইন্নাকা হামিদুম মাজীদ।\n\nআল্লাহুম্মা বারিক আলা মুহাম্মাদিও ওয়া আলা আলি মুহাম্মাদ, কামা বারাকতা আলা ইবরাহীما ওয়া আলা আলি ইবরাহীما, ইন্নাকা হামিদুম মাজীদ।", "হে আল্লাহ! মুহাম্মদ ﷺ এবং তাঁর পরিবারের ওপর রহমত বর্ষণ করুন, যেমন আপনি ইবরাহীম ও তাঁর পরিবারের ওপর রহমত বর্ষণ করেছেন। নিশ্চয়ই আপনি প্রশংসিত ও মহিমান্বিত। হে আল্লাহ! মুহাম্মদ ﷺ এবং তাঁর পরিবারের ওপর বরকত দিন, যেমন আপনি ইবরাহীম ও তাঁর পরিবারের ওপর বরকত দিয়েছেন।"),
            DoaItem("৯। দোয়া মাসুরা", "اللَّهُمَّ إِنِّي ظَلَمْتُ نَفْسِي ظُلْمًا كَثِيرًا، وَلَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ، فَاغْفِرْ لِي مَغْفِرَةً مِنْ عِنْدِكَ، وَارْحَمْنِي، إِنَّكَ أَنْتَ الْغَفُورُ الرَّحِيمُ", "আল্লাহুম্মা ইন্নী যালামতু নাফসী যুলমান কাছীরা, ওয়ালা ইয়াগফিরুয যুনূবা ইল্লা আন্তা, ফাগফির লী মাগফিরাতাম মিন ইন্দিকা, ওয়ারহামনী, ইন্নাকা আন্তাল গফুরুর রাহীম।", "হে আল্লাহ! আমি নিজের ওপর অনেক জুলুম করেছি। আপনি ছাড়া কেউ গুনাহ ক্ষমা করতে পারে না। তাই আমাকে আপনার পক্ষ থেকে ক্ষমা করুন এবং আমাদের প্রতি দয়া করুন। নিশ্চয়ই আপনি ক্ষমাশীল, পরম দয়ালু।"),
            DoaItem("১০। সালামের আগে গুরুত্বপূর্ণ দোয়া", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ عَذَابِ جَهَنَّمَ، وَمِنْ عَذَابِ الْقَبْرِ، وَمِنْ فِتْنَةِ الْمَحْيَا وَالْمَمَاتِ، وَمِنْ شَرِّ فِتْنَةِ الْمَسِيحِ الدَّجَّالِ", "আল্লাহুম্মা ইন্নী আউযু বিকা মিন আযাবি জাহান্নাম, ওয়া মিন আযাবিল কবর, ওয়া মিন ফিতনাতিল মাহইয়া ওয়াল মামাত, ওয়া মিন শাররি ফিতনাতিল মাসীহিদ দাজ্জাল।", "হে আল্লাহ! আমি আপনার কাছে জাহান্নামের আযাব, কবরের আযাব, জীবন ও মৃত্যুর ফিতনা এবং মাসীহ দাজ্জালের ফিতনার অনিষ্ট থেকে আশ্রয় চাই।"),
            DoaItem("১১। প্রাপ্তবয়স্ক পুরুষ ও নারীর জানাযার দোয়া", "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ، وَعَافِهِ وَاعْفُ عَنْهُ، وَأَكْرِمْ نُزُلَهُ، وَوَسِّعْ مُدْخَلَهُ، وَاغْسِلْهُ بِالْمَاءِ وَالثَّلْجِ وَالْبَرَدِ، وَنَقِّهِ مِنَ الْخَطَايَا كَمَا نَقَّيْتَ الثَّوْبَ الْأَبْيَضَ مِنَ الدَّنَسِ، وَأَبْدِلْهُ دَارًا خَيْرًا مِنْ دَارِهِ، وَأَهْلًا خَيْرًا مِنْ أَهْلِهِ، وَزَوْجًا خَيْرًا مِنْ زَوْجِهِ، وَأَدْخِلْهُ الْجَنَّةَ، وَأَعِذْهُ مِنْ عَذَابِ الْقَبْرِ وَعَذَابِ النَّارِ", "আল্লাহুম্মাগفیر লাহু ওয়ারহামহু, ওয়া আফিহি ওয়া'ফু আনহু, ওয়া আকরিম নুযুলাহু, ওয়া ওয়াসসি' মুদখালাহু, ওয়াগসিলহু বিল মায়ি ওয়াস সালজি ওয়াল বারাদ, ওয়া নাক্কিহি মিনাল খাতায়া কামা নাক্কাইতাস সাওবাল আবইয়াদা মিনাদ দানাস, ওয়া আবদিলহু দারান খাইরাম মিন দারিহি, ওয়া আহলান খাইরাম মিন আহলিহি, ওয়া যাওজান খাইরাম মিন যাওজিহি, ওয়া আদখিলহুল জান্নাহ, ওয়া আ'ইযহু মিন আযাবিল কবরি ওয়া আযাবিন্নার।", "হে আল্লাহ! তাকে ক্ষমা করুন, তার প্রতি দয়া করুন, তাকে নিরাপদ রাখুন এবং তাকে ক্ষমা করে দিন। তার আতিথেয়তা উত্তম করুন, তার প্রবেশস্থান প্রশস্ত করুন, পানি, বরফ ও শিলার মাধ্যমে তাকে ধৌত করুন। তার গুনাহগুলো এমনভাবে পরিষ্কার করুন, যেমন সাদা কাপড় ময়লা থেকে পরিষ্কার করা হয়। তাকে তার ঘরের চেয়ে উত্তম ঘর, তার পরিবারের চেয়ে উত্তম পরিবার ও উত্তম সঙ্গী দান করুন। তাকে জান্নাতে প্রবেশ করান এবং কবর ও জাহান্নামের আযাব থেকে রক্ষা করুন।"),
            DoaItem("১২। জানাযার সংক্ষিপ্ত ও جامع দোয়া", "اللَّهُمَّ اغْفِرْ لِحَيِّنَا وَمَيِّتِنَا، وَشَاهِدِنَا وَغَائِبِنَا، وَصَغِيرِنَا وَكَبِيرِنَا، وَذَكَرِنَا وَأُنْثَانَا، اللَّهُمَّ مَنْ أَحْيَيْتَهُ مِنَّا فَأَحْيِهِ عَلَى الْإِسْلَامِ، وَمَنْ تَوَفَّيْتَهُ مِنَّا فَتَوَفَّهُ عَلَى الْإِيمَانِ", "আল্লাহুম্মাগفیر লিহাইয়্যিনা ওয়া মাইয়্যিতিনা, ওয়া শাহিদিনা ওয়া গায়িবিনা, ওয়া সাগীরিনা ওয়া কাবীরিনা, ওয়া যাকারিনা ওয়া উনসানা। আল্লাহুম্মা মান আহইয়াইতাহু মিননা ফা আহইহি আলাল ইসলাম, ওয়া মান তাওয়াফ্ফাইতাহু মিননা ফাতাওয়াফ্ফাহু আলাল ঈমান।", "হে আল্লাহ! আমাদের জীবিত ও মৃত, উপস্থিত ও অনুপস্থিত, ছোট ও বড়, পুরুষ ও নারী—সকলকে ক্ষমা করুন। আমাদের মধ্যে যাকে জীবিত রাখবেন তাকে ইসলামের ওপর জীবিত রাখুন এবং যাকে মৃত্যু দেবেন তাকে ঈমানের ওপর মৃত্যু দিন।"),
            DoaItem("১৩। ছেলে শিশুর জানাযার দোয়া", "اللَّهُمَّ اجْعَلْهُ لَنَا فَرَطًا، وَاجْعَلْهُ لَنَا أَجْرًا وَذُخْرًا، وَاجْعَلْهُ لَنَا شَافِعًا وَمُشَفَّعًا", "আল্লাহুম্মাজ'আলহু লানা ফারাতান, ওয়াজ'আলহু লানা আজরান ওয়া যুখরান, ওয়াজ'আলহু লানা শাফি'আন ওয়া মুশাফ্ফা'আন।", "হে আল্লাহ! তাকে আমাদের জন্য অগ্রগামী বানান, তাকে আমাদের জন্য সওয়াব ও সঞ্চয় বানান এবং তাকে আমাদের জন্য সুপারিশকারী ও যার সুপারিশ কবুল করা হবে—এমন বানান।"),
            DoaItem("১৪। মেয়ে শিশুর জানাযার দোয়া", "اللَّهُمَّ اجْعَلْهَا لَنَا فَرَطًا، وَاجْعَلْهَا لَنَا أَجْرًا وَذُخْرًا، وَاجْعَلْهَا لَنَا شَافِعَةً وَمُشَفَّعَةً", "আল্লাহুম্মাজ'আলহা লানা ফারাতান, ওয়াজ'আলহা লানা আজরান ওয়া যুখরান, ওয়াজ'আলহা লানা শাফি'আতান ওয়া মুশাফ্ফা'আতান।", "হে আল্লাহ! তাকে আমাদের জন্য অগ্রগামী বানান, তাকে আমাদের জন্য সওয়াব ও সঞ্চয় বানান এবং তাকে আমাদের জন্য সুপারিশকারী ও যার সুপারিশ কবুল করা হবে—এমন বানান।")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showMainFolders()
    }
    
    override fun onBackPressed() {
        if (isInsideFolder) {
            showMainFolders()
        } else {
            finish()
        }
    }

    private fun showMainFolders() {
        isInsideFolder = false 

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(12), dp(12), dp(12), dp(12)); background = getCardDrawable()
        }
        top.addView(TextView(this).apply {
            text = "← হোম"; textSize = 16f; setTextColor(textMain); setPadding(0, 0, dp(12), 0)
            setOnClickListener { finish() }
        })
        top.addView(TextView(this).apply {
            text = "🤲 আমল ও দোয়া ভাণ্ডার"; textSize = 17f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)
        
        val scroll = ScrollView(this).apply { isFillViewport = true }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(16), dp(14), dp(80)) }

        content.addView(TextView(this).apply {
            text = "মাসনুন আমল, মানযিল ও দৈনন্দিন জীবনের সকল দোয়া"
            setTextColor(textSub); textSize = 15f; setPadding(0, 0, 0, dp(16))
        })

        content.addView(createAmolPdfCard(masnunPdf))
        content.addView(createAmolPdfCard(manzilPdf))

        listOf(folder3, folder4).forEach { folder ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; background = getCardDrawable()
                setPadding(dp(14), dp(14), dp(14), dp(14))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
            }
            card.addView(TextView(this).apply {
                text = folder.title; setTextColor(textMain); textSize = 18f; setTypeface(null, Typeface.BOLD)
            })
            card.addView(TextView(this).apply {
                text = folder.subtitle; setTextColor(textSub); textSize = 14f; setPadding(0, dp(6), 0, dp(14))
            })
            card.addView(Button(this).apply {
                text = folder.btnText; isAllCaps = false; setTextColor(Color.BLACK); background = getBtnDrawable(btnYellow)
                layoutParams = LinearLayout.LayoutParams(-1, dp(42))
                setOnClickListener { showFolderDetails(folder) }
            })
            content.addView(card)
        }

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf(
            Pair("🏠\nহোম", MainActivity::class.java),
            Pair("📿\nতাসবিহ", TasbihActivity::class.java),
            Pair("📚\nলাইব্রেরী", LibraryActivity::class.java),
            Pair("📖\nআমল", MasnunAmolActivity::class.java),
            Pair("📝\nনোটপ্যাড", NotepadActivity::class.java),
            Pair("🔄\nসিঙ্ক", null),
            Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java)
        )

        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("আমল")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@MasnunAmolActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@MasnunAmolActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@MasnunAmolActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> {}
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@MasnunAmolActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@MasnunAmolActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@MasnunAmolActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }

    private fun createAmolPdfCard(item: AmolPdfItem): View {
        val qCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = getCardDrawable()
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
        }
        qCard.addView(TextView(this).apply {
            text = if (item.fileName.contains("masnun")) "📁 ফোল্ডার ১: মাসনূন আমল" else "📁 ফোল্ডার ২: মানযিল আয়াত (সম্পূর্ণ)"
            setTextColor(textMain); textSize = 18f; setTypeface(null, Typeface.BOLD)
        })
        qCard.addView(TextView(this).apply {
            text = item.subtitle; setTextColor(textSub); textSize = 14f; setPadding(0, dp(4), 0, dp(10))
        })

        if (isFileExists(item.title)) {
            val qBtnRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum = 2f
                setPadding(0, dp(4), 0, dp(8))
            }
            qBtnRow.addView(Button(this).apply {
                text = "📖 পড়ুন"
                isAllCaps = false; setTextColor(Color.WHITE)
                background = getBtnDrawable(btnDark)
                layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { rightMargin = dp(6) }
                setOnClickListener { openPdf(item, 0) }
            })
            qBtnRow.addView(Button(this).apply {
                text = "🗑️ ডিলিট"
                isAllCaps = false; setTextColor(Color.WHITE)
                background = getBtnDrawable(btnRed)
                layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f).apply { leftMargin = dp(6) }
                setOnClickListener { deleteFileSilent(item.title) }
            })
            qCard.addView(qBtnRow)

            val continueLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(4), 0, 0)
            }
            val savedPage = getSharedPreferences("PdfLibrary", Context.MODE_PRIVATE).getInt(item.title, 0)
            continueLayout.addView(TextView(this).apply {
                text = "📖 পড়া চালিয়ে যান (পৃষ্ঠা ${bn(savedPage + 1)})"
                setTextColor(textSub); textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
                setOnClickListener { openPdf(item, savedPage) }
            })
            qCard.addView(continueLayout)
        } else {
            val dlBtn = Button(this).apply {
                text = "📥 ডাউনলোড করুন"
                isAllCaps = false; setTextColor(Color.BLACK)
                background = getBtnDrawable(btnYellow)
                layoutParams = LinearLayout.LayoutParams(-1, dp(42)).apply { topMargin = dp(4) }
            }
            dlBtn.setOnClickListener { startDownload(item, dlBtn) }
            qCard.addView(dlBtn)
        }
        return qCard
    }

    private fun startDownload(book: AmolPdfItem, btn: Button) {
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
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MasnunAmolActivity, "ডাউনলোড সফল হয়েছে!", Toast.LENGTH_SHORT).show()
                    showMainFolders()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btn.text = "📥 ডাউনলোড করুন"
                    btn.isEnabled = true
                    if (file.exists()) file.delete()
                    Toast.makeText(this@MasnunAmolActivity, "ডাউনলোড ব্যর্থ হয়েছে! ইন্টারনেট কানেকশন চেক করুন।", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openPdf(book: AmolPdfItem, page: Int) {
        val intent = Intent(this@MasnunAmolActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title)
        intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }

    private fun showFolderDetails(folder: DoaFolder) {
        isInsideFolder = true

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bgMain) }

        val top = LinearLayout(this).apply {
            gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8), dp(12), dp(8), dp(12)); background = getCardDrawable()
        }
        top.addView(TextView(this).apply {
            text = "← ফিরে যান"; textSize = 16f; setTextColor(textMain); setPadding(dp(10),0,dp(10),0)
            setOnClickListener { showMainFolders() }
        })
        top.addView(TextView(this).apply {
            text = folder.title.replace("📁 ", ""); textSize = 18f; setTextColor(textYellow); setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top)

        val scroll = ScrollView(this).apply { isFillViewport = true }
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(80)) }

        list.addView(TextView(this).apply {
            text = "সংকলনে: সাব্বির আহমাদ"
            setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER; setPadding(0, 0, 0, dp(16))
        })

        folder.doasList.forEach { doa ->
            val vCard = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; background = getCardDrawable()
                setPadding(dp(14), dp(16), dp(14), dp(16))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(14) }
            }
            
            vCard.addView(TextView(this).apply {
                text = doa.title; setTextColor(textYellow); textSize = 16f; setTypeface(null, Typeface.BOLD)
                background = getTitleBoxDrawable(); setPadding(dp(10), dp(8), dp(10), dp(8))
                layoutParams = LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(12) }
            })
            
            vCard.addView(TextView(this).apply {
                text = doa.arabic
                setTextColor(textMain)
                textSize = 21f 
                gravity = Gravity.CENTER
                setLineSpacing(dp(6).toFloat(), 1.3f)
                setPadding(0, 0, 0, dp(12))
            })

            if (doa.pron != null) {
                vCard.addView(TextView(this).apply {
                    val pText = if(doa.pron.startsWith("উচ্চারণ:")) doa.pron else "উচ্চারণ: ${doa.pron}"
                    text = pText; setTextColor(textPron); textSize = 15f; setPadding(0, 0, 0, dp(6))
                })
            }

            if (doa.meaning != null) {
                vCard.addView(TextView(this).apply {
                    val mText = if(doa.meaning.startsWith("অর্থ:")) doa.meaning else "অর্থ: ${doa.meaning}"
                    text = mText; setTextColor(textMeaning); textSize = 15f
                })
            }

            list.addView(vCard)
        }

        list.addView(Button(this).apply {
            text = "← ফোল্ডার তালিকায় ফিরে যান"
            isAllCaps = false; setTextColor(textMain); background = getTitleBoxDrawable()
            layoutParams = LinearLayout.LayoutParams(-1, dp(45)).apply { topMargin = dp(10) }
            setOnClickListener { showMainFolders() }
        })

        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottomNav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#0F172A"))
            setPadding(dp(2), dp(4), dp(2), dp(4))
            elevation = dp(8).toFloat()
        }

        val navItems = listOf(
            Pair("🏠\nহোম", MainActivity::class.java),
            Pair("📿\nতাসবিহ", TasbihActivity::class.java),
            Pair("📚\nলাইব্রেরী", LibraryActivity::class.java),
            Pair("📖\nআমল", MasnunAmolActivity::class.java),
            Pair("📝\nনোটপ্যাড", NotepadActivity::class.java),
            Pair("🔄\nসিঙ্ক", null),
            Pair("👤\nপ্রোফাইল", ProfileSettingsActivity::class.java)
        )

        navItems.forEach { (label, _) ->
            bottomNav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                minHeight = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                gravity = Gravity.CENTER
                setTextColor(if (label.contains("আমল")) Color.parseColor("#10B981") else Color.parseColor("#9CA3AF"))
                background = GradientDrawable()
                setOnClickListener {
                    when {
                        label.contains("হোম") -> { startActivity(Intent(this@MasnunAmolActivity, MainActivity::class.java)); finish() }
                        label.contains("তাসবিহ") -> { startActivity(Intent(this@MasnunAmolActivity, TasbihActivity::class.java)); finish() }
                        label.contains("লাইব্রেরী") -> { startActivity(Intent(this@MasnunAmolActivity, LibraryActivity::class.java)); finish() }
                        label.contains("আমল") -> { showMainFolders() }
                        label.contains("নোটপ্যাড") -> { startActivity(Intent(this@MasnunAmolActivity, NotepadActivity::class.java)); finish() }
                        label.contains("সিঙ্ক") -> { Toast.makeText(this@MasnunAmolActivity, "সিঙ্ক করা হয়েছে!", Toast.LENGTH_SHORT).show() }
                        label.contains("প্রোফাইল") -> { startActivity(Intent(this@MasnunAmolActivity, ProfileSettingsActivity::class.java)); finish() }
                    }
                }
            }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { setMargins(dp(2), 0, dp(2), 0) })
        }
        root.addView(bottomNav, LinearLayout.LayoutParams(-1, dp(60)))

        setContentView(root)
    }
}
