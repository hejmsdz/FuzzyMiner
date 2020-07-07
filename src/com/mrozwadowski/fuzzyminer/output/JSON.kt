package com.mrozwadowski.fuzzyminer.output

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.mrozwadowski.fuzzyminer.data.graph.Edge
import com.mrozwadowski.fuzzyminer.data.graph.Graph
import com.mrozwadowski.fuzzyminer.data.graph.Node
import java.lang.reflect.Type

class JSON(private val graph: Graph) {
    override fun toString(): String {
        val gson = GsonBuilder()
            .registerTypeAdapter(Node::class.java, NodeAdapter())
            .registerTypeAdapter(Edge::class.java, EdgeAdapter())
            .registerTypeAdapter(Graph::class.java, GraphAdapter())
//            .setPrettyPrinting()
            .create()

        return gson.toJson(graph)
    }

    class GraphAdapter: JsonSerializer<Graph> {
        override fun serialize(graph: Graph?, type: Type?, context: JsonSerializationContext?): JsonElement {
            if (graph == null) {
                return JsonNull.INSTANCE
            }
            val nodesType: TypeToken<List<Node>> = object : TypeToken<List<Node>>() {}
            val edgesType: TypeToken<List<Edge>> = object : TypeToken<List<Edge>>() {}
            val obj = JsonObject()
            obj.add("nodes", context?.serialize(graph.nodes, nodesType.type))
            obj.add("edges", context?.serialize(graph.allEdgeObjects(), edgesType.type))
            return obj
        }
    }

    class NodeAdapter: JsonSerializer<Node> {
        override fun serialize(node: Node?, type: Type?, context: JsonSerializationContext?): JsonElement {
            if (node == null) {
                return JsonNull.INSTANCE
            }
            val obj = JsonObject()
            obj.addProperty("id", node.id)
            obj.addProperty("label", node.toString())
            obj.addProperty("significance", node.significance)
            return obj
        }
    }

    class EdgeAdapter: JsonSerializer<Edge> {
        override fun serialize(edge: Edge?, type: Type?, context: JsonSerializationContext?): JsonElement {
            if (edge == null) {
                return JsonNull.INSTANCE
            }
            val obj = JsonObject()
            obj.addProperty("from", edge.source.id)
            obj.addProperty("to", edge.target.id)
            obj.addProperty("significance", edge.significance)
            obj.addProperty("correlation", edge.correlation)
            return obj
        }
    }
}