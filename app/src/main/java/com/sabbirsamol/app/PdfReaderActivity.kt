package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class PdfReaderActivity : ComponentActivity() {
    companion object { const val EXTRA_BOOK_ID = "book_id" }
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var status: TextView
    private var book: LibraryBook? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id=intent.getStringExtra(EXTRA_BOOK_ID).orEmpty(); book=LibraryBooks.all.firstOrNull{it.id==id}
        if(book==null){showMessage("বইটি পাওয়া যায়নি");return}; showLoading(); openOrDownload(book!!)
    }
    private fun showLoading(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(30,30,30,30)};root.addView(ProgressBar(this));status=TextView(this).apply{text="PDF প্রস্তুত হচ্ছে...";textSize=18f;gravity=Gravity.CENTER;setPadding(0,20,0,0)};root.addView(status);setContentView(root)}
    private fun openOrDownload(b:LibraryBook){val file=File(filesDir,b.fileName);if(file.exists()&&file.length()>1024){openExternal(file);return};executor.execute{try{download(b.id,file){d,t->runOnUiThread{status.text=if(t>0)"ডাউনলোড হচ্ছে: ${d*100/t}%" else "PDF ডাউনলোড হচ্ছে..."}};runOnUiThread{openExternal(file)}}catch(e:Exception){file.delete();runOnUiThread{status.text="PDF ডাউনলোড করা যায়নি";AlertDialog.Builder(this).setTitle("ডাউনলোড ব্যর্থ").setMessage("Google Drive ফাইলটি Public/Anyone with the link হিসেবে শেয়ার করা আছে কি না পরীক্ষা করুন।").setPositiveButton("ঠিক আছে",null).show()}}}}
    private fun download(id:String,destination:File,progress:(Long,Long)->Unit){val c=(URL("https://drive.usercontent.google.com/download?id=$id&export=download&confirm=t").openConnection() as HttpURLConnection).apply{requestMethod="GET";connectTimeout=30000;readTimeout=60000;instanceFollowRedirects=true;setRequestProperty("User-Agent","Mozilla/5.0 (Android)")};try{if(c.responseCode !in 200..299)throw IllegalStateException("HTTP ${c.responseCode}");val total=c.contentLengthLong;BufferedInputStream(c.inputStream).use{input->FileOutputStream(destination).use{out->val buf=ByteArray(65536);var done=0L;while(true){val n=input.read(buf);if(n<0)break;out.write(buf,0,n);done+=n;progress(done,total)};out.flush()}};if(destination.length()<1024)throw IllegalStateException("Invalid PDF")}finally{c.disconnect()}}
    private fun openExternal(file:File){val uri=Uri.fromFile(file);try{startActivity(Intent(Intent.ACTION_VIEW).apply{setDataAndType(uri,"application/pdf");addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)})}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://drive.google.com/file/d/${book!!.id}/view")))}}
    private fun showMessage(m:String){setContentView(TextView(this).apply{text=m;textSize=18f;gravity=Gravity.CENTER;setPadding(30,30,30,30)})}
    override fun onDestroy(){executor.shutdownNow();super.onDestroy()}
}