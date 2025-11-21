package com.ifpr.androidapptemplate.data.lottery

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class StatsRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    ) {

    suspend fun fetchStats(): Stats? = suspendCancellableCoroutine { cont ->
        firestore.collection(STATS_COLLECTION)
            .document(CURRENT_DOC)
            .get()
            .addOnSuccessListener { snapshot ->
                cont.resume(snapshot.toStats())
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    suspend fun fetchRecentDraws(limit: Long = 10): List<Draw> = suspendCancellableCoroutine { cont ->
        firestore.collection(DRAWS_COLLECTION)
            .orderBy("drawId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { query ->
                val results = query.documents.mapNotNull { it.toDraw() }
                cont.resume(results)
            }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    private fun DocumentSnapshot.toStats(): Stats? {
        if (!exists()) return null
        return Stats(
            generatedAt = getLong("generatedAt") ?: 0L,
            sampleSize = getLong("sampleSize")?.toInt() ?: 0,
            topNumbers = getList<Int>("topNumbers") ?: emptyList(),
            leastNumbers = getList<Int>("leastNumbers") ?: emptyList(),
            sumMean = getDouble("sumMean") ?: 0.0,
            pairOdd = get("pairOdd")?.let { raw ->
                val map = raw as? Map<*, *> ?: return@let null
                PairOdd(
                    pairs = (map["pairs"] as? Number)?.toInt() ?: 0,
                    odds = (map["odds"] as? Number)?.toInt() ?: 0
                )
            } ?: PairOdd(),
            streakMax = (getLong("streakMax") ?: 0L).toInt(),
            intervals = (get("intervals") as? List<*>)?.mapNotNull { item ->
                val map = item as? Map<*, *> ?: return@mapNotNull null
                IntervalItem(
                    number = (map["number"] as? Number)?.toInt() ?: 0,
                    avg = (map["avg"] as? Number)?.toDouble() ?: 0.0,
                    last = (map["last"] as? Number)?.toInt() ?: 0
                )
            } ?: emptyList(),
            pairCombos = extractComboList(get("pairCombos")),
            tripleCombos = extractComboList(get("tripleCombos")),
            suggestedBet = getList<Int>("suggestedBet") ?: emptyList()
        )
    }

    private fun DocumentSnapshot.toDraw(): Draw? {
        if (!exists()) return null
        return Draw(
            drawId = (getLong("drawId") ?: 0L).toInt(),
            date = getString("date") ?: "",
            numbers = getList<Int>("numbers") ?: emptyList(),
            source = getString("source")
        )
    }

    private fun extractComboList(raw: Any?): List<List<Int>> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapNotNull { combo ->
            (combo as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }
        }
    }

    companion object {
        private const val STATS_COLLECTION = "stats"
        private const val CURRENT_DOC = "current"
        private const val DRAWS_COLLECTION = "draws"
    }
}
