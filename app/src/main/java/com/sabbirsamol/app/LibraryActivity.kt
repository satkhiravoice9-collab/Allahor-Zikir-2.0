package com.sabbirsamol.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class LibraryActivity : ComponentActivity() {
    data class PdfBookItem(val title: String, val driveId: String)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(c: Int, r: Int = 16) = GradientDrawable().apply { setColor(c); cornerRadius = dp(r).toFloat() }

    private val quran = PdfBookItem("পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা)", "1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")
    private val groups = linkedMapOf(
        "সহীহ বুখারী" to listOf(
            PdfBookItem("সহীহ বুখারী ১ম খণ্ড","1PI-aFDlFgrbTaqvVAtvOeudOowWCKLe5"),
            PdfBookItem("সহীহ বুখারী ২য় খণ্ড","1velsDhV5jClX66XH2v9XXnYzvfU1RmKd"),
            PdfBookItem("সহীহ বুখারী ৩য় খণ্ড","14n-OUw5kMleYOQ1hpZK3ZlwCOgpEUdRR"),
            PdfBookItem("সহীহ বুখারী ৪র্থ খণ্ড","1i--rcV0Lg5hY5eStwqkFIKjCYYY9dYgd"),
            PdfBookItem("সহীহ বুখারী ৫ম খণ্ড","1H4jtEHhwNgUOVv4V3WMCWgJclj1VLpDX"),
            PdfBookItem("সহীহ বুখারী ৬ষ্ঠ খণ্ড","1gEnwH7LN03nSq0YneNxEoSvQjnI7ZaGb"),
            PdfBookItem("সহীহ বুখারী ৭ম খণ্ড","1u701BmLJvQg-yBBw8XwDXgtOU4Y1wHe9"),
            PdfBookItem("সহীহ বুখারী ৮ম খণ্ড","1nmvOSFLVfDJk3WYaSYptl8X-AP8VhaiA"),
            PdfBookItem("সহীহ বুখারী ৯ম খণ্ড","1m72pzXcUtdDO1DmSje1BeVHT4AT1ra9I"),
            PdfBookItem("সহীহ বুখারী ১০ম খণ্ড","1ZdRNfsOFdMtTW7ra_Px6l7B3huiwBrI8")
        ),
        "সহীহ মুসলিম" to listOf(
            PdfBookItem("সহীহ মুসলিম ১ম খণ্ড","1010GaPGDVYcq_zYzLS6zrD8Pv5pDfYJA"), PdfBookItem("সহীহ মুসলিম ২য় খণ্ড","1vQU2G5qbNPouVSp3aRwkhg7KrztasfTG"), PdfBookItem("সহীহ মুসলিম ৩য় খণ্ড","11Ul0Laj9YGGV37KYnep73RCUELz0yvU8"), PdfBookItem("সহীহ মুসলিম ৪র্থ খণ্ড","16cTetFECwjEPtSHeajv_WTQ11LTATtJp"), PdfBookItem("সহীহ মুসলিম ৫ম খণ্ড","129O3bHeq2O1xY8MMsxGPJNdp1zgQ838z"), PdfBookItem("সহীহ মুসলিম ৬ষ্ঠ খণ্ড","1o4mAG-Qx6KSumsohKgUK2k8S7TAy3PKp"), PdfBookItem("সহীহ মুসলিম ৭ম খণ্ড","121hx1VQv0HCZnztDFrhJrP4XPBuEzuQW"), PdfBookItem("সহীহ মুসলিম ৮ম খণ্ড","1mjTLc_svuuKcUlnQXRu0e3PEAZUzn7_u")
        ),
        "সুনান আবু দাউদ" to listOf(
            PdfBookItem("সুনান আবু দাউদ ১ম খণ্ড","15RGRxSsJeKSfITiCU20AOm477jrXf7tX"), PdfBookItem("সুনান আবু দাউদ ২য় খণ্ড","1LLjLCwj-CXLv6RHaTSHpfKK4zPBj6mRn"), PdfBookItem("সুনান আবু দাউদ ৩য় খণ্ড","1KD64uPxeVDliawvEKwOFNaNfH883rR6X"), PdfBookItem("সুনান আবু দাউদ ৪র্থ খণ্ড","1iuOGcHAegMxqJ3VZ7HiLNXTbz8a-iooI")
        ),
        "জামে আত-তিরমিযী" to listOf(
            PdfBookItem("জামে আত-তিরমিযী ১ম খণ্ড","1tN-N6skALr_G83cXreKmo0mRIqW_siGx"), PdfBookItem("জামে আত-তিরমিযী ২য় খণ্ড","1FCwMfqmzNHHrbfQr3ho502obdHAaSGr4"), PdfBookItem("জামে আত-তিরমিযী ৩য় খণ্ড","1Z5FxEDR_dcDFVgWTcx5lxfijVUSAxjcN"), PdfBookItem("জামে আত-তিরমিযী ৪র্থ খণ্ড","1yIiNsyBpYnJV5y8gnWAor23VwKgpD4N6"), PdfBookItem("জামে আত-তিরমিযী ৫ম খণ্ড","18GvNtMs_VK9fb1_oCyHR4nWwz-_6W53h"), PdfBookItem("জামে আত-তিরমিযী ৬ষ্ঠ খণ্ড","1D1dnryvH8iOY69ixBtGPQuF3_kmPRgu_")
        ),
        "সুনান আন-নাসায়ী" to listOf(
            PdfBookItem("সুনান আন-নাসায়ী ১ম খণ্ড","1VH9AkmVv3apCXYPBRu0mI7qmNsU66sZZ"), PdfBookItem("সুনান আন-নাসায়ী ২য় খণ্ড","1t_F71oekP3F1Dc2SDY2I-L5eyDgtPcIR"), PdfBookItem("সুনান আন-নাসায়ী ৩য় খণ্ড","1mjp-ZSvvBsLzkDRXj87oSrH88ifzLIKF"), PdfBookItem("সুনান আন-নাসায়ী ৪র্থ খণ্ড","19yY0S3jLumKpxff3n_Kf6qoX8A6Fmwqi")
        ),
        "সুনান ইবনে মাজাহ" to listOf(
            PdfBookItem("সুনান ইবনে মাজাহ ১ম খণ্ড","12nAeV_DjOOCK2WJmrFSSIPyf2fcAyWzd"), PdfBookItem("সুনান ইবনে মাজাহ ২য় খণ্ড","1B9ZNJrMmTW1sPXtAX3n2VZj4j4xjG-U6"), PdfBookItem("সুনান ইবনে মাজাহ ৩য় খণ্ড","1GVexhALQ3ISCd241Zj7PkgpZG-116Q59")
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showLibrary() }

    private fun showLibrary() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(248,250,247)) }
        val top = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(8),dp(8),dp(8),dp(8)); background = bg(Color.rgb(16,83,54),0) }
        top.addView(Button(this).apply { text="←"; textSize=20f; setTextColor(Color.WHITE); backgroundColor=Color.TRANSPARENT; setOnClickListener { finish() } }, LinearLayout.LayoutParams(dp(54),dp(50)))
        top.addView(TextView(this).apply { text="📚 ইসলামিক লাইব্রেরি"; textSize=20f; setTextColor(Color.WHITE); setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER }, LinearLayout.LayoutParams(0,dp(50),1f))
        root.addView(top)
        val list = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10)) }
        list.addView(bookButton("📖 ${quran.title}") { open(quran) })
        groups.forEach { (title, books) -> list.addView(bookButton("📚 $title • ${books.size} খণ্ড") { showVolumes(title, books) }) }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
    }

    private fun showVolumes(title: String, books: List<PdfBookItem>) {
        val root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(248,250,247)) }
        val back = Button(this).apply { text="← $title"; isAllCaps=false; textSize=19f; setTextColor(Color.WHITE); background=bg(Color.rgb(16,83,54),0); setOnClickListener { showLibrary() } }
        root.addView(back, LinearLayout.LayoutParams(-1,dp(58)))
        val list=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(10),dp(10),dp(10),dp(10)) }
        books.forEach { list.addView(bookButton("📄 ${it.title}") { open(it) }) }
        root.addView(ScrollView(this).apply { addView(list) }, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
    }

    private fun bookButton(text:String, action:()->Unit)=TextView(this).apply { this.text=text; textSize=17f; setTextColor(Color.rgb(28,47,38)); setTypeface(null,Typeface.BOLD); gravity=Gravity.CENTER_VERTICAL; setPadding(dp(14),0,dp(14),0); background=bg(Color.WHITE,14); elevation=dp(2).toFloat(); setOnClickListener { action() } }.also { it.layoutParams=LinearLayout.LayoutParams(-1,dp(62)).apply { bottomMargin=dp(9) } }
    private fun open(item: PdfBookItem) { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/file/d/${item.driveId}/view"))) }
    override fun onBackPressed() { showLibrary() }
}
