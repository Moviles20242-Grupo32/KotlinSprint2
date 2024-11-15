package com.example.foodies.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.foodies.model.Item
import com.example.foodies.model.ServiceAdapter
import kotlinx.coroutines.launch

class ProductDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val serviceAdapter = ServiceAdapter()

    // LiveData para almacenar el producto seleccionado
    private val _product = MutableLiveData<Item>()
    val product: LiveData<Item> get() = _product

    // Función para obtener los detalles del producto por su ID
    fun fetchProductDetails(productId: String) {
        viewModelScope.launch {
            try {
                // Fetch product details using the productId
                val item = serviceAdapter.getProductById(productId)
                Log.d("ProductDetailViewModel", "Fetched product: $item")  // Log the fetched product

                // Update LiveData with the fetched product
                _product.postValue(item)
            } catch (e: Exception) {
                Log.e("ProductDetailViewModel", "Error fetching product details: ${e.message}")
            }
        }
    }

}
