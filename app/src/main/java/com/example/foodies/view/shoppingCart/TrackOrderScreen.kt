package com.example.foodies.view.shoppingCart

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun TrackOrderScreen(navController: NavHostController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val userLocationState = remember { mutableStateOf<LatLng?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current


    // Verificar y pedir permisos de ubicación
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Obtener la ubicación actual
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userLocationState.value = LatLng(it.latitude, it.longitude)
                        }
                    }
                } else {
                    // Solicitar permisos
                    val activity = context as Activity
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        101
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa con la ubicación del usuario
        Mapa(
            userLocation = userLocationState.value,
            targetLocation = LatLng(4.6012, -74.0657) // Latitud y longitud de ejemplo (Bogotá)
        )

        // Caja de información en la parte inferior
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Color de fondo blanco
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sigue tu orden",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    ){
                        Text("El restaurante está alistando tu orden")
                    }

                }
            }
        }
    }
}

@Composable
fun Mapa(
    userLocation: LatLng?,
    targetLocation: LatLng,
    modifier: Modifier = Modifier
) {
    // Estado que controla si la cámara ya ha sido movida a la ubicación del usuario
    var hasMovedToUserLocation by remember { mutableStateOf(false) }

    val targetMarkerState = rememberMarkerState(position = targetLocation)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(targetLocation, 10f) // Posición inicial
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = remember { MapUiSettings(myLocationButtonEnabled = true) },
        properties = remember { MapProperties(isMyLocationEnabled = true) }
    ) {
        // Marcador en la ubicación enviada por parámetro (targetLocation)
        Marker(
            state = targetMarkerState,
            title = "Taller"
        )

        // Mover la cámara a la ubicación del usuario solo la primera vez
        if (userLocation != null && !hasMovedToUserLocation) {
            LaunchedEffect(userLocation) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(userLocation, 14f)
                )
                hasMovedToUserLocation = true // Evitar que se reestablezca constantemente
            }
        }
    }
}



