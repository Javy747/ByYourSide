package com.example.byyourside

import java.io.Serializable

data class Producto (
    var documentId: String? = null,
    var idProducto:String? = null,
    var lote: String? = null,
    var pais: String? = null,
    var nombre: String? = null,
    var marca: String? = null,
    var precio: Double? = null
): Serializable {

    constructor(pais: String, marca: String, nombre: String) :
            this( null, null,pais,
                nombre, marca, null)


    constructor() : this(null, null, null,
        null, null, null, null)

}