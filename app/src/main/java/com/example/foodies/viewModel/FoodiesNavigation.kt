package com.example.foodies.viewModel

import FoodiesProductDetailScreen
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodies.view.home.FoodiesHomeScreen
import com.example.foodies.view.login.FoodiesLoginScreen
import com.example.foodies.view.profile.FoodiesProfileScreen
import com.example.foodies.view.shoppingCart.ConfirmOrderScreen
import com.example.foodies.view.shoppingCart.FoodiesShoppingCartScreen
import com.example.foodies.view.shoppingCart.TrackOrderScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun itemsAvailability(shoppingViewModel: ShoppingViewModel) {
    // Ejecutar la tarea periódicamente en el hilo principal
    shoppingViewModel.viewModelScope.launch(Dispatchers.Main) {
        while (true) {
            Log.d("Items", "Actualizando")
            shoppingViewModel.fetchItems()  // Llamar al método del ViewModel

            // Esperar 2 minutos antes de la siguiente ejecución
            delay(2 * 60 * 1000L)  // 2 minutos en milisegundos
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FoodiesNavigation() {
    val navController = rememberNavController()
    val shoppingViewModel: ShoppingViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // Inicializar tareas periódicas y otros workers
    itemsAvailability(shoppingViewModel)

    NavHost(navController = navController, startDestination = if (authViewModel._user.value == null) {
        FoodiesScreens.FoodiesLoginScreen.name
    } else {
        FoodiesScreens.FoodiesHomeScreen.name
    }) {

        composable(FoodiesScreens.FoodiesLoginScreen.name){
            FoodiesLoginScreen(navController = navController)
        }

        composable(FoodiesScreens.FoodiesHomeScreen.name){
            FoodiesHomeScreen(navController = navController, viewModel = shoppingViewModel)
        }

        composable(FoodiesScreens.FoodiesShoppingCartScreen.name){
            FoodiesShoppingCartScreen(navController = navController, viewModel = shoppingViewModel)
        }

        composable(FoodiesScreens.FoodiesProfileScreen.name) {
            FoodiesProfileScreen(navController = navController, authViewModel = authViewModel, shoppingViewModel = shoppingViewModel)
        }

        // Update route to accept productId
        composable("productDetail/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            FoodiesProductDetailScreen(
                navController = navController,
                shoppingViewModel = shoppingViewModel
            )
        }

        composable(FoodiesScreens.FoodiesTrackScreen.name) {
            TrackOrderScreen(
                navController = navController,
                shoppingViewModel = shoppingViewModel
            )
        }

        composable(FoodiesScreens.ConfirmOrderScreen.name){
            ConfirmOrderScreen(navController = navController)
        }
    }
}
