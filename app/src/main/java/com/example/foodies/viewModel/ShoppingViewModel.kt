package com.example.foodies.viewModel

import TextToSpeechManager
import android.content.Context
import android.Manifest
import android.app.Activity
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
import java.util.Locale

class ShoppingViewModel : ViewModel() {
    private val serviceAdapter = ServiceAdapter()
    private var textToSpeechManager: TextToSpeechManager? = null

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
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    // LiveData para manejar el estado de la orden (éxito o error)
    private val _orderSuccess = MutableLiveData<Boolean>()
    val orderSuccess: LiveData<Boolean> get() = _orderSuccess

    private val _userLocation = MutableLiveData<String>()
    val userLocation: LiveData<String> = _userLocation

    fun getLastLocation(context: Context) {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no están otorgados (esto lo deberías hacer en la actividad)
            if (context is Activity) {
                ActivityCompat.requestPermissions(context, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            }
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("Foodies", "latitud: ${location.latitude}, longitud: ${location.longitude}")
                val geocoder = Geocoder(context, Locale.getDefault())

                try {
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val dir: String = address.getAddressLine(0).split(",")[0]
                        //_userLocation.postValue(address.getAddressLine(0))
                        _userLocation.postValue(dir)
                    } else {
                        Log.d("Foodies", "No se encontró ninguna dirección")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("Foodies", "Error al obtener la dirección: ${e.message}")
                }
                //_userLocation.postValue("${location.latitude}/${location.longitude}")
            } else {
                _userLocation.postValue("Ubicación no disponible")
            }
        }.addOnFailureListener {
            Log.d("Foodies", "Error al obtener la ubicación")
            _userLocation.postValue("Error al obtener la ubicación")
        }
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

    // Función para obtener los items desde Firebase
    fun fetchItems() {
        if (_isLoaded.value == true) return
        viewModelScope.launch {
            serviceAdapter.getAllItems(
                onSuccess = { itemList ->
                    _items.postValue(itemList)
                    _isLoaded.postValue(true)
                    Log.d("FoodiesHomeScreen", "items.isEmpty(): ${isLoaded.value}")
                },
                onFailure = { exception ->
                    // Publicar el error si ocurre
                    _error.postValue(exception.message)
                    _isLoaded.postValue(false)
                }
            )
        }
    }


    //Función para obtener el producto más vendido
    fun mostSellItem(){
        viewModelScope.launch {
            serviceAdapter.mostSellItem(
                onSuccess = { item ->
                    _msitem.postValue(item)
                },
                onFailure = { exception ->
                    _error.postValue(exception.message)
                }
            )
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

    // Función para cambiar el valor de isAdded y republicar la lista
    fun addItemToCart(itemId: String?) {
        val updatedList = _items.value?.map { item ->
            if (item.id == itemId) {
                // Cambiar el estado de isAdded
                val updatedItem = item.copy(isAdded = !item.isAdded)
                // Agregar el item al carrito si está marcado como añadido
                if (updatedItem.isAdded) {
                    addItem(updatedItem)
                    updateTotal()
                    if (updatedItem.id == _msitem.value?.id) {
                        _msitem.postValue(updatedItem)
                    }
                } else {
                    removeItem(updatedItem)
                    updateTotal()
                    if (updatedItem.id == _msitem.value?.id) {
                        _msitem.postValue(updatedItem)
                    }
                }
                updatedItem
            } else {
                item
            }
        } ?: emptyList()

        // Publicar la lista actualizada
        _items.postValue(updatedList)
    }

    // Función para agregar un item al carrito
    fun addItem(item: Item) {
        val currentCart = _cart.value ?: Cart()
        currentCart.addItem(item)
        _cart.postValue(currentCart)
    }

    // Función para eliminar un item del carrito
    fun removeItem(item: Item) {
        val currentCart = _cart.value ?: Cart()
        currentCart.removeItem(item)

        // Actualizar el estado de isAdded a false para el item eliminado
        val updatedList = _items.value?.map { itemList ->
            if (itemList.id == item.id) {
                itemList.copy(isAdded = false)
            } else {
                itemList
            }
        } ?: emptyList()

        _items.postValue(updatedList)
        _cart.postValue(currentCart)
        updateTotal() // Actualiza el total después de eliminar el item
    }


    // Función para agregar cantidad
    fun updateItemQuantity(item: Item, change: Int) {
        val currentCart = _cart.value ?: Cart()
        currentCart.updateItemQuantity(item,change)
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
        _items.postValue(updatedList)
        _cart.postValue(currentCart)  // Actualiza el estado del carrito
        updateTotal() // Actualiza el total después de eliminar el item
    }



}