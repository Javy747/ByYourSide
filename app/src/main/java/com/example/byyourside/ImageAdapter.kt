package com.example.byyourside // Asegúrate de que este sea tu paquete correcto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class ImageAdapter(
    private val imageList: MutableList<String>, // Acepta rutas locales y links de Firebase
    private val isEditable: Boolean,            // true = modo comercio (muestra la X)
    private val onDeleteClick: (Int) -> Unit    // Función que se ejecuta al presionar la X
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    // 1. Enlaza los elementos de tu diseño individual (item_image.xml)
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPreview: ImageView = view.findViewById(R.id.iv_imagen_producto)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    // 2. Infla el diseño para cada cuadrito de foto
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    // 3. Pinta los datos (la foto) y configura el botón en cada posición
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageSource = imageList[position]

        // Glide carga la imagen automáticamente de forma eficiente
        Glide.with(holder.itemView.context)
            .load(imageSource)
            .centerCrop()
            .into(holder.ivPreview)

        // Si es el comercio (isEditable = true), mostramos la X y le damos la función de borrar
        if (isEditable) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                onDeleteClick(position)
            }
        } else {
            // Si es el cliente, ocultamos la X por completo
            holder.btnDelete.visibility = View.GONE
        }
    }

    // 4. Le dice al RecyclerView cuántas fotos hay en total
    override fun getItemCount(): Int {
        return imageList.size
    }
}