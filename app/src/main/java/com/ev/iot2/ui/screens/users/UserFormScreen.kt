package com.ev.iot2.ui.screens.users

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
import androidx.compose.runtime.LaunchedEffect
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
// DatabaseHelper removed — user management via backend
import com.ev.iot2.data.model.User
import com.ev.iot2.ui.components.IoTempButton
import com.ev.iot2.ui.components.IoTempPasswordField
import com.ev.iot2.ui.components.IoTempTextField
import com.ev.iot2.ui.components.MessageText
import com.ev.iot2.ui.theme.PrimaryBlue
import com.ev.iot2.utils.Validators
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    // databaseHelper removed: backend API required
    // keep signature simple for now
    onNavigateBack: () -> Unit,
    userId: Long? = null,
    onSaveSuccess: () -> Unit
) {
    val isEditMode = userId != null
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var existingUser by remember { mutableStateOf<User?>(null) }
    
    val scope = rememberCoroutineScope()
    
    // Load user data if editing (TODO: implement via API)
    LaunchedEffect(userId) {
        if (userId != null) {
            // TODO: fetch user by id from backend and populate fields
            existingUser = null
        }
    }
    
    fun validateAndSave() {
        scope.launch {
            isLoading = true
            message = ""
            
            when {
                name.isBlank() -> {
                    message = "El nombre no puede estar vacío"
                    isError = true
                }
                !Validators.isValidName(name) -> {
                    message = "El nombre solo puede contener letras y espacios"
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
                // TODO: check email existence via backend
                false -> {
                    // placeholder, continue
                }
                else -> {
                    if (isEditMode) {
                        // Update existing user (TODO: call backend)
                        message = "Funcionalidad de edición disponible vía API (pendiente)"
                        isError = false
                        onSaveSuccess()
                    } else {
                        // Create new user - validate password
                        when {
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
                                } else {
                                    // Create new user via backend (requires auth) - TODO
                                    message = "Creación de usuario vía API (pendiente, requiere autenticación)"
                                    isError = false
                                    onSaveSuccess()
                                }
                            }
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
                title = { Text(if (isEditMode) "Editar Usuario" else "Nuevo Usuario") },
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
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isEditMode) "Modificar datos del usuario" else "Datos del nuevo usuario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
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
                imeAction = if (isEditMode) ImeAction.Done else ImeAction.Next,
                onImeAction = { if (isEditMode) validateAndSave() }
            )
            
            // Password fields only for new user
            if (!isEditMode) {
                Spacer(modifier = Modifier.height(16.dp))
                
                IoTempPasswordField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    imeAction = ImeAction.Next
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                IoTempPasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar Contraseña",
                    imeAction = ImeAction.Done,
                    onImeAction = { validateAndSave() }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MessageText(
                message = message,
                isError = isError
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            IoTempButton(
                text = if (isLoading) "Guardando..." else "Guardar",
                onClick = { validateAndSave() },
                enabled = !isLoading
            )
        }
    }
}
