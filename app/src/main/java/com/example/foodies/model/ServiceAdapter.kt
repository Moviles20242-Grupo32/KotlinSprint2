package com.example.foodies.model

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ServiceAdapter {
    // FireStore Data Base
    private val firestore = DbManager.firestoreInstance
    private var fireAnalytics: FirebaseAnalytics = Firebase.analytics

    private val auth: FirebaseAuth = Firebase.auth
    private val _loading = MutableLiveData(false)

    // Método para obtener el usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signInWithEmailAndPassword(email: String, password: String, home: () -> Unit, onError: (String) -> Unit) {
        try {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("Foodies", "signInWithEmailAndPassword logueado")
                        home()
                    } else {
                        val errorMessage = task.exception?.message ?: "Error desconocido"
                        Log.d("Foodies", "signInWithEmailAndPassword: $errorMessage")
                        onError(errorMessage)
                    }
                }
        } catch (ex: Exception) {
            Log.d("Foodies", "signInWithEmailAndPassword: ${ex.message}")
            onError(ex.message ?: "Error desconocido")
        }
    }

    fun createUserWithEmailAndPassword(email: String, password: String, name: String, home: () -> Unit, onError: (String) -> Unit) {
        if (_loading.value == false) {
            _loading.value = true
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        createUser(name, email)
                        home()
                    } else {
                        val errorMessage = task.exception?.message ?: "Error desconocido"
                        Log.d("Foodies", "createUserWithEmailAndPassword: $errorMessage")
                        onError(errorMessage)
                    }
                    _loading.value = false
                }
        }
    }

    fun getUserNameByUid(uid: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Obtiene el campo 'name' del documento
                    val name = document.getString("name")
                    if (name != null) {
                        onSuccess(name)  // Llama al callback de éxito con el nombre
                    } else {
                        onFailure(Exception("El campo 'name' no está disponible"))
                    }
                } else {
                    onFailure(Exception("El documento de usuario no existe"))
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)  // Llama al callback de fallo con la excepción
            }
    }


    private fun createUser(name: String, email: String) {
        val userId = auth.currentUser?.uid

        val user2 = User(userId, name, email).toMap()
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
    suspend fun getAllItems(): List<Item> {
        return try {
            val documents = firestore.collection("Items").get().await()  // Usa await para la llamada asíncrona
            val itemsList = documents.map { document ->
                val id = document.id
                document.toObject(Item::class.java).copy(id = id)  // Mapea cada documento a Item
            }
            Log.d("ServiceAdapter", "Items obtenidos correctamente")
            itemsList  // Devuelve la lista de items
        } catch (e: Exception) {
            Log.e("ServiceAdapter", "Error obteniendo los Items", e)
            throw e  // Lanza la excepción para manejarla en el ViewModel
        }
    }

    // Función para obtener el producto más vendido
    suspend fun mostSellItem(): Item {
        return try {
            // Llamada suspendida para obtener todos los ítems
            val itemsList = getAllItems()

            // Encontrar el ítem con mayor valor en times_ordered
            itemsList.maxByOrNull { it.times_ordered }
                ?: throw Exception("No items found")  // Lanza excepción si no hay ítems

        } catch (e: Exception) {
            Log.e("ServiceAdapter", "Error obteniendo el ítem más vendido", e)
            throw e  // Lanza la excepción para manejarla en el ViewModel o donde se invoque
        }
    }



    fun createOrder(cart: Cart, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        // Definir una ubicación estándar
        val standardLocation = hashMapOf(
            "latitude" to 37.785834, // Latitud estándar
            "longitude" to -122.406417 // Longitud estándar
        )

        // Crear un nuevo mapa para almacenar la orden
        val orderData = hashMapOf(
            "user_id" to userId,
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

    fun getRestaurants(onSuccess: (HashMap<String, List<Double>>) -> Unit, onFailure: (Exception) -> Unit) {
        // HashMap para almacenar los resultados
        val restaurantMap = HashMap<String, List<Double>>()

        // Acceder a la colección de restaurantes en Firestore
        firestore.collection("restaurants")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    // Obtener los datos de cada restaurante
                    val name = document.getString("name")
                    val latitude = document.getDouble("latitude")
                    val longitude = document.getDouble("longitude")

                    // Asegurarse de que los datos no sean nulos antes de agregarlos al mapa
                    if (name != null && latitude != null && longitude != null) {
                        restaurantMap[name] = listOf(latitude, longitude)
                    }
                }
                // Llamar al callback de éxito con el HashMap resultante
                onSuccess(restaurantMap)
            }
            .addOnFailureListener { exception ->
                // Llamar al callback de error en caso de fallo
                onFailure(exception)
            }
    }

    fun registerPriceFB(price: Double){
        fireAnalytics.logEvent("purchased"){
            param("Total", price)
        }

    }

    suspend fun getProductById(productId: String): Item {
        return try {
            val document = firestore.collection("Items").document(productId).get().await()
            document.toObject(Item::class.java) ?: throw Exception("Product not found")
        } catch (e: Exception) {
            throw Exception("Error fetching product: ${e.message}")
        }
    }


    fun getUserOrderHistory(userId: String, onSuccess: (Map<String, Int>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("Orders")
            .whereEqualTo("user_id", userId)
            .get()
            .addOnSuccessListener { documents ->
                val itemQuantityMap = mutableMapOf<String, Int>()

                for (document in documents) {
                    val orderedItems = document.get("ordered_food") as? List<Map<String, Any>> ?: continue
                    for (item in orderedItems) {
                        val itemName = item["item_name"] as? String ?: continue
                        val itemQuantity = (item["item_quantity"] as? Long)?.toInt() ?: 0

                        // Sum the quantity for each item
                        itemQuantityMap[itemName] = itemQuantityMap.getOrDefault(itemName, 0) + itemQuantity
                    }
                }
                onSuccess(itemQuantityMap) // Return the total quantities
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    // Function to sign out the user
    fun signOut() {
        auth.signOut()
    }

    fun registerTrack(){
        //val doc = firestore.collection("track").document(UUID.randomUUID().toString())
        val newId = UUID.randomUUID().toString()
        val data = mapOf(
            "id" to newId,
            "timestamp" to Timestamp.now()
        )

        firestore.collection("track").add(data)
    }


}
