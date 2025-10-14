package com.ifpr.androidapptemplate.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this)[DashboardViewModel::class.java]

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        viewModel.text.observe(viewLifecycleOwner) { binding.textDashboard.text = it }

        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("itens")

        binding.buttonSelectImage.setOnClickListener { openFileChooser() }
        binding.salvarItemButton.setOnClickListener { salvarItem() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, null), PICK_IMAGE_REQUEST)
    }

    private fun salvarItem() {
        val titulo = binding.titleEditText.text?.toString()?.trim().orEmpty()
        val dezenasInput = binding.numbersEditText.text?.toString()?.trim().orEmpty()
        val categoriaInput = binding.categoryEditText.text?.toString()?.trim().orEmpty()
        val probabilityInput = binding.probabilityEditText.text?.toString()?.trim()
        val concursoInput = binding.concursoEditText.text?.toString()?.trim()
        val observacoesInput = binding.notesEditText.text?.toString()?.trim().orEmpty()

        if (titulo.isEmpty()) {
            Toast.makeText(context, "Informe um nome para a analise", Toast.LENGTH_SHORT).show()
            return
        }

        if (dezenasInput.isEmpty()) {
            Toast.makeText(context, "Digite as dezenas analisadas", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri == null) {
            Toast.makeText(context, "Selecione uma imagem ilustrativa", Toast.LENGTH_SHORT).show()
            return
        }

        val dezenasNormalizadas = normalizarDezenas(dezenasInput)
        if (dezenasNormalizadas.isEmpty()) {
            Toast.makeText(context, "Nao foi possivel interpretar as dezenas", Toast.LENGTH_SHORT).show()
            return
        }

        val probabilidade = probabilityInput
            ?.takeIf { it.isNotEmpty() }
            ?.replace(',', '.')
            ?.toDoubleOrNull()
        if (probabilityInput?.isNotEmpty() == true && probabilidade == null) {
            Toast.makeText(context, "Informe uma probabilidade valida", Toast.LENGTH_SHORT).show()
            return
        }

        val concursoReferencia = concursoInput
            ?.takeIf { it.isNotEmpty() }
            ?.toIntOrNull()
        if (concursoInput?.isNotEmpty() == true && concursoReferencia == null) {
            Toast.makeText(context, "Informe um numero de concurso valido", Toast.LENGTH_SHORT).show()
            return
        }

        val dadosPendentes = PendingItem(
            titulo = titulo,
            dezenas = dezenasNormalizadas,
            categoria = categoriaInput.ifBlank { null },
            probabilidade = probabilidade,
            concursoReferencia = concursoReferencia,
            observacoes = observacoesInput.ifBlank { null }
        )

        uploadImageAndSave(dadosPendentes)
    }

    private fun uploadImageAndSave(pendingItem: PendingItem) {
        val uri = imageUri ?: return
        val inputStream = context?.contentResolver?.openInputStream(uri)
        val bytes = inputStream?.use { it.readBytes() }

        if (bytes == null) {
            Toast.makeText(context, "Nao foi possivel processar a imagem", Toast.LENGTH_SHORT).show()
            return
        }

        val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)
        val item = Item(
            titulo = pendingItem.titulo,
            dezenas = pendingItem.dezenas,
            categoria = pendingItem.categoria,
            probabilidade = pendingItem.probabilidade,
            concursoReferencia = pendingItem.concursoReferencia,
            observacoes = pendingItem.observacoes,
            base64Image = base64Image
        )

        saveItemIntoDatabase(item)
    }

    private fun saveItemIntoDatabase(item: Item) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Sessao expirada. Faça login novamente", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = databaseReference.child(currentUser.uid)
        val itemRef = userRef.push()

        itemRef.setValue(item)
            .addOnSuccessListener {
                Toast.makeText(context, "Analise salva com sucesso!", Toast.LENGTH_SHORT).show()
                limparFormulario()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Falha ao salvar a analise", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limparFormulario() {
        binding.titleEditText.text?.clear()
        binding.numbersEditText.text?.clear()
        binding.categoryEditText.text?.clear()
        binding.probabilityEditText.text?.clear()
        binding.concursoEditText.text?.clear()
        binding.notesEditText.text?.clear()
        binding.imageItem.setImageResource(com.ifpr.androidapptemplate.R.drawable.ic_profile_avatar)
        imageUri = null
    }

    private fun normalizarDezenas(input: String): String {
        val tokens = input
            .replace("[^0-9]".toRegex(), " ")
            .trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }

        if (tokens.isEmpty()) return ""

        return tokens.joinToString(separator = ",") { numero ->
            numero.take(2).padStart(2, '0')
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val uri = data?.data ?: return
            imageUri = uri
            Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(binding.imageItem)
        }
    }

    private data class PendingItem(
        val titulo: String,
        val dezenas: String,
        val categoria: String?,
        val probabilidade: Double?,
        val concursoReferencia: Int?,
        val observacoes: String?
    )
}
