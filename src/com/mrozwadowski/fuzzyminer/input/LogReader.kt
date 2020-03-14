package com.mrozwadowski.fuzzyminer.input

import org.deckfour.xes.model.XLog
import java.io.File

abstract class LogReader(protected val file: File) {
    abstract fun readLog(): XLog
}

fun getLogReader(file: File): LogReader {
    return when (file.extension.toLowerCase()) {
        "csv" -> CSV(file)
        else -> OpenXES(file)
    }
}
