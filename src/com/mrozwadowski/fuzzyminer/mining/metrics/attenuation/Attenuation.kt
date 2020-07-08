package com.mrozwadowski.fuzzyminer.mining.metrics.attenuation

abstract class Attenuation(val maxDistance: Int?) {
    abstract fun factor(distance: Int): Double
}
