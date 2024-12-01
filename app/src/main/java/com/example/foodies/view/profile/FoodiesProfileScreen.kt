package com.example.foodies.view.profile
import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.foodies.viewModel.AuthViewModel
import com.example.foodies.viewModel.FoodiesScreens
import com.example.foodies.viewModel.ShoppingViewModel

@Composable
fun FoodiesProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(), // Inject LogoutViewModel
    shoppingViewModel: ShoppingViewModel
) {

    val nombre = LocalContext.current.getSharedPreferences("user_info", Context.MODE_PRIVATE).getString("name", "NA")
    val email = LocalContext.current.getSharedPreferences("user_info", Context.MODE_PRIVATE).getString("email", "Email not avaiable")

    // If the user is null (after logging out), navigate to the login screen
    LaunchedEffect(Unit) {
        authViewModel.setUpUser()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Row for Back Arrow and Profile Title
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
                        .clickable {
                            // Navigate back to the home screen
                            navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User Info Section
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                color = Color(0.952f, 0.952f, 0.949f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0.945f, 0.600f, 0.216f, 1.0f)),
                        contentAlignment = Alignment.Center
                    ) {

                        //authViewModel.getUserById(user?.uid.toString())
                        val initials = nombre?.split(" ")
                            ?.mapNotNull { it.firstOrNull()?.uppercaseChar() } // Tomar la primera letra y convertirla a mayúscula
                            ?.joinToString("") // Unir todas las iniciales sin espacios

                        if (initials != null) {
                            Text(
                                text = initials,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display user's name
                    if (nombre != null) {
                        Text(
                            text = nombre,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Display user's email
                    if (email != null) {
                        Text(
                            //text = user?.email ?: "Email not available",
                            text = email,
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                        )
                    }
                }
            }

            // General Section Title
            Text(
                text = "GENERAL",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Version Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0.952f, 0.952f, 0.949f))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Version",
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Versión",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Account Section Title
            Text(
                text = "CUENTA",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Logout Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0.952f, 0.952f, 0.949f))
                    .padding(16.dp)
                    .clickable {
                        shoppingViewModel.resetCart()
                        shoppingViewModel.logout()
                        authViewModel.signOut() // Use LogoutViewModel for signing out
                        navController.navigate(FoodiesScreens.FoodiesLoginScreen.name) {
                            popUpTo(FoodiesScreens.FoodiesHomeScreen.name) {
                                inclusive = true
                            } // Clear backstack
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Cerrar Sesión",
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Cerrar Sesión",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Red)
                    )
                }
            }
        }
    }
}




