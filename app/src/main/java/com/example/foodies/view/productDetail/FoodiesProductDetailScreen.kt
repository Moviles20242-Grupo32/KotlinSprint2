import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.foodies.viewModel.ProductDetailViewModel

@Composable
fun FoodiesProductDetailScreen(
    navController: NavController,
    viewModel: ProductDetailViewModel = viewModel()
) {
    val productId = navController.previousBackStackEntry?.arguments?.getString("productId")
    Log.d("ProductDetail", "ProductId passed: $productId") // Log the productId

    // Check if productId is valid before proceeding
    LaunchedEffect(productId) {
        if (!productId.isNullOrEmpty()) {
            viewModel.fetchProductDetails(productId)  // Call the method to fetch product details
        }
    }

    val product by viewModel.product.observeAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Flecha de regreso
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0.945f, 0.600f, 0.216f, 1.0f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            navController.navigateUp()  // Navigate back
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Show product details if available
            product?.let { item ->
                Column {
                    Text(
                        text = item.item_name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0.352f, 0.196f, 0.070f, 1.0f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = item.item_details,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0.560f, 0.470f, 0.435f, 1.0f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "$${item.item_cost}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            } ?: run {
                // Display loading message until product details are fetched
                Text(text = "Cargando detalles del producto...")
            }
        }
    }
}
