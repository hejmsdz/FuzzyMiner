package com.mrozwadowski.fuzzyminer.input

import org.deckfour.xes.`in`.XUniversalParser
import org.deckfour.xes.model.XLog
import java.io.File

class OpenXES(file: File): LogReader(file) {
    override fun readLog(): XLog {
        val parser = XUniversalParser()
        val xes = parser.parse(file)
        return xes.first()
    }
}