package Foodies.Screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import java.security.AccessController

@Composable
fun FoodiesLoginScreen(navController: NavController){
    val showLoginForm = rememberSaveable {
        mutableStateOf(true)
    }

    Surface(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            if(showLoginForm.value){
                Text(text="Inicia Sesión")
                UserForm(
                    isCreateAccount = true
                ){
                    email, password ->
                }
            }
            else{
                Text(text="Crea una cuenta")
                UserForm(
                    isCreateAccount = true
                ){
                        email, password ->
                }

            }
        }

    }
}

@Composable
fun UserForm(isCreateAccount: Boolean = false,onDone: (String, String) -> Unit = {email, pwd ->})
{
    val email = rememberSaveable {
        mutableStateOf("")
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        EmailInput(
            emailState = email
        )
    }

}

@Composable
fun EmailInput(emailState: MutableState<String>,labelId: String = "Email") {
    InputField(
        valueState = emailState,
        labelId = labelId,
        keyboardType = KeyboardType.Email
    )

}

@Composable
fun InputField(valueState: MutableState<String>, labelId: String, isSingleLine: Boolean = true,keyboardType: KeyboardType) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = {valueState.value = it},
        label = { Text(text = labelId)},
        singleLine = isSingleLine,
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        )
    )




}
