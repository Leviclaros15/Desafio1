package com.example.desafio1

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.DecimalFormat

class PromedioActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var nota1EditText: EditText
    private lateinit var nota2EditText: EditText
    private lateinit var nota3EditText: EditText
    private lateinit var nota4EditText: EditText
    private lateinit var nota5EditText: EditText
    private lateinit var promedioTextView: TextView
    private lateinit var estadoTextView: TextView

    private val peso1 = 0.20
    private val peso2 = 0.20
    private val peso3 = 0.20
    private val peso4 = 0.20
    private val peso5 = 0.20


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "No se podrá mostrar la notificación sin permiso",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_promedio)

        nombreEditText = findViewById(R.id.nombreEditText)
        nota1EditText = findViewById(R.id.nota1EditText)
        nota2EditText = findViewById(R.id.nota2EditText)
        nota3EditText = findViewById(R.id.nota3EditText)
        nota4EditText = findViewById(R.id.nota4EditText)
        nota5EditText = findViewById(R.id.nota5EditText)
        promedioTextView = findViewById(R.id.promedioTextView)
        estadoTextView = findViewById(R.id.estadoTextView)

        val calcularBtn = findViewById<Button>(R.id.calcularBtn)
        val volverBtn = findViewById<Button>(R.id.volverBtn)

        crearCanalNotificaciones()
        pedirPermisoNotificaciones()

        calcularBtn.setOnClickListener {
            calcularPromedio()
        }

        volverBtn.setOnClickListener {
            finish()
        }
    }

    private fun calcularPromedio() {
        val nombre = nombreEditText.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_nombre_vacio), Toast.LENGTH_SHORT).show()
            return
        }

        val nota1 = validarNota(nota1EditText) ?: return
        val nota2 = validarNota(nota2EditText) ?: return
        val nota3 = validarNota(nota3EditText) ?: return
        val nota4 = validarNota(nota4EditText) ?: return
        val nota5 = validarNota(nota5EditText) ?: return

        val promedio = (nota1 * peso1) + (nota2 * peso2) + (nota3 * peso3) +
                (nota4 * peso4) + (nota5 * peso5)

        val formato = DecimalFormat("#.##")
        val promedioFormateado = formato.format(promedio)

        val aprobado = promedio >= 6.0
        val estado = if (aprobado) {
            getString(R.string.estado_aprobado)
        } else {
            getString(R.string.estado_reprobado)
        }

        promedioTextView.text = promedioFormateado
        estadoTextView.text = estado

        mostrarNotificacion(nombre, promedioFormateado, estado)
    }

    private fun validarNota(editText: EditText): Double? {
        val texto = editText.text.toString().trim()

        if (texto.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_nota_vacia), Toast.LENGTH_SHORT).show()
            return null
        }

        val nota = texto.toDoubleOrNull()
        if (nota == null) {
            Toast.makeText(this, getString(R.string.error_nota_invalida), Toast.LENGTH_SHORT).show()
            return null
        }

        if (nota < 0 || nota > 10) {
            Toast.makeText(this, getString(R.string.error_nota_rango), Toast.LENGTH_SHORT).show()
            return null
        }

        return nota
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.canal_notificaciones_id),
                getString(R.string.canal_notificaciones_nombre),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun mostrarNotificacion(nombre: String, promedio: String, estado: String) {
        val texto = getString(R.string.notificacion_texto, nombre, promedio, estado)

        val builder = NotificationCompat.Builder(this, getString(R.string.canal_notificaciones_id))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notificacion_titulo))
            .setContentText(texto)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(this).notify(1, builder.build())
        }
    }
}