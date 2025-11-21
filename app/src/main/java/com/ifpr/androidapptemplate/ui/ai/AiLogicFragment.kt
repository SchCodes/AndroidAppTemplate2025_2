
package com.ifpr.androidapptemplate.ui.ai

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.ifpr.androidapptemplate.databinding.FragmentAiLogicBinding
import kotlinx.coroutines.launch

class AiLogicFragment : Fragment() {

    private var _binding: FragmentAiLogicBinding? = null
    private val binding get() = _binding!!

    private lateinit var model: GenerativeModel
    private lateinit var chat: Chat
    private val adapter = AiChatAdapter()
    private var selectedBitmap: Bitmap? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            handleSelectedImage(uri)
        } else {
            clearSelectedImage()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAiLogicBinding.inflate(inflater, container, false)
        setupModel()
        setupUi()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupModel() {
        model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
        chat = model.startChat()
    }

    private fun setupUi() {
        binding.chatRecycler.layoutManager =
            LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.chatRecycler.adapter = adapter

        if (adapter.itemCount == 0) {
            adapter.addMessage(
                AiMessage(
                    text = "Oi! Posso ajudar com ideias de combinacoes, validacao de apostas e analises usando texto e imagens.",
                    fromUser = false
                )
            )
        }

        binding.btnSelectImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnClearImage.setOnClickListener { clearSelectedImage() }
        binding.btnSend.setOnClickListener { sendPrompt() }
    }

    private fun sendPrompt() {
        val prompt = binding.promptInput.text.toString().trim()
        if (prompt.isEmpty()) {
            binding.promptInput.error = "Digite uma mensagem"
            return
        }

        val bitmapForPrompt = selectedBitmap
        binding.promptInput.text?.clear()
        val withImage = bitmapForPrompt != null
        adapter.addMessage(AiMessage(text = prompt, fromUser = true, withImage = withImage))
        scrollToBottom()
        setLoading(true)

        lifecycleScope.launch {
            try {
                val response = sendToModel(prompt, bitmapForPrompt)
                val textResult = response.text?.takeIf { it.isNotBlank() }
                    ?: "Nenhuma resposta recebida."
                adapter.addMessage(
                    AiMessage(
                        text = textResult,
                        fromUser = false,
                        withImage = withImage
                    )
                )
            } catch (e: Exception) {
                adapter.addMessage(
                    AiMessage(
                        text = "Erro ao gerar resposta: ${e.message}",
                        fromUser = false,
                        isError = true
                    )
                )
            } finally {
                setLoading(false)
                scrollToBottom()
            }
        }
    }

    private suspend fun sendToModel(prompt: String, bitmap: Bitmap?) = if (bitmap != null) {
        val promptImage = content {
            image(bitmap)
            text(prompt)
        }
        chat.sendMessage(promptImage)
    } else {
        chat.sendMessage(prompt)
    }

    private fun handleSelectedImage(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }

            selectedBitmap = bitmap
            Glide.with(this).load(bitmap).into(binding.selectedImagePreview)
            binding.helperText.text =
                "Imagem anexada. Vamos usar como contexto junto do proximo prompt."
        } catch (e: Exception) {
            clearSelectedImage()
            binding.helperText.text = "Nao foi possivel carregar a imagem."
        }
    }

    private fun clearSelectedImage() {
        selectedBitmap = null
        binding.selectedImagePreview.setImageResource(android.R.drawable.gallery_thumb)
        binding.helperText.text =
            "Modelo: Gemini 2.0 Flash - Mantemos o historico da conversa e usamos imagem quando anexada."
    }

    private fun setLoading(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSend.isEnabled = !isLoading
    }

    private fun scrollToBottom() {
        binding.chatRecycler.post {
            val lastIndex = adapter.itemCount - 1
            if (lastIndex >= 0) {
                binding.chatRecycler.scrollToPosition(lastIndex)
            }
        }
    }
}
