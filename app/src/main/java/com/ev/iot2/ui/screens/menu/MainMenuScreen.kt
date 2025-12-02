package com.ev.iot2.ui.screens.menu

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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ev.iot2.ui.theme.PrimaryRed
import com.ev.iot2.utils.Constants
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToDeveloper: () -> Unit,
    onLogout: () -> Unit
) {
    var currentDateTime by remember { mutableStateOf("") }
    val dateFormat = remember { SimpleDateFormat(Constants.DATE_TIME_FORMAT, Locale.getDefault()) }
    
    // Update time every second
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = dateFormat.format(Date())
            delay(1000)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("iotemp") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryRed,
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Date and Time display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = PrimaryRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Fecha y Hora Actual",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentDateTime,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Menú Principal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Menu options
            MenuCard(
                title = "CRUD de Usuarios",
                description = "Gestionar usuarios del sistema",
                icon = Icons.Default.Person,
                onClick = onNavigateToUserManagement
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MenuCard(
                title = "Ver datos de sensores",
                description = "Temperatura, humedad y control de dispositivos",
                icon = Icons.Default.Sensors,
                onClick = onNavigateToSensors
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            MenuCard(
                title = "Datos del desarrollador",
                description = "Información del equipo de desarrollo",
                icon = Icons.Default.Code,
                onClick = onNavigateToDeveloper
            )
        }
    }
}

@Composable
fun MenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryRed,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
