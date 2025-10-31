package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.util.Base64
import android.widget.*
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.baseclasses.Item
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private lateinit var currentAddressTextView: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var lastLocation: Location? = null

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        inicializaGerenciamentoLocalizacao(view)

        val container = view.findViewById<LinearLayout>(R.id.itemContainer)
        carregarItensMarketplace(container)

        view.findViewById<Button>(R.id.mapsButton).setOnClickListener {
            openMap()
        }

        return view
    }

    private fun inicializaGerenciamentoLocalizacao(view: View) {
        currentAddressTextView = view.findViewById(R.id.currentAddressTextView)

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

        locationRequest = LocationRequest.create().apply {
            interval = 30000 // Intervalo em milissegundos para atualizacoes de localizacao
            fastestInterval =
                30000 // O menor intervalo de tempo para receber atualizacoes de localizacao
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun displayAddress(location: Location) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val address = addresses?.firstOrNull()?.getAddressLine(0) ?: "Address not found"
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = address
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    currentAddressTextView.text = "Error: ${e.message}"
                }
            }
        }
    }

    private fun openMap() {
        lastLocation?.let {
            val uri = "geo:${it.latitude},${it.longitude}?q=${it.latitude},${it.longitude}(Minha+Localização)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.setPackage("com.google.android.apps.maps")
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        _binding = null
    }

    fun carregarItensMarketplace(container: LinearLayout) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("itens")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                container.removeAllViews()

                for (userSnapshot in snapshot.children) {
                    for (itemSnapshot in userSnapshot.children) {
                        val item = itemSnapshot.getValue(Item::class.java) ?: continue

                        val itemView = LayoutInflater.from(container.context)
                            .inflate(R.layout.item_template, container, false)

                        val imageView = itemView.findViewById<ImageView>(R.id.item_image)
                        val enderecoView = itemView.findViewById<TextView>(R.id.item_endereco)

                        enderecoView.text = "Endereço: ${item.endereco ?: "Não informado"}"

                        if (!item.imageUrl.isNullOrEmpty()) {
                            Glide.with(container.context).load(item.imageUrl).into(imageView)
                        } else if (!item.base64Image.isNullOrEmpty()) {
                            try {
                                val bytes = Base64.decode(item.base64Image, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                imageView.setImageBitmap(bitmap)
                            } catch (_: Exception) {}
                        }

                        container.addView(itemView)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(container.context, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
