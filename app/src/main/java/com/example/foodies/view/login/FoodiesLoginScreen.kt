package com.example.foodies.view.login

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.foodies.R
import com.example.foodies.viewModel.AuthViewModel
import com.example.foodies.viewModel.FoodiesScreens

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun FoodiesLoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel(),
) {
    val showLoginForm = rememberSaveable { mutableStateOf(true) }
    val internetConnected by viewModel._internetConnected.observeAsState()
    val errorMessage = remember { mutableStateOf("") }
    val context = LocalContext.current

    // Estados para diálogos
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Verifica los permisos
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
        if (context is Activity) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                ),
                101
            )
        }
    }

    // Diálogo de pérdida de conexión
    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            title = { Text(text = "Sin conexión a internet") },
            text = { Text(text = "No puede iniciar sesión o registrarse sin internet") },
            confirmButton = {
                Button(onClick = { showNoInternetDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Pantalla principal
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Hacer desplazable
        ) {
            // Logo superior
            Image(
                painter = painterResource(id = R.drawable.tipografia),
                contentDescription = "Foodies",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Foodies",
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .size(100.dp) // Tamaño adaptado
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Formulario (login o registro)
            if (showLoginForm.value) {
                UserForm(isCreateAccount = false) { email, password, name ->
                    viewModel.signInWithEmailAndPassword(email, password, {
                        navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                    }, { message ->
                        errorMessage.value = message
                        if (internetConnected == false && !showNoInternetDialog) {
                            showNoInternetDialog = true
                        }
                    })
                }
            } else {
                UserForm(isCreateAccount = true) { email, password, name ->
                    viewModel.createUserWithEmailAndPassword(email, password, name, {
                        navController.navigate(FoodiesScreens.FoodiesHomeScreen.name)
                    }, { message ->
                        errorMessage.value = message
                        if (internetConnected == false && !showNoInternetDialog) {
                            showNoInternetDialog = true
                        }
                    })
                }
            }

            var mensajeError = ""

            if(errorMessage.value.isNotEmpty()){
                showErrorDialog = true
                if(errorMessage.value == "The email address is badly formatted."){
                    mensajeError = "Formato de email inválido"
                }
                else if(errorMessage.value == "The supplied auth credential is incorrect, malformed or has expired."){
                    mensajeError = "Email o contraseña incorrectos o no registrados"
                }
                else if(errorMessage.value == "The email address is already in use by another account."){
                    mensajeError = "Email ya en uso por favor ingrese uno nuevo"
                }
                else if(errorMessage.value == "The given password is invalid. [ Password should be at least 6 characters ]"){
                    mensajeError = "Contraseña inválida, debe tener al menos 6 caracteres"
                }
                else{
                    mensajeError = "Error desconocido"
                }
            }

            if (showErrorDialog) {
                AlertDialog(
                    modifier = Modifier
                        .padding(24.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    onDismissRequest = {
                        showErrorDialog = false // Esto cerrará el diálogo si se toca fuera
                    },
                    title = { Text(text = "Error") },
                    text = { Text(text = mensajeError) },
                    confirmButton = {
                        Button(onClick = {
                            showErrorDialog = false // Asegúrate de que esto cierre el diálogo
                            errorMessage.value = "" // Limpia el mensaje de error si es necesario
                        }) {
                            Text("Aceptar")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón de cambio entre login/registro
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val text1 =
                    if (showLoginForm.value) "¿No tienes cuenta?" else "¿Ya tienes cuenta?"

                val text2 =
                    if (showLoginForm.value) "Regístrate" else "Inicia sesión"

                Text(text = text1)
                Text(
                    text = text2,
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
fun NameInput(nameState: MutableState<String>, labelId: String = "Ingrese su nombre") {
    Text(text = "Nombre", fontWeight = FontWeight.Bold, textAlign = TextAlign.Left)
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
