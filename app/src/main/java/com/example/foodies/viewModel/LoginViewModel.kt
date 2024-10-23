package com.example.foodies.viewModel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodies.model.ServiceAdapter
import com.example.foodies.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {
    private val serviceAdapter = ServiceAdapter()

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    // Updated function to sign in with email and password
    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        home: () -> Unit,
        onError: (String) -> Unit// Pass the LogoutViewModel to refresh user state
    ) = viewModelScope.launch {
        serviceAdapter.signInWithEmailAndPassword(email, password, {
            home() // Navigate to home
        }, onError)
    }

    // Updated function to create a new user account
    fun createUserWithEmailAndPassword(
        email: String,
        password: String,
        name: String,
        home: () -> Unit,
        onError: (String) -> Unit// Pass LogoutViewModel to refresh user state
    ) {
        serviceAdapter.createUserWithEmailAndPassword(email, password, name, {
            val currentUser = auth.currentUser
            currentUser?.let {
                val profileUpdates = userProfileChangeRequest {
                    displayName = name
                }
                it.updateProfile(profileUpdates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        home()
                    } else {
                        onError("Profile update failed")
                    }
                }
            }
        }, onError)
    }
}

