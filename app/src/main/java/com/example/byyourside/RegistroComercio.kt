package com.example.byyourside

import android.content.Intent
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroComercio : AppCompatActivity(), VerificacionCampos {

    private lateinit var comercio: Comercio
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_comercio)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        val vistaPrincipal = findViewById<android.view.View>(android.R.id.content)
        resetScrollEditText(vistaPrincipal)

        val etNifComercio = findViewById<EditText>(R.id.et_nif_comercio_registro)
        val etNombreComercio = findViewById<EditText>(R.id.et_nombre_comercio_registro)

        val spinnerPaisComercio = findViewById<Spinner>(R.id.spr_pais_comercio)

        val switchEcommerce = findViewById<SwitchCompat>(R.id.stch_ecommerce)

        /*
        val spinnerProvinciaComercio = findViewById<Spinner>(R.id.spr_provincia_comercio)
        val spinnerMunicipioComercio = findViewById<Spinner>(R.id.spr_municipio_comercio)
        */

        val etProvinciaComercio = findViewById<EditText>(R.id.et_provincia_comercio_registro)
        val etMunicipioComercio = findViewById<EditText>(R.id.et_municipio_comercio_registro)
        val etCalleComercio = findViewById<EditText>(R.id.et_calle_comercio_registro)
        val etNumeroLocalComercio = findViewById<EditText>(R.id.et_calle_numero_comercio_registro)
        val etCodigoPostalComercio = findViewById<EditText>(R.id.et_cp_comercio_registro)

        val etWebComercio = findViewById<EditText>(R.id.et_web_comercio_registro)
        val etEmailComercio = findViewById<EditText>(R.id.et_email_comercio_registro)
        val etTelefonoComercio = findViewById<EditText>(R.id.et_telefono_comercio_registro)
        val etContrasenhaComercio = findViewById<EditText>(R.id.et_contrasenha_comercio_registro)
        val etConfirmarContrasenhaComercio = findViewById<EditText>(R.id.et_confirmar_contrasenha_comercio_registro)

        val btnRegistroComercio = findViewById<Button>(R.id.btn_registro_comercio)

        val btnVolverLoginComercio = findViewById<ImageView>(R.id.volver_login_comercio)

        switchEcommerce.setOnCheckedChangeListener { _, isChecked ->

            val visibilidad = if (isChecked) View.GONE else View.VISIBLE

/*
            spinnerProvinciaComercio.isEnabled = !desactivar
            spinnerProvinciaComercio.isClickable = !desactivar
            spinnerMunicipioComercio.isEnabled = !desactivar
*/
            etProvinciaComercio.visibility = visibilidad
            etMunicipioComercio.visibility = visibilidad
            etCalleComercio.visibility = visibilidad
            etNumeroLocalComercio.visibility = visibilidad
            etCodigoPostalComercio.visibility = visibilidad

            if (isChecked) {
               /*
                  spinnerProvinciaComercio.setSelection(0)
                  spinnerMunicipioComercio.setSelection(0)
                */
                etProvinciaComercio.setText("")
                etMunicipioComercio.setText("")
                etCalleComercio.setText("")
                etNumeroLocalComercio.setText("")
                etCodigoPostalComercio.setText("")
            }
        }

        val paisesAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.paises,
            android.R.layout.simple_spinner_item
        )
//        val provinciasAdapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.provincias,
//            android.R.layout.simple_spinner_item
//        )
//        val municipiosAdapter = ArrayAdapter.createFromResource(
//            this,
//            R.array.municipios,
//            android.R.layout.simple_spinner_item
//        )

        paisesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//      provinciasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//      municipiosAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerPaisComercio.adapter = paisesAdapter
//        spinnerProvinciaComercio.adapter = provinciasAdapter
//        spinnerMunicipioComercio.adapter = municipiosAdapter

        val paisSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
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

//        val provinciaMunicipioSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//
//                val textView = view as? TextView
//
//                if (position == 0) {
//                    textView?.setTextColor(getColor(R.color.gray))
//                } else {
//                    textView?.setTextColor(getColor(R.color.black))
//                }
//
//                (view as? TextView)?.textSize = 25f
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//
//        }

        // Asignación de los listeners a los spinners
        spinnerPaisComercio.onItemSelectedListener = paisSelectedListener
//        spinnerProvinciaComercio.onItemSelectedListener = provinciaMunicipioSelectedListener
//        spinnerMunicipioComercio.onItemSelectedListener = provinciaMunicipioSelectedListener

        btnRegistroComercio.setOnClickListener {
            // Recopilación de datos de la interfaz de usuario
            val paisComercio = spinnerPaisComercio.selectedItemPosition
//            val provinciaComercio = spinnerProvinciaComercio.selectedItemPosition
//            val municipioComercio = spinnerMunicipioComercio.selectedItemPosition

            val nif = etNifComercio.text.toString().trim()
            val nombre = etNombreComercio.text.toString().trim()
            val pais = spinnerPaisComercio.selectedItem.toString()
            val provincia = etProvinciaComercio.text.toString()
            val municipio = etMunicipioComercio.text.toString()
            val calle = etCalleComercio.text.toString().trim()
            val calleNumeroString = etNumeroLocalComercio.text.toString().trim()
            val codigoPostalString = etCodigoPostalComercio.text.toString()
            val web = etWebComercio.text.toString().trim()
            val email = etEmailComercio.text.toString().trim()
            val telefono = etTelefonoComercio.text.toString().trim()
            val contrasenha = etContrasenhaComercio.text.toString().trim()
            val confirmarContrasenha = etConfirmarContrasenhaComercio.text.toString().trim()

            if (nif.isEmpty()) {
                Toast.makeText(this, "El NIF es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty()) {
                Toast.makeText(this, "El nombre del comercio es obligatorio", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if(paisComercio == 0 || paisComercio == 1){
                Toast.makeText(this, "Debes seleccionar el país donde se encuentra tu comercio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "El email es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (telefono.isEmpty()) {
                Toast.makeText(this, "El teléfono es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (contrasenha.isEmpty()) {
                Toast.makeText(this, "La contraseña es obligatoria", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmarContrasenha.isEmpty()) {
                Toast.makeText(this, "Repetir la contraseña es obligatorio", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }



            val esEcommerce = switchEcommerce.isChecked

            when {
                esEcommerce && pais.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Los e-commerce deben especificar el país de operación",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                !esEcommerce && (pais.isEmpty() || provincia.isEmpty() || municipio.isEmpty() ||
                        calle.isEmpty() || calleNumeroString.isEmpty() || codigoPostalString.isEmpty()) -> {
                    Toast.makeText(
                        this,
                        "Las comercios físicas deben tener dirección completa",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
            }

            val provinciaFinal = if (esEcommerce) null else provincia
            val municipioFinal = if (esEcommerce) null else municipio
            val calleFinal = if (esEcommerce) null else calle
            val calleNumero: Long? = if (!esEcommerce) calleNumeroString.toLongOrNull() else null
            val codigoPostal: Long? = if (!esEcommerce) codigoPostalString.toLongOrNull() else null

            val campos = mutableListOf(
                Triple(
                    etNifComercio,
                    ::validarNifComercio,
                    "Formato de NIF inválido. Ejemplo: A-12345678 o A12345678 "
                ),
                Triple(
                    etNombreComercio,
                    ::validarNombreComercio,
                    "Solo letras, números, espacios y .'-"
                ),
                Triple(
                    etEmailComercio,
                    ::validarEmail,
                    "El email debe tener el siguente formato: ejemplo27@example.com"
                ),
                Triple(etTelefonoComercio, ::validarTelefono, "Introduce un telefono válido"),
                Triple(
                    etContrasenhaComercio,
                    ::validarContrasenha,
                    "Introduce una contraseña de al menos 8 carácteres, 1 mayúscula, 1 minúscula y 1 número"
                )
            )

            if (!esEcommerce) {
                campos.addAll(
                    listOf(
                        Triple(
                            etCalleComercio,
                            ::validarNombreCalleComercio,
                            "Introduce un nombre de calle válido"
                        ),
                        Triple(
                            etNumeroLocalComercio,
                            ::validarNumeroLocal,
                            "Introduce un número de la dirección"
                        ),
                        Triple(
                            etCodigoPostalComercio,
                            ::validar_codigo_postal,
                            "Introduce un código postal válido"
                        )
                    )
                )
            } else {
                campos.add(Triple(etWebComercio, ::validarWeb, "Introduce una url valida"))
            }

            for ((editText, validador, errorMessage) in campos) {
                val texto = editText.text.toString()
                if (!validador(texto)) {
                    editText.error = errorMessage
                    editText.requestFocus()
                    return@setOnClickListener
                }
            }

            if (contrasenha.isEmpty() || confirmarContrasenha.isEmpty()) {
                Toast.makeText(this, "Debe ingresar y confirmar la contraseña", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Validación de coincidencia de contraseñas
            if (contrasenha != confirmarContrasenha) {
                etConfirmarContrasenhaComercio.error = "Las contraseñas no coinciden"
                etConfirmarContrasenhaComercio.requestFocus()
                return@setOnClickListener
            }

            // Creación del objeto Comercio
            comercio = Comercio(
                nif,
                nombre,
                pais,
                provinciaFinal,
                municipioFinal,
                calleFinal,
                calleNumero,
                codigoPostal,
                web,
                telefono,
                email,
                tipo_comercio = if (esEcommerce) TipoComercio.ECOMMERCE else TipoComercio.FISICO
            )

            registroComercio(email, contrasenha)

        }

        btnVolverLoginComercio.setOnClickListener {
            val intentVolverAlLogin = Intent(this, LoginComercio::class.java)
            startActivity(intentVolverAlLogin)
        }

    }

    private fun registroComercio(email: String, contrasenha: String) {
        auth.createUserWithEmailAndPassword(email, contrasenha)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        db.collection("comercios")
                            .document(user.uid)
                            .set(comercio)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Registro del comercio fue exitoso",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this, OpcionesComercio::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error al guardar datos del comercio", e)
                                Toast.makeText(
                                    this,
                                    "Error al guardar los datos del comercio",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Error: No se pudieron obtener los datos del usuario.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                        Toast.makeText(this, "Error: Usuario Existente", Toast.LENGTH_LONG).show()
                    }
                }

    }

    private fun resetScrollEditText(view: android.view.View) {
        if( view is EditText){
            view.onFocusChangeListener = android.view.View.OnFocusChangeListener { v, hasFocus ->
                if(!hasFocus){
                    v.post {
                        view.scrollTo(0, 0)
                        view.setSelection(0)

                    }
                }
            }
        }

        if(view is android.view.ViewGroup){
            for (i in 0 until view.childCount){
                val vistaHija = view.getChildAt(i)
                resetScrollEditText(vistaHija)
            }
        }
    }

}
