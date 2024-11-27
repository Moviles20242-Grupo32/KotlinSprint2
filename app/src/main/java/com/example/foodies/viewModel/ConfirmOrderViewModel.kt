package com.example.foodies.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.foodies.model.Cart
import com.example.foodies.model.LruCashingManager

class ConfirmOrderViewModel(application: Application) : AndroidViewModel(application) {

    // LiveData para el carrito de compras
    private val _cart = MutableLiveData<Cart?>()
    val cart: MutableLiveData<Cart?> get() = _cart

    //Caching
    val lruCache = LruCashingManager

    //Función para cargar carrito
    fun loadCart(){
        //Obtener vehículo
        val cartJson = lruCache.lruCashing.get("cartKey")
        val cartObj = cartJson?.let { Cart.fromJson(it) }
        Log.d("ConfirmOrder", cartJson)
        //Publicar carrito
        _cart.postValue(cartObj)
    }
}