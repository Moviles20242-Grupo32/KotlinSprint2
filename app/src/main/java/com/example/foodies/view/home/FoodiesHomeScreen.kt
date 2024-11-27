package com.example.foodies.view.home

import android.util.Log
import android.widget.ImageView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.foodies.model.Item
import com.example.foodies.viewModel.FoodiesScreens
import com.example.foodies.viewModel.ShoppingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
    val internetConnected by viewModel.internetConnected.observeAsState()
    val hasActiveOrder by viewModel.hasActiveOrder.observeAsState(false)
    //Remembers
    var sort by rememberSaveable { mutableStateOf(false) }
    // Llamar a la función para obtener los datos al entrar en la pantalla
    LaunchedEffect(Unit) {
        //Obtener productos iniciales
        viewModel.fetchItems()
        //Incialización de elementos adicionales
        viewModel.initTextToSpeech(context)
        viewModel.requestLocationUpdate(context)
        viewModel.storeInfo()
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

            // Fila 3: Barra de búsqueda
            FilterBar(onFilter = { query ->
                viewModel.filterItemsByName(query)
            }, sort = {
                viewModel.sortByCheaperItems()
                sort = !sort
            })



            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ocupa el ancho total del contenedor
                    .padding(vertical = 16.dp) // Espaciado opcional
            ) {
                Button(
                    onClick = { viewModel.loadLastOrder()
                                viewModel.registerUseOfTrack()
                              },
                    modifier = Modifier.align(Alignment.Center), // Centra el botón horizontalmente en el Box
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(241, 153, 55)
                    )
                ) {
                    Text(
                        text = "Realizar pedido anterior",
                        modifier = Modifier.padding(5.dp)
                    )
                }
            }
            viewModel.getOrderStatus()

            if (hasActiveOrder) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tienes una orden activa",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Button(
                            onClick = {
                                navController.navigate(FoodiesScreens.FoodiesTrackScreen.name)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(241, 153, 55)
                            ),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(text = "Seguir tu orden")
                        }
                    }
                }
            }





            Log.d("Items-v", "$items")
            // Lista de ítems usando la función modularizada
            if (internetConnected == false){
                ShimmerList("Esperando conexión a internet...")
            }
            else if (items.isEmpty()){
                ShimmerList("Cargando productos disponibles")
            }
            else{
                //Obtener datos
                if (!sort){
                    FetchItemsData(viewModel, onComplete = {
                        msitem?.let { ItemsList(items,viewModel, it) }
                    })
                }else{
                    msitem?.let { ItemsList(items,viewModel, it) }
                }
            }



        }
    }
}


//Función para obtener datos
@Composable
fun FetchItemsData(viewModel: ShoppingViewModel, onComplete: @Composable () -> Unit) {
    //Producto más vendido
    viewModel.mostSellItem()
    //Obtiene los items disponibles
    val userId = Firebase.auth.currentUser?.uid
    if (userId != null) {
        viewModel.fetchUserPreferences(userId)
    }
    // Marcar como completo una vez finalicen las operaciones
    onComplete()
}

//Botones de acciones
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
                .clickable {
                    navController.navigate(FoodiesScreens.FoodiesProfileScreen.name) // Navigate to profile screen
                }

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
fun FilterBar(onFilter: (String) -> Unit,sort: () -> Unit) {
    var searchText by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Row interno para el icono de búsqueda y el campo de texto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(19.dp))
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(19.dp))
                    .background(Color(0.952f, 0.952f, 0.949f, 1.0f))
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 8.dp)
                )

                BasicTextField(
                    value = searchText,
                    onValueChange = { newText ->
                        searchText = newText
                        onFilter(newText)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = Color.Black)
                )
            }

            Spacer(modifier = Modifier.width(5.dp))

            // Ícono de dinero
            Icon(
                imageVector = Icons.Filled.AttachMoney, // Icono de dinero
                contentDescription = "Dinero",
                tint = Color.White,
                modifier = Modifier
                    .size(45.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(0.968f, 0.588f, 0.066f, 1.0f),
                        shape = CircleShape
                    )
                    .padding(3.dp)
                    .clickable {
                        sort()
                    }
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


//LISTA DE ITEMS
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
        //AsyncImage(
          //  model = item.item_image,
            //contentDescription = "Imagen de ${item.item_name}",
            //modifier = Modifier
              //  .size(100.dp)
        //)

        GlideImage(
            imageUrl = item.item_image,
            contentDescription = "Imagen de ${item.item_name}",
            modifier = Modifier.size(100.dp)
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

@Composable
fun GlideImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ImageView(context) },
        modifier = modifier,
        update = { imageView ->
            Glide.with(context)
                .load(imageUrl)
                .into(imageView)

            imageView.contentDescription = contentDescription
        }
    )
}

//Skelon effect
@Composable
fun ShimmerList(message: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp) // Espacio entre los items
    ) {
        // Texto informativo
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    color = Color(0.352f, 0.196f, 0.070f, 1.0f),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(fontSize = 15.sp)
                )
            }
        }
        // Placeholder de shimmer
        items(3) {
            ShimmerEffect()
        }
    }
}

@Composable
fun ShimmerEffect() {
    val transition = rememberInfiniteTransition(label = "")
    val alphaAnim = transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically // Alinear elementos verticalmente
    ) {
        // Círculo en la primera columna
        Box(
            modifier = Modifier
                .size(80.dp) // Tamaño del círculo
                .alpha(alphaAnim.value)
                .background(
                    color = Color(0.855f, 0.855f, 0.855f, 1.0f),
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(16.dp)) // Espacio entre columnas

        // Segunda columna con dos filas
        Column(
            modifier = Modifier.weight(1f) // Ocupa todo el espacio restante
        ) {
            // Primera fila con rectángulo más grande
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho disponible
                    .height(20.dp) // Altura del rectángulo
                    .alpha(alphaAnim.value)
                    .background(
                        color = Color(0.855f, 0.855f, 0.855f, 1.0f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre filas

            // Segunda fila con rectángulo más pequeño (3/4 del ancho)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f) // Ocupa 3/4 del ancho
                    .height(15.dp) // Menor altura
                    .alpha(alphaAnim.value)
                    .background(
                        color = Color(0.855f, 0.855f, 0.855f, 1.0f),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

