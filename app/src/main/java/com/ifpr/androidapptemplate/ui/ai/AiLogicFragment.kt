package com.ifpr.androidapptemplate.ui.ai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.ifpr.androidapptemplate.R
import kotlinx.coroutines.launch

class AiLogicFragment : Fragment() {

    private lateinit var promptInput: EditText
    private lateinit var resultText: TextView
    private lateinit var generateButton: Button
    private lateinit var model: GenerativeModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_logic, container, false)

        promptInput = view.findViewById(R.id.prompt_input)
        resultText = view.findViewById(R.id.result_text)
        generateButton = view.findViewById(R.id.btn_generate)

        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")

        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                resultText.text = "Aguardando resposta..."
                generateFromPrompt(prompt)
            } else {
                resultText.text = "Digite um prompt para continuar."
            }
        }

        return view
    }

    private fun generateFromPrompt(prompt: String) {
        lifecycleScope.launch {
            try {
                val response = model.generateContent(prompt)
                resultText.text = response.text ?: "Nenhuma resposta recebida."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta: ${e.message}"
            }
        }
    }
}