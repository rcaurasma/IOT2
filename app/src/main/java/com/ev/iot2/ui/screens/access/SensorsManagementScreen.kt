package com.ev.iot2.ui.screens.access

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ev.iot2.network.ApiClient
import com.ev.iot2.network.SensorRequest
import com.ev.iot2.network.ChangeSensorStateRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsManagementScreen(
    onBack: () -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Llavero") }
    var estado by remember { mutableStateOf("ACTIVO") }
    var idDepartamentoText by remember { mutableStateOf("") }
    var userIdText by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Gestión de sensores") }, colors = TopAppBarDefaults.topAppBarColors())
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(colors = CardDefaults.cardColors()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Registrar nuevo sensor")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código sensor (UID/MAC)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (Llavero/Tarjeta)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("Estado inicial (ACTIVO/INACTIVO/PERDIDO/BLOQUEADO)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = idDepartamentoText, onValueChange = { idDepartamentoText = it }, label = { Text("ID Departamento") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = userIdText, onValueChange = { userIdText = it }, label = { Text("ID Usuario administrador (opcional)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                val idDept = idDepartamentoText.toIntOrNull() ?: run {
                                    message = "ID departamento inválido"
                                    return@launch
                                }
                                val idUser = userIdText.toIntOrNull()
                                val req = SensorRequest(codigo_sensor = codigo, estado = estado, id_departamento = idDept, tipo = tipo, id_usuario = idUser)
                                val resp = ApiClient.iotService.registerSensor(req)
                                if (resp.isSuccessful) {
                                    message = resp.body()?.message ?: "Sensor registrado"
                                } else {
                                    message = resp.errorBody()?.string() ?: "Error al registrar sensor"
                                }
                            } catch (e: Exception) {
                                message = "Error de red: ${e.message}"
                            }
                        }
                    }) {
                        Text("Registrar sensor")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(colors = CardDefaults.cardColors()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Cambiar estado de sensor (admin)")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("ID sensor (numérico)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = estado, onValueChange = { estado = it }, label = { Text("Nuevo estado (ACTIVO/INACTIVO/PERDIDO/BLOQUEADO)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        scope.launch {
                            try {
                                val sensorId = codigo.toIntOrNull() ?: run {
                                    message = "ID sensor inválido (debe ser numérico)"
                                    return@launch
                                }
                                val resp = ApiClient.iotService.changeSensorState(sensorId, ChangeSensorStateRequest(estado = estado))
                                if (resp.isSuccessful) {
                                    message = resp.body()?.message ?: "Estado cambiado"
                                } else {
                                    message = resp.errorBody()?.string() ?: "Error al cambiar estado"
                                }
                            } catch (e: Exception) {
                                message = "Error de red: ${e.message}"
                            }
                        }
                    }) {
                        Text("Cambiar estado")
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
