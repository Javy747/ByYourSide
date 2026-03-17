package com.example.byyourside

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Producto (
    var documentId: String? = null,
    var idProducto:String? = null,
    var lote: String? = null,
    var pais: String? = null,
    var nombre: String? = null,
    var marca: String? = null,
    var precio: Double? = null,
    var fechaCaducidad: Date? = null
) : Parcelable {

    constructor(pais: String, marca: String, nombre: String) :
            this( null, null,pais,
                nombre, marca, null, null, null)


    constructor() : this(null, null, null,
        null, null, null, null, null)

}