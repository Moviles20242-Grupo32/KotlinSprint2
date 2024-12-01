package com.example.foodies.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodies.model.NetworkMonitor
import com.example.foodies.model.ServiceAdapter
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(application: Application): AndroidViewModel(application)  {

    private val serviceAdapter = ServiceAdapter()

    private val _loading = MutableLiveData(false)

    //LiveData para atender el estado de conexión de internet
    private val _internetConnected = MutableLiveData<Boolean>()
    val internetConnected: LiveData<Boolean> get() = _internetConnected

    private val _user = MutableLiveData<FirebaseUser?>()
    val user: LiveData<FirebaseUser?> get() = _user

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName


    //Inicialización: Cargamos la LocationManager address
    init {
        //Observamos los cambios en la red
        NetworkMonitor.isConnected.observeForever { connection->
            _internetConnected.postValue(connection)
        }
        _user.value = serviceAdapter.getCurrentUser()
    }
    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit, onError: (String) -> Unit) = viewModelScope.launch {
        serviceAdapter.signInWithEmailAndPassword(email, password, home, onError)
    }

    fun createUserWithEmailAndPassword(email: String, password: String, name: String, home: () -> Unit, onError: (String) -> Unit) {
        serviceAdapter.createUserWithEmailAndPassword(email, password, name, home, onError)
    }


    // Modificación de la función signOut
    fun signOut() {
            serviceAdapter.signOut()
            _user.postValue(null)
    }

    // Function to refresh user state after login
    fun setUpUser() {
        val currentUser = serviceAdapter.getCurrentUser()
        // Publica el valor del usuario actual
        _user.postValue(currentUser)
    }

    fun getUserById(uid: String) {
        serviceAdapter.getUserNameByUid(
            uid,
            onSuccess = { nombre ->
                _userName.value = nombre // Actualiza el LiveData con el nombre obtenido
            },
            onFailure = { error ->
                Log.e("Error", "No se pudo obtener el nombre: ${error.message}")
            }
        )
    }





}