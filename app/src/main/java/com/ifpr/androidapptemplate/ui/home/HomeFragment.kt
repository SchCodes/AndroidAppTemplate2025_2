package com.ifpr.androidapptemplate.ui.home

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ifpr.androidapptemplate.R
import com.ifpr.androidapptemplate.data.lottery.LocalDraw
import com.ifpr.androidapptemplate.data.lottery.LotofacilSyncRepository
import com.ifpr.androidapptemplate.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val safeBinding get() = _binding

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val syncRepo by lazy { LotofacilSyncRepository(requireContext()) }
    private val dbRef: DatabaseReference by lazy { FirebaseDatabase.getInstance().getReference("bets") }
    private var betsListener: ValueEventListener? = null
    private var betsRefForUser: DatabaseReference? = null

    private var suggestedBet: List<Int> = emptyList()
    private var savedBets: List<SavedBet> = emptyList()

    // localização
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSettingsRequest: LocationSettingsRequest
    private var lastKnownLocation: Location? = null
    private var lastKnownLocality: String? = null
    private val locationSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) startLocationUpdates()
        else withBinding { currentAddressTextView.text = getString(R.string.location_settings_disabled) }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 101
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.useSuggestionButton.visibility = View.GONE
        binding.saveBetButton.setOnClickListener { salvarJogoDigitado() }
        binding.saveSuggestedButton.visibility = View.GONE
        binding.lotterySearchButton.setOnClickListener { buscarLotericasProximas() }

        inicializaLocalizacao()
        carregarDados()
        carregarBets()

        return binding.root
    }

    override fun onDestroyView() {
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        betsListener?.let { listener ->
            betsRefForUser?.removeEventListener(listener)
        }
        betsListener = null
        betsRefForUser = null
        _binding = null
        super.onDestroyView()
    }

    private fun carregarDados() {
        mostrarLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                syncRepo.syncIfNeeded()
                val bundle = syncRepo.readLocalBundle()
                if (bundle == null) {
                    showError("Não encontrei os dados locais. Tente novamente.")
                    return@launch
                }
                val lastDraw = bundle.draws.firstOrNull()
                suggestedBet = bundle.rawStats.toSuggestedBet()
                renderLastDraw(lastDraw)
                renderSugestao()
                renderSavedBets(savedBets)
                withBinding { errorText.visibility = View.GONE }
            } catch (e: Exception) {
                showError("Erro ao carregar dados: ${e.message}")
            } finally {
                mostrarLoading(false)
            }
        }
    }

    private fun carregarBets() {
        betsListener?.let { listener ->
            betsRefForUser?.removeEventListener(listener)
        }
        val user = auth.currentUser ?: run {
            renderSavedBets(emptyList())
            return
        }
        val node = dbRef.child(user.uid)
        betsRefForUser = node
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SavedBet>()
                snapshot.children.forEach { child ->
                    val numbers = child.child("numbers").children.mapNotNull { it.getValue(Int::class.java) }
                    if (numbers.size != 15) return@forEach
                    val createdAt = child.child("createdAt").getValue(Long::class.java) ?: 0L
                    val source = child.child("source").getValue(String::class.java) ?: "usuario"
                    list += SavedBet(
                        id = child.key ?: "",
                        numbers = numbers.sorted(),
                        createdAt = createdAt,
                        source = source
                    )
                }
                savedBets = list.sortedByDescending { it.createdAt }
                renderSavedBets(savedBets)
            }

            override fun onCancelled(error: DatabaseError) {
                renderSavedBets(emptyList())
            }
        }
        betsListener = listener
        node.addValueEventListener(listener)
    }

    private fun renderLastDraw(draw: LocalDraw?) {
        if (draw == null) {
            withBinding { lastDrawCard.visibility = View.GONE }
            return
        }
        withBinding {
            lastDrawCard.visibility = View.VISIBLE
            lastDrawTitle.text = "Concurso ${draw.id}"
            lastDrawSubtitle.text = formatDateBR(draw.date)
        }
        safeBinding?.let { preencherChips(it.lastDrawChipGroup, draw.numbers) }
    }

    private fun renderSugestao() {
        if (suggestedBet.isEmpty()) {
            withBinding { suggestionText.text = getString(R.string.home_no_suggestion) }
            return
        }
        withBinding { suggestionText.text = suggestedBet.joinToString(", ") { it.toString().padStart(2, '0') } }
    }

    private fun renderSavedBets(bets: List<SavedBet>) {
        val binding = safeBinding ?: return
        val last = bets.firstOrNull()
        if (last == null) {
            binding.savedBetsCard.visibility = View.GONE
            return
        }
        binding.savedBetsCard.visibility = View.VISIBLE
        binding.savedBetsContainer.removeAllViews()
        val spacing = (8 * resources.displayMetrics.density).toInt()
        val chipGroup = ChipGroup(requireContext()).apply {
            isSingleLine = false
            chipSpacingHorizontal = spacing
            chipSpacingVertical = spacing
        }
        last.numbers.forEach { n ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Entry).apply {
                text = n.toString().padStart(2, '0')
                isCheckable = false
                isClickable = false
                setChipBackgroundColorResource(R.color.caixa_chip_bg)
                setChipStrokeColorResource(R.color.caixa_azul)
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
            }
            chipGroup.addView(chip)
        }
        binding.savedBetsContainer.addView(chipGroup)
    }

        private fun preencherSugestao() {
        if (suggestedBet.isEmpty()) return
        withBinding {
            betInput.setText(suggestedBet.joinToString(",") { it.toString().padStart(2, '0') })
            saveStatus.text = "Sugest?o preenchida. Edite se quiser e salve."
            saveStatus.visibility = View.VISIBLE
        }
    }

    private fun salvarJogoDigitado() {
        val numeros = safeBinding?.let { parseEntrada(it.betInput) } ?: return
        if (numeros == null) {
            showErrorEntrada()
            return
        }
        salvarJogo(numeros, "usuario")
    }

private fun salvarSugestao() {
        if (suggestedBet.isEmpty()) {
            Toast.makeText(requireContext(), "Não há sugestão disponível.", Toast.LENGTH_SHORT).show()
            return
        }
        salvarJogo(suggestedBet, "sugestao_app")
    }

    private fun salvarJogo(numeros: List<Int>, origem: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Faça login para salvar jogos.", Toast.LENGTH_SHORT).show()
            return
        }
        val betId = dbRef.child(user.uid).push().key ?: Instant.now().toString()
        val payload = mapOf(
            "numbers" to numeros,
            "createdAt" to System.currentTimeMillis(),
            "source" to origem
        )
        dbRef.child(user.uid).child(betId).setValue(payload)
            .addOnSuccessListener {
                withBinding {
                    saveStatus.visibility = View.VISIBLE
                    saveStatus.text = "Jogo salvo!"
                }
            }
            .addOnFailureListener {
                withBinding {
                    saveStatus.visibility = View.VISIBLE
                    saveStatus.text = "Falha ao salvar: ${it.message}"
                }
            }
    }

    private fun parseEntrada(input: TextInputEditText): List<Int>? {
        val raw = input.text?.toString() ?: return null
        val numeros = raw.split(",", " ", ";")
            .mapNotNull { it.trim().takeIf { t -> t.isNotEmpty() }?.toIntOrNull() }
        if (numeros.size != 15) return null
        if (numeros.any { it !in 1..25 }) return null
        if (numeros.distinct().size != numeros.size) return null
        return numeros.sorted()
    }

    private fun showErrorEntrada() {
        withBinding {
            saveStatus.visibility = View.VISIBLE
            saveStatus.text = "Informe 15 números entre 1 e 25, separados por vírgula ou espaço."
        }
    }

    private fun mostrarLoading(isLoading: Boolean) {
        withBinding {
            heroLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            saveBetButton.isEnabled = !isLoading
            saveSuggestedButton.isEnabled = !isLoading
        }
    }

    private fun showError(msg: String) {
        withBinding {
            errorText.visibility = View.VISIBLE
            errorText.text = msg
        }
    }

    private fun preencherChips(group: ChipGroup, numeros: List<Int>) {
        group.removeAllViews()
        numeros.forEach { n ->
            val chip = Chip(requireContext(), null, com.google.android.material.R.style.Widget_MaterialComponents_Chip_Entry).apply {
                text = n.toString().padStart(2, '0')
                isCheckable = false
                isClickable = false
                setChipStrokeColorResource(R.color.caixa_azul)
                chipStrokeWidth = resources.getDimension(R.dimen.chip_stroke_width)
            }
            group.addView(chip)
        }
    }

    private fun formatDateBR(raw: String?): String {
        return try {
            val parsed = LocalDate.parse(raw, DateTimeFormatter.ISO_DATE)
            parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {
            raw ?: "--"
        }
    }

    // --- localização e lotéricas ---
    private fun inicializaLocalizacao() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000L)
            .setMinUpdateIntervalMillis(5_000L)
            .setWaitForAccurateLocation(true)
            .build()
        locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)
            .build()
        solicitarPermissaoLocalizacao()
    }

    private fun solicitarPermissaoLocalizacao() {
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
            val granted = grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (granted) getCurrentLocation() else withBinding { currentAddressTextView.text = getString(R.string.location_permission_denied) }
        }
    }

    private fun getCurrentLocation() {
        if (!hasLocationPermission()) return
        withBinding { currentAddressTextView.text = getString(R.string.loading_address) }
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener { startLocationUpdates() }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        val intent = IntentSenderRequest.Builder(exception.resolution).build()
                        locationSettingsLauncher.launch(intent)
                    } catch (_: Exception) {
                        withBinding { currentAddressTextView.text = getString(R.string.location_settings_disabled) }
                    }
                } else {
                    withBinding { currentAddressTextView.text = getString(R.string.location_settings_disabled) }
                }
            }
    }

    private fun hasLocationPermission(): Boolean {
        val ctx = requireContext()
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == android.content.pm.PackageManager.PERMISSION_GRANTED || coarse == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun startLocationUpdates() {
        if (!hasLocationPermission()) return
        locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { displayAddress(it) }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback as LocationCallback, Looper.getMainLooper())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc -> loc?.let { displayAddress(it) } }
            .addOnFailureListener {
                withBinding { currentAddressTextView.text = getString(R.string.location_unavailable) }
            }
    }

    private fun displayAddress(location: Location) {
        lastKnownLocation = location
        if (!Geocoder.isPresent()) {
            withBinding { currentAddressTextView.text = formatCoordinates(location) }
            return
        }
        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 3)
                val first = addresses?.firstOrNull { !it.getAddressLine(0).isNullOrBlank() }
                val resolved = first?.getAddressLine(0)
                lastKnownLocality = first?.locality ?: first?.subAdminArea ?: first?.subLocality
                withContext(Dispatchers.Main) {
                    withBinding { currentAddressTextView.text = resolved ?: formatCoordinates(location) }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    withBinding { currentAddressTextView.text = formatCoordinates(location) }
                }
            }
        }
    }

    private fun formatCoordinates(location: Location): String =
        getString(R.string.current_coordinates, location.latitude, location.longitude)

    private fun buscarLotericasProximas() {
        val currentLocation = lastKnownLocation
        if (currentLocation == null) {
            withBinding { currentAddressTextView.text = getString(R.string.location_unavailable) }
            return
        }
        showLotteryLoading(true)
        withBinding {
            lotteryEmptyStateText.visibility = View.GONE
            lotteryListContainer.removeAllViews()
        }
        val geocoder = Geocoder(requireContext(), Locale("pt", "BR"))
        val keywords = listOf(
            "Lotérica",
            "Lotérica Caixa",
            "Casa Lotérica",
            "Loterias Caixa",
            "Unidade Lotérica"
        ).toMutableSet()
        lastKnownLocality?.let { city ->
            keywords.add("Lotérica $city")
            keywords.add("Casa Lotérica $city")
        }
        val radiusStepsKm = listOf(5.0, 10.0, 20.0, 40.0, 80.0)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val seen = mutableSetOf<String>()
                var lotteries: List<NearbyLottery> = emptyList()
                run breaking@{
                    radiusStepsKm.forEach { radiusKm ->
                        val found = mutableListOf<NearbyLottery>()
                        val delta = radiusKm / 111.0
                        val maxAllowed = radiusKm + 5
                        keywords.forEach { keyword ->
                            val addresses = geocoder.getFromLocationName(
                                keyword,
                                5,
                                currentLocation.latitude - delta,
                                currentLocation.longitude - delta,
                                currentLocation.latitude + delta,
                                currentLocation.longitude + delta
                            )
                            addresses?.forEach { addr ->
                                val lat = addr.latitude
                                val lon = addr.longitude
                                if (lat == 0.0 && lon == 0.0) return@forEach
                                val key = "%.5f|%.5f".format(lat, lon)
                                if (!seen.add(key)) return@forEach
                                val lotLoc = Location("geo").apply {
                                    latitude = lat
                                    longitude = lon
                                }
                                val distKm = currentLocation.distanceTo(lotLoc) / 1000f
                                if (distKm > maxAllowed) return@forEach
                                val name = addr.featureName?.takeIf { it.isNotBlank() } ?: "Lotérica"
                                val formatted = addr.getAddressLine(0) ?: getString(R.string.item_address_placeholder)
                                found += NearbyLottery(name, formatted, lat, lon, distKm.toDouble())
                            }
                        }
                        if (found.isNotEmpty()) {
                            lotteries = found.sortedBy { it.distanceKm }
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
                    renderLotteryResults(emptyList())
                }
            }
        }
    }

    private fun showLotteryLoading(isLoading: Boolean) {
        withBinding {
            lotteryProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            lotterySearchButton.isEnabled = !isLoading
        }
    }

    private fun renderLotteryResults(lotteries: List<NearbyLottery>) {
        val container = safeBinding?.lotteryListContainer ?: return
        container.removeAllViews()
        if (lotteries.isEmpty()) {
            withBinding { lotteryEmptyStateText.visibility = View.VISIBLE }
            return
        }
        withBinding { lotteryEmptyStateText.visibility = View.GONE }
        lotteries.take(5).forEach { lot ->
            val itemView = layoutInflater.inflate(R.layout.item_nearby_lottery, container, false)
            val nameView = itemView.findViewById<android.widget.TextView>(R.id.lotteryNameTextView)
            val addressView = itemView.findViewById<android.widget.TextView>(R.id.lotteryAddressTextView)
            val distanceView = itemView.findViewById<android.widget.TextView>(R.id.lotteryDistanceTextView)
            val openMapsButton = itemView.findViewById<android.widget.Button>(R.id.openInMapsButton)
            nameView.text = lot.name
            addressView.text = lot.address
            distanceView.text = getString(R.string.lottery_distance_format, lot.distanceKm)
            openMapsButton.setOnClickListener { openInMaps(lot) }
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
        val httpsIntent = Intent(Intent.ACTION_VIEW, httpsUri).apply { addCategory(Intent.CATEGORY_BROWSABLE) }
        try {
            startActivity(Intent.createChooser(httpsIntent, chooserTitle))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(requireContext(), R.string.no_maps_app_found, Toast.LENGTH_SHORT).show()
        }
    }
}

private fun Map<String, Any?>.toSuggestedBet(): List<Int> {
    val freqRaw = this["frequencia_absoluta"]
    val freq = (freqRaw as? Map<*, *>)?.entries?.mapNotNull { (k, v) ->
        val num = (k as? String)?.toIntOrNull() ?: (k as? Number)?.toInt()
        val count = (v as? Number)?.toInt()
        if (num != null && count != null) num to count else null
    }?.toMap() ?: emptyMap()
    if (freq.isEmpty()) return emptyList()
    return freq.entries.sortedByDescending { it.value }.take(15).map { it.key }
}

private data class SavedBet(
    val id: String,
    val numbers: List<Int>,
    val createdAt: Long,
    val source: String
)

private data class NearbyLottery(
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val distanceKm: Double
)

private inline fun HomeFragment.withBinding(block: FragmentHomeBinding.() -> Unit) {
    val b = this._binding ?: return
    b.block()
}
