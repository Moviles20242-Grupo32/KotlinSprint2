import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.example.foodies.viewModel.ProductDetailViewModel
import com.example.foodies.model.Item
import com.example.foodies.view.home.GlideImage
import com.example.foodies.viewModel.ShoppingViewModel


@Composable
fun FoodiesProductDetailScreen(
    navController: NavController,
    viewModel: ProductDetailViewModel,
    shoppingViewModel: ShoppingViewModel,
    productId: String
) {
    val product by viewModel.product.observeAsState()

    LaunchedEffect(productId) {
        if (productId.isNotEmpty()) {
            viewModel.fetchProductDetails(productId) // Fetch product details based on productId
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp), // Ensure there is padding around the entire layout
        color = Color.White
    ) {
        // Box for vertical scrolling
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Add scrolling capability
                .padding(bottom = 16.dp) // Avoid bottom space issues
        ) {
            // Back arrow
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0.945f, 0.600f, 0.216f, 1.0f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            navController.navigateUp() // Navigate back
                        }
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

            // Show product details if available
            product?.let { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Product name with increased font size
                    Text(
                        text = item.item_name,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
                        color = Color(0.352f, 0.196f, 0.070f, 1.0f),
                        textAlign = TextAlign.Center // Center the text
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

                    // Rating stars
                    Row(
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        val rating = item.item_ratings.toFloatOrNull() ?: 0f
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = Color(0.968f, 0.588f, 0.066f, 1.0f),
                                modifier = Modifier.size(28.dp) // Increased star size
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

                    // Product image with padding
                    GlideImage(
                        imageUrl = item.item_image,
                        contentDescription = "Imagen de ${item.item_name}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp) // Increased image height
                            .padding(16.dp)
                    )
                }
            } ?: run {
                // Display loading message until product details are fetched
                Text(text = "Cargando detalles del producto...")
            }

            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

            // Product cost with larger font size
            Text(
                text = "$${product?.item_cost}",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 30.sp), // Increased font size
                color = Color(0.352f, 0.196f, 0.070f, 1.0f),
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            Spacer(modifier = Modifier.height(16.dp)) // Increased spacing

            // Product details (Possible content) with larger title
            Text(
                text = "Posible contenido",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = Color(0.352f, 0.196f, 0.070f, 1.0f),
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Show item ingredients (as possible content)
            Text(
                text = product?.item_details ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp), // Increased font size
                color = Color(0.560f, 0.470f, 0.435f, 1.0f),
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            // Divider for separating sections
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Ingredientes section with larger font
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = Color(0.968f, 0.588f, 0.066f, 1.0f), // Updated orange color
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder text for ingredients section
            Text(
                text = "Pendiente", // Placeholder text for now
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp), // Increased font size
                color = Color(0.560f, 0.470f, 0.435f, 1.0f),
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            // Divider for separating sections
            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Productos estrella section with larger font
            Text(
                text = "Productos estrella",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = Color(0.968f, 0.588f, 0.066f, 1.0f), // Updated orange color
                modifier = Modifier.fillMaxWidth() // Align to the left
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Placeholder text for featured products section
            Text(
                text = "Pendiente", // Placeholder text for now
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp), // Increased font size
                color = Color(0.560f, 0.470f, 0.435f, 1.0f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp)) // Increased spacing

            // Add the orange "plus" button at the bottom
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (product?.isAdded == true) Color(0.192f, 0.262f, 0.254f) // Blue when added
                        else Color(0.968f, 0.588f, 0.066f) // Orange when not added
                    )
                    .clickable {
                        product?.id?.let {
                            shoppingViewModel.addItemToCart(it) // Add the item to the cart
                        }
                    }
            ) {
                Icon(
                    imageVector = if (product?.isAdded == true) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = "Agregar al carrito",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(30.dp)
                )
            }
        }
    }
}
