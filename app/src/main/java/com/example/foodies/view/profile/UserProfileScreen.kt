package com.example.foodies.view.profile

import androidx.compose.runtime.Composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodies.viewModel.LoginViewModel
import com.example.foodies.model.User
import com.example.foodies.viewModel.FoodiesScreens
import androidx.compose.material3.Surface


@Composable
fun UserProfileScreen(
    navController: NavController,
    viewModel: LoginViewModel // LoginViewModel to handle session logic
) {
    // Observe the current user session state from the ViewModel
    val userSession by viewModel.userSession.observeAsState()

    // Main surface with full screen size and background color
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF6F6F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Arrow and Profile Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0.945f, 0.600f, 0.216f),
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { navController.navigate(FoodiesScreens.FoodiesHomeScreen.name) }
                )
                Text(
                    text = "Perfil",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0.353f, 0.196f, 0.071f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // If the user is logged in, display their profile information
            userSession?.let { user ->
                // Profile avatar using the first two characters of the user's name
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFA726)), // Background color for avatar
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineLarge.copy(color = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User's full name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF333333)
                )

                // User's email address
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF777777)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // App version section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.Gray,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Versión",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "1.0.0",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Out button
            Button(
                onClick = {
                    viewModel.signOut() // Sign-out logic
                    navController.navigate(FoodiesScreens.FoodiesLoginScreen.name) {
                        popUpTo(0) // Remove backstack to avoid returning after sign-out
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red), // Corrected: Use containerColor
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cerrar Sesión", color = Color.White)
            }

        }
    }
}

