package com.example.foodies.view.shoppingCart

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.foodies.viewModel.FoodiesScreens

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

        // Botón de regreso en la esquina superior izquierda
        IconButton(
            onClick = { navController.navigate(FoodiesScreens.FoodiesHomeScreen.name) }, // Navegar al Home
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver al Home",
                tint = Color.Black
            )
        }

        // Caja de estado del pedido en la parte inferior
        OrderStatusBox(
            estimatedTime = 15,
            isOrderReady = false,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
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
            title = "Restaurante"
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

@Composable
fun OrderStatusBox(
    estimatedTime: Int,
    isOrderReady: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sigue tu orden",
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Row para los iconos y etiquetas, espaciados uniformemente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround, // Espaciado uniforme entre los íconos
                verticalAlignment = Alignment.CenterVertically
            ) {
                OrderStatusIcon(
                    isActive = !isOrderReady,
                    label = "Alistando tu pedido"
                )
                OrderStatusIcon(
                    isActive = isOrderReady,
                    label = "Pedido listo"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tiempo estimado: $estimatedTime minutos"
            )
        }
    }
}

@Composable
fun OrderStatusIcon(
    isActive: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFFFFA726) else Color(0xFFFFCC80)),
            contentAlignment = Alignment.Center
        ) {
            // Aquí puedes agregar un ícono si lo deseas
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isActive) Color.Black else Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp) // Ajusta el ancho para forzar el texto en dos líneas
        )
    }
}
