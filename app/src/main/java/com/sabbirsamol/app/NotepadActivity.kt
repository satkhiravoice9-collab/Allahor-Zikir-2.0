package com.sabbirsamol.app

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity

class NotepadActivity : ComponentActivity() {
    private val prefs by lazy { getSharedPreferences("muslim_time", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showNotepad()
    }

    private fun showNotepad(){
        val edit=EditText(this).apply{setText(prefs.getString("note", ""));hint="আপনার নোট লিখুন...";minLines=8;gravity=Gravity.TOP}
        AlertDialog.Builder(this).setTitle("📝 নোটপ্যাড").setView(edit).setPositiveButton("💾 সেভ"){_,_->prefs.edit().putString("note",edit.text.toString()).apply();Toast.makeText(this,"নোট সেভ হয়েছে",Toast.LENGTH_SHORT).show()}.setNegativeButton("বন্ধ",null).show()
    }
}
