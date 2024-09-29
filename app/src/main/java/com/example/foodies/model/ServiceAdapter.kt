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
                    val id = document.id
                    // Mapear cada documento a un objeto Item y agregar el ID
                    val item = document.toObject(Item::class.java).copy(id = id)
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

    // Función para obtener el producto más vendido
    fun mostSellItem(onSuccess: (Item) -> Unit, onFailure: (Exception) -> Unit) {
        getAllItems(
            onSuccess = { itemsList ->
                // Encontrar el ítem con el mayor valor en times_ordered
                val mostSoldItem = itemsList.maxByOrNull { it.times_ordered }
                if (mostSoldItem != null) {
                    onSuccess(mostSoldItem)
                } else {
                    onFailure(Exception("No items found"))
                }
            },
            onFailure = { exception ->
                onFailure(exception)
            }
        )
    }

    fun createOrder(cart: Cart, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        // Definir una ubicación estándar
        val standardLocation = hashMapOf(
            "latitude" to 37.785834, // Latitud estándar
            "longitude" to -122.406417 // Longitud estándar
        )

        // Crear un nuevo mapa para almacenar la orden
        val orderData = hashMapOf(
            "location" to standardLocation,
            "total_cost" to cart.getTotalCost(),
            "ordered_food" to cart.getItems().map { item ->
                hashMapOf(
                    "item_name" to item.item_name,
                    "item_cost" to item.item_cost,
                    "item_quantity" to item.cart_quantity
                )
            }
        )

        // Guardar la orden en Firestore
        firestore.collection("Orders")
            .add(orderData)
            .addOnSuccessListener {
                Log.d("ServiceAdapter", "Orden creada exitosamente")
                // Actualizar times_ordered de cada producto después de crear la orden
                cart.getItems().forEach { item ->
                    updateTimesOrdered(item) // Llamar la función para actualizar
                }
                onSuccess() // Llamar al callback de éxito
            }
            .addOnFailureListener { exception ->
                Log.d("ServiceAdapter", "Error al crear la orden", exception)
                onFailure(exception) // Llamar al callback de fallo
            }
    }

    // Función para actualizar el campo times_ordered del producto en Firestore
    private fun updateTimesOrdered(item: Item) {
        // Obtener la referencia del documento del ítem en Firestore
        val itemRef = firestore.collection("Items").document(item.id ?: return)

        // Realizar la actualización acumulativa del campo times_ordered
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(itemRef)
            val currentTimesOrdered = snapshot.getLong("times_ordered") ?: 0
            val newTimesOrdered = currentTimesOrdered + item.cart_quantity // Sumar la cantidad pedida
            transaction.update(itemRef, "times_ordered", newTimesOrdered)
        }.addOnSuccessListener {
            Log.d("ServiceAdapter", "times_ordered actualizado correctamente para el producto ${item.item_name}")
        }.addOnFailureListener { exception ->
            Log.d("ServiceAdapter", "Error al actualizar times_ordered", exception)
        }
    }

}
