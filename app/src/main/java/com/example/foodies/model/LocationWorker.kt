package com.example.foodies.model

import LocationManager
import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlin.math.abs

class LocationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private var location = LocationManager
    private val serviceAdapter = ServiceAdapter()

    override fun doWork(): Result {
        // Actualizar la ubicación del usuario
        location.updateLocation(applicationContext){succes ->
            if(succes)
                // Verificar y notificar si hay algún restaurante cercano
                checkAndNotifyNearbyRestaurant()
        }
        return Result.success()
    }

    private fun checkAndNotifyNearbyRestaurant() {
        // Obtener la ubicación actual del usuario
        val userLatitude = location.latitude
        val userLongitude = location.longitude

        if (userLatitude != null && userLongitude != null) {
            // Obtener restaurantes del ServiceAdapter
            serviceAdapter.getRestaurants(
                onSuccess = { restaurantMap ->
                    // Definir el radio de proximidad (aprox. 1 km)
                    val radius = 0.01

                    // Buscar restaurante cercano
                    val nearbyRestaurant = restaurantMap.entries.find { (_, location) ->
                        val (restaurantLat, restaurantLon) = location
                        val distanceLat = abs(userLatitude - restaurantLat)
                        val distanceLon = abs(userLongitude - restaurantLon)
                        distanceLat <= radius && distanceLon <= radius
                    }
                    // Enviar notificación si hay un restaurante cercano
                    nearbyRestaurant?.let { (restaurantName, _) ->
                        val notificationManager = FoodiesNotificationManager(applicationContext)
                        val message = "¡Estás cerca de $restaurantName!"
                        notificationManager.sendNotification()
                    }
                },
                onFailure = { exception ->
                    Log.e("LocationWorker", "Error al obtener los restaurantes: ${exception.message}")
                }
            )
        } else {
            Log.e("LocationWorker", "Ubicación no disponible")
        }
    }
}
