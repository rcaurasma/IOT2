package com.ev.iot2.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ev.iot2.R
import com.ev.iot2.network.ApiClient
import com.ev.iot2.network.TokenManager
import com.ev.iot2.network.LoginRequest
import com.ev.iot2.ui.components.IoTempButton
import com.ev.iot2.ui.components.IoTempPasswordField
import com.ev.iot2.ui.components.IoTempTextField
import com.ev.iot2.ui.components.IoTempTextButton
import com.ev.iot2.ui.components.MessageText
import com.ev.iot2.ui.theme.PrimaryBlue
import com.ev.iot2.utils.Validators
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.ui.tooling.preview.Preview
import com.ev.iot2.ui.theme.IOT2Theme

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToRecovery: () -> Unit,
    onLoginSuccess: (String?) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    fun validateAndLogin() {
        scope.launch {
            isLoading = true
            message = ""

            when {
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
                        try {
                            val resp = ApiClient.authService.login(LoginRequest(email, password))
                            if (resp.isSuccessful) {
                                val body = resp.body()
                                if (body != null && body.success && body.token != null) {
                                    message = "¡Login correcto!"
                                    isError = false
                                    // persist token
                                    TokenManager.saveToken(body.token)
                                    // persist user object for role/id_departamento usage
                                    val u = body.user
                                    if (u != null) {
                                        try {
                                            val obj = JSONObject()
                                            obj.put("id", u.id)
                                            obj.put("name", u.name)
                                            obj.put("last_name", u.last_name)
                                            obj.put("email", u.email)
                                            obj.put("role", u.role ?: "OPERATOR")
                                            obj.put("id_departamento", u.id_departamento)
                                            obj.put("estado", u.estado ?: "ACTIVO")
                                            TokenManager.saveUserJson(obj.toString())
                                        } catch (e: Exception) {
                                            // ignore save error
                                        }
                                    }
                                    onLoginSuccess(body.token)
                                } else {
                                    message = "Credenciales incorrectas"
                                    isError = true
                                }
                            } else {
                                val err = resp.errorBody()?.string()
                                message = err ?: "Error en login"
                                isError = true
                            }
                        } catch (e: Exception) {
                            message = "Error de red: ${e.message}"
                            isError = true
                        }
                }
            }
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.rf_solutions),
            contentDescription = "RF Solutions",
            modifier = Modifier.size(120.dp)
        )
        
        // App name
        Text(
            text = "Iniciar Sesión",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlue
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
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
            imeAction = ImeAction.Done,
            onImeAction = { validateAndLogin() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Message
        MessageText(
            message = message,
            isError = isError
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Login button
        IoTempButton(
            text = if (isLoading) "Ingresando..." else "Ingresar",
            onClick = { validateAndLogin() },
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Links
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IoTempTextButton(
                text = "Registrarse",
                onClick = onNavigateToRegister
            )
            
            IoTempTextButton(
                text = "¿Olvidaste tu contraseña?",
                onClick = onNavigateToRecovery
            )
        }
    }
}

@Preview(showBackground = true, name = "Login Screen Preview")
@Composable
fun LoginScreenPreview() {
    IOT2Theme {
        LoginScreen(
            onNavigateToRegister = {},
            onNavigateToRecovery = {},
            onLoginSuccess = {}
        )
    }
}