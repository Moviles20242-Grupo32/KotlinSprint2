package com.example.foodies.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodies.model.ServiceAdapter
import com.example.foodies.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class LoginViewModel: ViewModel() {

    private val serviceAdapter = ServiceAdapter()

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        serviceAdapter.signInWithEmailAndPassword(email, password, home, onError)
    }

    fun createUserWithEmailAndPassword(email: String, password: String, name: String, home: () -> Unit, onError: (String) -> Unit) {
        serviceAdapter.createUserWithEmailAndPassword(email, password, name, home, onError)
    }


}