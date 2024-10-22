package com.example.foodies.viewModel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodies.view.home.FoodiesHomeScreen
import com.example.foodies.view.login.FoodiesLoginScreen
import com.example.foodies.view.profile.FoodiesProfileScreen
import com.example.foodies.view.shoppingCart.FoodiesShoppingCartScreen

@Composable
fun FoodiesNavigation(){
    val navController = rememberNavController()
    val shoppingViewModel: ShoppingViewModel = viewModel() // Inject ShoppingViewModel
    val logoutViewModel: LogoutViewModel = viewModel() // Inject LogoutViewModel

    NavHost(navController = navController, startDestination = FoodiesScreens.FoodiesLoginScreen.name) {

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
            FoodiesProfileScreen(navController = navController, logoutViewModel = logoutViewModel)
        }
    }
}
