package com.example.desafio1

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.DecimalFormat

class SalarioActivity : AppCompatActivity() {

    private lateinit var nombreEditText: EditText
    private lateinit var salarioEditText: EditText
    private lateinit var salarioBrutoTextView: TextView
    private lateinit var rentaTextView: TextView
    private lateinit var afpTextView: TextView
    private lateinit var isssTextView: TextView
    private lateinit var salarioNetoTextView: TextView

    private val formato = DecimalFormat("$#,##0.00")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_salario)

        nombreEditText = findViewById(R.id.nombreEmpleadoEditText)
        salarioEditText = findViewById(R.id.salarioEditText)
        salarioBrutoTextView = findViewById(R.id.salarioBrutoTextView)
        rentaTextView = findViewById(R.id.rentaTextView)
        afpTextView = findViewById(R.id.afpTextView)
        isssTextView = findViewById(R.id.isssTextView)
        salarioNetoTextView = findViewById(R.id.salarioNetoTextView)

        val calcularBtn = findViewById<Button>(R.id.calcularSalarioBtn)
        val volverBtn = findViewById<Button>(R.id.volverBtn)

        calcularBtn.setOnClickListener {
            calcularDescuentos()
        }

        volverBtn.setOnClickListener {
            finish()
        }
    }

    private fun calcularDescuentos() {
        val texto = salarioEditText.text.toString().trim()

        if (texto.isEmpty()) {
            salarioEditText.error = getString(R.string.error_salario_vacio)
            vibrar()
            return
        }

        val salario = texto.toDoubleOrNull()

        if (salario == null) {
            salarioEditText.error = getString(R.string.error_salario_invalido)
            vibrar()
            return
        }

        if (salario <= 0) {
            salarioEditText.error = getString(R.string.error_salario_negativo)
            vibrar()
            return
        }

        val afp = salario * 0.0725
        val isss = calcularIsss(salario)
        val renta = calcularRenta(salario)
        val salarioNeto = salario - afp - isss - renta

        salarioBrutoTextView.text = formato.format(salario)
        rentaTextView.text = formato.format(renta)
        afpTextView.text = formato.format(afp)
        isssTextView.text = formato.format(isss)
        salarioNetoTextView.text = formato.format(salarioNeto)
    }


    private fun calcularIsss(salario: Double): Double {
        val base = if (salario > 1000.0) 1000.0 else salario
        return base * 0.03
    }


    private fun calcularRenta(salario: Double): Double {
        return when {
            salario <= 472.00 -> {
                0.0
            }
            salario <= 895.24 -> {
                val excedente = salario - 472.00
                (excedente * 0.10) + 17.67
            }
            salario <= 2038.10 -> {
                val excedente = salario - 895.24
                (excedente * 0.20) + 60.00
            }
            else -> {
                val excedente = salario - 2038.10
                (excedente * 0.30) + 288.57
            }
        }
    }

    private fun vibrar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(300)
            }
        }
    }
}