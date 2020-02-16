package com.mrozwadowski.fuzzyminer.data.log

data class Activity(val name: String, val id: Int) {
    override fun toString(): String = name
}