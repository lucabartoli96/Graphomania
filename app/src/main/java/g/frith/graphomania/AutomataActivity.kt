package g.frith.graphomania

import android.graphics.*
import android.view.MotionEvent
import org.json.JSONObject
import java.util.*
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_input_dialog.view.*


class AutomataActivity : AbstractLoopedGraphActivity() {


    companion object {

        const val EDGE_CURVE = 40f

        const val START_ARROW_RADIUS = 30f
        const val START_ARROW_ANGLE = 60f

        const val EXECUTION = "execution"


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
     * Execution animation-related fields
     *
     */
    private var inputPaint = Paint()
    private var input = ""
    private var inputRect = Rect()
    private var currentChar = -1

    init {
        inputPaint.style = Paint.Style.FILL
        inputPaint.textSize = 40f
    }

    fun drawInput(canvas: Canvas) {

        if ( currentChar != input.length ) {

            inputPaint.getTextBounds(input, currentChar, input.length, inputRect)
            val height = inputRect.height()

            val posY = height.toFloat()
            val posX = canvas.width/2f

            canvas.drawText(input, currentChar, input.length, posX, posY, inputPaint)

            canvas.drawArrowedStraightEdge(posX - START_ARROW_RADIUS, posY,
                    posX, posY, inputPaint, 0f,
                    ARROW_RADIUS, ARROW_ANGLE, inputPaint)
        }

    }



    /**
     *
     * Procedures
     *
     */
    private val execOnInput = Procedure<String, GraphComponent?, Boolean> {

        val EDGE = "edge"
        val NODE = "node"
        val RESTORE = "restore"

        start {
            currentAnimation = EXECUTION
            animationRunning = true
        }

        end {
            currentAnimation = ""
            animationRunning = false
            (if (it) alert(R.string.accepted) else alert (R.string.not_accepted)).show()
        }

        procedure { // String
            input = it[0]

            var node = (AutomataNode.start as AutomataNode)
            var usedEdge: GraphComponent? = null

            for ( i in input.indices ) {

                val c = input[i]

                currentChar = i

                checkPoint(EDGE, usedEdge)
                checkPoint(RESTORE, usedEdge)
                usedEdge = null
                checkPoint(NODE, node)
                checkPoint(RESTORE, node)


                val loop = node.loop as AutomataLoop?
                if ( loop !== null && c in loop.symbols)  {
                    usedEdge = loop
                    continue
                }

                for ( edge in node.edges ) {
                    if ( c in (edge as AutomataEdge).symbols ) {
                        node = (edge.to as AutomataNode)
                        usedEdge = edge
                        break
                    }
                }

                if ( usedEdge === null ) {
                    break
                }
            }

            currentChar = input.length

            checkPoint(EDGE, usedEdge)
            checkPoint(RESTORE, usedEdge)
            checkPoint(NODE, node)
            checkPoint(RESTORE, node)

            node.final &&  usedEdge !== null

        }

        checkPoint(NODE, 500) {
            it[0]?.let {
                it.setVertexColor(Color.RED)
                graphInvalidate()
            }
        }

        checkPoint(EDGE, 500) {
            it[0]?.let {
                it.setEdgeColor(Color.GREEN)
                graphInvalidate()
            }
        }

        checkPoint(RESTORE, 10) {
            it[0]?.let {
                it.setDefaultPaint()
                graphInvalidate()
            }
        }

    }

    override fun drawAnimation(canvas: Canvas) {
        when( currentAnimation ) {
            EXECUTION -> drawInput(canvas)
        }

    }


    override val menuItems = mapOf<Int, ()->Unit>(

            R.string.input_automata to {
                if ( nodes.isEmpty() ) {
                    alert(R.string.sorry, R.string.empty_automata).show()
                } else {
                    alert(R.string.choose_input) {

                        view(R.layout.fragment_input_dialog) {
                            errorMsg.visibility = View.GONE
                            nameInput.onTextChanged {
                                errorMsg.visibility = View.GONE
                                var isAdmitted = true

                                for ( c in it ) {
                                    isAdmitted = isAdmitted && c in 'a'..'z'
                                }

                                if ( !isAdmitted ) {
                                    errorMsg.visibility = View.VISIBLE
                                }

                                yesButton?.isEnabled = it.isNotEmpty() && isAdmitted
                            }
                        }

                        positiveButton(R.string.ok) {
                            execOnInput(nameInput.text.toString())
                        }

                        negativeButton(R.string.cancel)
                    }.show()
                }
            }

    )


    /**
     *
     * Derived "Automata" classes
     *
     */
    private class AutomataNode(x: Float, y: Float, var final: Boolean) : LoopedNode(x, y) {

        companion object {
            var start: Node? = null
        }

        fun isStart(): Boolean {
            return this === start
        }

        override fun draw(canvas: Canvas) {

            if ( final ) {

                canvas.drawCircle(x, y, NODE_RADIUS - 6, getNodePaint())
                canvas.drawCircle(x, y, NODE_RADIUS, getArcPaint())

            } else {
                canvas.drawCircle(x, y, NODE_RADIUS, getNodePaint())
            }

            if ( isStart() ) {
                canvas.drawArrowedStraightEdge(
                        x - 2*NODE_RADIUS, y, x, y, getArcPaint(),  NODE_RADIUS,
                        START_ARROW_RADIUS, START_ARROW_ANGLE, getArrowPaint()
                )
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
            "start" To if ( nodes.isEmpty() ) -1 else nodes.indexOf(AutomataNode.start)
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

        if ( nodes.isEmpty() ) {
            AutomataNode.start = null
        } else {
            val i = graph.getInt("start")
            AutomataNode.start = nodes[i]
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
        if ( nodes.size == 1 ) {
            AutomataNode.start = nodes[0]
        }
    }


    override fun createEdge(firstNode: Node, node: Node) {

        val found = firstNode.edges.find { it.to === node }

        if ( found === null ) {
            pendingEdge = PendingEdge(firstNode, node)
            openSymbolPicker(firstNode)
        }
    }

    override fun createLoop(node: Node) {

        if ( (node as LoopedNode).loop === null ) {
            pendingLoopedNode = node
            openSymbolPicker(node)
        }

    }

    override fun removeNode(node: Node) {
        nodes.remove(node)

        for ( from in nodes ) {
            from.edges.removeAll {it.to === node}
        }

        if ( (node as AutomataNode).isStart() ) {
            AutomataNode.start = if ( nodes.isEmpty() ) null else nodes[0]
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
    private fun openSymbolPicker( node: Node, symbolsList: List<Char> = listOf() ) {

        val symbols = symbolsList.toMutableSet()
        val disabled = mutableListOf<Char>()

        for ( edge in node.edges ) {
            disabled.addAll((edge as AutomataEdge).symbols)
        }

        ((node as LoopedNode).loop as AutomataLoop?)?.let {
            disabled.addAll(it.symbols)
        }

        alert("Pick symbols") {
            view {
                vertical {
                    for (v in 'a'..'z' step 6) {

                        horizontal {

                            for (h in v..(if ('z' < v + 5) 'z' else v + 5)) {

                                toggleButton(R.style.ToggleButton) {

                                    text = h.toString()
                                    textOn = h.toString()
                                    textOff = h.toString()
                                    layoutParams = LinearLayout.LayoutParams(100, 100)
                                    setBackgroundColor(Color.TRANSPARENT)

                                    when (h) {
                                        in symbols -> {
                                            isChecked = true
                                        }
                                        in disabled -> {
                                            isEnabled = false
                                        }
                                    }

                                    setOnCheckedChangeListener { _, isChecked: Boolean ->

                                        val symbol = text[0]

                                        if (isChecked) {
                                            symbols.add(symbol)
                                        } else {
                                            symbols.remove(symbol)
                                        }

                                        yesButton?.isEnabled = !symbols.isEmpty()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            positiveButton(R.string.ok) {
                onSymbolsPicked(symbols.toList())
            }

            negativeButton(R.string.cancel) {
                onSymbolsCancel()
            }

        }.show()


    }

    fun onSymbolsPicked(symbols: List<Char>) {
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

    fun onSymbolsCancel() {
        pendingEdge = null
        pendingLoopedNode = null
        modifyingEdge = null
        modifyingLoop = null
        val fragment = supportFragmentManager.findFragmentByTag("SymbolsPicker")
        if(fragment !== null)
            supportFragmentManager.beginTransaction().remove(fragment).commit()
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
                openSymbolPicker((component as Edge).from, component.symbols)
            }
            is AutomataLoop -> {
                modifyingLoop = component
                openSymbolPicker((component as Loop).node, component.symbols)
            }
            is AutomataNode -> {
                AutomataNode.start = component
            }
        }
    }

}
