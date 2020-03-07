package com.mrozwadowski.fuzzyminer

import com.mrozwadowski.fuzzyminer.data.log.Log
import com.mrozwadowski.fuzzyminer.input.CSV
import com.mrozwadowski.fuzzyminer.mining.DumbMiner
import com.mrozwadowski.fuzzyminer.output.Dot
import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Expected input file as command line argument")
        exitProcess(1)
    }
    val path = args[0]

    val log: Log
    try {
        val file = File(path)
        log = CSV(file).readLog()
    } catch (e: IOException) {
        System.err.println("Failed to open $path for reading")
        exitProcess(1)
    }

    val graph = DumbMiner(log) { it.activity }.mine()
    print(Dot(graph))
}
