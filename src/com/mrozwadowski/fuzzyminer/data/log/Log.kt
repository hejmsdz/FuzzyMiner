package com.mrozwadowski.fuzzyminer.data.log

data class Log(val traces: List<Trace>) {
    val activities = traces.flatMap { trace ->
        trace.events.map { it.activity }.toSet()
    }.toSet()
}