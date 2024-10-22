package com.example.foodies.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Item::class], version = 1)
abstract class CartDB : RoomDatabase() {
    abstract fun cartDao(): CartDao
}