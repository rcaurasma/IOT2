package com.ev.iot2.ui.screens.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ev.iot2.network.ApiClient
import com.ev.iot2.network.AccessEventRequest
import com.ev.iot2.network.Evento
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessManagementScreen(
    onBack: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var userIdText by remember { mutableStateOf("") }
    var tipoEvento by remember { mutableStateOf("ACCESO_VALIDO") }
    var deptIdText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var events by remember { mutableStateOf<List<Evento>>(emptyList()) }
    var barrierState by remember { mutableStateOf("Cerrada") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de acceso") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Manual barrier control card (styled like other management screens)
            Card(colors = CardDefaults.cardColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Control manual de barrera", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            scope.launch {
                                try {
                                    // send apertura manual
                                    val req = AccessEventRequest(codigo_sensor = codigo, id_usuario = userIdText.toIntOrNull(), tipo_evento = "APERTURA_MANUAL")
                                    val resp = ApiClient.iotService.sendAccessEvent(req)
                                    if (resp.isSuccessful) {
                                        message = resp.body()?.message ?: "Barrera abierta"
                                        barrierState = "Abierta"
                                        // auto close after 10s
                                        delay(10000)
                                        val closeReq = AccessEventRequest(codigo_sensor = codigo, id_usuario = userIdText.toIntOrNull(), tipo_evento = "CIERRE_MANUAL")
                                        ApiClient.iotService.sendAccessEvent(closeReq)
                                        barrierState = "Cerrada"
                                    } else {
                                        message = resp.errorBody()?.string() ?: "Error al abrir barrera"
                                    }
                                } catch (e: Exception) {
                                    message = "Error de red: ${e.message}"
                                }
                            }
                        }) {
                            Text("Abrir barrera")
                        }

                        Button(onClick = {
                            scope.launch {
                                try {
                                    val req = AccessEventRequest(codigo_sensor = codigo, id_usuario = userIdText.toIntOrNull(), tipo_evento = "CIERRE_MANUAL")
                                    val resp = ApiClient.iotService.sendAccessEvent(req)
                                    if (resp.isSuccessful) {
                                        message = resp.body()?.message ?: "Barrera cerrada"
                                        barrierState = "Cerrada"
                                    } else {
                                        message = resp.errorBody()?.string() ?: "Error al cerrar barrera"
                                    }
                                } catch (e: Exception) {
                                    message = "Error de red: ${e.message}"
                                }
                            }
                        }) {
                            Text("Cerrar barrera")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado barrera: $barrierState")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Enviar evento de acceso", fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código sensor (UID)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = userIdText, onValueChange = { userIdText = it }, label = { Text("ID Usuario (opcional)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tipoEvento, onValueChange = { tipoEvento = it }, label = { Text("Tipo Evento (ACCESO_VALIDO / ACCESO_RECHAZADO / APERTURA_MANUAL / CIERRE_MANUAL)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                val userId = userIdText.toIntOrNull()
                                val req = AccessEventRequest(codigo_sensor = codigo, id_usuario = userId, tipo_evento = tipoEvento)
                                val resp = ApiClient.iotService.sendAccessEvent(req)
                                if (resp.isSuccessful) {
                                    message = resp.body()?.message ?: "Evento enviado"
                                } else {
                                    message = resp.errorBody()?.string() ?: "Error al enviar evento"
                                }
                            } catch (e: Exception) {
                                message = "Error de red: ${e.message}"
                            }
                        }
                    }) {
                        Text("Enviar evento")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // History card
            Card(colors = CardDefaults.cardColors(), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ver historial por departamento", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = deptIdText, onValueChange = { deptIdText = it }, label = { Text("ID Departamento") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                val id = deptIdText.toIntOrNull()
                                if (id == null) {
                                    message = "ID departamento inválido"
                                    return@launch
                                }
                                val resp = ApiClient.iotService.getDepartmentEvents(id)
                                if (resp.isSuccessful) {
                                    events = resp.body() ?: emptyList()
                                    message = "Eventos cargados: ${events.size}"
                                } else {
                                    message = resp.errorBody()?.string() ?: "Error al obtener eventos"
                                }
                            } catch (e: Exception) {
                                message = "Error de red: ${e.message}"
                            }
                        }
                    }) {
                        Text("Cargar historial")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    events.forEach { ev ->
                        Text("[${ev.fecha_hora}] Sensor:${ev.id_sensor} Usuario:${ev.id_usuario} Tipo:${ev.tipo_evento} Resultado:${ev.resultado}")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(onClick = onBack) {
                        Text("Volver")
                    }
                }
            }
        }
    }
}
