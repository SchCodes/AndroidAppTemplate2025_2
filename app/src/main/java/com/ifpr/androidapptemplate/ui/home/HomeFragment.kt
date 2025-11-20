package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import com.ifpr.androidapptemplate.ui.ai.AiLogicActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var itensReference: DatabaseReference

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()

        inicializaGerenciamentoLocalizacao()
        binding.mapsButton.setOnClickListener { openMap() }
        binding.fabAi.setOnClickListener {
            startActivity(Intent(requireContext(), AiLogicActivity::class.java))
        }

        carregarAnalisesDoUsuario()

        return binding.root
    }

    private fun inicializaGerenciamentoLocalizacao() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
        } else {
            getCurrentLocation()
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Snackbar.make(
                    requireView(),
                    "Permission denied. Cannot access location.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    lastLocation = location
                    displayAddress(location)
                }
            }
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 30_000L)
            .setMinUpdateIntervalMillis(30_000L)
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback as LocationCallback,
            Looper.getMainLooper()
        )
    }

    private fun displayAddress(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                withContext(Dispatchers.Main) {
                    binding.currentAddressTextView.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.currentAddressTextView.text = "Error: ${e.message}"
                }
            }
        }
    }

    private fun openMap() {
        val location = lastLocation
        if (location == null) {
            Toast.makeText(requireContext(), R.string.location_unavailable, Toast.LENGTH_SHORT)
                .show()
            return
        }

        val uri = "geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(Minha+Localização)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    override fun onDestroyView() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        _binding = null
        super.onDestroyView()
    }

    private fun carregarAnalisesDoUsuario() {
        val usuarioAtual = auth.currentUser

        if (usuarioAtual == null) {
            binding.homeEmptyStateText.visibility = View.VISIBLE
            binding.homeEmptyStateText.text = getString(R.string.home_empty_state)
            Toast.makeText(
                requireContext(),
                "Sessão expirada. Faça login novamente.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        itensReference = FirebaseDatabase.getInstance()
            .getReference("itens")
            .child(usuarioAtual.uid)

        itensReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itens = mutableListOf<Item>()

                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(Item::class.java)
                    if (item != null) {
                        itens += item
                    }
                }

                itens.sortByDescending { it.createdAt }
                exibirItens(itens)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Erro ao carregar análises.",
                    Toast.LENGTH_SHORT
                ).show()
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

            titleView.text = when {
                item.titulo.isNotBlank() -> item.titulo
                else -> getString(R.string.item_title_placeholder)
            }

            categoryView.text = item.categoria?.takeIf { it.isNotBlank() }
                ?: getString(R.string.item_category_placeholder)

            numbersView.text = if (item.dezenas.isNotBlank()) {
                "Dezenas: ${item.dezenas}"
            } else {
                getString(R.string.item_numbers_placeholder)
            }

            probabilityView.text = item.probabilidade?.let { prob ->
                "Probabilidade: %.2f%%".format(prob)
            } ?: getString(R.string.item_probability_placeholder)

            concursoView.text = item.concursoReferencia?.let { numero ->
                "Concurso: $numero"
            } ?: getString(R.string.item_concurso_placeholder)

            if (!item.observacoes.isNullOrBlank()) {
                notesView.visibility = View.VISIBLE
                notesView.text = "Observações: ${item.observacoes}"
            } else {
                notesView.visibility = View.GONE
            }

            when {
                !item.imageUrl.isNullOrEmpty() -> {
                    Glide.with(this@HomeFragment)
                        .load(item.imageUrl)
                        .centerCrop()
                        .into(imageView)
                }

                !item.base64Image.isNullOrEmpty() -> {
                    try {
                        val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        imageView.setImageBitmap(bitmap)
                    } catch (_: Exception) {
                        imageView.setImageResource(R.drawable.ic_profile_avatar)
                    }
                }

                else -> imageView.setImageResource(R.drawable.ic_profile_avatar)
            }

            container.addView(itemView)
        }
    }
}
