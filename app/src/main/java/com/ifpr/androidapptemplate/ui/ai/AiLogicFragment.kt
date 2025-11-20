
package com.ifpr.androidapptemplate.ui.ai

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.ifpr.androidapptemplate.R
import kotlinx.coroutines.launch

class AiLogicFragment : Fragment() {

    private lateinit var promptInput: EditText
    private lateinit var resultText: TextView
    private lateinit var generateButton: Button
    private lateinit var model: GenerativeModel
    private lateinit var imageButton: Button
    private var imageUri: Uri? = null
    private lateinit var itemImageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ai_logic, container, false)

        promptInput = view.findViewById(R.id.prompt_input)
        resultText = view.findViewById(R.id.result_text)
        generateButton = view.findViewById(R.id.btn_generate)
        imageButton = view.findViewById(R.id.btn_select_image)
        itemImageView = view.findViewById(R.id.bitmapImageView)

        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                Glide.with(this).load(imageUri).into(itemImageView)
                resultText.text = "Imagem selecionada. Pronto para gerar."
            } else {
                resultText.text = "Nenhuma imagem selecionada."
            }
        }

        imageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        generateButton.setOnClickListener {
            val prompt = promptInput.text.toString().trim()
            if (prompt.isNotEmpty()) {
                resultText.text = "Aguardando resposta..."
                val drawable = itemImageView.drawable
                if (drawable != null) {
                    try {
                        val bitmap = itemImageView.drawToBitmap()
                        generateFromPrompt(prompt, bitmap)
                    } catch (e: Exception) {
                        resultText.text = "Erro ao processar imagem: ${e.message}"
                    }
                } else {
                    resultText.text = "Selecione uma imagem."
                }
            } else {
                resultText.text = "Digite um prompt para continuar."
            }
        }

        return view
    }

    private fun generateFromPrompt(prompt: String, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val promptImage = content {
                    image(bitmap)
                    text(prompt)
                }
                val response = model.generateContent(promptImage)
                resultText.text = response.text ?: "Nenhuma resposta recebida."
            } catch (e: Exception) {
                resultText.text = "Erro ao gerar resposta: ${e.message}"
            }
        }
    }
}