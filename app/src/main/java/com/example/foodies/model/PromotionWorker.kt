package com.example.foodies.model

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PromotionWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val serviceAdapter = ServiceAdapter()

    override fun doWork(): Result {
        managePromotions()
        return Result.success()
    }

    private fun managePromotions() {
        serviceAdapter.getPromotions { promotions ->
            val notificationManager = FoodiesNotificationManager(applicationContext)
            if (promotions.isNotEmpty()) {
                for (index in promotions.indices) {
                    // Contenido de la notificación
                    val message = "¡Hay promoción!"
                    val content = promotions[index]
                    // Enviar notificación
                    val notificationId = content.hashCode()
                    notificationManager.sendNotification(message, content,notificationId)
                }
            } else {
                Log.d("PromotionLog", "No promotions available.")
            }
        }
    }

}