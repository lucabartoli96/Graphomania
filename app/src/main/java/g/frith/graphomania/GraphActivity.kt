package g.frith.graphomania

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.MotionEvent
import android.widget.TextView
import org.json.JSONObject
import java.util.*


class GraphActivity : AbstractGraphActivity() {


    companion object {
        /**
         *  Graph components default sizes constants
         */
        const val EDGE_CURVE = 0f
    }



    /**
     *  Override abstract property type
     */
    override val type = "graph"



    /**
     *  GraphComponents derivate classes
     */
    private class GraphNode(x: Float, y: Float): Node(x, y) {
        val edgesTo = mutableListOf<Edge>()

        override fun adj(): Iterable<Node> {
            return super.adj() union edgesTo.map { it.from }
        }

        override fun allEdges(): Iterable<Edge> {
            return super.allEdges() union edgesTo
        }
    }

    private class GraphEdge(from: Node, to: Node, curve: Float, delay: Long? = null):
            Edge(from, to, curve, delay) {
        init {
            (to as GraphNode).edgesTo.add(this)
        }
    }



    /**
     *  maps menu item to action to perform
     */
    override val menuItems = mapOf(

            R.string.reset_colors to {
                if ( !animationRunning ) {
                    resetColors()
                }
                graphInvalidate()
            },

            R.string.dfs to {
                if ( !animationRunning && !dfsPending && !bfsPending) {
                    alert(R.string.dfs, R.string.click_node) {

                        positiveButton(R.string.ok) {
                            resetColors()
                            dfsPending = true
                        }

                        negativeButton(R.string.quit)
                    }.show()
                }
            },

            R.string.bfs to {
                if ( !animationRunning && !dfsPending && !bfsPending) {
                    alert(R.string.bfs, R.string.click_node) {

                        positiveButton(R.string.ok) {
                            resetColors()
                            bfsPending = true
                        }

                        negativeButton(R.string.quit)
                    }.show()
                }
            }

    )

    /**
     *  Toolbar label
     */
    private lateinit var label: TextView

    /**
     *  onCreate only registers the animated procedures
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        procedures.add(dfs)
        procedures.add(bfs)

        label = TextView(this)
        label.setTextColor(Color.BLACK)
        label.typeface = Typeface.DEFAULT_BOLD
        label.textSize = 25f

    }


    /**
     *
     * DFS Related fields
     *
     */
    private var dfsPending = false

    /**
     *
     * BFS Related fields
     *
     */
    private var bfsPending = false


    /**
     * Resets graph components colors
     */
    private fun resetColors() {
        for ( node in nodes ) {
            node.setDefaultPaint()
            for (edge in node.edges) {
                edge.setDefaultPaint()
            }
        }
    }


    /**
     *
     * Procedures
     *
     */
    private val dfs = Procedure<Node, GraphComponent, Unit> {

        val LONG_INTERVAL: Long = 150
        val SHORT_INTERVAL: Long = 10

        val VISIT = "visit"
        val EDGE = "edge"
        val MARK = "mark"
        val RESTORE = "restore"
        val BACK = "back"

        var visited = hashMapOf<Node, Boolean>()
        var marked = mutableSetOf<Edge>()
        var path = mutableSetOf<Edge>()
        var visiting: Node? = null

        val visitingPaint = Paint()
        visitingPaint.style = Paint.Style.FILL_AND_STROKE
        visitingPaint.color = Color.RED

        start {
            label.text = "DFS"
            replaceToolbar(label)
            visited = HashMap<Node, Boolean>().apply{
                for( node in nodes ) {
                    node to false
                }
            }
            marked = mutableSetOf()
            path = mutableSetOf()
            visiting = null

            animationRunning = true
        }

        end {
            restoreToolbar()
            //toggleToolbar()
            animationRunning = false
        }


        procedure {

            val v = it[0]

            visited[v] = true

            checkPoint(VISIT, v)

            for( e in v.allEdges() ) {

                val w = if (v === e.to) e.from else e.to

                checkPoint(EDGE, e)

                //works when visited[w] is null
                if ( visited[w] == true ) {
                    checkPoint(RESTORE, e)
                } else {
                    checkPoint(MARK, e)
                    procedure(w)
                    checkPoint(BACK, e)
                    checkPoint(VISIT, v)
                }

            }

        }

        checkPoint(VISIT, LONG_INTERVAL) {
            it[0]?.let {
                visiting?.setDefaultPaint()
                visiting?.setVertexColor(Color.RED)

                visiting = it as Node

                it.setNodePaint(visitingPaint)
                graphInvalidate()
            }
        }

        checkPoint(EDGE, LONG_INTERVAL) {
            it[0]?.let {
                it.setEdgeColor(Color.GREEN)
                graphInvalidate()
            }
        }

        checkPoint(MARK, LONG_INTERVAL) {
            it[0]?.let {
                marked.add(it as Edge)
                path.add(it)
                it.setEdgeColor(Color.BLUE)
                graphInvalidate()
            }
        }

        checkPoint(RESTORE, SHORT_INTERVAL) {
            it[0]?.let {

                when (it as Edge) {
                    in path -> it.setEdgeColor(Color.BLUE)
                    in marked -> it.setEdgeColor(Color.MAGENTA)
                    else -> it.setDefaultPaint()

                }

                graphInvalidate()
            }
        }

        checkPoint(BACK, SHORT_INTERVAL) {
            it[0]?.let {
                path.remove(it)
                it.setEdgeColor(Color.MAGENTA)
                graphInvalidate()
            }
        }

    }


    private val bfs = Procedure<Node, GraphComponent, Unit> {

        val LONG_INTERVAL: Long = 150
        val SHORT_INTERVAL: Long = 10

        val ENQUE = "enque"
        val DEQUE = "deque"
        val EDGE = "edge"
        val MARK = "mark"
        val RESTORE = "restore"
        val VISITED = "visited"

        var visited = hashMapOf<Node, Boolean>()
        var marked = mutableSetOf<Edge>()

        val visitingPaint = Paint()
        visitingPaint.style = Paint.Style.FILL_AND_STROKE
        visitingPaint.color = Color.RED

        start {
            label.text = "BFS"
            replaceToolbar(label)
            visited = HashMap<Node, Boolean>().apply{
                for( node in nodes ) {
                    node to false
                }
            }
            marked = mutableSetOf()

            animationRunning = true
        }

        end {
            restoreToolbar()
            animationRunning = false
        }


        procedure {

            val root = it[0]
            val q = LinkedList<Node>()

            visited[root] = true

            q.add(root)
            checkPoint(ENQUE, root)

            while ( q.isNotEmpty() ) {

                val v = q.poll()
                checkPoint(DEQUE, v)

                for ( e in v.allEdges() ) {

                    checkPoint(EDGE, e)

                    val w = if ( v === e.to ) e.from else e.to

                    //works when visited[w] is null
                    if ( visited[w] != true ) {
                        checkPoint(MARK, e)
                        visited[w] = true
                        q.add(w)
                        checkPoint(ENQUE, w)
                    }

                    checkPoint(RESTORE, e)

                }

                checkPoint(VISITED, v)

            }

            checkPoint(DEQUE, root)

        }


        checkPoint(ENQUE, SHORT_INTERVAL) {
            it[0]?.let {
                it.setVertexColor(Color.BLUE)
                graphInvalidate()
            }
        }

        checkPoint(DEQUE, SHORT_INTERVAL) {
            it[0]?.let {
                it.setDefaultPaint()
                it.setNodePaint(visitingPaint)
                graphInvalidate()
            }
        }

        checkPoint(EDGE, LONG_INTERVAL) {
            it[0]?.let {
                it.setEdgeColor(Color.GREEN)
                graphInvalidate()
            }
        }

        checkPoint(MARK, SHORT_INTERVAL) {
            it[0]?.let {
                marked.add(it as Edge)
                graphInvalidate()
            }
        }

        checkPoint(RESTORE, SHORT_INTERVAL) {
            it[0]?.let {

                when (it as Edge) {
                    in marked -> it.setEdgeColor(Color.MAGENTA)
                    else -> it.setDefaultPaint()
                }

                graphInvalidate()
            }
        }

        checkPoint(VISITED, SHORT_INTERVAL) {
            it[0]?.let {
                it.setVertexColor(Color.RED)
                graphInvalidate()
            }
        }

    }



    /**
     *
     *  Json related functions
     *
     */
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
                    edgeJson.getDouble("curve").toFloat(),
                    NODE_ANIM_DURATION
            )
        }
    }



    /**
     *  Concrete creation/deletion functions
     */
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



    /**
     *  Gestures
     */
    override fun firstPointerDown(e: MotionEvent) {

        if ( !animationRunning ) {

            when {
                dfsPending -> {
                    val node = getClickedNode(e)
                    if (node !== null) {
                        dfs(node)
                        dfsPending = false
                    }
                }
                bfsPending -> {
                    val node = getClickedNode(e)
                    if (node !== null) {
                        bfs(node)
                        bfsPending = false
                    }
                }
                else -> super.firstPointerDown(e)
            }
        }
    }


}
