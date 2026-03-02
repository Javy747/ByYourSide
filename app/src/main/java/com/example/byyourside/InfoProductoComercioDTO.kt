package com.example.byyourside


import kotlinx.parcelize.Parcelize

 @Parcelize
data class InfoProductoComercioDTO(
     val nombreComercio: String? = null,
     val nombreProducto: String? = null,
     val marcaProducto: String? = null,
     val paisOrigenProducto: String? = null,
     val precioProducto: Double? = null,
     val nombreCalle: String? = null,
     val numeroCalleComercio: Long? = null,
     val provinciaComercio: String? = null,
     val municipioComercio: String? = null,
     val codigoPostalComercio: Long? = null,
     val webComercio: String? = null
) : android.os.Parcelable



