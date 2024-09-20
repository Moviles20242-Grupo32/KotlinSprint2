package com.example.foodies.screens.shoppingCart

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.foodies.model.Cart
import com.example.foodies.model.Item

// Ejemplo de productos en el carrito
val cart = Cart().apply {
    addItem(Item(id = "1", item_name = "Pizza", item_cost = 25000, item_details = "Creps, waffles, postres, helados...",item_image = "https://raw.githubusercontent.com/Moviles20242-Grupo32/MovilesSprint1/main/Imagen_Creps.jpg"))
    addItem(Item(id = "2", item_name = "Burger", item_cost = 15000,item_details = "Pizza hawaina, pepperoni, toscana seis quesos...",item_image = "https://raw.githubusercontent.com/Moviles20242-Grupo32/MovilesSprint1/main/Imagen_PapaJohns.jpg"))
}

@Composable
fun FoodiesShoppingCartScreen(
    navController: NavController,
){
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Shopping Cart",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ItemsList(cart.getItems())
    }
}

@Composable
fun ItemsList(items: List<Item>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ItemCard(item)
        }
    }
}

@Composable
fun ItemCard(item: Item) {
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
            //Nombre
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
            //Detalle
            Text(
                text = item.item_details,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0.560f, 0.470f, 0.435f, 1.0f)
            )
            //Fila para precio y cantidad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ){
                Text(
                    text = "$${item.item_cost}",
                    style = TextStyle(
                        fontSize = 20.sp, // Tamaño de fuente fijo
                        fontWeight = FontWeight.Bold // Negrita
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0.352f, 0.196f, 0.070f, 1.0f)
                )
                ItemQuantityControl(item)

            }

        }

    }
}

@Composable
fun ItemQuantityControl(item: Item) {
    var quantity by remember { mutableStateOf(item.cart_quantity) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Botón de restar
        Icon(
            imageVector = Icons.Default.Remove,
            contentDescription = "Reduce quantity",
            tint = Color(0.352f, 0.196f, 0.070f, 1.0f),
            modifier = Modifier
                .clickable {
                    if (quantity > 1) {
                        quantity--
                    }
                }
        )

        // Mostrar cantidad actual
        Text(
            text = quantity.toString(),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Botón de sumar
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Increase quantity",
            tint = Color(0.352f, 0.196f, 0.070f, 1.0f),
            modifier = Modifier
                .clickable {
                    quantity++
                }
        )
    }
}
