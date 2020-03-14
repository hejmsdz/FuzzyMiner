package com.mrozwadowski.fuzzyminer.input

import java.io.File
import com.mrozwadowski.fuzzyminer.data.log.Log

abstract class LogReader(protected val file: File) {
    abstract fun readLog(): Log
}

fun getLogReader(file: File): LogReader {
    return when (val extension = file.extension.toLowerCase()) {
        "csv" -> CSV(file)
        else -> throw UnknownLogFormat(extension)
    }
}
