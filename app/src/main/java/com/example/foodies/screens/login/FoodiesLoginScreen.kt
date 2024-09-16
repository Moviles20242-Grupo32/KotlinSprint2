package com.example.foodies.screens.login

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.foodies.navigation.FoodiesScreens
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.foodies.R

@Composable
fun FoodiesLoginScreen(navController: NavController, viewModel: LoginScreenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()){
    val showLoginForm = rememberSaveable {
        mutableStateOf(true)
    }

    Surface(modifier = Modifier
        .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            //verticalArrangement = Arrangement.spacedBy(30.dp),
            modifier = Modifier
                .fillMaxSize()

        ){
                Image(
                    painterResource(id = R.drawable.tipografia),
                    contentDescription = "Foodies",
                    modifier = Modifier

                )

                //Spacer(modifier = Modifier.height(10.dp))

                Image(
                    painterResource(id = R.drawable.logo),
                    contentDescription = "Foodies",
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .size(80.dp)
                )


            if(showLoginForm.value){
                UserForm(
                    isCreateAccount = false
                ){
                    email, password, name->
                    Log.d("Foodies", "Logueando con $email y $password")
                    viewModel.signInWithEmailAndPassword(email, password){
                        navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                    }
                }
            }
            else{
                UserForm(
                    isCreateAccount = true
                ){
                        email, password, name->
                        Log.d("Foodies", "Creando cuenta con $email y $password")
                    viewModel.createUserWithEmailAndPassword(email, password, name){
                        navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                    }
                }

            }

            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                val text1 =
                    if(showLoginForm.value) "¿No tienes cuenta?" else "¿Ya tienes cuenta?"

                val text2 =
                    if(showLoginForm.value) "Regístrate" else "Inicia sesión"

                Text(text = text1)
                Text(text = text2,
                    modifier = Modifier
                        .clickable { showLoginForm.value = !showLoginForm.value }
                        .padding(start = 5.dp),
                    fontWeight = FontWeight.Bold

                )

            }
        }

    }
}

@Composable
fun UserForm(isCreateAccount: Boolean = false,onDone: (String, String, String) -> Unit = {email, pwd, name ->})
{
    val email = rememberSaveable {
        mutableStateOf("")
    }

    val name = rememberSaveable {
        mutableStateOf("")
    }
    val password = rememberSaveable {
        mutableStateOf("")
    }
    val passwordVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val valido = remember(email.value, password.value){
        email.value.trim().isNotEmpty() && password.value.trim().isNotEmpty()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(horizontalAlignment = Alignment.CenterHorizontally){
        if(isCreateAccount){
            NameInput(
                nameState = name,
            )
        }
        EmailInput(
            emailState = email,

        )
        PasswordInput(
            passwordState = password,
            labelId = "Ingrese su contraseña",
            passwordVisible = passwordVisible
        )
        Spacer(modifier = Modifier.height(15.dp))
        SubmitButton(
            textId = if(isCreateAccount) "Registrar" else "Iniciar sesión →",
            inputValido = valido
        )
        {
            onDone(email.value.trim(), password.value.trim(), name.value.trim())
            keyboardController?.hide()
        }
    }

}

@Composable
fun NameInput(nameState: MutableState<String>, labelId: String = "Enter your name") {
    Text(text = "Name", fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
    InputField(
        valueState = nameState,
        labelId = labelId,
        keyboardType = KeyboardType.Text,
        placeholder = "Enter name"
    )

}

@Composable
fun SubmitButton(textId: String, inputValido: Boolean, onClic: ()->Unit) {
    Button(onClick = onClic,
        modifier = Modifier
            .padding(3.dp)
            .fillMaxWidth(),
        shape = CircleShape,
        enabled = inputValido
        ) {
        Text(text = textId,
            modifier = Modifier
                .padding(5.dp)
        )
    }

}

@Composable
fun PasswordInput(passwordState: MutableState<String>, labelId: String, passwordVisible: MutableState<Boolean>, placeholder: String = "Ingrese su contraseña") {
    val visualTransformation = if(passwordVisible.value){
        VisualTransformation.None
    }
    else{
        PasswordVisualTransformation()
    }
    Text(text = "Contraseña", fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
    OutlinedTextField(
        value =  passwordState.value,
        onValueChange = {passwordState.value = it},
        label = {Text(text = labelId, color = Color.Gray)},
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password
        ),
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        visualTransformation = visualTransformation,
        trailingIcon = {
            if(passwordState.value.isNotBlank()){
                PasswordVisibleIcon(passwordVisible)
            }
            else{
                null
            }
        }
        )
}

@Composable
fun PasswordVisibleIcon(passwordVisible: MutableState<Boolean>) {
    val image =
        if(passwordVisible.value){
            Icons.Default.VisibilityOff
        }
    else{
            Icons.Default.Visibility
        }
    IconButton(onClick = {
        passwordVisible.value = !passwordVisible.value
    }) {
        Icon(imageVector = image,
            contentDescription = "")

    }
}

@Composable
fun EmailInput(emailState: MutableState<String>,labelId: String = "nombre@ejemplo.com") {
    Text(text = "Email", fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
    InputField(
        valueState = emailState,
        labelId = labelId,
        keyboardType = KeyboardType.Email,
        placeholder = "nombre@ejemplo.com"
    )

}

@Composable
fun InputField(valueState: MutableState<String>, labelId: String, isSingleLine: Boolean = true,keyboardType: KeyboardType, placeholder: String) {
    OutlinedTextField(
        value = valueState.value,
        onValueChange = {valueState.value = it},
        label = { Text(text = labelId, color = Color.Gray)},
        placeholder = { Text(text = placeholder, color = Color.Gray) },
        singleLine = isSingleLine,
        modifier = Modifier
            .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        //textStyle = TextStyle(fontSize = 3.sp)


    )




}
