
import com.google.gson.Gson
import com.mrozwadowski.fuzzyminer.data.Parameters
import com.mrozwadowski.fuzzyminer.input.getLogReader
import com.mrozwadowski.fuzzyminer.mining.FuzzyMiner
import com.mrozwadowski.fuzzyminer.mining.metrics.defaultMetrics
import com.mrozwadowski.fuzzyminer.output.JSON
import org.deckfour.xes.classification.XEventNameClassifier

import spark.kotlin.Http
import spark.kotlin.ignite
import java.io.File

fun enableCORS(http: Http, origin: String?, methods: String?, headers: String?) {
    http.options("/*") {
        val accessControlRequestHeaders = request.headers("Access-Control-Request-Headers")
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders)
        val accessControlRequestMethod = request.headers("Access-Control-Request-Method")
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod)
        "OK"
    }
    http.before {
        response.header("Access-Control-Allow-Origin", origin)
        response.header("Access-Control-Request-Method", methods)
        response.header("Access-Control-Allow-Headers", headers)
        response.type("application/json")
    }
}

fun main() {
    val http: Http = ignite()
    enableCORS(http, "*", "POST", null)

    http.get("/logs") {
        val filenames = File("sampleData").listFiles().map { it.name }
        Gson().toJson(filenames)
    }

    http.post("/mine") {
        val log = getLogReader(File("sampleData/journal_review.xes")).readLog()
        val classifier = XEventNameClassifier()
        val miner = FuzzyMiner(log, classifier, defaultMetrics())
        try {
            val body = request.body()
            val parameters = Gson().fromJson(body, Parameters::class.java)
            if (parameters != null) {
                miner.parameters = parameters
            }
        } catch (e: Exception) {
            println(e)
            e.printStackTrace()
        }
        val graph = miner.mine()
        JSON(graph)
    }
}
