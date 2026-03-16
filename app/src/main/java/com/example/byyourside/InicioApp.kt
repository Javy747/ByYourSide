package com.example.byyourside

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class InicioApp : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio_app)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnComercio = findViewById<Button>(R.id.btn_comercio_cd_inicio_app)
        val btnCliente = findViewById<Button>(R.id.btn_cliente_cd_inicio_app)

        btnComercio.setOnClickListener{
            val mainToComercio = Intent(this, LoginComercio::class.java)
           startActivity(mainToComercio)
        }

        btnCliente.setOnClickListener {
            val mainToCliente = Intent(this, LoginCliente::class.java)
            startActivity(mainToCliente)
        }
    }
}