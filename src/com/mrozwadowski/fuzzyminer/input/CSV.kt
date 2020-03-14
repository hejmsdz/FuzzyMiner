package com.mrozwadowski.fuzzyminer.input

import org.deckfour.xes.factory.XFactoryNaiveImpl
import org.deckfour.xes.model.XLog
import java.io.File

class CSV(file: File): LogReader(file) {
    private var traceIdColumn: Int = -1
    private var activityColumn: Int = -1
    private var nextActivityId: Int = 0
//    private val activities = mutableMapOf<String, Activity>()
//    private val traces = mutableMapOf<String, MutableList<Event>>()

    override fun readLog(): XLog {
        readFile()
        val factory = XFactoryNaiveImpl()
        return factory.createLog()
//        return Log(traces.values.map { Trace(it) })
    }

    private fun readFile() {
        val reader = file.bufferedReader()
        readHeaders(reader.readLine())
//        reader.forEachLine { readEvent(it) }
        reader.close()
    }

    private fun readHeaders(line: String) {
        val fields = line.split(",")
        traceIdColumn = fields.indexOf("Case ID")
        activityColumn = fields.indexOf("Activity")
    }

    /*
    private fun readEvent(line: String) {
        val fields = line.split(",")
        val traceId = fields[traceIdColumn]
        val activityName = fields[activityColumn]

        val activity = activities.getOrPut(activityName, { Activity(activityName, nextActivityId++) })
        val trace = traces.getOrPut(traceId, { mutableListOf() })
        val event = Event(activity)
        trace.add(event)
    }
     */
}