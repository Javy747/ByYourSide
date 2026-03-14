package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ConsultarInventario : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productoHolderAdapter: ProductoHolderAdapter
    private val productosList = mutableListOf<Producto>()
    private lateinit var tvNoResultadosInv: TextView
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(auth.currentUser == null){
            val intent = Intent(this, LoginComercio::class.java)
            startActivity(intent)
            finish()
            return
        }

        val idDelComercioActual = auth.currentUser!!.uid

        enableEdgeToEdge()
        setContentView(R.layout.activity_consulta_inventario)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbarInventario = findViewById<Toolbar>(R.id.toolbar_inventario)
        setSupportActionBar(toolbarInventario)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recyclerView = findViewById(R.id.recycler_inventario)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productoHolderAdapter = ProductoHolderAdapter(productosList, idDelComercioActual)
        recyclerView.adapter = productoHolderAdapter

        val btnVolverOpcionesComercio = findViewById<ImageView>(R.id.inv_btn_volver_opciones_comercio)

        tvNoResultadosInv = findViewById(R.id.tvNoResultadosInv)

        btnVolverOpcionesComercio.setOnClickListener {
            val intent = Intent(this, OpcionesComercio::class.java)
            startActivity(intent)
        }

        cargarDatosComercio()

        cargarDatosProductos()

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

    private fun cargarDatosProductos() {
        consultarInventario(
            onResultado = { productos ->
                productosList.clear()
                productosList.addAll(productos)
                productoHolderAdapter.notifyDataSetChanged()

                if (productos.isEmpty()) {
                    tvNoResultadosInv.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    tvNoResultadosInv.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            },
            onError = { ex ->
                Log.e("ConsultaInventario", "Error al cargar inventario", ex)
                Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                tvNoResultadosInv.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        )
    }

    private fun cargarDatosComercio(){
        val db = FirebaseFirestore.getInstance()
        val usuarioActual = auth.currentUser

        if(usuarioActual != null){
            val comercioId = usuarioActual.uid
            val nombreComercio = findViewById<TextView>(R.id.nombre_comercio_inv)
            val idComercio = findViewById<TextView>(R.id.id_comercio_inv)

            idComercio.text = "ID: $comercioId"

            db.collection("comercios").document(comercioId).get()
                .addOnSuccessListener { document ->
                    if(document != null && document.exists()){
                        val nombre = document.getString("nombre") ?: "Nombre no encontrado"
                        nombreComercio.text = "Nombre: $nombre"
                    } else {
                        nombreComercio.text = "Comercio no encontrado"
                        Log.d("Firestore", "No se encontró el documento del comercio")
                    }

                }
                .addOnFailureListener { exception ->
                    nombreComercio.text = "Error al cargar"
                    Log.e("Firestore", "Error al obtener del comercio", exception)
                }
        }
    }

    private fun consultarInventario(
        onResultado: (List<Producto>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usuarioActual = auth.currentUser

        if (usuarioActual == null) {
            onError(Exception("No hay sesión iniciada"))
            return
        }

        val idComercio = usuarioActual.uid

        Log.d("ConsultaInventario", "Buscando inventario para el comercio ID: $idComercio")

        db.collection("inventario")
            .whereEqualTo("idComercio", idComercio)
            .get()
            .addOnSuccessListener inventarioListener@{ inventarioSnapshot ->

                Log.d("ConsultaInventario", "Paso 1: Se encontraron ${inventarioSnapshot.size()} documentos en 'inventario'.")

                if(inventarioSnapshot.isEmpty){
                    onResultado(emptyList())
                    return@inventarioListener
                }

                val mapaIdMaestroAInventario = inventarioSnapshot.documents.associate { doc ->
                    val idMaestro = doc.getString("idMaestroProducto") ?: ""
                    val idInventario = doc.id
                    idMaestro to idInventario
                }

                val idProductosMaestros = mapaIdMaestroAInventario.keys.toList()

                Log.d("ConsultaInventario", "Paso 2: IDs maestros a buscar en 'productos': $idProductosMaestros")

                if (idProductosMaestros.isEmpty() || (idProductosMaestros.size == 1 && idProductosMaestros[0] == "")) {
                    onResultado(emptyList())
                    return@inventarioListener
                }

                db.collection("productos")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), idProductosMaestros)
                    .get()
                    .addOnSuccessListener { productoSnapshot ->

                        Log.d("ConsultaInventario", "Paso 3: Se encontraron ${productoSnapshot.size()} documentos en 'productos'.")

                        val productos = productoSnapshot.documents.mapNotNull { docProducto ->
                            try {
                                val producto = docProducto.toObject(Producto::class.java)
                                if (producto != null) {
                                    val idMaestro = docProducto.id
                                    producto.documentId = mapaIdMaestroAInventario[idMaestro]
                                } else {
                                    Log.e("ConsultaInventario", "¡FALLO! toObject() devolvió null para el doc: ${docProducto.id}")
                                }
                                producto
                            } catch (e: Exception) {
                                Log.e("ConsultaInventario", "¡EXCEPCIÓN! Error al convertir documento ${docProducto.id}", e)
                                null
                            }
                        }

                        Log.d("ConsultaInventario", "Paso 4: Lista final de productos procesada. Total: ${productos.size}")
                        onResultado(productos)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ConsultaInventario", "Error al buscar en 'productos' con whereIn", exception)
                        onError(exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("ConsultaInventario", "Error al buscar en 'inventario'", exception)
                onError(exception)
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
                val intent = Intent(this@ConsultarInventario, InicioApp::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
