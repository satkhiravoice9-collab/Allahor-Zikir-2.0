package com.sabbirsamol.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object AuthManager {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val database by lazy { FirebaseDatabase.getInstance().reference }

    fun currentUid(): String? = auth.currentUser?.uid

    fun getUserDataKey(context: Context): String {
        return currentUid()
            ?: context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
                .getString("user_mobile", "01700000000")
                .orEmpty()
                .ifBlank { "01700000000" }
    }

    fun migrateLegacyData(context: Context, mobile: String, onComplete: (Boolean, String?) -> Unit) {
        val uid = currentUid()
        if (uid.isNullOrBlank()) {
            onComplete(false, "Firebase authentication পাওয়া যায়নি।")
            return
        }

        val legacyRef = database.child("users").child(mobile)
        val uidRef = database.child("users").child(uid)

        legacyRef.get().addOnSuccessListener { legacySnapshot ->
            uidRef.get().addOnSuccessListener { uidSnapshot ->
                val updates = mutableMapOf<String, Any?>()
                if (legacySnapshot.exists()) {
                    if (!uidSnapshot.exists()) {
                        updates["users/$uid"] = legacySnapshot.value
                    } else {
                        for (child in legacySnapshot.children) {
                            val key = child.key ?: continue
                            if (!uidSnapshot.child(key).exists()) {
                                updates["users/$uid/$key"] = child.value
                            }
                        }
                    }
                }
                updates["users/$uid/phone_number"] = mobile
                updates["users/$uid/firebase_uid"] = uid

                database.updateChildren(updates)
                    .addOnSuccessListener { onComplete(true, null) }
                    .addOnFailureListener { e -> onComplete(false, e.message) }
            }.addOnFailureListener { e -> onComplete(false, e.message) }
        }.addOnFailureListener { e -> onComplete(false, e.message) }
    }
}
