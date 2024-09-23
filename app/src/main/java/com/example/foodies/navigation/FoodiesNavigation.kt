package com.example.foodies.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodies.screens.home.FoodiesHomeScreen
import com.example.foodies.screens.login.FoodiesLoginScreen
import com.example.foodies.screens.shoppingCart.FoodiesShoppingCartScreen

@Composable
fun FoodiesNavigation(){
    val navController = rememberNavController()
    val viewModel: ShoppingViewModel = viewModel()
    NavHost(navController = navController, startDestination = FoodiesScreens.FoodiesLoginScreen.name) {
        
        composable(FoodiesScreens.FoodiesLoginScreen.name){
            FoodiesLoginScreen(navController = navController)
        }

        composable(FoodiesScreens.FoodiesHomeScreen.name){
            FoodiesHomeScreen(navController = navController, viewModel = viewModel)
        }

        composable(FoodiesScreens.FoodiesShoppingCartScreen.name){
            FoodiesShoppingCartScreen(navController = navController, viewModel = viewModel)
        }
    }
}