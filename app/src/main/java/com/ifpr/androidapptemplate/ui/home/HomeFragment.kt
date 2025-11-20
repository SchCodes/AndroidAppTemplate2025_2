package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
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
import android.widget.Button
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
    private var lastKnownLocation: Location? = null
    private var lastKnownLocality: String? = null
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
        setupLotterySection()

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
        lastKnownLocation = location
        if (!Geocoder.isPresent()) {
            binding.currentAddressTextView.text = formatCoordinates(location)
            return
        }

        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 3)
                val firstAddress = addresses?.firstOrNull { !it.getAddressLine(0).isNullOrBlank() }
                val resolvedAddress = firstAddress?.getAddressLine(0)
                lastKnownLocality = firstAddress?.locality
                    ?: firstAddress?.subAdminArea
                    ?: firstAddress?.subLocality

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

    private fun setupLotterySection() {
        binding.lotterySearchButton.setOnClickListener {
            buscarLotericasProximas()
        }
    }

    private fun buscarLotericasProximas() {
        if (!Geocoder.isPresent()) {
            Snackbar.make(binding.root, R.string.lottery_geocoder_error, Snackbar.LENGTH_LONG).show()
            return
        }

        val currentLocation = lastKnownLocation
        if (currentLocation == null) {
            Snackbar.make(binding.root, R.string.lottery_error_no_location, Snackbar.LENGTH_LONG).show()
            return
        }

        showLotteryLoading(true)
        binding.lotteryEmptyStateText.visibility = View.GONE
        binding.lotteryListContainer.removeAllViews()

        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))
        val baseKeywords = listOf(
            "Lotérica",
            "Lotérica Caixa",
            "Loterica",
            "Loterias",
            "Casa Lotérica",
            "Caixa Lotérica",
            "Unidade Lotérica"
        )
        val dynamicKeywords = mutableSetOf<String>().apply {
            addAll(baseKeywords)
            lastKnownLocality?.let { city ->
                add("Lotérica $city")
                add("Casa Lotérica $city")
                add("Loteria Caixa $city")
            }
        }
        val radiusStepsKm = listOf(5.0, 10.0, 20.0, 40.0, 80.0, 150.0, 250.0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val seenCoordinates = mutableSetOf<String>()
                var lotteries: List<NearbyLottery> = emptyList()

                run breaking@{
                    radiusStepsKm.forEach { radiusKm ->
                        val foundForRadius = mutableListOf<NearbyLottery>()
                        val delta = radiusKm / 111.0
                        val maxAllowedDistanceKm = radiusKm + 5

                        dynamicKeywords.forEach { keyword ->
                            val addresses = geocoder.getFromLocationName(
                                keyword,
                                5,
                                currentLocation.latitude - delta,
                                currentLocation.longitude - delta,
                                currentLocation.latitude + delta,
                                currentLocation.longitude + delta
                            )

                            addresses?.forEach { address ->
                                val lat = address.latitude
                                val lon = address.longitude
                                if (lat == 0.0 && lon == 0.0) return@forEach

                                val key = "%.5f|%.5f".format(lat, lon)
                                if (!seenCoordinates.add(key)) return@forEach

                                val loteriaLocation = Location("geocoder").apply {
                                    latitude = lat
                                    longitude = lon
                                }

                                val distanceKm = currentLocation.distanceTo(loteriaLocation) / 1000f
                                if (distanceKm > maxAllowedDistanceKm) return@forEach
                                val name = address.featureName?.takeIf { it.isNotBlank() } ?: "Lotérica"
                                val formattedAddress = address.getAddressLine(0) ?: getString(R.string.item_address_placeholder)

                                foundForRadius += NearbyLottery(
                                    name = name,
                                    address = formattedAddress,
                                    latitude = lat,
                                    longitude = lon,
                                    distanceKm = distanceKm.toDouble()
                                )
                            }
                        }

                        if (foundForRadius.isNotEmpty()) {
                            lotteries = foundForRadius.sortedBy { it.distanceKm }
                            return@breaking
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    showLotteryLoading(false)
                    renderLotteryResults(lotteries)
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    showLotteryLoading(false)
                    Snackbar.make(binding.root, R.string.lottery_geocoder_error, Snackbar.LENGTH_LONG).show()
                    renderLotteryResults(emptyList())
                }
            }
        }
    }

    private fun showLotteryLoading(isLoading: Boolean) {
        binding.lotteryProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.lotterySearchButton.isEnabled = !isLoading
    }

    private fun renderLotteryResults(lotteries: List<NearbyLottery>) {
        val container = binding.lotteryListContainer
        container.removeAllViews()

        if (lotteries.isEmpty()) {
            binding.lotteryEmptyStateText.visibility = View.VISIBLE
            return
        }

        binding.lotteryEmptyStateText.visibility = View.GONE

        lotteries.forEach { lottery ->
            val itemView = layoutInflater.inflate(R.layout.item_nearby_lottery, container, false)
            val nameView = itemView.findViewById<TextView>(R.id.lotteryNameTextView)
            val addressView = itemView.findViewById<TextView>(R.id.lotteryAddressTextView)
            val distanceView = itemView.findViewById<TextView>(R.id.lotteryDistanceTextView)
            val openMapsButton = itemView.findViewById<Button>(R.id.openInMapsButton)

            nameView.text = lottery.name
            addressView.text = lottery.address
            distanceView.text = getString(R.string.lottery_distance_format, lottery.distanceKm)
            openMapsButton.text = getString(R.string.lottery_open_maps)
            openMapsButton.setOnClickListener { openInMaps(lottery) }

            container.addView(itemView)
        }
    }

    private fun openInMaps(lottery: NearbyLottery) {
        val chooserTitle = getString(R.string.lottery_open_maps)
        val geoUri = Uri.parse("geo:${lottery.latitude},${lottery.longitude}?q=${lottery.latitude},${lottery.longitude}(${Uri.encode(lottery.name)})")
        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)

        try {
            startActivity(Intent.createChooser(geoIntent, chooserTitle))
            return
        } catch (_: ActivityNotFoundException) {
        }

        val httpsQuery = Uri.encode("${lottery.latitude},${lottery.longitude} (${lottery.name})")
        val httpsUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$httpsQuery")
        val httpsIntent = Intent(Intent.ACTION_VIEW, httpsUri).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }

        try {
            startActivity(Intent.createChooser(httpsIntent, chooserTitle))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_maps_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private data class NearbyLottery(
        val name: String,
        val address: String,
        val latitude: Double,
        val longitude: Double,
        val distanceKm: Double
    )

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
