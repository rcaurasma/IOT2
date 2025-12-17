package com.ev.iot2.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

// Models
data class AccessEventRequest(
    val codigo_sensor: String,
    val id_usuario: Int? = null,
    val tipo_evento: String,
    val fecha_hora: String? = null
)

data class AccessEventResponse(val message: String, val evento: Map<String, Any>?)

data class SensorRequest(
    val codigo_sensor: String,
    val estado: String,
    val id_departamento: Int?,
    val tipo: String,
    val id_usuario: Int? = null
)

data class SensorResponse(val message: String, val sensor: Map<String, Any>?)

data class ChangeSensorStateRequest(val estado: String)

data class Evento(val id_evento: Int, val id_sensor: Int?, val id_usuario: Int?, val tipo_evento: String, val fecha_hora: String, val resultado: String)

interface IoTService {
    @POST("iot/access")
    suspend fun sendAccessEvent(@Body body: AccessEventRequest): Response<AccessEventResponse>

    @GET("iot/departamento/{id}/eventos")
    suspend fun getDepartmentEvents(@Path("id") id: Int): Response<List<Evento>>

    @POST("iot/sensors")
    suspend fun registerSensor(@Body body: SensorRequest): Response<SensorResponse>

    @PATCH("iot/sensors/{id}/estado")
    suspend fun changeSensorState(@Path("id") id: Int, @Body body: ChangeSensorStateRequest): Response<SensorResponse>
}
