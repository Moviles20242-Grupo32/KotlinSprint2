package com.example.foodies.view.shoppingCart

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.foodies.model.Item
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import com.example.foodies.viewModel.FoodiesScreens
import com.example.foodies.viewModel.ShoppingViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton


@Composable
fun FoodiesShoppingCartScreen(
    navController: NavController,
    viewModel: ShoppingViewModel
) {
    // Estados
    val cart by viewModel.cart.observeAsState()
    val totalAmount by viewModel.totalAmount.observeAsState(0)
    val orderSuccess by viewModel.orderSuccess.observeAsState() // Observa el éxito de la orden
    val errorMessage by viewModel.error.observeAsState() // Observa los errores
    val internetConnected by viewModel.internetConnected.observeAsState()

    // Estado para mostrar o no el diálogo
    var showDialog by remember { mutableStateOf(false) }
    var showEmptyCartDialog by remember { mutableStateOf(false) } // Estado para el diálogo de carrito vacío
    var showNoInternetDialog by remember { mutableStateOf(false) }

    // Si la orden se guardó con éxito, mostrar el diálogo
    if (orderSuccess == true && !showDialog) {
        showDialog = true // Activar el diálogo cuando la orden es exitosa
    }

    // Mostrar el diálogo de carrito vacío si hay un error de carrito vacío
    if (errorMessage != null && !showEmptyCartDialog) {
        showEmptyCartDialog = true
    }

    // Mostrar el diálogo de pérdida de internet si no hay conexión
    if (internetConnected == false && !showNoInternetDialog) {
        showNoInternetDialog = true
    }

    // Mostrar el diálogo de éxito de la orden
    if (showDialog) {
        AlertDialog(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            onDismissRequest = {
                showDialog = false
                viewModel.resetOrderSuccess() // Resetear el estado de éxito
            },
            title = { Text(text = "Orden Creada") },
            text = { Text(text = "Tu orden ha sido creada exitosamente.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.clearCart() // Vaciar el carrito
                    showDialog = false    // Cerrar el diálogo
                    viewModel.resetOrderSuccess() // Resetear orderSuccess
                }) {
                    Text("Aceptar")
                }
            }
        )
    }


    // Diálogo de error de carrito vacío
    if (showEmptyCartDialog) {
        AlertDialog(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            onDismissRequest = {
                showEmptyCartDialog = false
                viewModel.resetError() // Resetear el estado de error
            },
            title = { Text(text = "Error") },
            text = { Text(text = "El carrito está vacío, agrega productos antes de realizar el pedido.") },
            confirmButton = {
                Button(onClick = {
                    showEmptyCartDialog = false // Cerrar el diálogo
                    viewModel.resetError() // Resetear el estado de error
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de pérdida de conexión a internet
    if (showNoInternetDialog) {
        AlertDialog(
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(16.dp)),
            onDismissRequest = {
                // Evitar que el diálogo se cierre accidentalmente
                showNoInternetDialog = true
            },
            title = { Text(text = "Sin conexión a internet") },
            text = { Text(text = "No puede realizar una orden sin internet") },
            confirmButton = {
                Button(onClick = {
                    showNoInternetDialog = false
                    navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

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
            // Fila para la flecha y el título
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0.945f, 0.600f, 0.216f, 1.0f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { navController.navigate(FoodiesScreens.FoodiesHomeScreen.name) } // Navegar solo al hacer clic
                )
                Text(
                    text = "Carrito",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0.353f, 0.196f, 0.071f, 1.0f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de items ocupando el espacio restante
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                cart?.let {
                    items(it.getItems()) { item ->
                        ItemCard(item, viewModel)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de total y botón de Check Out en la parte inferior
            totalAmount?.let {
                CheckoutSection(
                    total = it,
                    viewModel = viewModel,
                    navController = navController
                )
            }

        }
    }
}

@Composable
fun CheckoutSection(total: Int, viewModel: ShoppingViewModel, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
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

        Spacer(modifier = Modifier.height(8.dp))

        // Botón de Check Out
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = Color(0.192f, 0.263f, 0.255f, 1.0f),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    viewModel.registerPrice()
                    viewModel.saveOrder(
                        onSuccess = {
                            // Solo limpiar el carrito, no navegar automáticamente
                            viewModel.clearCart()
                        },
                        onFailure = { exception ->
                            // Manejo de errores, por ejemplo, mostrar un diálogo
                        }
                    )
                }, // Ejecuta la función pasada al hacer clic
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ordenar",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}


@Composable
fun ItemCard(item: Item, viewModel: ShoppingViewModel) {
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
                horizontalArrangement = Arrangement.SpaceAround
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
                // Pasar itemCartQuantity como MutableState<Int> a ItemQuantityControl
                ItemQuantityControl(item, viewModel, itemCartQuantity) { newQuantity ->
                    itemCartQuantity = newQuantity
                }
            }
        }

        // Espacio entre las columnas
        Spacer(modifier = Modifier.width(20.dp))

        // Columna 3: Botón para eliminar item del carrito
        IconButton(
            modifier = Modifier.align(Alignment.CenterVertically), // Centrar el ícono de eliminar verticalmente
            onClick = { viewModel.removeItemFromCart(item) }  // Llamar a la función de ViewModel para eliminar el ítem
        ) {
            Icon(
                imageVector = Icons.Default.Delete,  // Icono de eliminación
                contentDescription = "Eliminar",  // Descripción del icono
                tint = Color(0.352f, 0.196f, 0.070f, 1.0f) // Color del icono
            )
        }
    }
}

@Composable
fun ItemQuantityControl(item: Item, viewModel: ShoppingViewModel, itemCartQuantity: Int, onQuantityChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Botón de restar
        Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Reduce quantity",
            tint = Color(0.352f, 0.196f, 0.070f, 1.0f),
            modifier = Modifier
                .clickable {
                    if (itemCartQuantity > 1) { // Evitar valores negativos
                        val newQuantity = itemCartQuantity - 1
                        onQuantityChange(newQuantity) // Actualiza la cantidad
                        viewModel.updateItemQuantity(item, -1)
                        viewModel.updateTotal()
                    }
                }
        )
        // Mostrar cantidad actual
        Text(
            text = itemCartQuantity.toString(),
            color = Color(0.560f, 0.470f, 0.435f, 1.0f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Botón de sumar
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase quantity",
            tint = Color(0.352f, 0.196f, 0.070f, 1.0f),
            modifier = Modifier
                .clickable {
                    val newQuantity = itemCartQuantity + 1
                    onQuantityChange(newQuantity) // Actualiza la cantidad
                    viewModel.updateItemQuantity(item, 1)
                    viewModel.updateTotal()
                }
        )
    }
}

