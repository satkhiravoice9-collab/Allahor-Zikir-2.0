package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity

class ZikirManagerActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("tasbih_list", Context.MODE_PRIVATE) }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun card(c:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(18).toFloat()}
    private val colors=intArrayOf(Color.rgb(145,28,35),Color.rgb(12,105,65),Color.rgb(190,125,10),Color.rgb(25,75,145),Color.rgb(112,35,125),Color.rgb(185,65,22),Color.rgb(18,120,120),Color.rgb(88,65,30))
    private fun items():MutableList<String>{val d=linkedSetOf("সুবহানাল্লাহ","আলহামদুলিল্লাহ","আল্লাহু আকবার","আস্তাগফিরুল্লাহ","লা ইলাহা ইল্লাল্লাহ");return prefs.getStringSet("items",d)?.toMutableList()?:d.toMutableList()}
    private fun saveItems(a:List<String>){prefs.edit().putStringSet("items",a.toSet()).apply()}
    private fun target(s:String)=prefs.getInt("target_$s",0)

    override fun onCreate(b:Bundle?){super.onCreate(b);showList()}
    private fun showList(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(12),dp(12),dp(12));setBackgroundColor(Color.rgb(7,45,31))}
        val head=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        head.addView(TextView(this).apply{text="📁  জিকির লিস্ট";textSize=24f;setTextColor(Color.WHITE);setTypeface(null,1)},LinearLayout.LayoutParams(0,dp(55),1f))
        head.addView(Button(this).apply{text="‹";textSize=25f;contentDescription="ফিরে যান";setOnClickListener{finish()}},LinearLayout.LayoutParams(dp(60),dp(50)))
        root.addView(head)
        val scroll=ScrollView(this);val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        for((i,z) in items().withIndex()){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;background=card(colors[i%colors.size]);setPadding(dp(8),dp(4),dp(6),dp(4))}
            row.addView(TextView(this).apply{text="$z\n🎯 ${if(target(z)>0)target(z) else "Unlimited"}";textSize=16f;setTextColor(Color.WHITE);gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,dp(70),1f))
            row.addView(Button(this).apply{text="🎯";setOnClickListener{setTarget(z)}},LinearLayout.LayoutParams(dp(52),dp(52)))
            row.addView(Button(this).apply{text="✏️";setOnClickListener{edit(z)}},LinearLayout.LayoutParams(dp(52),dp(52)))
            row.addView(Button(this).apply{text="🗑";setOnClickListener{delete(z)}},LinearLayout.LayoutParams(dp(52),dp(52)))
            row.setOnClickListener{select(z)}
            list.addView(row,LinearLayout.LayoutParams(-1,dp(78)).apply{bottomMargin=dp(8)})
        }
        scroll.addView(list);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Button(this).apply{text="➕  নতুন জিকির যোগ করুন";textSize=16f;setOnClickListener{add()}},LinearLayout.LayoutParams(-1,dp(58)))
        setContentView(root)
    }
    private fun select(z:String){getSharedPreferences("tasbih_only",0).edit().putString("selected",z).putInt("count",0).apply();finish()}
    private fun add(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,dp(12),0)}
        val n=EditText(this).apply{hint="জিকিরের নাম"};val t=EditText(this).apply{hint="Target (0 = Unlimited)";inputType=InputType.TYPE_CLASS_NUMBER}
        box.addView(n);box.addView(t)
        AlertDialog.Builder(this).setTitle("নতুন জিকির").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("যোগ"){_,_->val s=n.text.toString().trim();if(s.isNotEmpty()){val a=items();if(!a.contains(s)){a.add(s);saveItems(a);prefs.edit().putInt("target_$s",t.text.toString().toIntOrNull()?:0).apply()};showList()}}.show()
    }
    private fun edit(old:String){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,dp(12),0)}
        val n=EditText(this).apply{setText(old);selectAll()};val t=EditText(this).apply{hint="Target (0 = Unlimited)";inputType=InputType.TYPE_CLASS_NUMBER;setText(if(target(old)>0)target(old).toString() else "")};box.addView(n);box.addView(t)
        AlertDialog.Builder(this).setTitle("জিকির ও Target Edit").setView(box).setNegativeButton("বাতিল",null).setPositiveButton("Save"){_,_->val s=n.text.toString().trim();if(s.isNotEmpty()){val a=items();val i=a.indexOf(old);if(i>=0)a[i]=s;saveItems(a);prefs.edit().remove("target_$old").putInt("target_$s",t.text.toString().toIntOrNull()?:0).apply();if(getSharedPreferences("tasbih_only",0).getString("selected","")==old)getSharedPreferences("tasbih_only",0).edit().putString("selected",s).apply();showList()}}.show()
    }
    private fun delete(s:String){AlertDialog.Builder(this).setTitle("জিকির মুছবেন?").setMessage(s).setNegativeButton("না",null).setPositiveButton("হ্যাঁ"){_,_->val a=items();a.remove(s);if(a.isEmpty())a.add("সুবহানাল্লাহ");saveItems(a);prefs.edit().remove("target_$s").apply();if(getSharedPreferences("tasbih_only",0).getString("selected","")==s)getSharedPreferences("tasbih_only",0).edit().putString("selected",a.first()).putInt("count",0).apply();showList()}}.show()}
    private fun setTarget(s:String){val e=EditText(this).apply{hint="0 = Unlimited";inputType=InputType.TYPE_CLASS_NUMBER;setText(if(target(s)>0)target(s).toString() else "")};AlertDialog.Builder(this).setTitle("🎯 $s এর Target").setView(e).setNegativeButton("বাতিল",null).setPositiveButton("Save"){_,_->prefs.edit().putInt("target_$s",e.text.toString().toIntOrNull()?:0).apply();showList()}.show()}
}
