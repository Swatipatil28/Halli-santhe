package com.hallisanthe.hallisanthe

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserSession {
    var uid: String = ""
    var name: String = ""
    var mobile: String = ""
    var role: String = "" // "Buyer" or "Artisan"
    var village: String = ""
    var profileImageUrl: String = ""

    fun load(onComplete: () -> Unit) {
        val authUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onComplete()
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(authUid)
            .get()
            .addOnSuccessListener { doc ->
                uid = doc.getString("uid") ?: authUid
                name = doc.getString("name") ?: ""
                mobile = doc.getString("mobile") ?: ""
                role = doc.getString("role") ?: "Buyer"
                village = doc.getString("village") ?: ""
                profileImageUrl = doc.getString("profileImageUrl") ?: ""
                onComplete()
            }
            .addOnFailureListener {
                onComplete()
            }
    }

    fun clear() {
        uid = ""; name = ""; mobile = ""; role = ""; village = ""; profileImageUrl = ""
    }
}
