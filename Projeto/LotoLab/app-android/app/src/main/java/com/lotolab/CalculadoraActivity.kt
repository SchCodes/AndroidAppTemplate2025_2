package com.lotolab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class CalculadoraActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculadora)

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        val probModule = py.getModule("probabilidades")
        val resultado = probModule.callAttr("calcular_probabilidade", listOf(1,2,3,4,5))
        println("Resultado probabilidade: $resultado")
    }
}