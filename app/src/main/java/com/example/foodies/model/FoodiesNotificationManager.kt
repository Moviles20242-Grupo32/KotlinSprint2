package com.example.foodies.model

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.foodies.R

class FoodiesNotificationManager(private val context: Context) {

    companion object {
        // Constantes
        const val CHANNEL_ID = "Foodies Channel"
        const val CHANNEL_NAME = "Foodies Nearby Notifications"
        const val CHANNEL_DESCRIPTION = "Canal para notificaciones de restaurantes Foodies cercanos"
    }

    fun sendNotification() {
        // Crear el NotificationManager
        val notificationManager = NotificationManagerCompat.from(context)

        // Crear el canal de notificación si es necesario (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Crear la notificación
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo) // Icono de la notificación
            .setContentTitle("¡Estas cerca!")
            .setContentText("Tienes un restaurante Foodies cerca de ti, ¡aprovecha y pide ya!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("¡Estas cerca!")
                    .setContentText("Tienes un restaurante Foodies cerca de ti, ¡aprovecha y pide ya!")
                    .build()
            )

        // Enviar la notificación
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("FoodiesNotification", "Permiso para enviar notificaciones no concedido.")
            return
        }
        notificationManager.notify(1, notificationBuilder.build())
    }
}