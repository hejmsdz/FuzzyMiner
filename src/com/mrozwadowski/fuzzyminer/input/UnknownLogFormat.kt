package com.mrozwadowski.fuzzyminer.input

import java.io.IOException

class UnknownLogFormat(extension: String): IOException(
    "${extension.toUpperCase()} file extension is unsupported"
)
