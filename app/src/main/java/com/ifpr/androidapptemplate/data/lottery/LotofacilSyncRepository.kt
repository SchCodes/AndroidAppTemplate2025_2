package com.ifpr.androidapptemplate.data.lottery

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Responsável por:
 * - Ler metadados do RTDB (/metadata/latest)
 * - Baixar processed/draws.json do Storage para o armazenamento interno
 * - Ler e parsear o bundle local para uso nos cálculos offline
 */
class LotofacilSyncRepository(
    context: Context,
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val localBundleFile: File =
        File(context.filesDir, "lotofacil/draws.json")

    suspend fun fetchRemoteMetadata(): RemoteMetadata? = suspendCancellableCoroutine { cont ->
        database.getReference("metadata/latest")
            .get()
            .addOnSuccessListener { snap ->
                val map = snap.value as? Map<*, *>
                if (map == null) {
                    cont.resume(null)
                    return@addOnSuccessListener
                }
                cont.resume(map.toRemoteMetadata())
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun downloadBundle(bucketUrl: String = DEFAULT_BUCKET_URL): File =
        suspendCancellableCoroutine { cont ->
            localBundleFile.parentFile?.mkdirs()
            val ref = if (bucketUrl.isNotBlank()) {
                storage.getReferenceFromUrl(bucketUrl).child(BUNDLE_PATH)
            } else {
                storage.reference.child(BUNDLE_PATH)
            }

            ref.getFile(localBundleFile)
                .addOnSuccessListener { cont.resume(localBundleFile) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }

    fun readLocalBundle(): LocalBundle? {
        if (!localBundleFile.exists()) return null
        val text = localBundleFile.readText()
        val root = JSONObject(text)

        val metadata = RemoteMetadata(
            version = root.optLongOrNull("version"),
            checksum = root.optStringOrNull("checksum"),
            generatedAt = root.optStringOrNull("generatedAt"),
            schemaVersion = root.optIntOrNull("schemaVersion"),
            rowCount = root.optIntOrNull("rowCount"),
            contestMin = root.optIntOrNull("contestMin"),
            contestMax = root.optIntOrNull("contestMax"),
            numbersPerDraw = root.optIntOrNull("numbersPerDraw")
        )

        val drawsArray = root.optJSONArray("draws") ?: JSONArray()
        val draws = mutableListOf<LocalDraw>()
        for (i in 0 until drawsArray.length()) {
            val item = drawsArray.optJSONObject(i) ?: continue
            val numbersJson = item.optJSONArray("numbers") ?: JSONArray()
            val numbers = buildList {
                for (n in 0 until numbersJson.length()) {
                    add(numbersJson.optInt(n))
                }
            }
            draws.add(
                LocalDraw(
                    id = item.optInt("id"),
                    date = item.optString("date"),
                    numbers = numbers
                )
            )
        }

        val rawStats = root.optJSONObject("stats")?.toMap() ?: emptyMap()

        return LocalBundle(
            metadata = metadata,
            draws = draws,
            rawStats = rawStats
        )
    }

    fun getCachedMetadata(): CachedMetadata =
        CachedMetadata(
            version = prefs.getLongOrNull(KEY_VERSION),
            checksum = prefs.getString(KEY_CHECKSUM, null)
        )

    fun saveCachedMetadata(meta: RemoteMetadata) {
        prefs.edit()
            .putLongIfNotNull(KEY_VERSION, meta.version)
            .putString(KEY_CHECKSUM, meta.checksum)
            .apply()
    }

    fun hasLocalBundle(): Boolean = localBundleFile.exists()

    /**
     * Faz a sincronização simples:
     * - Busca metadata remota
     * - Compara checksum com o cache local
     * - Se diferente (ou sem arquivo local), baixa o JSON e atualiza o cache
     *
     * @return true se baixou/atualizou, false se já estava igual
     */
    suspend fun syncIfNeeded(bucketUrl: String = DEFAULT_BUCKET_URL): Boolean {
        val remote = fetchRemoteMetadata() ?: return false
        val cached = getCachedMetadata()

        val needsDownload = !hasLocalBundle() ||
            remote.checksum != null && remote.checksum != cached.checksum

        if (!needsDownload) return false

        downloadBundle(bucketUrl)
        saveCachedMetadata(remote)
        Log.d(TAG, "Bundle atualizado: version=${remote.version} checksum=${remote.checksum}")
        return true
    }

    companion object {
        private const val TAG = "LotofacilSyncRepository"
        private const val PREFS_NAME = "lotofacil_bundle_cache"
        private const val KEY_VERSION = "version"
        private const val KEY_CHECKSUM = "checksum"
        private const val BUNDLE_PATH = "processed/draws.json"
        private const val DEFAULT_BUCKET_URL = "gs://baseforfirebase-f3545.firebasestorage.app"
    }
}

private fun Map<*, *>.toRemoteMetadata(): RemoteMetadata =
    RemoteMetadata(
        version = (this["version"] as? Number)?.toLong(),
        checksum = this["checksum"] as? String,
        generatedAt = this["generatedAt"] as? String,
        schemaVersion = (this["schemaVersion"] as? Number)?.toInt(),
        rowCount = (this["rowCount"] as? Number)?.toInt(),
        contestMin = (this["contestMin"] as? Number)?.toInt(),
        contestMax = (this["contestMax"] as? Number)?.toInt(),
        numbersPerDraw = (this["numbersPerDraw"] as? Number)?.toInt()
    )

private fun JSONObject.toMap(): Map<String, Any?> =
    keys().asSequence().associateWith { key -> this.opt(key) }

private fun SharedPreferences.Editor.putLongIfNotNull(key: String, value: Long?): SharedPreferences.Editor {
    if (value != null) putLong(key, value)
    return this
}

private fun SharedPreferences.getLongOrNull(key: String): Long? =
    if (contains(key)) getLong(key, 0L) else null

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key)) optString(key, null) else null

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (has(key)) optInt(key) else null

private fun JSONObject.optLongOrNull(key: String): Long? =
    if (has(key)) optLong(key) else null
