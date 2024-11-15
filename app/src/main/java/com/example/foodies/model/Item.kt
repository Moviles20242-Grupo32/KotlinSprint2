package com.example.foodies.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class Item(

    @PrimaryKey(autoGenerate = false) val id: String = "",
    val item_cost: Int = 0,
    val item_details: String = "",
    val item_image: String = "",
    val item_name: String = "",
    val item_ratings: String = "",
    val times_ordered: Int = 0,
    val cart_quantity: Int = 0,
    val show: Boolean = true,
    var isAdded: Boolean = false,
    val item_ingredients: String = "",
    val item_starProducts: String = ""
) {
    fun toMap(): MutableMap<String, Any?> {
        return mutableMapOf(
            "id" to this.id,
            "item_cost" to this.item_cost,
            "item_details" to this.item_details,
            "item_image" to this.item_image,
            "item_name" to this.item_name,
            "item_ratings" to this.item_ratings,
            "times_ordered" to this.times_ordered,
            "item_ingredients" to this.item_ingredients,
            "item_starProducts" to this.item_starProducts

        )
    }
}