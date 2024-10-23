package com.example.foodies

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.foodies.model.FoodiesNotificationManager
import com.example.foodies.model.LocationWorker
import com.example.foodies.model.NetworkMonitor
import com.example.foodies.viewModel.FoodiesNavigation
import com.example.foodies.ui.theme.FoodiesTheme
import java.util.concurrent.TimeUnit


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodiesTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FoodiesApp()
                }
            }
        }
        // Iniciar los Workers al crear la actividad
        locationWorker()
        NetworkMonitor.initialize(this)
    }

    //Workers
    private fun locationWorker(){
        val locationWorker = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueue(locationWorker)
    }

    //On destroy activity
    override fun onDestroy() {
        super.onDestroy()
        NetworkMonitor.stopMonitoring() // Liberar recursos
    }
}

@Composable
fun FoodiesApp(){
    Surface (modifier = Modifier
        .fillMaxSize()
        .padding(top = 46.dp)){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            FoodiesNavigation()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FoodiesTheme {
        Greeting("Android")
    }
}