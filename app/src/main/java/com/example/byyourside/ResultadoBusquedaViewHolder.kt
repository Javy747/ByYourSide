package com.example.byyourside

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ResultadoBusquedaViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val nombreComercio: TextView = itemView.findViewById(R.id.nombre_comercio_rb)
    val nombreComercioValor: TextView = itemView.findViewById(R.id.nombre_comercio_rbv)

    val nombreProducto: TextView = itemView.findViewById(R.id.nombre_producto_rb)
    val nombreProductoValor: TextView = itemView.findViewById(R.id.nombre_producto_rbv)

    val marcaProducto: TextView = itemView.findViewById(R.id.marca_producto_rb)
    val marcaProductoValor: TextView = itemView.findViewById(R.id.marca_producto_rbv)

    val paisOrigenProducto: TextView = itemView.findViewById(R.id.pais_origen_producto_rb)
    val paisOrigenProductoValor: TextView = itemView.findViewById(R.id.pais_origen_producto_rbv)

    val precioProducto: TextView = itemView.findViewById(R.id.precio_producto_rb)
    val precioProductoValor: TextView = itemView.findViewById(R.id.precio_producto_rbv)

    val direccionComercio: TextView = itemView.findViewById(R.id.direccion_comercio_rb)
    val direccionComercioValor: TextView = itemView.findViewById(R.id.direccion_comercio_rbv)

    val urlComercio: TextView = itemView.findViewById(R.id.url_comercio_rb)
    val urlComercioValor: TextView = itemView.findViewById(R.id.url_comercio_rbv)

    val btnComoLlegar: Button = itemView.findViewById(R.id.btn_como_llegar)

    val btnIrWeb: Button = itemView.findViewById(R.id.btn_ir_web)
}