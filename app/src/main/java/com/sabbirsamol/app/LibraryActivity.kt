package com.sabbirsamol.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity

data class PdfBookItem(val fileName: String, val title: String, val id: String)

class LibraryActivity : ComponentActivity() {
    private fun dp(v: Int)=(v*resources.displayMetrics.density).toInt()
    private fun bg(c:Int,r:Int=18)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private val quran=PdfBookItem("quran_full.pdf","পবিত্র কুরআন শরীফ (সম্পূর্ণ ৩০ পারা)","1FChVXAx1JKFs_0AFL9TYxqgO1Yc34Z7Q")
    private val groups=LibraryBooks.groups
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);showLibrary()}
    private fun showLibrary(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(248,250,247))};val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(8),dp(8),dp(8));background=bg(Color.rgb(16,83,54),0)};top.addView(TextView(this).apply{text="←";textSize=20f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setOnClickListener{finish()}},LinearLayout.LayoutParams(dp(54),dp(50)));top.addView(TextView(this).apply{text="📚 ইসলামিক লাইব্রেরি";textSize=20f;setTextColor(Color.WHITE);setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER},LinearLayout.LayoutParams(0,dp(50),1f));root.addView(top);val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(10),dp(10),dp(10))};list.addView(bookButton("📖 ${quran.title}"){open(quran)});groups.forEach{(title,books)->list.addView(bookButton("📚 $title • ${books.size} খণ্ড"){showVolumes(title,books)})};root.addView(ScrollView(this).apply{addView(list)},LinearLayout.LayoutParams(-1,0,1f));setContentView(root)}
    private fun bookButton(t:String,a:()->Unit)=TextView(this).apply{text=t;textSize=18f;setTextColor(Color.rgb(20,55,40));setTypeface(null,Typeface.BOLD);gravity=Gravity.CENTER_VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));background=bg(Color.WHITE,16);setOnClickListener{a()};layoutParams=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,0,0,dp(10))}}
    private fun showVolumes(title:String,books:List<PdfBookItem>){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(248,250,247));setPadding(dp(10),dp(10),dp(10),dp(10))};root.addView(TextView(this).apply{text=title;textSize=22f;setTypeface(null,Typeface.BOLD)});root.addView(TextView(this).apply{text="← লাইব্রেরিতে ফিরে যান";textSize=16f;setTextColor(Color.rgb(20,83,54));setPadding(dp(8),dp(14),dp(8),dp(14));setOnClickListener{showLibrary()}});books.forEach{root.addView(bookButton(it.title){open(it)})};setContentView(ScrollView(this).apply{addView(root)})}
    private fun open(book:PdfBookItem){startActivity(Intent(this,PdfReaderActivity::class.java).putExtra(PdfReaderActivity.EXTRA_BOOK_ID,book.id))}
}