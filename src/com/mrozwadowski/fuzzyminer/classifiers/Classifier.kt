package com.mrozwadowski.fuzzyminer.classifiers

import org.deckfour.xes.model.XEvent

typealias Classifier<EventClass> = (XEvent) -> EventClass
