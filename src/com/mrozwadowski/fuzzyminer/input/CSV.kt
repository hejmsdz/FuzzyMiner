package com.mrozwadowski.fuzzyminer.input

import com.mrozwadowski.fuzzyminer.data.log.Activity
import com.mrozwadowski.fuzzyminer.data.log.Event
import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.data.log.Trace
import java.io.File

class CSV(private val file: File) {
    private var traceIdColumn: Int = -1
    private var activityColumn: Int = -1
    private var activities = HashMap<String, Activity>();
    private var traces = HashMap<String, ArrayList<Event>>();

    fun readLog() : Log {
        readFile()
        return Log(traces.values.map { Trace(it) })
    }

    private fun readFile() {
        var headersRead = false
        file.forEachLine { line ->
            val fields = line.split(",")
            if (!headersRead) {
                readHeaders(fields)
                headersRead = true
            } else {
                readActivity(fields)
            }
        }
    }

    private fun readHeaders(fields: List<String>) {
        traceIdColumn = fields.indexOf("Case ID")
        activityColumn = fields.indexOf("Activity")
    }

    private fun readActivity(fields: List<String>) {
        val traceId = fields[traceIdColumn]
        val activityName = fields[activityColumn]

        val activity = activities.getOrPut(activityName, { Activity(activityName) })
        val trace = traces.getOrPut(traceId, { arrayListOf() })
        val event = Event(activity)
        trace.add(event)
    }
}