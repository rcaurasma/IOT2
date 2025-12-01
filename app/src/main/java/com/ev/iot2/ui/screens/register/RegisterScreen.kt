package com.ev.iot2.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ev.iot2.data.database.DatabaseHelper
import com.ev.iot2.ui.components.IoTempButton
import com.ev.iot2.ui.components.IoTempPasswordField
import com.ev.iot2.ui.components.IoTempTextField
import com.ev.iot2.ui.components.MessageText
import com.ev.iot2.ui.theme.PrimaryRed
import com.ev.iot2.utils.Validators
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    databaseHelper: DatabaseHelper,
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    fun validateAndRegister() {
        scope.launch {
            isLoading = true
            message = ""
            
            when {
                name.isBlank() -> {
                    message = "El nombre no puede estar vacío"
                    isError = true
                }
                email.isBlank() -> {
                    message = "El email no puede estar vacío"
                    isError = true
                }
                !Validators.isValidEmail(email) -> {
                    message = "Formato de email inválido"
                    isError = true
                }
                password.isBlank() -> {
                    message = "La contraseña no puede estar vacía"
                    isError = true
                }
                else -> {
                    val passwordValidation = Validators.isStrongPassword(password)
                    if (!passwordValidation.isValid) {
                        message = passwordValidation.errors.first()
                        isError = true
                    } else if (!Validators.passwordsMatch(password, confirmPassword)) {
                        message = "Las contraseñas no coinciden"
                        isError = true
                    } else if (databaseHelper.isEmailExists(email)) {
                        message = "El email ya está registrado"
                        isError = true
                    } else {
                        val result = databaseHelper.insertUser(name, email, password)
                        if (result > 0) {
                            message = "¡Registro exitoso!"
                            isError = false
                            onRegisterSuccess()
                        } else {
                            message = "Error al registrar usuario"
                            isError = true
                        }
                    }
                }
            }
            isLoading = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRed,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Crear Cuenta",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Name field
            IoTempTextField(
                value = name,
                onValueChange = { name = it },
                label = "Nombre",
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email field
            IoTempTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            IoTempPasswordField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm password field
            IoTempPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar Contraseña",
                imeAction = ImeAction.Done,
                onImeAction = { validateAndRegister() }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Message
            MessageText(
                message = message,
                isError = isError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Register button
            IoTempButton(
                text = if (isLoading) "Registrando..." else "Registrarse",
                onClick = { validateAndRegister() },
                enabled = !isLoading
            )
        }
    }
}
