package com.example.foodies.viewModel

import LocationManager
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
import android.location.Location
import java.util.Locale

class ShoppingViewModel : ViewModel() {
    private val serviceAdapter = ServiceAdapter()
    private var textToSpeechManager: TextToSpeechManager? = null
    private var location = LocationManager

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

    // LiveData para mantener la dirección del usuario
    private val _userLocation = MutableLiveData<String>()
    val userLocation: LiveData<String> get() = _userLocation

    //Inicialización: Cargamos la LocationManager address
    init {
        // Observamos los cambios en la dirección
        LocationManager.address.observeForever { newAddress ->
            _userLocation.postValue(newAddress)
        }
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
        _cart.postValue(currentCart)
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

    // Función para guardar la orden en Firestore
    fun saveOrder() {
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
            },
            onFailure = { exception ->
                _error.postValue(exception.message) // Publicar error
            }
        )
    }

    // Función para vaciar el carrito
    fun clearCart() {
        val emptyCart = Cart() // Crea un nuevo carrito vacío
        _cart.postValue(emptyCart) // Publica el carrito vacío en LiveData
        _totalAmount.postValue(0)  // Resetear el total a 0
    }

    // Nueva función para resetear el estado de orderSuccess
    fun resetOrderSuccess() {
        _orderSuccess.postValue(false)
    }

    fun registerPrice(){
        val totalAmountCalc = _cart.value?.getTotalCost() ?: 0
        val price = totalAmountCalc.toDouble()
        serviceAdapter.registerPriceFB(price)

    }



}