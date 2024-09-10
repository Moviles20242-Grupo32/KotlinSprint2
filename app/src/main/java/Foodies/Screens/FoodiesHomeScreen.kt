package foodies.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.foodies.ui.theme.FoodiesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FoodiesTheme {
                // A surface container using the background color from the theme
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    // A simple composable that displays "Home"
    Text(
        text = "Home",
        modifier = Modifier.fillMaxSize(),
        style = MaterialTheme.typography.headlineMedium
    )
}