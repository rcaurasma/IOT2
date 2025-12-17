package com.ev.iot2.data.model

data class User(
    val id: Int,
    val name: String,
    val last_name: String? = null,
    val email: String
)
