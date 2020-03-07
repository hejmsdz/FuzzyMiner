package com.mrozwadowski.fuzzyminer.classifiers

import com.mrozwadowski.fuzzyminer.data.log.Event

typealias Classifier<EventClass> = (Event) -> EventClass
