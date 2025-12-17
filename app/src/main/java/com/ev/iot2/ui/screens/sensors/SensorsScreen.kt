package com.ev.iot2.ui.screens.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ev.iot2.R
import com.ev.iot2.ui.theme.ErrorRed
import com.ev.iot2.ui.theme.InfoBlue
import com.ev.iot2.ui.theme.PrimaryBlue
import com.ev.iot2.ui.theme.SuccessGreen
import com.ev.iot2.ui.theme.WarningOrange
import com.ev.iot2.utils.Constants
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    // Sensor data states
    var temperature by remember { mutableFloatStateOf(20f) }
    var humidity by remember { mutableFloatStateOf(50f) }
    
    // Device states
    var isBulbOn by remember { mutableStateOf(false) }
    var isFlashlightOn by remember { mutableStateOf(false) }
    var flashlightMessage by remember { mutableStateOf("") }
    
    // Camera manager for flashlight
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    var cameraId by remember { mutableStateOf<String?>(null) }
    
    // Find camera with flash
    LaunchedEffect(Unit) {
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (hasFlash) {
                    cameraId = id
                    break
                }
            }
        } catch (e: Exception) {
            flashlightMessage = "Flash no disponible"
        }
    }
    
    // Update sensor data every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            temperature = Random.nextFloat() * 30 + 5 // 5°C to 35°C
            humidity = Random.nextFloat() * 60 + 30 // 30% to 90%
            delay(Constants.SENSOR_UPDATE_INTERVAL_MS)
        }
    }
    
    // Cleanup flashlight when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            if (isFlashlightOn && cameraId != null) {
                try {
                    cameraManager.setTorchMode(cameraId!!, false)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }
    
    fun toggleFlashlight() {
        if (cameraId == null) {
            flashlightMessage = "Flash no disponible en este dispositivo"
            return
        }
        
        // Check permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            flashlightMessage = "Se requiere permiso de cámara"
            return
        }
        
        try {
            val newState = !isFlashlightOn
            cameraManager.setTorchMode(cameraId!!, newState)
            isFlashlightOn = newState
            flashlightMessage = if (newState) "Linterna activada" else "Linterna desactivada"
        } catch (e: Exception) {
            flashlightMessage = "Error al controlar la linterna"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos de Sensores") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Temperature Card
            SensorCard(
                title = "Temperatura",
                value = String.format("%.1f°C", temperature),
                icon = {
                    Icon(
                        Icons.Default.Thermostat,
                        contentDescription = "Temperatura",
                        tint = if (temperature > 20) ErrorRed else InfoBlue,
                        modifier = Modifier.size(48.dp)
                    )
                },
                status = if (temperature > 25) "Alta" else if (temperature < 15) "Baja" else "Normal",
                statusColor = if (temperature > 25) ErrorRed else if (temperature < 15) InfoBlue else SuccessGreen
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Humidity Card
            SensorCard(
                title = "Humedad",
                value = String.format("%.1f%%", humidity),
                icon = {
                    Icon(
                        Icons.Default.WaterDrop,
                        contentDescription = "Humedad",
                        tint = InfoBlue,
                        modifier = Modifier.size(48.dp)
                    )
                },
                status = if (humidity > 70) "Alta" else if (humidity < 40) "Baja" else "Normal",
                statusColor = if (humidity > 70) WarningOrange else if (humidity < 40) WarningOrange else SuccessGreen
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Control de Dispositivos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bulb Control (use built-in icon instead of missing drawable)
            DeviceControlCard(
                title = "Ampolleta",
                isOn = isBulbOn,
                onToggle = { isBulbOn = !isBulbOn },
                message = if (isBulbOn) "Ampolleta encendida" else "Ampolleta apagada",
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = "Ampolleta",
                        tint = if (isBulbOn) WarningOrange else Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Flashlight Control
            DeviceControlCard(
                title = "Linterna",
                isOn = isFlashlightOn,
                onToggle = { toggleFlashlight() },
                message = flashlightMessage.ifBlank { if (isFlashlightOn) "Linterna activada" else "Linterna desactivada" },
                icon = {
                    Icon(
                        imageVector = if (isFlashlightOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Linterna",
                        tint = if (isFlashlightOn) WarningOrange else Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun SensorCard(
    title: String,
    value: String,
    icon: @Composable () -> Unit,
    status: String,
    statusColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            
            Card(
                colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun DeviceControlCard(
    title: String,
    isOn: Boolean,
    onToggle: () -> Unit,
    message: String,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) PrimaryBlue.copy(alpha = 0.1f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            icon()
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                fontSize = 14.sp,
                color = if (isOn) SuccessGreen else Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Toca para ${if (isOn) "apagar" else "encender"}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
