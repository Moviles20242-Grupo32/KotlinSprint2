package com.example.foodies.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ServiceAdapter {
    // FireStore Data Base
    private val firestore = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit)
    //= viewModelScope.launch
    {

        try{
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task->
                    if(task.isSuccessful){
                        Log.d("Foodies", "signInWithEmailAndPassword logueado")
                        home()
                    }
                    else{
                        Log.d("Foodies", "signInWithEmailAndPassword ${task.result.toString()}")
                    }
                }
        }
        catch(ex: Exception){
            Log.d("Foodies", "signInWithEmailAndPassword ${ex.message}")
        }


    }

    fun createUserWithEmailAndPassword(email: String, password: String,name: String, home: () -> Unit){
        if(_loading.value == false){
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        createUser(name, email)
                        home()
                    }
                    else{
                        Log.d("Foodies", "createUserWithEmailAndPassword: ${task.result.toString()}")
                    }
                    _loading.value = false
                }
        }
    }

    private fun createUser(name: String, email: String) {
        val userId = auth.currentUser?.uid

        val user2 = User(userId, name, email).toMap()
        //val user = hashMapOf(
        //  "name" to name,
        //"email" to email,
        //"id" to userId
        //)
        userId?.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
                .set(user2)
                .addOnSuccessListener {
                    Log.d("Foodies", "Creado ${it.toString()}")
                }.addOnFailureListener {
                    Log.d("Foodies", "Ocurrio error")
                }
        }
    }


    //SHOPPING SERVICES:
    // Función para obtener todos los documentos de la colección "Items" y mapearlos a objetos Item
    fun getAllItems(onSuccess: (List<Item>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Items")
            .get()  // Solicitar todos los documentos
            .addOnSuccessListener { documents ->
                // Lista para almacenar los ítems
                val itemsList = mutableListOf<Item>()
                for (document in documents) {
                    // Mapear cada documento a un objeto Item
                    val item = document.toObject(Item::class.java)
                    itemsList.add(item)
                }
                Log.d("ServiceAdapter", "Items obtenidos correctamente")
                // Retornar los datos
                onSuccess(itemsList)
            }
            .addOnFailureListener { exception ->
                Log.d("ServiceAdapter", "Error obteniendo los Items", exception)
                // Manejar el error
                onFailure(exception)
            }
    }
}
