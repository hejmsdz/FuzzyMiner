package com.mrozwadowski.fuzzyminer.input

import org.deckfour.xes.factory.XFactory
import org.deckfour.xes.factory.XFactoryNaiveImpl
import org.deckfour.xes.model.XAttribute
import org.deckfour.xes.model.XLog
import org.deckfour.xes.model.XTrace
import java.io.File

class CSV(file: File): LogReader(file) {
    private val factory: XFactory = XFactoryNaiveImpl()
    private val log: XLog = factory.createLog()
    private var traceIdColumn: Int = -1
    private var activityColumn: Int = -1
    private val traces = mutableMapOf<String, XTrace>()

    override fun readLog(): XLog {
        readFile()
        log.addAll(traces.values)
        return log
    }

    private fun readFile() {
        val reader = file.bufferedReader()
        readHeaders(reader.readLine())
        reader.forEachLine { readEvent(it) }
        reader.close()
    }

    private fun readHeaders(line: String) {
        val fields = line.split(",")
        traceIdColumn = fields.indexOf("Case ID")
        activityColumn = fields.indexOf("Activity")
    }

    private fun readEvent(line: String) {
        val fields = line.split(",")
        val traceId = fields[traceIdColumn]
        val activityName = fields[activityColumn]

        val trace = traces.getOrPut(traceId, { factory.createTrace() })
        val event = factory.createEvent()
        event.attributes["concept:name"] = conceptName(activityName)
        trace.add(event)
    }

    private fun conceptName(name: String): XAttribute {
        return factory.createAttributeLiteral("concept:name", name, null)
    }
}