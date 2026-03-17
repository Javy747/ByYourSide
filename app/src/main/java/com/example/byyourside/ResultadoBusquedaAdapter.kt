package com.example.byyourside

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class ResultadoBusquedaAdapter(
    private val comerciosList: List<InfoProductoComercioDTO>,
    private val onComoLlegarClicked: (String) -> Unit
) : RecyclerView.Adapter<ResultadoBusquedaViewHolder>() {

    private lateinit var context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultadoBusquedaViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_resultado_busqueda, parent, false)
        return ResultadoBusquedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultadoBusquedaViewHolder, position: Int) {
        val comercio = comerciosList[position]

        holder.nombreComercioValor.text = comercio.nombreComercio ?: "Nombre no disponible"
        holder.nombreProductoValor.text = comercio.nombreProducto ?: "No disponible"
        holder.marcaProductoValor.text = comercio.marcaProducto ?: "No disponible"
        holder.paisOrigenProductoValor.text = comercio.paisOrigenProducto ?: "No disponible"
        holder.precioProductoValor.text = comercio.precioProducto?.let { "%.2f€".format(it) } ?: "Precio no disponible"

        val direccionPartes = listOfNotNull(
            comercio.nombreCalle?.takeIf { it.isNotBlank() },
            comercio.numeroCalleComercio?.toString(),
            comercio.municipioComercio?.takeIf { it.isNotBlank() },
            comercio.provinciaComercio?.takeIf { it.isNotBlank() }
        )
        val direccionConcatenada = if (direccionPartes.isNotEmpty()) {
            direccionPartes.joinToString(", ")
        } else {
            "Dirección no disponible"
        }


        holder.direccionComercioValor.text = direccionConcatenada
        holder.urlComercioValor.text = comercio.webComercio?.takeIf { it.isNotBlank() } ?: "No disponible"

        holder.btnComoLlegar.setOnClickListener {
            if (direccionConcatenada == "Dirección no disponible") {
                Toast.makeText(context, "Este comercio no dispone de una dirección", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            onComoLlegarClicked(direccionConcatenada)
        }

        holder.btnIrWeb.setOnClickListener {
            val url = comercio.webComercio?.takeIf { it.isNotBlank() } ?: run {
                Toast.makeText(context, "Este comercio no dispone de una página web", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else {
                url
            }

            try {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl))
                context.startActivity(webIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo abrir el sitio web", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = comerciosList.size
}