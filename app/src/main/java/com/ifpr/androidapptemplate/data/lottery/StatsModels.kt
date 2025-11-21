package com.ifpr.androidapptemplate.data.lottery

data class PairOdd(
    val pairs: Int = 0,
    val odds: Int = 0
)

data class IntervalItem(
    val number: Int = 0,
    val avg: Double = 0.0,
    val last: Int = 0
)

data class Draw(
    val drawId: Int = 0,
    val date: String = "",
    val numbers: List<Int> = emptyList(),
    val source: String? = null
)

data class Stats(
    val generatedAt: Long = 0L,
    val sampleSize: Int = 0,
    val topNumbers: List<Int> = emptyList(),
    val leastNumbers: List<Int> = emptyList(),
    val sumMean: Double = 0.0,
    val pairOdd: PairOdd = PairOdd(),
    val streakMax: Int = 0,
    val intervals: List<IntervalItem> = emptyList(),
    val pairCombos: List<List<Int>> = emptyList(),
    val tripleCombos: List<List<Int>> = emptyList(),
    val suggestedBet: List<Int> = emptyList()
)
