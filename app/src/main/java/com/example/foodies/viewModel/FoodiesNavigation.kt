package com.example.foodies.viewModel

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodies.view.home.FoodiesHomeScreen
import com.example.foodies.view.login.FoodiesLoginScreen
import com.example.foodies.view.shoppingCart.FoodiesShoppingCartScreen

@Composable
fun FoodiesNavigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = FoodiesScreens.FoodiesLoginScreen.name) {
        
        composable(FoodiesScreens.FoodiesLoginScreen.name){
            FoodiesLoginScreen(navController = navController)
        }

        composable(FoodiesScreens.FoodiesHomeScreen.name){
            FoodiesHomeScreen(navController = navController)
        }

        composable(FoodiesScreens.FoodiesShoppingCartScreen.name){
            FoodiesShoppingCartScreen(navController = navController)
        }
    }
}