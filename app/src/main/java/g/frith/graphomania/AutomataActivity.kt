package g.frith.graphomania

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import org.json.JSONObject
import java.util.*


class AutomataActivity : AbstractLoopedGraphActivity(),
        SymbolPickerDialog.OnFragmentInteractionListener {


    companion object {

        const val EDGE_CURVE = 40f


        /**
         *  fun to format label strings
         */
        private fun joinToLabel(symbols: List<Char>): String {

            val sb = StringBuilder()
            var contiguity = false

            sb.append(symbols[0])

            for ( i in 1..(symbols.size-1) ) {

                val prev = symbols[i-1]
                val curr = symbols[i]

                if ( prev != curr-1 ) {
                    if ( contiguity ) {
                        sb.append(prev)
                    }
                    sb.append(", ").append(curr)
                    contiguity = false
                } else if ( !contiguity ) {
                    sb.append('-')
                    contiguity = true
                }
            }

            if ( contiguity ) {
                sb.append(symbols.last())
            }

            return sb.toString()
        }

    }


    /**
     * override of type property
     */
    override val type = "automata"



    /**
     * to get random angles
     */
    private var random = Random(GregorianCalendar().timeInMillis)



    /**
     *
     * store components to be created/modified,
     * while symbolpicker is open
     *
     */
    private data class PendingEdge(val from: Node, val to: Node)
    private var pendingEdge: PendingEdge? = null
    private var pendingLoopedNode: Node? = null
    private var modifyingEdge: AutomataEdge? = null
    private var modifyingLoop: AutomataLoop? = null


    /**
     *
     * Procedures
     *
     */

    val checkInput = Procedure {

        

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if ( super.onCreateOptionsMenu(menu) ) {

            menu.add(getString(R.string.input_automata))
                    .setOnMenuItemClickListener {
                        checkInput()
                        true
                    }

            return true
        }
        return false
    }


    /**
     *
     * Derived "Automata" classes
     *
     */
    private class AutomataNode(x: Float, y: Float, var final: Boolean) : LoopedNode(x, y) {

        override fun draw(canvas: Canvas) {
            if ( final ) {

                canvas.drawCircle(x, y, NODE_RADIUS - 6, getNodePaint())
                canvas.drawCircle(x, y, NODE_RADIUS, getArcPaint())

            } else {
                canvas.drawCircle(x, y, NODE_RADIUS, getNodePaint())
            }
        }

        fun toggleFinal() {
            final = !final
        }
    }


    private class AutomataEdge(from: Node, to: Node, curve: Float, symbols: List<Char>) :
            Edge(from, to, curve) {

        constructor(edge: Edge, symbols: List<Char>) :
                this(edge.from, edge.to, edge.curve, symbols)

        private var symbolsStr: String = joinToLabel(symbols.sorted())

        var symbols: List<Char> = symbols
            set(symbols) {
                field = symbols.sorted()
                symbolsStr = joinToLabel(field)
            }

        override fun draw(canvas: Canvas) {
            canvas.drawArrowedCurveLabeledEdge(
                    from.x, from.y, to.x, to.y,
                    curve, NODE_RADIUS, getArcPaint(),
                    ARROW_RADIUS, ARROW_ANGLE, getArrowPaint(),
                    symbolsStr, getTextPaint()
            )
        }
    }


    private class AutomataLoop(node: Node, angle: Float, symbols: List<Char>) :
            Loop(node, angle) {

        constructor(loop: Loop, symbols: List<Char>) :
                this(loop.node, loop.angle, symbols)

        private var symbolsStr: String = joinToLabel(symbols.sorted())

        var symbols: List<Char> = symbols
            set(symbols) {
                field = symbols.sorted()
                symbolsStr = joinToLabel(field)
            }

        override fun draw(canvas: Canvas) {
            canvas.drawArrowedLabeledLoop(
                    node.x, node.y, LOOP_CURVE, angle, NODE_RADIUS, getArcPaint(),
                    ARROW_RADIUS, ARROW_ANGLE, getArrowPaint(),
                    symbolsStr, getTextPaint()
            )
        }

    }



    /**
     *
     * Json related classes
     *
     */
    override fun getJson(): String {

        val nodesJson = JsonArr()
        val edgesJson = JsonArr()
        val loopsJson = JsonArr()

        for ( i in nodes.indices ) {

            val node = (nodes[i] as AutomataNode)

            nodesJson.append(JsonObj {
                "x" To node.x
                "y" To node.y
                "final" To node.final
            })


            val loop = (node.loop as AutomataLoop?)

            if ( loop !== null ) {

                loopsJson.append(JsonObj {
                    "node" To i
                    "angle" To loop.angle
                    "symbols" To loop.symbols
                })
            }

            for ( j in node.edges.indices ) {

                val edge = (node.edges[j] as AutomataEdge)

                edgesJson.append(JsonObj {
                    "from" To  i
                    "to" To nodes.indexOf(edge.to)
                    "curve" To edge.curve
                    "symbols" To edge.symbols
                })
            }
        }

        val graphJson = JsonObj {
            "nodes" To nodesJson
            "edges" To edgesJson
            "loops" To loopsJson
        }

        return graphJson.toString()
    }


    override fun parseJson(text: String) {

        val graph = JSONObject(text)

        val nodesJson = graph.getJSONArray("nodes")

        for ( idx in 0 until nodesJson.length() ) {
            val nodeJson = nodesJson.getJSONObject(idx)

            nodes.add(AutomataNode(
                    nodeJson.getDouble("x").toFloat(),
                    nodeJson.getDouble("y").toFloat(),
                    nodeJson.getBoolean("final")
            ))
        }

        val edgesJson = graph.getJSONArray("edges")

        for ( idx in 0 until edgesJson.length()) {
            val edgeJson = edgesJson.getJSONObject(idx)

            val i = edgeJson.getInt("from")
            val j = edgeJson.getInt("to")

            val symbolsJson = edgeJson.getJSONArray("symbols")

            val symbols = mutableListOf<Char>()

            for ( sy in 0 until symbolsJson.length() ) {
                symbols.add(symbolsJson.getString(sy)[0])
            }

            AutomataEdge(
                    nodes[i], nodes[j],
                    edgeJson.getDouble("curve").toFloat(),
                    symbols
            )
        }


        val loopsJson = graph.getJSONArray("loops")

        for ( idx in 0 until loopsJson.length()) {
            val loopJson = loopsJson.getJSONObject(idx)

            val i = loopJson.getInt("node")

            val symbolsJson = loopJson.getJSONArray("symbols")

            val symbols = mutableListOf<Char>()

            for ( sy in 0 until symbolsJson.length() ) {
                symbols.add(symbolsJson.getString(sy)[0])
            }

            (nodes[i] as LoopedNode).loop = AutomataLoop(
                    nodes[i],
                    loopJson.getDouble("angle").toFloat(),
                    symbols
            )
        }

    }



    /**
     *
     * Concrete creation deletion methods
     *
     */
    override fun createNode(x: Float, y: Float) {
        nodes.add(AutomataNode(x, y, false))
    }


    override fun createEdge(firstNode: Node, node: Node) {

        val found = firstNode.edges.find { it.to === node }

        if ( found === null ) {
            pendingEdge = PendingEdge(firstNode, node)
            openSymbolPicker()
        }
    }

    override fun createLoop(node: Node) {

        if ( (node as LoopedNode).loop === null ) {
            pendingLoopedNode = node
            openSymbolPicker()
        }

    }

    override fun removeNode(node: Node) {
        nodes.remove(node)
        for ( from in nodes ) {
            from.edges.removeAll {it.to === node}
        }
        graphInvalidate()
    }

    override fun removeEdge(edge: Edge) {
        edge.from.edges.remove(edge)
        graphInvalidate()
    }

    override fun removeLoop(loop: Loop) {
        (loop.node as LoopedNode).loop = null
        graphInvalidate()
    }




    /**
     *
     * Symbol picker related functions
     *
     */
    private fun openSymbolPicker( symbols: List<Char>? = null ) {
        val ft =  supportFragmentManager.beginTransaction()
        SymbolPickerDialog.newInstance(symbols).show(ft, "SymbolPicker")
    }

    override fun onSymbolsPicked(symbols: List<Char>) {
        val storePendingEdge = pendingEdge
        val storePendingLoopedNode = pendingLoopedNode
        val storeModifyingEdge = modifyingEdge
        val storeModifyingLoop = modifyingLoop

        when {
            storePendingEdge !== null -> {
                AutomataEdge(
                        storePendingEdge.from,
                        storePendingEdge.to,
                        EDGE_CURVE, symbols
                )
                graphInvalidate()
            }
            storePendingLoopedNode !== null -> {
                AutomataLoop(
                        storePendingLoopedNode,
                        random.nextAngleDegrees(), symbols
                )
                graphInvalidate()
            }
            storeModifyingEdge !== null -> {
                storeModifyingEdge.symbols = symbols
                graphInvalidate()
            }
            storeModifyingLoop !== null -> {
                storeModifyingLoop.symbols = symbols
                graphInvalidate()
            }
        }

        onSymbolsCancel()
    }

    override fun onSymbolsCancel() {
        pendingEdge = null
        pendingLoopedNode = null
        modifyingEdge = null
        modifyingLoop = null
    }




    /**
     *
     * Gestures listeners
     *
     */
    override fun doubleTap(e: MotionEvent) {
        val node = getClickedNode(e)

        if ( node == null ) {
            createNode(e.x, e.y)
            graphInvalidate()
        } else {
            (node as AutomataNode).toggleFinal()
            graphInvalidate()
        }
    }

    override fun longPress(e: MotionEvent) {
        val component = getClickedComponent(e)

        when ( component ) {
            is AutomataEdge -> {
                modifyingEdge = component
                openSymbolPicker(component.symbols)
            }
            is AutomataLoop -> {
                modifyingLoop = component
                openSymbolPicker(component.symbols)
            }
        }
    }

}
