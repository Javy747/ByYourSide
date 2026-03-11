package com.example.byyourside

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AgregarProducto : AppCompatActivity(), VerificacionCampos {

    private lateinit var producto: Producto
    private var auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
//  private val storage = FirebaseFirestore.getInstance()

    // Vistas
    private lateinit var etIdProducto: EditText
    private lateinit var etLoteProducto: EditText
    private lateinit var spinnerPaisProducto: Spinner
    private lateinit var etNombreProducto: EditText
    private lateinit var etMarcaProducto: EditText
    private lateinit var etPrecioProducto: EditText

    // Nuevas vistas para Fecha e Imagen
    private lateinit var etFechaCaducidad: EditText
    private lateinit var ivImagenProducto: ImageView
    private lateinit var btnSeleccionarImagen: Button

    // Variables para guardar la selección del usuario
    private var fechaSeleccionada: Date? = null
    private var imagenSeleccionadaUri: Uri? = null

//     Lanzador para abrir la galería de fotos
//    private val seleccionarImagenLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
//        if(uri != null){
//            imagenSeleccionadaUri = uri
//            ivImagenProducto.setImageURI(uri) // Mostramos la foto en pantalla
//        }
//    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginComercio::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_productos)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        etIdProducto = findViewById(R.id.et_id_producto)
        etLoteProducto = findViewById(R.id.et_lote_producto)
        spinnerPaisProducto = findViewById(R.id.spr_pais_agregar_producto)
        etNombreProducto = findViewById(R.id.et_nombre_producto_registro)
        etMarcaProducto = findViewById(R.id.et_marca_producto_registro)
        etPrecioProducto = findViewById(R.id.et_precio_producto)
        etFechaCaducidad = findViewById(R.id.etd_fecha_caducidad)
        ivImagenProducto = findViewById(R.id.iv_imagen_producto)
        btnSeleccionarImagen = findViewById(R.id.btn_seleccionar_imagen)

        val toolbarAgregarProducto = findViewById<Toolbar>(R.id.toolbar_agregar_producto)
        setSupportActionBar(toolbarAgregarProducto)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val btnVolverOpcionesComercio = findViewById<ImageView>(R.id.btn_volver_opciones_comercio)
        val btnAgregarProducto = findViewById<Button>(R.id.btn_agregar_producto)

        val paisProductoAdapter = ArrayAdapter.createFromResource(this, R.array.paises, android.R.layout.simple_spinner_item)
        paisProductoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaisProducto.adapter = paisProductoAdapter
        spinnerPaisProducto.setSelection(1)

        spinnerPaisProducto.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val textView = view as? TextView
                if (position == 0 || position == 1) {
                    textView?.setTextColor(getColor(R.color.gray))
                } else {
                    textView?.setTextColor(getColor(R.color.black))
                }
                (view as? TextView)?.textSize = 25f
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

//        // ACCIÓN: Abrir Calendario al tocar la fecha
//        etFechaCaducidad.setOnClickListener {
//            mostrarDatePicker()
//        }
//
//        // ACCIÓN: Abrir Galería al tocar "Subir Foto"
//        btnSeleccionarImagen.setOnClickListener {
//            seleccionarImagenLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//        }

        // ACCIÓN: Botón Principal de Guardar
        btnAgregarProducto.setOnClickListener {
            val paisOrigenPosicion = spinnerPaisProducto.selectedItemPosition
            val lote = etLoteProducto.text.toString().trim()
            val id = etIdProducto.text.toString().trim()
            val pais = spinnerPaisProducto.selectedItem.toString().trim()
            val nombre = etNombreProducto.text.toString().trim()
            val marca = etMarcaProducto.text.toString().trim()
            val precioString = etPrecioProducto.text.toString().trim()

            if (id.isEmpty() || lote.isEmpty() || pais.isEmpty() || nombre.isEmpty() || marca.isEmpty() || precioString.isEmpty()) {
                Toast.makeText(this, "Todos los campos deben estar rellenos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (paisOrigenPosicion == 0 || paisOrigenPosicion == 1) {
                Toast.makeText(this, "Debes seleccionar un país de origen del producto válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (fechaSeleccionada == null) {
                Toast.makeText(this, "Por favor, selecciona una fecha de caducidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = try {
                precioString.toDouble()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "Error: Introduce un precio valido: 5.20 o 10", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val campos = listOf(
                Triple(etIdProducto, ::validarIdProducto, "Asigna un ID valido. Ejemplo: A-123456, AB-123456 o ABC-123456. El '_' es opcional"),
                Triple(etLoteProducto, ::validarLoteProducto, "Introduce un número de lote válido. Ejemplo: L-12345678"),
                Triple(etMarcaProducto, ::validarMarcaNombreProducto, "Introduce un nombre de marca valido"),
                Triple(etNombreProducto, ::validarMarcaNombreProducto, "Introduce un nombre de producto válido"),
                Triple(etPrecioProducto, ::validarPrecioProducto, "Introduce el precio en un formato válido. Ejemplo: 5.20€ o 10€")
            )

            for ((editText, validador, errorMessage) in campos) {
                if (!validador(editText.text.toString())) {
                    editText.error = errorMessage
                    editText.requestFocus()
                    return@setOnClickListener
                }
            }

            val idComercio = auth.currentUser?.uid
            if (idComercio == null) {
                Toast.makeText(this, "Error de autenticación, por favor inicie sesión de nuevo.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Validación de Firestore (ID, Lote, Nombre/Marca)
            db.collection("inventario")
                .whereEqualTo("idComercio", idComercio)
                .whereEqualTo("idProducto", id)
                .limit(1)
                .get()
                .addOnSuccessListener { idDocuments ->
                    if (!idDocuments.isEmpty) {
                        Toast.makeText(this, "Error: Ya existe un producto con este ID en tu inventario.", Toast.LENGTH_LONG).show()
                    } else {
                        db.collection("inventario")
                            .whereEqualTo("idComercio", idComercio)
                            .whereEqualTo("lote", lote)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { loteDocuments ->
                                if (!loteDocuments.isEmpty) {
                                    // FALLO 2: Lote duplicado
                                    Toast.makeText(this, "Error: Ya existe un producto con este Lote en tu inventario.", Toast.LENGTH_LONG).show()
                                } else {
                                    db.collection("inventario")
                                        .whereEqualTo("idComercio", idComercio)
                                        .whereEqualTo("nombreProducto", nombre.lowercase())
                                        .whereEqualTo("marcaProducto", marca.lowercase())
                                        .limit(1)
                                        .get()
                                        .addOnSuccessListener { nombreMarcaDocuments ->
                                            if (!nombreMarcaDocuments.isEmpty) {
                                                Toast.makeText(this, "Error: Ya hay productos con el mismo Nombre y Marca en tu inventario.", Toast.LENGTH_LONG).show()
                                            } else {
                                                val producto = Producto(
                                                    idProducto = id,
                                                    lote = lote,
                                                    pais = pais,
                                                    nombre = nombre,
                                                    marca = marca,
                                                    precio = precio
                                                )
                                                agregarProducto(producto, idComercio)
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("AgregarProducto", "Error al verificar Nombre/Marca", e)
                                            Toast.makeText(this, "Error al verificar Nombre/Marca. ¿Creaste el índice en Firestore?", Toast.LENGTH_LONG).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("AgregarProducto", "Error al verificar Lote", e)
                                Toast.makeText(this, "Error al verificar Lote", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("AgregarProducto", "Error al verificar ID Producto", e)
                    Toast.makeText(this, "Error al verificar ID del producto", Toast.LENGTH_SHORT).show()
                }
        }

        btnVolverOpcionesComercio.setOnClickListener {
            val intent = Intent(this, OpcionesComercio::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_cerrar_sesion, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_cerrar_sesion -> {
                cerrarSesion()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun agregarProducto(producto: Producto, idComercio: String) {
        db.collection("comercios").document(idComercio).get()
            .addOnSuccessListener { document ->
                if (document == null || !document.exists()) {
                    Toast.makeText(this, "No se encontró el comercio", Toast.LENGTH_SHORT).show()
                }

                val nombreComercio = document.getString("nombre") ?: "Nombre desconocido"

                val productoRef = db.collection("productos").document()

                val idProductoMaestro = productoRef.id

                val productoData = hashMapOf(
                    "idProducto" to producto.idProducto,
                    "lote" to producto.lote,
                    "pais" to producto.pais,
                    "nombre" to producto.nombre?.lowercase(),
                    "marca" to producto.marca?.lowercase(),
                    "precio" to producto.precio
                )

                val inventarioRef = db.collection("inventario").document()

                val productoInventario = hashMapOf (
                    "idMaestroProducto" to idProductoMaestro,
                    "idProducto" to producto.idProducto,
                    "lote" to producto.lote,
                    "nombreProducto" to producto.nombre?.lowercase(),
                    "marcaProducto" to producto.marca?.lowercase(),
                    "precioProducto" to producto.precio,
                    "idComercio" to idComercio,
                    "nombreComercio" to nombreComercio,
                    "paisProducto" to producto.pais
                )


                    val batch = db.batch()
                    batch.set(productoRef, productoData)
                    batch.set(inventarioRef, productoInventario)

                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                            etLoteProducto.text.clear()
                            etIdProducto.text.clear()
                            etNombreProducto.text.clear()
                            etMarcaProducto.text.clear()
                            etPrecioProducto.text.clear()
                            spinnerPaisProducto.setSelection(0)
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error al agregar producto en batch", e)
                            Toast.makeText(this, "Error al agregar producto", Toast.LENGTH_SHORT).show()
                        }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al obtener datos del comercio", e)
                Toast.makeText(this, "Error al obtener datos del comercio", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cerrarSesion() {
        // 1. Cerramos la sesión en Firebase (Cubre Email y Google)
        FirebaseAuth.getInstance().signOut()

        // 2. Limpiamos el estado de Credential Manager (Específico para Google/Passkeys)
        val credentialManager = CredentialManager.create(this)

        lifecycleScope.launch {
            try {
                // Esto obliga a que la próxima vez Google pida elegir cuenta
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("CierreSesion", "Error al limpiar credenciales: ${e.message}")
            } finally {
                // 3. Siempre redirigimos al inicio, falle o no la limpieza de credenciales
                val intent = Intent(this@AgregarProducto, InicioApp::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }

}