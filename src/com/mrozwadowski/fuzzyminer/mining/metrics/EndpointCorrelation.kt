package com.mrozwadowski.fuzzyminer.mining.metrics

import org.deckfour.xes.model.XAttributeLiteral
import org.deckfour.xes.model.XEvent

class EndpointCorrelation: LogBasedBinaryMetric(normalize = true) {
    override fun evaluate(previousEvent: XEvent, event: XEvent): Double {
        val name1 = (previousEvent.attributes["concept:name"] as XAttributeLiteral).value
        val name2 = (event.attributes["concept:name"] as XAttributeLiteral).value
        val distance = levenshtein(name1, name2).toDouble()
        val length = maxOf(name1.length, name2.length)
        return 1 - (distance / length)
    }

    companion object {
        fun levenshtein(str1: String, str2: String): Int {
            val length1 = str1.length
            val length2 = str2.length
            val d =  Array(length1 + 1) { IntArray(length2 + 1) }
            (0..length1).forEach { i -> d[i][0] = i }
            (0..length2).forEach { j -> d[0][j] = j }

            str1.forEachIndexed { i, ch1 ->
                str2.forEachIndexed { j, ch2 ->
                    val deletionCost = d[i][j+1] + 1
                    val insertionCost = d[i+1][j] + 1
                    val replacementCost = d[i][j] + (if (ch1 == ch2) 0 else 1)
                    d[i+1][j+1] = minOf(deletionCost, insertionCost, replacementCost)
                }
            }

            return d[length1][length2]
        }
    }
}