package com.example.foodies.model

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

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

    // Verificar si el carrito está vacío
    fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    // Convertir el carrito a JSON
    fun toJson(): String {
        val jsonObject = JSONObject()
        val jsonArray = JSONArray()

        for (item in items) {
            val itemJson = JSONObject()
            itemJson.put("id", item.id)
            itemJson.put("item_cost", item.item_cost)
            itemJson.put("item_details", item.item_details)
            itemJson.put("item_image", item.item_image)
            itemJson.put("name", item.item_name)
            itemJson.put("item_rating", item.item_ratings)
            itemJson.put("times_ordered", item.times_ordered)
            itemJson.put("cart_quantity", item.cart_quantity)
            itemJson.put("show", item.show)
            itemJson.put("isAdded", item.isAdded)

            jsonArray.put(itemJson)
        }

        jsonObject.put("items", jsonArray)
        return jsonObject.toString()
    }

    // Crear un carrito a partir de un JSON
    companion object {
        fun fromJson(cartJson: String): Cart {
            val jsonObject = JSONObject(cartJson)
            val jsonArray = jsonObject.getJSONArray("items")
            val cart = Cart()

            for (i in 0 until jsonArray.length()) {
                val itemJson = jsonArray.getJSONObject(i)
                val item = Item(
                    id = itemJson.getString("id"),
                    item_cost = itemJson.getInt("item_cost"),
                    item_details = itemJson.getString("item_details"),
                    item_image = itemJson.getString("item_image"),
                    item_name = itemJson.getString("name"),
                    item_ratings = itemJson.getString("item_rating"),
                    times_ordered = itemJson.getInt("times_ordered"),
                    cart_quantity = itemJson.getInt("cart_quantity"),
                    show = itemJson.getBoolean("show"),
                    isAdded = itemJson.getBoolean("isAdded"),


                )
                cart.addItem(item,itemJson.getInt("cart_quantity"))
            }

            return cart
        }
    }
}
