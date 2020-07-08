package com.mrozwadowski.fuzzyminer.mining.metrics

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

fun levenshteinRatio(str1: String, str2: String): Double {
    val distance = levenshtein(str1, str2).toDouble()
    val length = maxOf(str1.length, str2.length)
    return 1 - (distance / length)
}
