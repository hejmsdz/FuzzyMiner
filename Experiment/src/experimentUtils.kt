package com.mrozwadowski.fuzzyminer.timingExperiment

import com.mrozwadowski.fuzzyminer.input.getLogReader
import org.deckfour.xes.model.XLog
import sun.misc.Signal
import java.io.File
import kotlin.math.roundToInt

fun getLogFiles(): List<File> {
    return (File("experimentData").listFiles()?.toList() ?: listOf())
        .filter { it.extension == "xes" }
        .sortedBy { it.length() }
}

fun experimentLoop(
    logFiles: List<File>,
    windowSizes: Iterable<Int> = (20..200 step 20),
    relativeStrides: Iterable<Double> = listOf(0.2, 0.4, 0.6, 0.8),
    callback: (XLog, Int, Int, String) -> Unit
) {
    var loop = true

    Signal.handle(Signal("INT")) {
        println("[program interrupted, will quit after completing this window configuration]")
        loop = false
    }

    logFiles.forEach eachLog@ { logFile ->
        if (!loop) {
            return
        }
        val log = getLogReader(logFile).readLog()
        val logName = logFile.nameWithoutExtension
        println("$logName (${log.size} traces)")

        windowSizes.filter { it < log.size / 2 }.forEach eachWindowSize@ { windowSize ->
            relativeStrides.map { (windowSize * it).roundToInt() }.forEach eachStride@ { stride ->
                print("$windowSize:$stride ")
                callback(log, windowSize, stride, logName)
                if (!loop) {
                    return@eachLog
                }
            }
        }
        println()
    }
}