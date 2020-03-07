package com.mrozwadowski.fuzzyminer.data.log

import com.mrozwadowski.fuzzyminer.classifiers.Classifier

data class Log(val traces: List<Trace>) {
    fun <EventClass>eventClasses(classifier: Classifier<EventClass>): Set<EventClass> {
        return traces.flatMap { trace ->
            trace.events.map { classifier(it) }.toSet()
        }.toSet()
    }
}