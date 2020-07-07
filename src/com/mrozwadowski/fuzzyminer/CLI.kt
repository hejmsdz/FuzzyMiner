package com.mrozwadowski.fuzzyminer

import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.metrics.defaultMetrics
import com.mrozwadowski.fuzzyminer.output.Dot
import org.deckfour.xes.classification.XEventNameClassifier
import org.deckfour.xes.model.XLog
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Expected input file as command line argument")
        exitProcess(1)
    }
    val path = args[0]

    val log: XLog
    try {
        val file = File(path)
        log = getLogReader(file).readLog()
    } catch (e: IOException) {
        System.err.println("Failed to open $path for reading: ${e.message}.")
        exitProcess(1)
    }

    val classifier = XEventNameClassifier()
    val graph = FuzzyMiner(log, classifier, defaultMetrics()).mine()
    print(Dot(graph))
}
