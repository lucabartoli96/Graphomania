package g.frith.graphomania

import android.util.Log
import org.json.JSONObject


class GraphActivity : AbstractGraphActivity() {

    companion object {
        const val EDGE_CURVE = 0f
    }

    override val type = "graph"

    override val menuItems = mapOf<Int, ()->Unit>()

    private class GraphNode(x: Float, y: Float): Node(x, y) {
        val edgesTo = mutableListOf<Edge>()
    }

    private class GraphEdge(from: Node, to: Node, curve: Float):
            Edge(from, to, curve) {
        init {
            (to as GraphNode).edgesTo.add(this)
        }
    }


    override fun getJson(): String {

        val nodesJson = JsonArr()
        val edgesJson = JsonArr()

        for ( i in nodes.indices ) {

            val node = (nodes[i] as GraphNode)

            nodesJson.append(JsonObj {
                "x" To node.x
                "y" To node.y
            })


            for ( j in node.edges.indices ) {

                val edge = (node.edges[j] as GraphEdge)

                edgesJson.append(JsonObj {
                    "from" To  i
                    "to" To nodes.indexOf(edge.to)
                    "curve" To edge.curve
                })
            }
        }

        val graphJson = JsonObj {
            "nodes" To nodesJson
            "edges" To edgesJson
        }

        return graphJson.toString()
    }


    override fun parseJson(text: String) {

        val graph = JSONObject(text)

        val nodesJson = graph.getJSONArray("nodes")

        for ( idx in 0 until nodesJson.length() ) {
            val nodeJson = nodesJson.getJSONObject(idx)

            nodes.add(GraphNode(
                    nodeJson.getDouble("x").toFloat(),
                    nodeJson.getDouble("y").toFloat()
            ))
        }

        val edgesJson = graph.getJSONArray("edges")

        for ( idx in 0 until edgesJson.length()) {
            val edgeJson = edgesJson.getJSONObject(idx)

            val i = edgeJson.getInt("from")
            val j = edgeJson.getInt("to")

            GraphEdge(
                    nodes[i], nodes[j],
                    edgeJson.getDouble("curve").toFloat()
            )
        }
    }

    override fun createNode(x: Float, y: Float) {
        nodes.add(GraphNode(x, y))
        graphInvalidate()
    }

    override fun createEdge(firstNode: Node, node: Node) {

        val found = firstNode.edges.find { it.to === node } ?:
                    node.edges.find { it.to === firstNode }

        if ( found === null ) {
            GraphEdge(firstNode, node, EDGE_CURVE)
        }

        graphInvalidate()
    }


    override fun removeNode(node: Node) {
        nodes.remove(node)

        for ( edge in node.edges ) {
            (edge.to as GraphNode).edgesTo.remove(edge)
        }
        for ( edge in (node as GraphNode).edgesTo ) {
            edge.from.edges.remove(edge)
        }

        graphInvalidate()
    }

    override fun removeEdge(edge: Edge) {
        edge.from.edges.remove(edge)
        (edge.to as GraphNode).edgesTo.remove(edge)
        graphInvalidate()
    }


}
