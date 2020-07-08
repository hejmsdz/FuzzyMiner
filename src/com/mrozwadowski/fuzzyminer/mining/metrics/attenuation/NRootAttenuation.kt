package com.mrozwadowski.fuzzyminer.mining.metrics.attenuation

import kotlin.math.pow

class NRootAttenuation(private val base: Double, maxDistance: Int): Attenuation(maxDistance) {
    override fun factor(distance: Int): Double = base.pow(1 - distance)
}