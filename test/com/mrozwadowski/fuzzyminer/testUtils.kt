package com.mrozwadowski.fuzzyminer

import org.deckfour.xes.factory.XFactoryNaiveImpl
import org.deckfour.xes.model.XEvent
import org.deckfour.xes.model.XLog

fun createSimpleLog(traces: List<String>): XLog {
    return createLog(traces.map { it.toCharArray().map(Char::toString) })
}

fun createLog(traces: List<List<String>>): XLog {
    val factory = XFactoryNaiveImpl()
    val log = factory.createLog()

    traces.forEach { traceStr ->
        val trace = factory.createTrace()
        traceStr.forEach { eventStr ->
            val event = factory.createEvent()
            val key = "concept:name"
            val conceptName = factory.createAttributeLiteral(key, eventStr, null)
            event.attributes[key] = conceptName
            trace.add(event)
        }
        log.add(trace)
    }
    return log
}

