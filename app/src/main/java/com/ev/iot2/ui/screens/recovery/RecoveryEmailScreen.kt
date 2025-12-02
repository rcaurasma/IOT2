package com.ev.iot2.ui.screens.recovery

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ev.iot2.data.database.DatabaseHelper
import com.ev.iot2.ui.components.IoTempButton
import com.ev.iot2.ui.components.IoTempTextField
import com.ev.iot2.ui.components.MessageText
import com.ev.iot2.ui.theme.PrimaryBlue
import com.ev.iot2.utils.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryEmailScreen(
    databaseHelper: DatabaseHelper,
    onNavigateBack: () -> Unit,
    onCodeSent: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }
    var generatedCode by remember { mutableStateOf("") }
    
    fun validateAndSendCode() {
        when {
            email.isBlank() -> {
                message = "El email no puede estar vacío"
                isError = true
            }
            !Validators.isValidEmail(email) -> {
                message = "Formato de email inválido"
                isError = true
            }
            else -> {
                val user = databaseHelper.getUserByEmail(email)
                if (user == null) {
                    message = "El email no está registrado"
                    isError = true
                } else {
                    // Generate and store recovery code
                    val code = Validators.generateRecoveryCode()
                    generatedCode = code
                    databaseHelper.insertRecoveryCode(email, code)
                    
                    // In a real app, this would send an email
                    // For demo purposes, we show the code
                    message = "Código de recuperación enviado: $code"
                    isError = false
                    onCodeSent(email)
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
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
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Ingresa tu email",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Te enviaremos un código de 5 dígitos para recuperar tu contraseña. El código tiene una validez de 1 minuto.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            IoTempTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { validateAndSendCode() }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MessageText(
                message = message,
                isError = isError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            IoTempButton(
                text = "Recuperar",
                onClick = { validateAndSendCode() }
            )
        }
    }
}
