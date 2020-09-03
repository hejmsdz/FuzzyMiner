package com.mrozwadowski.fuzzyminer.utils

import kotlin.math.absoluteValue

const val EPSILON = 0.00001

fun significantlyGreater(a: Double, b: Double): Boolean = a - b >= EPSILON

fun significantlyLess(a: Double, b: Double): Boolean = b - a >= EPSILON

fun significantlyEqual(a: Double, b: Double): Boolean = (a - b).absoluteValue < EPSILON
