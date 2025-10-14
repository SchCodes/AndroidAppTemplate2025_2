package com.ifpr.androidapptemplate.ui.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.google.android.material.imageview.ShapeableImageView

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        carregarItensAnalise()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun carregarItensAnalise() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itens = mutableListOf<Item>()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java)
                        if (item != null) {
                            itens += item
                        }
                    }
                }

                itens.sortByDescending { it.createdAt }
                exibirItens(itens)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun exibirItens(itens: List<Item>) {
        val container: LinearLayout = binding.itemContainer
        container.removeAllViews()

        if (itens.isEmpty()) {
            binding.homeEmptyStateText.visibility = View.VISIBLE
            return
        }

        binding.homeEmptyStateText.visibility = View.GONE

        itens.forEach { item ->
            val itemView = layoutInflater.inflate(R.layout.item_template, container, false)

            val imageView = itemView.findViewById<ShapeableImageView>(R.id.item_image)
            val titleView = itemView.findViewById<TextView>(R.id.item_title)
            val categoryView = itemView.findViewById<TextView>(R.id.item_category)
            val numbersView = itemView.findViewById<TextView>(R.id.item_numbers)
            val probabilityView = itemView.findViewById<TextView>(R.id.item_probability)
            val concursoView = itemView.findViewById<TextView>(R.id.item_concurso)
            val notesView = itemView.findViewById<TextView>(R.id.item_notes)

            val titulo = item.titulo.takeIf { it.isNotBlank() }
                ?: getString(R.string.item_title_placeholder)
            titleView.text = titulo

            categoryView.text = item.categoria?.takeIf { it.isNotBlank() }
                ?: getString(R.string.item_category_placeholder)

            numbersView.text = if (item.dezenas.isNotBlank()) {
                "Dezenas: ${item.dezenas}"
            } else {
                getString(R.string.item_numbers_placeholder)
            }

            probabilityView.text = item.probabilidade?.let {
                "Probabilidade: %.2f%%".format(it)
            } ?: getString(R.string.item_probability_placeholder)

            concursoView.text = item.concursoReferencia?.let {
                "Concurso: $it"
            } ?: getString(R.string.item_concurso_placeholder)

            if (!item.observacoes.isNullOrBlank()) {
                notesView.visibility = View.VISIBLE
                notesView.text = "Observacoes: ${item.observacoes}".trim()
            } else {
                notesView.visibility = View.GONE
            }

            when {
                !item.imageUrl.isNullOrEmpty() ->
                    Glide.with(this@HomeFragment).load(item.imageUrl).centerCrop().into(imageView)

                !item.base64Image.isNullOrEmpty() ->
                    try {
                        val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.setImageBitmap(bitmap)
                    } catch (_: Exception) {
                        imageView.setImageResource(R.drawable.ic_profile_avatar)
                    }

                else -> imageView.setImageResource(R.drawable.ic_profile_avatar)
            }

            container.addView(itemView)
        }
    }
}
