package com.example.byyourside


import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("ClickableViewAccessibility")
class ProductoHolderAdapter(
    private val productos: MutableList<Producto>,
    private val idComercio: String
) : RecyclerView.Adapter<ProductoHolderAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Referencias a los TextViews que muestran los datos del producto.
        val id = view.findViewById<TextView>(R.id.tv_id_producto_iv)
        val lote = view.findViewById<TextView>(R.id.tv_lote_producto_iv)
        val paisOrigen = view.findViewById<TextView>(R.id.tv_pais_origen_producto_iv)
        val nombre = view.findViewById<TextView>(R.id.tv_nombre_producto_iv)
        val marca = view.findViewById<TextView>(R.id.tv_marca_iv)
        val precio = view.findViewById<TextView>(R.id.tv_precio_producto_iv)
        val fechaCaducidad = view.findViewById<TextView>(R.id.tv_fecha_caducidad_iv)

        val btnModificarProducto = view.findViewById<Button>(R.id.btn_modificar_producto)
        val btnEliminarProducto = view.findViewById<Button>(R.id.btn_eliminar_producto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventario_comercio, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, posicion: Int) {
        val producto = productos[posicion]

        holder.id.text = producto.idProducto ?: "-"
        holder.lote.text = producto.lote ?: "-"
        holder.paisOrigen.text = producto.pais ?: "-"
        holder.nombre.text = producto.nombre ?: "-"
        holder.marca.text = producto.marca ?: "-"
        holder.precio.text = producto.precio?.let { "%.2f€".format(it) } ?: "-"

        if(producto.fechaCaducidad != null){
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.fechaCaducidad.text = sdf.format(producto.fechaCaducidad!!)
        }else{
            holder.fechaCaducidad.text = "Sin caducidad"
        }

        holder.btnModificarProducto.setOnClickListener {
            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialogo_modificar_producto, null)

            val etId = dialogView.findViewById<EditText>(R.id.et_id_producto_manual)
            val etLote = dialogView.findViewById<EditText>(R.id.et_lote_producto)
            val etPaisOrigen = dialogView.findViewById<EditText>(R.id.et_pais_origen_producto)
            val etNombre = dialogView.findViewById<EditText>(R.id.et_nombre_producto)
            val etMarca = dialogView.findViewById<EditText>(R.id.et_marca_producto)
            val etPrecio = dialogView.findViewById<EditText>(R.id.et_precio_producto)
            val etFechaCaducidad = dialogView.findViewById<EditText>(R.id.etd_fecha_caducidad)

            etFechaCaducidad.setOnTouchListener { view, event ->
                if (event.action == android.view.MotionEvent.ACTION_UP) {
                    val drawableRight = 2
                    val drawable = etFechaCaducidad.compoundDrawables[drawableRight]

                    // 1. Si tocó la "X" para borrar
                    if (drawable != null && event.rawX >= (etFechaCaducidad.right - drawable.bounds.width() - etFechaCaducidad.paddingRight)) {
                        etFechaCaducidad.text.clear()
                        return@setOnTouchListener true // Devolvemos true para detener el evento aquí
                    }
                    // 2. Si tocó en cualquier otra parte del campo (para abrir el calendario)
                    else {
                        view.performClick() // Esto llama al setOnClickListener manualmente
                        return@setOnTouchListener true // 👇 DEVOLVEMOS TRUE PARA QUE NO SE DUPLIQUE 👇
                    }
                }
                false
            }

            etFechaCaducidad.setOnClickListener {
                mostrarCalendarioEdicion(context, etFechaCaducidad)
            }
            // 👆 FIN DEL BLOQUE NUEVO 👆

            etId.setText(producto.idProducto)
            etLote.setText(producto.lote)
            etPaisOrigen.setText(producto.pais)
            etNombre.setText(producto.nombre)
            etMarca.setText(producto.marca)
            etPrecio.setText(producto.precio?.toString() ?: "")



            AlertDialog.Builder(context)
                .setTitle("Modificar Producto")
                .setView(dialogView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar") { _, _ ->
                    val nuevoId = etId.text.toString().trim()
                    val nuevoLote = etLote.text.toString().trim()
                    val nuevoPaisOrigen = etPaisOrigen.text.toString().trim()
                    val nuevoNombre = etNombre.text.toString().trim()
                    val nuevoMarca = etMarca.text.toString().trim()
                    val nuevoPrecio = etPrecio.text.toString().toDoubleOrNull()
                    val nuevaFechaCaducidadStr = etFechaCaducidad.text.toString().trim()

                    if (nuevoId.isBlank() || nuevoLote.isBlank() || nuevoPaisOrigen.isBlank() || nuevoNombre.isBlank() || nuevoMarca.isBlank() || nuevoPrecio == null) {
                        Toast.makeText(context, "Ningún campo puede quedar vacío", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val nuevaFechaDate: Date? = if (nuevaFechaCaducidadStr.isBlank()) {
                        null
                    } else {
                        try {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(nuevaFechaCaducidadStr)
                        } catch (e: Exception) {
                            Log.e("AdapterFecha", "Error al parsear", e)
                            Toast.makeText(context, "Formato de fecha incorrecto.", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }

                    verificarYActualizar(producto, nuevoId, nuevoLote, nuevoPaisOrigen, nuevoNombre, nuevoMarca, nuevoPrecio, nuevaFechaDate, holder)
                }
                .show()
        }

        holder.btnEliminarProducto.setOnClickListener {
            val context = holder.itemView.context
            AlertDialog.Builder(context)
                .setTitle("Eliminar producto")
                .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                .setPositiveButton("Sí") { _, _ ->

                    val idDocumentoInventario = producto.documentId
                    if (idDocumentoInventario == null) {
                        Toast.makeText(context, "Error: ID de documento no encontrado.", Toast.LENGTH_LONG).show()
                        return@setPositiveButton
                    }

                    val db = FirebaseFirestore.getInstance()

                    val inventarioRef = db.collection("inventario").document(idDocumentoInventario)

                    inventarioRef.get()
                        .addOnSuccessListener { inventarioDoc ->
                            val idMaestroProducto = inventarioDoc.getString("idMaestroProducto")

                            val batch = db.batch()

                            batch.delete(inventarioRef)

                            if (idMaestroProducto != null) {
                                val productoRef = db.collection("productos").document(idMaestroProducto)
                                batch.delete(productoRef)
                            } else {
                                Log.w("EliminarProducto", "No se encontró idMaestroProducto, se eliminará solo de inventario.")
                            }

                            batch.commit()
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                                    val position = holder.bindingAdapterPosition
                                    if (position != RecyclerView.NO_POSITION) {
                                        productos.removeAt(position)
                                        notifyItemRemoved(position)
                                        notifyItemRangeChanged(position, productos.size)
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al leer el inventario: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun mostrarCalendarioEdicion(context : android.content.Context, editText : EditText){

        val activity = context as? androidx.appcompat.app.AppCompatActivity ?: return

        val  constraintBuilder = com.google.android.material.datepicker.CalendarConstraints.Builder()
            .setStart(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
            .setValidator(com.google.android.material.datepicker.DateValidatorPointForward.now())

        val picker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setCalendarConstraints(constraintBuilder.build())
            .build()

        picker.addOnPositiveButtonClickListener { timeInMillis ->
            val fechaSeleccionada = Date(timeInMillis)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            editText.setText(sdf.format(fechaSeleccionada))
        }

        picker.show(activity.supportFragmentManager, "DATE_PICKER_EDIT")
    }

    private fun verificarYActualizar(
        producto: Producto,
        nuevoId: String,
        nuevoLote: String,
        nuevoPais: String,
        nuevoNombre: String,
        nuevoMarca: String,
        nuevoPrecio: Double,
        nuevaFechaCaducidad: Date?,

        holder: ProductoViewHolder
    ) {
        val db = FirebaseFirestore.getInstance()
        val context = holder.itemView.context

        val idDocumentoInventario = producto.documentId
        val idComercioActual = this.idComercio // ID del constructor

        if (idDocumentoInventario == null || idComercioActual.isBlank()) {
            Toast.makeText(context, "Error: Faltan datos clave del producto o comercio.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("inventario")
            .whereEqualTo("idComercio", idComercioActual)
            .whereEqualTo("idProducto", nuevoId)
            .get()
            .addOnSuccessListener { idDocuments ->
                val idEsDuplicado = idDocuments.documents.any { it.id != idDocumentoInventario }

                if (idEsDuplicado) {
                    Toast.makeText(context, "Error: Ya existe otro producto con este ID.", Toast.LENGTH_LONG).show()
                } else {

                    db.collection("inventario")
                        .whereEqualTo("idComercio", idComercioActual)
                        .whereEqualTo("lote", nuevoLote)
                        .get()
                        .addOnSuccessListener { loteDocuments ->
                            val loteEsDuplicado = loteDocuments.documents.any { it.id != idDocumentoInventario }

                            if (loteEsDuplicado) {
                                Toast.makeText(context, "Error: Ya existe otro producto con este número de Lote.", Toast.LENGTH_LONG).show()
                            } else {

                                db.collection("inventario")
                                    .whereEqualTo("idComercio", idComercioActual)
                                    .whereEqualTo("nombreProducto", nuevoNombre.lowercase())
                                    .whereEqualTo("marcaProducto", nuevoMarca.lowercase())
                                    .get()
                                    .addOnSuccessListener { nombreMarcaDocuments ->
                                        val nombreMarcaEsDuplicado = nombreMarcaDocuments.documents.any { it.id != idDocumentoInventario }

                                        if (nombreMarcaEsDuplicado) {
                                            Toast.makeText(context, "Error: Ya existe otro producto con este nombre y marca.", Toast.LENGTH_LONG).show()
                                        } else {

                                            val updatesInventario = mapOf(
                                                "idProducto" to nuevoId,
                                                "lote" to nuevoLote,
                                                "paisProducto" to nuevoPais,
                                                "nombreProducto" to nuevoNombre.lowercase(),
                                                "marcaProducto" to nuevoMarca.lowercase(),
                                                "precioProducto" to nuevoPrecio,
                                                "fechaCaducidad" to nuevaFechaCaducidad
                                            )

                                            val updatesProductos = mapOf(
                                                "idProducto" to nuevoId,
                                                "lote" to nuevoLote,
                                                "pais" to nuevoPais,
                                                "nombre" to nuevoNombre.lowercase(),
                                                "marca" to nuevoMarca.lowercase(),
                                                "precio" to nuevoPrecio,
                                                "fechaCaducidad" to nuevaFechaCaducidad
                                            )

                                            val batch = db.batch()

                                            val inventarioRef = db.collection("inventario").document(idDocumentoInventario)
                                            batch.update(inventarioRef, updatesInventario)

                                            inventarioRef.get().addOnSuccessListener { inventarioDoc ->
                                                val idMaestroProducto = inventarioDoc.getString("idMaestroProducto")
                                                if (idMaestroProducto != null) {
                                                    val productoRef = db.collection("productos").document(idMaestroProducto)
                                                    batch.update(productoRef, updatesProductos)
                                                }

                                                batch.commit()
                                                    .addOnSuccessListener {
                                                        producto.idProducto = nuevoId
                                                        producto.lote = nuevoLote
                                                        producto.pais = nuevoPais
                                                        producto.nombre = nuevoNombre
                                                        producto.marca = nuevoMarca
                                                        producto.precio = nuevoPrecio
                                                        producto.fechaCaducidad = nuevaFechaCaducidad

                                                        notifyItemChanged(holder.bindingAdapterPosition)
                                                        Toast.makeText(context, "Producto actualizado", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Toast.makeText(context, "Error al actualizar (commit): ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }

                                            }.addOnFailureListener { e ->
                                                Toast.makeText(context, "Error al leer inventario para actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error al verificar Nombre/Marca: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("VerificarNombreMarca", "Error en consulta", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al verificar Lote: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al verificar ID: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int = productos.size
}