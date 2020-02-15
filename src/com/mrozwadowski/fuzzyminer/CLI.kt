package com.mrozwadowski.fuzzyminer

import java.io.File
import java.io.IOException
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.size != 1) {
        System.err.println("Expected input file as command line argument")
        exitProcess(1)
    }
    val path = args[0]

    try {
        val file = File(path)
        println(file)
    } catch (e: IOException) {
        System.err.println("Failed to open $path for reading")
        exitProcess(1)
    }
}
