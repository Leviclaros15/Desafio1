package com.example.desafio1

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

class CalculadoraActivity : AppCompatActivity() {

    private lateinit var num1EditText: EditText
    private lateinit var num2EditText: EditText
    private lateinit var resultadoTextView: TextView

    private val formato = DecimalFormat("#.####")
    private val nombreArchivo = "historial_calculadora.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora)

        num1EditText = findViewById(R.id.num1EditText)
        num2EditText = findViewById(R.id.num2EditText)
        resultadoTextView = findViewById(R.id.resultadoTextView)

        findViewById<Button>(R.id.sumarBtn).setOnClickListener { operar("Suma") }
        findViewById<Button>(R.id.restarBtn).setOnClickListener { operar("Resta") }
        findViewById<Button>(R.id.multiplicarBtn).setOnClickListener { operar("Multiplicación") }
        findViewById<Button>(R.id.dividirBtn).setOnClickListener { operar("División") }
        findViewById<Button>(R.id.exponenteBtn).setOnClickListener { operar("Exponente") }
        findViewById<Button>(R.id.raizBtn).setOnClickListener { operar("Raíz cuadrada") }

        findViewById<Button>(R.id.exportarBtn).setOnClickListener { exportarHistorial() }
        findViewById<Button>(R.id.volverBtn).setOnClickListener { finish() }
    }

    private fun operar(operacion: String) {
        val texto1 = num1EditText.text.toString().trim()
        val texto2 = num2EditText.text.toString().trim()

        if (texto1.isEmpty() || (texto2.isEmpty() && operacion != "Raíz cuadrada")) {
            Toast.makeText(this, getString(R.string.error_num_vacio), Toast.LENGTH_SHORT).show()
            return
        }

        val num1 = texto1.toDoubleOrNull()
        val num2 = if (texto2.isEmpty()) null else texto2.toDoubleOrNull()

        if (num1 == null || (num2 == null && operacion != "Raíz cuadrada")) {
            Toast.makeText(this, getString(R.string.error_num_invalido), Toast.LENGTH_SHORT).show()
            return
        }

        val resultado: Double

        when (operacion) {
            "Suma" -> resultado = num1 + num2!!
            "Resta" -> resultado = num1 - num2!!
            "Multiplicación" -> resultado = num1 * num2!!
            "División" -> {
                if (num2 == 0.0) {
                    Toast.makeText(this, getString(R.string.error_division_cero), Toast.LENGTH_SHORT).show()
                    return
                }
                resultado = num1 / num2!!
            }
            "Exponente" -> resultado = num1.pow(num2!!)
            "Raíz cuadrada" -> {
                if (num1 < 0) {
                    Toast.makeText(this, getString(R.string.error_raiz_negativa), Toast.LENGTH_SHORT).show()
                    return
                }
                resultado = sqrt(num1)
            }
            else -> return
        }

        val resultadoFormateado = formato.format(resultado)
        resultadoTextView.text = resultadoFormateado

        guardarEnHistorial(operacion, num1, num2, resultadoFormateado)
    }

    private fun guardarEnHistorial(operacion: String, num1: Double, num2: Double?, resultado: String) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val linea = if (num2 != null) {
            "[$fecha] $operacion: $num1 y $num2 = $resultado\n"
        } else {
            "[$fecha] $operacion: $num1 = $resultado\n"
        }

        try {
            openFileOutput(nombreArchivo, Context.MODE_APPEND).use { output ->
                output.write(linea.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exportarHistorial() {
        try {
            val contenido = openFileInput(nombreArchivo).bufferedReader().use { it.readText() }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ usa MediaStore, no necesita permiso especial
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                uri?.let {
                    resolver.openOutputStream(it)?.use { stream ->
                        stream.write(contenido.toByteArray())
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                )
                val file = java.io.File(downloadsDir, nombreArchivo)
                file.writeText(contenido)
            }

            Toast.makeText(this, getString(R.string.historial_exportado), Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.historial_error_exportar), Toast.LENGTH_SHORT).show()
        }
    }
}