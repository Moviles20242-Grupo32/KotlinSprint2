package com.example.foodies.viewModel

import LocationManager
import TextToSpeechManager
import android.content.Context
import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodies.model.Cart
import com.example.foodies.model.Item
import com.example.foodies.model.ServiceAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.location.Location
import android.util.LruCache
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.foodies.model.LocationWorker
import com.example.foodies.model.LruCashingManager
import androidx.lifecycle.AndroidViewModel
import com.example.foodies.model.CartDao
import com.example.foodies.model.DBProvider
import com.example.foodies.model.NetworkMonitor
import com.example.foodies.model.OrderWorker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
      application.getSharedPreferences("shopping_cart", Context.MODE_PRIVATE)

    private val sharedPreferencesUser: SharedPreferences =
        application.getSharedPreferences("user_info", Context.MODE_PRIVATE)

    private val serviceAdapter = ServiceAdapter()
    private var textToSpeechManager: TextToSpeechManager? = null
    private var location = LocationManager
    val lruCache = LruCashingManager

    // LiveData para la lista de Items
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> get() = _items

    // LiveData para item más vendido
    private val _msitem = MutableLiveData<Item>()
    val msitem: LiveData<Item> get() = _msitem

    // LiveData para el carrito de compras
    private val _cart = MutableLiveData<Cart>()
    val cart: LiveData<Cart> get() = _cart

    // LiveData para el total Amount
    private val _totalAmount = MutableLiveData<Int>()
    val totalAmount: LiveData<Int> get() = _totalAmount

    // Live Data para saber si los datos ya fueron cargados
    private val _isLoaded = MutableLiveData<Boolean>()
    val isLoaded: LiveData<Boolean> get() = _isLoaded

    // LiveData para manejar errores
    private val _error = MutableLiveData<String?>()
    val error: MutableLiveData<String?> get() = _error

    // LiveData para manejar el estado de la orden (éxito o error)
    private val _orderSuccess = MutableLiveData<Boolean>()
    val orderSuccess: LiveData<Boolean> get() = _orderSuccess

    // LiveData para mantener la dirección del usuario
    private val _userLocation = MutableLiveData<String>()
    val userLocation: LiveData<String> get() = _userLocation

    //LiveData para atender el estado de conexión de internet
    private val _internetConnected = MutableLiveData<Boolean>()
    val internetConnected: LiveData<Boolean> get() = _internetConnected

    // LiveData for product details
    private val _product = MutableLiveData<Item?>()
    val product: MutableLiveData<Item?> get() = _product
    
    private val cartDao: CartDao = DBProvider.getDatabase(application).cartDao()



    //Inicialización: Cargamos la LocationManager address
    init {
        // Observamos los cambios en la dirección
        LocationManager.address.observeForever { newAddress ->
            _userLocation.postValue(newAddress)
        }
        _cart.value = Cart()
        loadCartItems()
        _isLoaded.postValue(false)
    }

    private fun saveItemToCart(item: Item){
        viewModelScope.launch {
            cartDao.insertItem(item)
        }
    }

    private fun removeItemFromCartId(id: String){
        viewModelScope.launch {
                cartDao.deleteItemById(id)
            }

    }

    private fun loadCartItems() {
        viewModelScope.launch {
            // Cargar los items del carrito desde la base de datos
            val itemsInCart = cartDao.getAllItems()

            // Crear un carrito temporal para añadir los items del carrito
            val carrito = Cart()

            itemsInCart.forEach {
                carrito.addItem(it, it.cart_quantity)

            }

            _cart.postValue(carrito)

            //Observamos los cambios en la red
            NetworkMonitor.isConnected.observeForever { connection ->
                _internetConnected.postValue(connection)
            }

            _cart.observeForever { newCart ->
                //Volver el cart un JSON
                val cartJson = newCart.toJson()
                // Guarda el JSON en el LRU Cache usando una clave
                lruCache.lruCashing.put("cartKey", cartJson)
            }
        }
    }


    private fun saveItemSavedIcon() {
        val editor = sharedPreferences.edit()
        _items.value?.forEachIndexed { index, item ->
            editor.putBoolean("item_${item.id}_isAdded", item.isAdded)
            Log.d("SharedPreferences", "Guardando item_${item.id}_isAdded = ${item.isAdded}")
        }
        editor.apply()
    }


    override fun onCleared() {
        super.onCleared()
        saveItemSavedIcon() // Guardar el estado al destruir el ViewModel
    }


    fun resetCart(){
        viewModelScope.launch {
            cartDao.deleteAllItems()
            lruCache.lruCashing.remove("cartKey")

            val editor = sharedPreferences.edit()

            _items.value?.forEachIndexed { index, item ->
                // Guardar el estado booleano directamente
                editor.putBoolean("item${index + 1}_isAdded", false)
                //editor.putBoolean("item${index + 1}_isAdded", true)
            }

            editor.apply()
        }
    }

    private fun resetViewModelData() {
        _cart.postValue(Cart())               // Reinicia el carrito a uno vacío
        _totalAmount.postValue(0)             // Reinicia el total del carrito a cero
        _items.postValue(emptyList())         // Limpia la lista de ítems
        _isLoaded.postValue(false)            // Marca los datos como no cargados
        _orderSuccess.postValue(false)        // Reinicia el estado de la orden
        _userLocation.postValue("")           // Limpia la dirección del usuario
    }

    private fun clearCartDatabase() {
        viewModelScope.launch {
            cartDao.deleteAllItems()  // Asegúrate de que exista un método en `cartDao` para eliminar todos los ítems
        }
    }

    fun logout() {
        clearSharedPreferences()   // Limpia SharedPreferences
        resetViewModelData()       // Restablece todos los LiveData
        clearCartDatabase()        // Limpia la base de datos del carrito
    }

    private fun clearSharedPreferences() {
        val editor = sharedPreferences.edit()
        editor.clear()  // Elimina todos los valores guardados
        editor.apply()  // Aplica los cambios

        val editor2 = sharedPreferencesUser.edit()
        editor2.clear()  // Elimina todos los valores guardados
        editor2.apply()  // Aplica los cambios
    }


    // Método para solicitar la actualización de la ubicación
    fun requestLocationUpdate(context: Context) {
        location.updateLocation(context){}
    }

    //funcion para incicializr el texttospeech
    fun initTextToSpeech(context: Context) {
        textToSpeechManager = TextToSpeechManager(context)
    }

    //Función para leer lista de productos
    fun readItemList(items: List<Item>, onComplete: () -> Unit) {
        val combinedText = items.joinToString(separator = ". ") { item ->
            "${item.item_name}: ${item.item_details}"
        }

        // Llamar a la función de TextToSpeechManager con callback
        textToSpeechManager?.speakWithCallback(combinedText) {
            onComplete()
        }
    }

    fun fetchItems() {
        viewModelScope.launch {
            try {
                // Llamada al servicio en un hilo de I/O
                val fetchedItems = withContext(Dispatchers.IO) {
                    serviceAdapter.getAllItems()
                }

                // Encuentra los elementos que ya no están en la lista actualizada y remuévelos
                val itemsToRemove = _items.value?.filter { currentItem ->
                    fetchedItems.none { it.id == currentItem.id }
                } ?: emptyList()

                for (element in itemsToRemove) {
                    removeItem(element)
                }

                // Actualiza el LiveData en el hilo principal solo si las listas son diferentes
                val updatedItems = fetchedItems.map { item ->
                    // Usa el ID o una clave más consistente para almacenar el estado en SharedPreferences
                    val isAddedState = sharedPreferences.getBoolean("item_${item.id}_isAdded", false)
                    item.copy(isAdded = isAddedState)  // Mantener estado
                }
                Log.d("Items-sp", "$updatedItems")
                // Asignar la lista actualizada
                _items.postValue(updatedItems)
            } catch (exception: Exception) {
                // Maneja cualquier error y publica el mensaje de errorzhy7
                _error.postValue(exception.message)
            }
        }
    }

    fun fetchUserPreferences(userId: String) {
        serviceAdapter.getUserOrderHistory(
            userId = userId,
            onSuccess = { itemQuantityMap ->
                Log.d("FoodiesHome-items-p", "$itemQuantityMap")
                // Si los items aún no están inicializados, usa una lista por defecto
                val currentItems = _items.value ?: emptyList()
                Log.d("FoodiesHome-items-cp", "$currentItems")
                Log.d("FoodiesHome-items-v", "${_items.value}")
                // Ordenar los items según la historia del usuario
                val sortedItems = currentItems.sortedByDescending { item ->
                    Log.d("FoodiesHome-item-p", "$item")
                    itemQuantityMap[item.item_name] ?: 0
                }

                // Update LiveData with the sorted items
                _items.postValue(sortedItems)
            },
            onFailure = { exception ->
                _error.postValue(exception.message)
            }
        )
    }

    //Función que ordena por precio del producto
    fun sortByCheaperItems(){
        _items.value = _items.value?.sortedBy { it.item_cost }
    }

    //Función para obtener el producto más vendido
    fun mostSellItem(){
        viewModelScope.launch {
           val item =  serviceAdapter.mostSellItem()
            _msitem.postValue(item)
        }
    }

    // Función para filtrar los items por el nombre
    fun filterItemsByName(query: String) {
        val itemList = _items.value ?: emptyList()

        // Filtrar y actualizar el valor de `show` según el nombre que contenga `query`
        val updatedList = itemList.map { item ->
            item.copy(show = item.item_name.contains(query, ignoreCase = true))
        }

        // Publicar la lista actualizada con el atributo `show` modificado
        _items.postValue(updatedList)
    }

    //función para el detalle
    fun detailProduct(itemId: String?){
        _items.value?.map { item ->
            if(item.id == itemId){
                _product.postValue(item)
            }
        }
    }

    //funcion para agregar producto detalle al carrito
    fun addDetailToCart(){
        val item = _product.value?.copy(isAdded = !_product.value!!.isAdded)
        _product.postValue(item)
    }

    fun addItemToCart(itemId: String?) {
        // Actualizar la lista de items basada en el ID
        val updatedList = _items.value?.map { item ->
            if (item.id == itemId) {
                val updatedItem = item.copy(isAdded = !item.isAdded) // Cambiar estado de isAdded
                if (updatedItem.isAdded) {
                    // Agregar el item al carrito
                    addItem(updatedItem, 1)
                    updateTotal()
                    // Guardar item con cantidad 1 en la base de datos
                    val itemDB = item.copy(cart_quantity = 1)
                    saveItemToCart(itemDB)
                } else {
                    // Eliminar el item del carrito
                    removeItem(updatedItem)
                    updateTotal()
                    // Remover el item del carrito en la base de datos
                    removeItemFromCartId(itemId)
                }
                updatedItem
            } else {
                item
            }
        } ?: emptyList()

        // Actualizar la lista de items para reflejar cambios
        _items.value = updatedList
        // Guardar el estado actualizado en SharedPreferences
        saveItemSavedIcon()
    }


    // Función para agregar un item al carrito
    private fun addItem(item: Item, q: Int) {
        val currentCart = _cart.value ?: Cart()
        currentCart.addItem(item, q)
        _cart.postValue(currentCart)
    }

    // Función para eliminar un item del carrito
    private fun removeItem(item: Item) {
        val currentCart = _cart.value ?: Cart()
        currentCart.removeItem(item) // Remueve el item del carrito

        // Actualiza el estado de isAdded del item a false
        val updatedList = _items.value?.map { itemList ->
            if (itemList.id == item.id) {
                itemList.copy(isAdded = false)
            } else {
                itemList
            }
        } ?: emptyList()

        // Publica los cambios en la lista de items y el carrito
        Log.d("SharedPreferences-list", "$updatedList")
        _items.postValue(updatedList)
        // Remover la preferencia
        val editor = sharedPreferences.edit()
        editor.remove("item_${item.id}_isAdded")
        editor.commit()
        // Actualiza el estado del carrito
        _cart.postValue(currentCart)
        updateTotal()
        removeItemFromCartId(item.id)
    }


    // Función para agregar cantidad
    fun updateItemQuantity(item: Item, change: Int) {
        val currentCart = _cart.value ?: Cart()
        currentCart.updateItemQuantity(item,change)
        viewModelScope.launch {
            val itemDB = item.copy(cart_quantity = item.cart_quantity + change)
            cartDao.updateItem(itemDB)
        }
        _cart.postValue(currentCart)
    }

    // Función para actualizar total
    fun updateTotal(){
        val totalAmountCalc = _cart.value?.getTotalCost() ?: 0
        _totalAmount.postValue(totalAmountCalc)
    }

    // Función para guardar la orden en Firestore o backend
    fun saveOrder(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val cartValue = _cart.value
        if (cartValue == null || cartValue.getItems().isEmpty()) {
            // Si el carrito está vacío, mostramos un error
            _error.postValue("El carrito está vacío, agrega productos antes de realizar el pedido.")
            return
        }

        // Si el carrito tiene productos, procesamos la orden
        serviceAdapter.createOrder(
            cart = cartValue,
            onSuccess = {
                _orderSuccess.postValue(true) // Publicar éxito
                onSuccess() // Llamar al callback de éxito
            },
            onFailure = { exception ->
                _error.postValue("Error al procesar la orden: ${exception.message}") // Publicar error
                onFailure(exception) // Llamar al callback de fallo
            }
        )
    }

    // Función para vaciar el carrito
    fun clearCart() {
        val emptyCart = Cart() // Crea un nuevo carrito vacío
        _cart.postValue(emptyCart) // Publica el carrito vacío en LiveData
        _totalAmount.postValue(0)  // Resetear el total a 0

        // Recorrer los items y marcar isAdded como false
        val updatedItems = _items.value?.map { item ->
            item.copy(isAdded = false)
        } ?: emptyList()

        // Publicar la lista de items actualizada
        _items.postValue(updatedItems)
    }


    // Nueva función para resetear el estado de orderSuccess
    fun resetOrderSuccess() {
        _orderSuccess.postValue(false)
    }
    // Función para resetear el estado de error
    fun resetError() {
        _error.postValue(null) // Limpiar el error
    }

    // Función para remover el item del carrito
    fun removeItemFromCart(item: Item) {
        val currentCart = _cart.value ?: Cart()
        currentCart.removeItem(item) // Remueve el item del carrito

        // Actualiza el estado de isAdded del item a false
        val updatedList = _items.value?.map { itemList ->
            if (itemList.id == item.id) {
                itemList.copy(isAdded = false)
            } else {
                itemList
            }
        } ?: emptyList()


        // Publica los cambios en la lista de items y el carrito
        Log.d("SharedPreferences-list", "$updatedList")
        _items.postValue(updatedList)
        //Modificar estado en shared preferences
        val editor = sharedPreferences.edit()
        editor.putBoolean("item_${item.id}_isAdded", false)
        editor.commit()
        // Actualiza el estado del carrito
        _cart.postValue(currentCart)
        updateTotal()
        removeItemFromCartId(item.id)
    }


    fun registerPrice(){
        val totalAmountCalc = _cart.value?.getTotalCost() ?: 0
        val price = totalAmountCalc.toDouble()
        serviceAdapter.registerPriceFB(price)

    }

    fun storeInfo() {
        val fbUser = serviceAdapter.getCurrentUser()
        fbUser?.let { user ->
            val editor = sharedPreferencesUser.edit()
            editor.putString("email", user.email)  // Establecer el email

            // Obtener y guardar el nombre del usuario
            serviceAdapter.getUserNameByUid(
                user.uid,
                onSuccess = { nombre ->
                    editor.putString("name", nombre)
                    editor.apply()  // Guarda email y nombre al obtener ambos correctamente
                    Log.d("SharedPreferences", "Email y Nombre guardados en SharedPreferences")
                },
                onFailure = { error ->
                    editor.putString("name", "NA")  // Guarda "NA" en caso de error
                    editor.apply()  // Guarda email y nombre con NA en caso de fallo
                    Log.e("SharedPreferences", "No se pudo obtener el nombre: ${error.message}")
                }
            )
        }
    }





}