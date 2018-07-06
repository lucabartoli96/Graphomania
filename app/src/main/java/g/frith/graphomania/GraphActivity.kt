package g.frith.graphomania

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class GraphActivity : AbstractGraphActivity() {

    companion object {
        const val EDGE_CURVE = 0f
    }

    override val type = "graph"
    

    override fun getJson(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun parseJson(text: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createNode(x: Float, y: Float) {
        nodes.add(Node(x, y))
        graphInvalidate()
    }

    override fun createEdge(firstNode: Node, node: Node) {
        edges.add(Edge(firstNode, node, EDGE_CURVE))
        graphInvalidate()
    }


    override fun removeNode(node: Node) {
        nodes.remove(node)
        edges.removeAll { it.from == node || it.to == node }
        graphInvalidate()
    }

    override fun removeEdge(edge: Edge) {
        edges.remove(edge)
        edge.from.edges.remove(edge)
        graphInvalidate()
    }


}
