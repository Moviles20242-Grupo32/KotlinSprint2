package com.example.foodies.model

data class Cart(
    private val items: MutableList<Item> = mutableListOf()
) {

    // Agregar un item al carrito
    fun addItem(item: Item) {
        val existingItem = items.find { it.id == item.id }
        if (existingItem == null) {
            items.add(item.copy(cart_quantity = 1)) // Inicializa la cantidad en el carrito a 1
        } else {
            updateItemQuantity(existingItem, existingItem.cart_quantity + 1)
        }
    }

    // Remover un item del carrito
    fun removeItem(item: Item) {
        items.removeAll { it.id == item.id }
    }

    // Actualizar la cantidad de un item en el carrito
    fun updateItemQuantity(item: Item, quantity: Int) {
        val index = items.indexOfFirst { it.id == item.id }
        if (index >= 0) {
            items[index] = items[index].copy(cart_quantity = quantity)
            if (quantity <= 0) {
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
