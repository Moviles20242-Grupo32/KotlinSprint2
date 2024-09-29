package com.example.foodies.model

import com.google.firebase.firestore.FirebaseFirestore

object DbManager {
    // Instancia única de FirebaseFirestore
    val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}