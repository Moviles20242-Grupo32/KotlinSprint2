package com.example.foodies.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodies.model.ServiceAdapter
import com.example.foodies.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {

    private val serviceAdapter = ServiceAdapter()

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    // LiveData for tracking the current user session (custom User)
    private val _userSession = MutableLiveData<User?>()
    val userSession: LiveData<User?> = _userSession

    // Initialize with current user if already logged in
    init {
        // Check if a Firebase user is logged in and fetch the corresponding User data from Firestore
        auth.currentUser?.let { firebaseUser ->
            fetchUserData(firebaseUser.uid)
        }
    }

    // Fetch user data from Firestore
    private fun fetchUserData(userId: String) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Map Firestore document to User data class
                    val user = document.toObject(User::class.java)
                    _userSession.value = user
                }
            }
            .addOnFailureListener {
                _userSession.value = null // Clear the session if fetching fails
            }
    }

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        serviceAdapter.signInWithEmailAndPassword(email, password, home, onError).also {
            // After successful sign-in, fetch the user data
            auth.currentUser?.let { firebaseUser ->
                fetchUserData(firebaseUser.uid)
            }
        }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, name: String, home: () -> Unit, onError: (String) -> Unit) {
        serviceAdapter.createUserWithEmailAndPassword(email, password, name, home, onError).also {
            // After successful sign-up, fetch the user data
            auth.currentUser?.let { firebaseUser ->
                fetchUserData(firebaseUser.uid)
            }
        }
    }

    // Function to sign out the current user
    fun signOut() {
        auth.signOut()
        _userSession.value = null // Clear the user session after sign-out
    }
}
