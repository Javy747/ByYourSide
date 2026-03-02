package com.example.byyourside

import java.io.Serializable

enum class TipoComercio {
    FISICO,
    ECOMMERCE
}

data class Comercio (
    var nif : String? = null,
    var nombre : String? = null,
    var pais : String? = null,
    var provincia : String? = null,
    var municipio : String? = null,
    var calle : String? = null,
    var numero_local : Long? = null,
    var codigo_postal : Long? = null,
    var web : String? = null,
    var telefono : String? = null,
    var e_mail : String? = null,
    var tipo_comercio : TipoComercio? = null,
    val role : String = "comercio"
): Serializable {

    constructor() : this(null, null, null, null, null,
        null, null, null, null, null,
        null, null)
}

