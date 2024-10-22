package com.example.foodies.model

import android.util.Log

data class Cart(
    private val items: MutableList<Item> = mutableListOf()
) {

    // Agregar un item al carrito
    fun addItem(item: Item, quantity: Int) {
        val existingItem = items.find { it.id == item.id }
        if (existingItem == null) {
            items.add(item.copy(cart_quantity = quantity)) // Inicializa la cantidad en el carrito a 1
        } else {
            updateItemQuantity(existingItem, existingItem.cart_quantity + 1)
        }
    }

    // Remover un item del carrito
    fun removeItem(item: Item) {
        items.removeAll { it.id == item.id }
    }

    // Actualizar la cantidad de un item en el carrito
    fun updateItemQuantity(item: Item, change: Int) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            val currentQuantity = items[index].cart_quantity
            val newQuantity = currentQuantity + change

            // Si la nueva cantidad es mayor que 0, actualizamos el item con la nueva cantidad
            if (newQuantity > 0) {
                items[index] = items[index].copy(cart_quantity = newQuantity)
                Log.d("Cart", "${items[index].cart_quantity}")
            } else {
                // Si la nueva cantidad es menor o igual a 0, removemos el item del carrito
                removeItem(item)
            }
        }
    }


    // Obtener el total de costos de los items en el carrito
    fun getTotalCost(): Int {
        return items.sumOf { it.item_cost * it.cart_quantity }
    }

    // Obtener todos los items en el carrito
    fun getItems(): List<Item> {
        return items.toList() // Retorna una lista inmutable para evitar modificaciones externas
    }

    // Vaciar el carrito
    fun clearCart() {
        items.clear()
    }
}
