package com.example.foodies.model

import android.content.Context
import androidx.room.Room

object DBProvider {
    private var instance: CartDB? = null

    fun getDatabase(context: Context): CartDB {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.applicationContext,
                CartDB::class.java,
                "cart_database"
            ).build()
        }
        return instance!!
    }
}
