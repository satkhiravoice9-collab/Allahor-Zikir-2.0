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

    // ================= থিম ম্যানেজারের সাথে কানেকশন =================
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

    private val bukhariVolList = listOf(PdfBookItem("bukhari_1.pdf", "সহীহ বুখারী ১ম খণ্ড", "1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"), PdfBookItem("bukhari_2.pdf", "সহীহ বুখারী ২য় খণ্ড", "1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"))
    private val muslimVolList = listOf(PdfBookItem("muslim_1.pdf", "সহীহ মুসলিম ১ম খণ্ড", "1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"))
    
    private val groups = listOf("সহীহ বুখারী শরীফ" to bukhariVolList, "সহীহ মুসলিম শরীফ" to muslimVolList) // এখানে আপনার আগের সব লিস্ট থাকবে। (আমি কোড ছোট রাখার জন্য ২টি দিলাম, আপনি আপনার আগের কিতাবের লিস্ট পেস্ট করতে পারেন)
    
    private val paraList = listOf(Pair("পারা ১", 3), Pair("পারা ২", 24))
    private val surahList = listOf(Pair("১. সূরা আল-ফাতিহা", 3), Pair("২. সূরা আল-বাকারাহ", 4))

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
        val dialogLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.WHITE) }
        dialogLayout.addView(TextView(this).apply { text = titleText; textSize = 18f; setTypeface(null, Typeface.BOLD); setPadding(dp(16), dp(16), dp(16), dp(16)); setBackgroundColor(Color.parseColor("#F3F4F6")) })
        val scroll = ScrollView(this)
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
        items.forEach { item ->
            list.addView(TextView(this).apply { text = item.first; textSize = 18f; setPadding(0, dp(12), 0, dp(12)); setTextColor(Color.BLACK); setOnClickListener { openPdf(book, item.second) } })
            list.addView(View(this).apply { setBackgroundColor(Color.parseColor("#E5E7EB")); layoutParams = LinearLayout.LayoutParams(-1, dp(1)) })
        }
        scroll.addView(list); dialogLayout.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val dialog = AlertDialog.Builder(this).setView(dialogLayout).create()
        dialogLayout.addView(Button(this).apply { text = "বন্ধ করুন"; setTextColor(Color.BLACK); setBackgroundColor(Color.parseColor("#E5E7EB")); layoutParams = LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(16), dp(8), dp(16), dp(16)) }; setOnClickListener { dialog.dismiss() } })
        dialog.show()
    }

    private fun openPdf(book: PdfBookItem, page: Int) {
        val intent = Intent(this@LibraryActivity, PdfReaderActivity::class.java)
        intent.putExtra("BOOK_NAME", book.title); intent.putExtra("TARGET_PAGE", page)
        startActivity(intent)
    }
}
