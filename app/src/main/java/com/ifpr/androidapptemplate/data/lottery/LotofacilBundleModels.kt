package com.ifpr.androidapptemplate.data.lottery

data class RemoteMetadata(
    val version: Long? = null,
    val checksum: String? = null,
    val generatedAt: String? = null,
    val schemaVersion: Int? = null,
    val rowCount: Int? = null,
    val contestMin: Int? = null,
    val contestMax: Int? = null,
    val numbersPerDraw: Int? = null
)

data class CachedMetadata(
    val version: Long? = null,
    val checksum: String? = null
)

data class LocalDraw(
    val id: Int,
    val date: String,
    val numbers: List<Int>
)

data class LocalBundle(
    val metadata: RemoteMetadata,
    val draws: List<LocalDraw>,
    val rawStats: Map<String, Any?>
)
