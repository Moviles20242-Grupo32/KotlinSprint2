package com.example.foodies.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodies.model.Cart
import com.example.foodies.model.Item
import com.example.foodies.model.ServiceAdapter

class ShoppingViewModel : ViewModel() {
    private val serviceAdapter = ServiceAdapter()

    // LiveData para la lista de Items
    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> get() = _items

    // LiveData para item más vendido
    private val _msitem = MutableLiveData<Item>()
    val msitem: LiveData<Item> get() = _msitem

    // LiveData para el carrito de compras
    private val _cart = MutableLiveData<Cart>()
    val cart: LiveData<Cart> get() = _cart

    // Live Data para saber si los datos ya fueron cargados
    private val _isLoaded = MutableLiveData<Boolean>()
    val isLoaded: LiveData<Boolean> get() = _isLoaded

    // LiveData para manejar errores
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

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
                    if (updatedItem.id == _msitem.value?.id) {
                        _msitem.postValue(updatedItem)
                    }
                } else {
                    removeItem(updatedItem)
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


}
