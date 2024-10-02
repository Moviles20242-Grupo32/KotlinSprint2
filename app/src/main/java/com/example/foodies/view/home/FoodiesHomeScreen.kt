package com.example.foodies.view.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.foodies.model.Item
import com.example.foodies.viewModel.FoodiesScreens
import com.example.foodies.viewModel.ShoppingViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

@Composable
fun FoodiesHomeScreen(
    navController: NavController,
    viewModel: ShoppingViewModel
) {
    // Obtener el estado de los items desde el ViewModel
    val items by viewModel.items.observeAsState(emptyList())
    val msitem by viewModel.msitem.observeAsState(null)
    val isLoaded by viewModel.isLoaded.observeAsState(false)
    val error by viewModel.error.observeAsState()
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.observeAsState("Ubicación no disponible")

    // Llamar a la función para obtener los datos al entrar en la pantalla
    LaunchedEffect(Unit) {
        if (!isLoaded) {
            viewModel.mostSellItem()
            viewModel.fetchItems()
        }
        viewModel.initTextToSpeech(context)
        viewModel.requestLocationUpdate(context)
    }

    // Manejar posibles errores
    error?.let {
        Text(text = "Error: $it", style = MaterialTheme.typography.bodyLarge)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Fila 1: Texto "Botones"
            ActionButtons(items,navController,viewModel)

            // Fila 2: Texto "Locación"
            Location(userLocation)

            // Fila 3: Barra de busqueda
            FilterBar(onFilter = { query ->
                viewModel.filterItemsByName(query)
            })

            // Lista de ítems usando la función modularizada
            msitem?.let { ItemsList(items,viewModel, it) }
        }
    }
}

@Composable
fun ActionButtons(items: List<Item>,navController: NavController, viewModel: ShoppingViewModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =  Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart, // Carrito de compras
            contentDescription = "Carrito de compras",
            tint = Color(0.968f, 0.588f, 0.066f, 1.0f), // Color del icono
            modifier = Modifier
                .size(45.dp)
                .clickable { navController.navigate(FoodiesScreens.FoodiesShoppingCartScreen.name) }
        )
        Spacer(modifier = Modifier.width(10.dp))
        var textSpeech by rememberSaveable { mutableStateOf(false) }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.VolumeUp, // Megáfono
            contentDescription = "Megáfono",
            tint = Color.White, // Tinte blanco del ícono
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (textSpeech) Color(0.192f, 0.262f, 0.254f) else Color(
                        0.968f,
                        0.588f,
                        0.066f,
                        1.0f
                    )
                )
                .padding(8.dp)
                .clickable {
                    textSpeech = true
                    viewModel.readItemList(items){
                        textSpeech = false
                    }
                }
        )
        Spacer(modifier = Modifier.width(10.dp))
        Icon(
            imageVector = Icons.Filled.Person, // Usuario
            contentDescription = "Usuario",
            tint = Color(0.968f, 0.588f, 0.066f, 1.0f),
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = Color(0.968f, 0.588f, 0.066f, 1.0f),
                    shape = CircleShape
                )
        )
    }
}

@Composable
fun Location(ubi: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Localización",
            tint = Color(0.192f, 0.262f, 0.254f) ,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = ubi,
            color = Color(0.352f, 0.196f, 0.070f, 1.0f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun FilterBar(onFilter: (String) -> Unit) {
    // Definir el estado localmente dentro de la función
    var searchText by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(19.dp))
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(19.dp))
            .background(Color(0.952f, 0.952f, 0.949f, 1.0f))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = Color.Gray, // Color de la lupa
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )

            BasicTextField(
                value = searchText,
                onValueChange = { newText ->
                    searchText = newText
                    onFilter(newText) // Llama al método de filtrado en el ViewModel
                },
                modifier = Modifier
                    .fillMaxWidth(),
                textStyle = TextStyle(color = Color.Black) // Estilo de texto
            )
        }
    }
}

@Composable
fun MostSellItem(item: Item, viewModel: ShoppingViewModel){
    Column {
        Text(
            text = "Most Ordered Box",
            style = TextStyle(
                fontSize = 15.sp, // Tamaño de fuente fijo
                fontWeight = FontWeight.Bold // Negrita
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color(0.352f, 0.196f, 0.070f, 1.0f)
        )
        ItemCard(item,viewModel,item)
    }
}

@Composable
fun ItemsList(items: List<Item>, viewModel: ShoppingViewModel, msitem:Item) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        val filteredItems = items.filter { it.show }
        items(filteredItems) { item ->
            ItemCard(item,viewModel,msitem)
        }
    }
}

@Composable
fun ItemCard(item: Item, viewModel: ShoppingViewModel, msitem:Item) {
    val rating = item.item_ratings.toFloatOrNull() ?: 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        // Columna 1: Imagen del item
        AsyncImage(
            model = item.item_image,
            contentDescription = "Imagen de ${item.item_name}",
            modifier = Modifier
                .size(100.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Columna 2: Información del item (nombre, detalles, calificación)
        Column(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

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

            if (item.id == msitem.id) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0.925f, 0.925f, 0.922f, 1.0f), // Color de fondo
                            shape = RoundedCornerShape(16.dp) // Esquinas redondeadas
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp) // Padding interno del texto
                ) {
                    Text(
                        text = "Favorita del público",
                        color = Color(0.352f, 0.196f, 0.070f, 1.0f), // Color del texto
                        style = MaterialTheme.typography.bodySmall // Estilo del texto
                    )
                }
            }

            Text(
                text = item.item_details,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0.560f, 0.470f, 0.435f, 1.0f)
            )
            // Representación de las estrellas de rating
            Row {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = Color(0.968f, 0.588f, 0.066f, 1.0f), // Color de la estrella
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(20.dp))

        //Columna 3: Agregar a carrito
        Box(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(40.dp) // Tamaño total del Box
                .clip(CircleShape) // Aplica el recorte circular al Box
                .background(
                    if (item.isAdded) Color(0.192f, 0.262f, 0.254f) else Color(0.968f, 0.588f,0.066f, 1.0f)
                )
                .border(
                    width = 1.dp, // Ancho del borde
                    color = Color.Transparent, // Color del borde
                    shape = CircleShape // Esquinas redondeadas completamente
                )
                .clickable {viewModel.addItemToCart(item.id)}
        ) {
            Icon(
                imageVector = if (item.isAdded) Icons.Filled.Check else Icons.Filled.Add,
                contentDescription = "Agregar al carrito",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center) // Centra el Icon dentro del Box
                    .size(30.dp) // Tamaño del Icono
            )
        }
    }
}

