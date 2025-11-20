package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
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
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startLocationUpdates()
        } else {
            _binding?.currentAddressTextView?.text = getString(R.string.location_settings_disabled)
        }
    }

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
        carregarAnalisesDoUsuario()

        return binding.root
    }

    private fun inicializaGerenciamentoLocalizacao() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setWaitForAccurateLocation(true)
            .build()

        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()

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
                    R.string.location_permission_denied,
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

        binding.currentAddressTextView.text = getString(R.string.loading_address)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                startLocationUpdates()
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intent = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intent)
                    } catch (_: Exception) {
                        binding.currentAddressTextView.text = getString(R.string.location_settings_disabled)
                    }
                } else {
                    binding.currentAddressTextView.text = getString(R.string.location_settings_disabled)
                }
            }
    }

    private fun startLocationUpdates() {
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

        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    displayAddress(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback as LocationCallback,
            Looper.getMainLooper()
        )

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    displayAddress(location)
                } else {
                    binding.currentAddressTextView.text = getString(R.string.waiting_gps_fix)
                }
            }
            .addOnFailureListener {
                binding.currentAddressTextView.text = getString(R.string.address_error, it.message ?: "")
            }
    }

    private fun displayAddress(location: Location) {
        if (!Geocoder.isPresent()) {
            binding.currentAddressTextView.text = formatCoordinates(location)
            return
        }

        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 3)
                val resolvedAddress = addresses
                    ?.firstOrNull { !it.getAddressLine(0).isNullOrBlank() }
                    ?.getAddressLine(0)

                withContext(Dispatchers.Main) {
                    binding.currentAddressTextView.text = resolvedAddress ?: formatCoordinates(location)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.currentAddressTextView.text = formatCoordinates(location)
                }
            }
        }
    }

    private fun formatCoordinates(location: Location): String {
        return getString(R.string.current_coordinates, location.latitude, location.longitude)
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
