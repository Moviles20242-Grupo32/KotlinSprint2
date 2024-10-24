package com.example.foodies.viewModel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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

class LogoutViewModel : ViewModel() {
    // LiveData to observe the logout status
    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    init {
        // Initialize FirebaseAuth and get the current user
        _user.value = Firebase.auth.currentUser
    }

    // Function to sign out the user
    fun signOut() {
        Firebase.auth.signOut()
        _user.postValue(null) // Clear the user data after signing out
    }

    // Function to refresh user state after login
    fun setUpUser() {
        val currentUser = Firebase.auth.currentUser
        // Publica el valor del usuario actual
        _user.postValue(currentUser)
    }


}



