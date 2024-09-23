package com.example.foodies.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class ServiceAdapter {
    // FireStore Data Base
    private val firestore = FirebaseFirestore.getInstance()

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
}
