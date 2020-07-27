package com.mrozwadowski.fuzzyminer.mining.metrics

import com.google.gson.Gson

data class MetricsDump(
    val tracesProcessed: Long,
    val unarySignificanceNames: List<String>,
    val binarySignificanceNames: List<String>,
    val binaryCorrelationNames: List<String>,
    val eventClasses: List<String>,
    val unarySignificanceMatrix: Array<Array<Double>>,
    val binarySignificanceMatrix: Array<Array<Array<Double>>>,
    val binaryCorrelationMatrix: Array<Array<Array<Double>>>
) {
    companion object {
        fun fromString(json: String): MetricsDump? {
            return Gson().fromJson(json, MetricsDump::class.java)
        }
    }

    override fun toString(): String {
        return Gson().toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MetricsDump

        if (tracesProcessed != other.tracesProcessed) return false
        if (unarySignificanceNames != other.unarySignificanceNames) return false
        if (binarySignificanceNames != other.binarySignificanceNames) return false
        if (binaryCorrelationNames != other.binaryCorrelationNames) return false
        if (eventClasses != other.eventClasses) return false
        if (!unarySignificanceMatrix.contentDeepEquals(other.unarySignificanceMatrix)) return false
        if (!binarySignificanceMatrix.contentDeepEquals(other.binarySignificanceMatrix)) return false
        if (!binaryCorrelationMatrix.contentDeepEquals(other.binaryCorrelationMatrix)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tracesProcessed.hashCode()
        result = 31 * result + unarySignificanceNames.hashCode()
        result = 31 * result + binarySignificanceNames.hashCode()
        result = 31 * result + binaryCorrelationNames.hashCode()
        result = 31 * result + eventClasses.hashCode()
        result = 31 * result + unarySignificanceMatrix.contentDeepHashCode()
        result = 31 * result + binarySignificanceMatrix.contentDeepHashCode()
        result = 31 * result + binaryCorrelationMatrix.contentDeepHashCode()
        return result
    }
}