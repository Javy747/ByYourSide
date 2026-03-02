package com.example.byyourside

import java.io.Serializable

data class Cliente (
    var nombre : String,
    var apellidos : String,
    var telefono : String,
    var e_mail : String,
    val role : String = "cliente"
): Serializable { }