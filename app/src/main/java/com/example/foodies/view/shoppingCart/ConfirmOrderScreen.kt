package com.example.foodies.view.shoppingCart

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.foodies.model.Item
import com.example.foodies.viewModel.ConfirmOrderViewModel
import com.example.foodies.viewModel.FoodiesScreens
import com.example.foodies.viewModel.ShoppingViewModel

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ConfirmOrderScreen(
    navController: NavHostController,
    viewModel: ConfirmOrderViewModel = viewModel()
){
    // Estados
    val cart by viewModel.cart.observeAsState()
    viewModel.loadCart()
    // Surface con fondo blanco
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Fila para el título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Resumen de orden",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0.353f, 0.196f, 0.071f, 1.0f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Lista de items ocupando el espacio restante
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                cart?.let {
                    items(it.getItems()) { item ->
                        ItemOrder(item, viewModel)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            //Total de la cuenta
            cart?.getTotalCost()?.let { total -> TotalSection(total) }
            //Navegar a track order
            NavigateTrackOrder(navController)
        }
    }
}

@Composable
fun ItemOrder(item: Item, viewModel: ConfirmOrderViewModel) {
    // Crear estados mutables para item_cost y cart_quantity
    var itemCost by remember { mutableStateOf(item.item_cost) }
    var itemCartQuantity by remember { mutableStateOf(item.cart_quantity) }

    // Calcular itemTotalPrice basado en itemCost y itemCartQuantity
    val itemTotalPrice by remember { derivedStateOf { itemCost * itemCartQuantity } }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically // Alineación vertical al centro
    ) {
        // Columna 1: Imagen del item
        AsyncImage(
            model = item.item_image,
            contentDescription = "Imagen de ${item.item_name}",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Columna 2: Información del item (nombre, detalles, precio)
        Column(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically), // Alineación vertical al centro
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Nombre
            Text(
                text = item.item_name,
                style = TextStyle(
                    fontSize = 20.sp, // Tamaño de fuente fijo
                    fontWeight = FontWeight.Bold // Negrita
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0.352f, 0.196f, 0.070f, 1.0f)
            )
            // Detalle
            Text(
                text = item.item_details,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0.560f, 0.470f, 0.435f, 1.0f)
            )
            // Fila para precio y cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$${itemTotalPrice}",
                    style = TextStyle(
                        fontSize = 20.sp, // Tamaño de fuente fijo
                        fontWeight = FontWeight.Bold // Negrita
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0.352f, 0.196f, 0.070f, 1.0f)
                )
                // Mostrar cantidad actual
                Text(
                    text = "$itemCartQuantity ${if (itemCartQuantity > 1) "unds" else "und"}",
                    style = TextStyle(
                        fontSize = 20.sp, // Tamaño de fuente fijo
                        fontWeight = FontWeight.Bold // Negrita
                    ),
                    color = Color(0.560f, 0.470f, 0.435f, 1.0f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun TotalSection(total: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp),
                color = Color(0.353f, 0.196f, 0.071f, 1.0f)
            )
            Text(
                text = "$$total", // Mostrar el total en formato de precio
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 30.sp),
                color = Color(0.353f, 0.196f, 0.071f, 1.0f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NavigateTrackOrder(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom, // Alinea los elementos al fondo
        horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = Color(0.192f, 0.263f, 0.255f, 1.0f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    navController.navigate(FoodiesScreens.FoodiesTrackScreen.name)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Rastrear Orden",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

