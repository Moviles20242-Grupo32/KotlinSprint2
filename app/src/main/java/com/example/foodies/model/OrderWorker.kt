package com.example.foodies.model

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class OrderWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val lruCashing = LruCashingManager
    override fun doWork(): Result {
        //Obtener carro
        val cartJson = lruCashing.lruCashing.get("cartKey")
        val cart = cartJson?.let { Cart.fromJson(it) }

        // Enviar notificación solo cuando el carrito esté listo
        if (cart != null && !cart.isEmpty()) {
            // Manejador de notificaciones
            val notificationManager = FoodiesNotificationManager(applicationContext)
            // Contenido de la notificación
            val message = "¡Haz tu orden!"
            val content = "Recuerda que tienes productos en tu carrito, no olvides realizar tu orden."
            // Enviar notificación
            val notificationId = message.hashCode()
            notificationManager.sendNotification(message, content,notificationId)
        }

        // Resultado exitoso
        return Result.success()
    }

    override fun onStopped() {
        super.onStopped()
        lruCashing.lruCashing.remove("cartKey")
    }
}