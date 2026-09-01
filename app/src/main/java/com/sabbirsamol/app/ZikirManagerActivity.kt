package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class ZikirManagerActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("tasbih_list", Context.MODE_PRIVATE) }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun items(): MutableList<String> = prefs.getStringSet("items", linkedSetOf("সুবহানাল্লাহ","আলহামদুলিল্লাহ","আল্লাহু আকবার","আস্তাগফিরুল্লাহ","লা ইলাহা ইল্লাল্লাহ"))!!.toMutableList()
    private fun targets(): MutableMap<String,Int> = prefs.getStringSet("targets", emptySet())!!.associate { val p=it.split("|",limit=2); p[0] to (p.getOrNull(1)?.toIntOrNull()?:0) }.toMutableMap()
    private fun save(a:List<String>,t:Map<String,Int>){prefs.edit().putStringSet("items",a.toSet()).putStringSet("targets",t.map{ "${it.key}|${it.value}" }.toSet()).apply()}

    override fun onCreate(b:Bundle?){super.onCreate(b);showList()}
    private fun showList(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(16),dp(16),dp(16));setBackgroundColor(Color.rgb(7,45,31))}
        root.addView(TextView(this).apply{text="📁 জিকির লিস্ট";textSize=24f;setTextColor(Color.WHITE);gravity=Gravity.CENTER;setPadding(0,0,0,dp(14))},LinearLayout.LayoutParams(-1,dp(55)))
        val scroll=ScrollView(this);val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val a=items();val t=targets()
        for(z in a){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(4),dp(8),dp(4));setBackgroundColor(Color.rgb(15,104,69))}
            row.addView(TextView(this).apply{text="$z\n🎯 ${if((t[z]?:0)>0)t[z] else "Unlimited"}";textSize=17f;setTextColor(Color.WHITE);gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,dp(68),1f))
            row.addView(Button(this).apply{text="✏️";setOnClickListener{edit(z)}},LinearLayout.LayoutParams(dp(58),dp(55)))
            row.addView(Button(this).apply{text="🗑";setOnClickListener{delete(z)}},LinearLayout.LayoutParams(dp(58),dp(55)))
            row.setOnClickListener{select(z)}
            list.addView(row,LinearLayout.LayoutParams(-1,dp(76)).apply{bottomMargin=dp(8)})
        }
        scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Button(this).apply{text="➕ নতুন জিকির যোগ করুন";textSize=16f;setOnClickListener{add()}},LinearLayout.LayoutParams(-1,dp(58)))
        setContentView(root)
    }
    private fun select(z:String){getSharedPreferences("tasbih_only",0).edit().putString("selected",z).putInt("count",0).apply();finish()}
    private fun add(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,dp(12),0)}
        val name=EditText(this).apply{hint="জিকিরের নাম"};val target=EditText(this).apply{hint="Target (0 = Unlimited)";inputType=InputType.TYPE_CLASS_NUMBER}
        box.addView(name);box.addView(target)
        AlertDialog.Builder(this).setTitle("নতুন জিকির").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("যোগ"){_,_->val n=name.text.toString().trim();if(n.isNotEmpty()){val a=items();val t=targets();a.add(n);t[n]=target.text.toString().toIntOrNull()?:0;save(a,t);showList()}}.show()
    }
    private fun edit(old:String){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,dp(12),0)}
        val name=EditText(this).apply{setText(old);selectAll()};val target=EditText(this).apply{hint="Target (0 = Unlimited)";inputType=InputType.TYPE_CLASS_NUMBER;setText(if((targets()[old]?:0)>0)targets()[old].toString() else "")}
        box.addView(name);box.addView(target)
        AlertDialog.Builder(this).setTitle("জিকির ও Target Edit").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("Save"){_,_->val n=name.text.toString().trim();if(n.isNotEmpty()){val a=items();val t=targets();val i=a.indexOf(old);if(i>=0)a[i]=n;t.remove(old);t[n]=target.text.toString().toIntOrNull()?:0;save(a,t);showList()}}.show()
    }
    private fun delete(z:String){AlertDialog.Builder(this).setTitle("জিকির মুছবেন?").setMessage(z).setNegativeButton("না",null).setPositiveButton("হ্যাঁ"){_,_->val a=items();a.remove(z);if(a.isEmpty())a.add("সুবহানাল্লাহ");val t=targets();t.remove(z);save(a,t);showList()}.show()}
}
