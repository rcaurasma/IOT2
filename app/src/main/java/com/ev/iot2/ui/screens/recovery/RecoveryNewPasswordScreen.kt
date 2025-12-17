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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ev.iot2.network.ApiClient
import com.ev.iot2.network.ResetPasswordRequest
import com.ev.iot2.ui.components.IoTempButton
import com.ev.iot2.ui.components.IoTempPasswordField
import com.ev.iot2.ui.components.MessageText
import com.ev.iot2.ui.theme.PrimaryBlue
import com.ev.iot2.utils.Validators

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecoveryNewPasswordScreen(
    email: String,
    code: String,
    onPasswordChanged: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()

    fun validateAndChangePassword() {
        when {
            newPassword.isBlank() -> {
                message = "La contraseña no puede estar vacía"
                isError = true
            }
            else -> {
                val passwordValidation = Validators.isStrongPassword(newPassword)
                if (!passwordValidation.isValid) {
                    message = passwordValidation.errors.first()
                    isError = true
                } else if (!Validators.passwordsMatch(newPassword, confirmPassword)) {
                    message = "Las contraseñas no coinciden"
                    isError = true
                } else {
                    scope.launch {
                        try {
                            val resp = ApiClient.authService.resetPassword(ResetPasswordRequest(email, code, newPassword))
                            if (resp.isSuccessful) {
                                message = resp.body()?.get("message") ?: "Contraseña actualizada correctamente"
                                isError = false
                                onPasswordChanged()
                            } else {
                                message = resp.errorBody()?.string() ?: "Error al cambiar la contraseña"
                                isError = true
                            }
                        } catch (e: Exception) {
                            message = "Error de red: ${e.message}"
                            isError = true
                        }
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Contraseña") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
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
                text = "Crea tu nueva contraseña",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "La contraseña debe tener:\n• Al menos 8 caracteres\n• Una mayúscula\n• Una minúscula\n• Un número\n• Un carácter especial",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            IoTempPasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Nueva Contraseña",
                imeAction = ImeAction.Next
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            IoTempPasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirmar Contraseña",
                imeAction = ImeAction.Done,
                onImeAction = { validateAndChangePassword() }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MessageText(
                message = message,
                isError = isError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            IoTempButton(
                text = "Cambiar Contraseña",
                onClick = { validateAndChangePassword() }
            )
        }
    }
}
