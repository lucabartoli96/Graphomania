package g.frith.graphomania

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GestureDetectorCompat
import android.view.*
import kotlinx.android.synthetic.main.activity_abstract_graph.*
import android.util.Log
import java.io.*
import android.os.AsyncTask
import android.widget.Toast
import android.support.v7.app.AlertDialog


abstract class AbstractGraphActivity : AppCompatActivity() {

    companion object {

        private val fileName = {type: String, name: String -> "${type}_$name.json"}


        // Canvas-related constants (sizes and paints)
        const val NODE_RADIUS = 35f
        const val ENLARGE_TOUCH = 10f
        const val ARROW_ANGLE = 30f
        const val ARROW_RADIUS = 30f

        private val nodePaint: Paint = Paint()
        private val selectedNodePaint = Paint()

        init {
            nodePaint.style = Paint.Style.STROKE
            nodePaint.strokeWidth = 4f

            selectedNodePaint.style = Paint.Style.STROKE
            selectedNodePaint.strokeWidth = 4f
            selectedNodePaint.color = Color.CYAN
        }

        private val arcPaint = Paint()
        private val selectedArcPaint = Paint()
        private val textPaint = Paint()

        init {
            arcPaint.style = Paint.Style.STROKE
            arcPaint.strokeWidth = 3f

            selectedArcPaint.style = Paint.Style.STROKE
            selectedArcPaint.strokeWidth = 3f
            selectedArcPaint.color = Color.CYAN

            textPaint.style = Paint.Style.FILL
            textPaint.textSize = 30f
        }

        private val arrowPaint = Paint()
        private val selectedArrowPaint = Paint()

        init {
            arrowPaint.style = Paint.Style.FILL_AND_STROKE

            selectedArrowPaint.style = Paint.Style.FILL_AND_STROKE
            selectedArrowPaint.color = Color.CYAN
        }

    }



    /**
     * Unique couple that describes "project"
     * type must be overridden with string literal
     */
    abstract val type: String
    protected lateinit var name: String



    /**
     *
     * Graph view is where the graph is drawn,
     * private, accessed only to invalidate
     *
     */
    private lateinit var graphView: GraphView
    private var saved = true


    protected fun graphInvalidate() {
        graphView.postInvalidate()
        saved = false
    }

    private inner class GraphView(context: Context) : View(context) {

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            if ( canvas !== null ) {
                drawComponents(canvas)
            }
        }

    }



    /**
     *
     * Android Activity override funs
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abstract_graph)
        setSupportActionBar(graphToolbar)

        graphView = GraphView(this)
        mainContainer.addView(graphView)

        initGraphGestures()

        name = intent.getStringExtra("name")

        if ( File(filesDir, fileName(type, name)).exists() ) {
            load()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.graph_toolbar, menu)
        supportActionBar?.title = name
        return super.onCreateOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem?) = when(item?.itemId) {
        R.id.save -> {
            save()
            true
        }
        else -> false
    }


    override fun onBackPressed() {
        if (!saved) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.changes))
                    .setMessage(getString(R.string.want_save))
                    .setNegativeButton(getString(R.string.no), {_,_->
                        super.onBackPressed()
                    })
                    .setPositiveButton(getString(R.string.yes),  {_,_->
                        save()
                        super.onBackPressed()
                    }).create().show()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Json related functions
     */
    abstract fun getJson(): String
    abstract fun parseJson(text: String)



    /**
     *
     * Read/Write functions
     *
     */
    private fun load() {

        IOTask {

            try {
                val inputStream = openFileInput(fileName(type, name))

                if (inputStream != null) {
                    val inputStreamReader = InputStreamReader(inputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)

                    val allText = bufferedReader.use(BufferedReader::readText)

                    inputStream.close()
                    allText
                } else {
                    ""
                }
            } catch (e: FileNotFoundException) {
                Log.e("graph activity", "File not found: " + e.toString())
                ""
            } catch (e: IOException) {
                Log.e("graph activity", "Can not read file: " + e.toString())
                ""
            }
        }.post {
            if ( !it.isEmpty() ) {
                parseJson(it)
            }
        }


    }

    private fun save() {

        IOTask {
            saved = try {
                val file = openFileOutput(fileName(type, name),  Context.MODE_PRIVATE)
                val outputStreamWriter = OutputStreamWriter(file)
                outputStreamWriter.write(getJson())
                outputStreamWriter.close()
                true
            } catch (e: IOException) {
                Log.e("Exception", "File write failed: " + e.toString())
                false
            }

            saved

        }.post {
            val id = if (it) R.string.save_success else  R.string.save_fail
            val text = getString(id)
            val toast = Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT)
            toast.show()
        }


    }

    private class IOTask<T>(val task: ()->T) : AsyncTask<Void, Void, T>() {

        private var postCallback: ((T)->Unit)? = null

        init {
            execute()
        }

        fun post(callback: (T)->Unit) {
            postCallback = callback
        }


        override fun doInBackground(vararg p0: Void?): T {
            return task()
        }

        override fun onPostExecute(result: T?) {
            super.onPostExecute(result)
            if ( result !== null ) {
                postCallback?.invoke(result)
            }
        }
    }


    /**
     *
     * Graphic/Logic components of graph, base of class hierarchy
     *
     */
    protected abstract class GraphComponent {

        abstract fun select()
        abstract fun isSelected(): Boolean
        abstract fun update(fingerX: Float, fingerY: Float)
        abstract fun draw(canvas: Canvas)
        abstract fun contains(fingerX: Float, fingerY: Float): Boolean

        fun update(e: MotionEvent) {
            update(e.x, e.y)
        }

        fun contains(e: MotionEvent): Boolean {
            return contains(e.x, e.y)
        }

        protected fun getNodePaint(): Paint {
            return if( isSelected() ) selectedNodePaint else nodePaint
        }


        protected fun getArcPaint(): Paint {
            return if ( isSelected() ) selectedArcPaint else arcPaint
        }

        protected fun getTextPaint(): Paint {
            return textPaint
        }

        protected fun getArrowPaint(): Paint {
            return if ( isSelected() ) selectedArrowPaint else arrowPaint
        }

    }


    protected open class Node(var x: Float, var y: Float) : GraphComponent() {

        val edges = mutableListOf<Edge>()

        companion object {
            var selected: Node? = null
        }


        override fun select() {
            selected = this
        }

        override fun isSelected(): Boolean {
            return this === selected
        }

        override fun update(fingerX: Float, fingerY: Float) {
            x = fingerX
            y = fingerY
        }

        override fun draw(canvas: Canvas) {
            canvas.drawCircle(x, y, NODE_RADIUS, if ( isSelected() ) selectedArcPaint else arcPaint)
        }

        override fun contains(fingerX: Float, fingerY: Float): Boolean {

            val radius = NODE_RADIUS + ENLARGE_TOUCH

            return fingerX in (x - radius)..(x + radius) &&
                    fingerY in (y - radius)..(y + radius)
        }


    }


    protected open class Edge(val from: Node, val to: Node, var curve: Float) : GraphComponent() {

        companion object {
            var selected: Edge? = null
        }


        override fun select() {
            selected = this
        }

        override fun isSelected(): Boolean {
            return this === selected
        }

        override fun update(fingerX: Float, fingerY: Float) {
            curve = getDistanceToLineSigned(fingerX, fingerY, from.x, from.y, to.x, to.y)
        }

        override fun draw(canvas: Canvas) {
            canvas.drawStraightEdge(
                    from.x, from.y, to.x, to.y, getArcPaint(), NODE_RADIUS
            )
        }

        override fun contains(fingerX: Float, fingerY: Float): Boolean {

            return if ( curve == 0f ) {
                val dist = getDistanceToLine(fingerX, fingerY, from.x, from.y, to.x, to.y)
                dist in 0f..2* ENLARGE_TOUCH
            } else {
                val radius = findRadius(from.x, from.y, to.x, to.y, curve)
                val center = getCenter(from.x, from.y, to.x, to.y, radius)
                val distance = getDistance(center.x, center.y, fingerX, fingerY)
                val fromCurve = Math.abs(distance - Math.abs(radius))

                val diffSides = isAboveLine(center.x, center.y, from.x, from.y, to.x, to.y) !=
                        isAboveLine(fingerX, fingerY, from.x, from.y, to.x, to.y)
                val inEdge = fromCurve in 0f..2*ENLARGE_TOUCH

                diffSides && inEdge
            }

        }

    }


    /**
     *
     * nodes and edges provide the base of the data structure
     * that stores the whole graph.
     * TODO: Decidere se tenere o meno edges
     */
    protected val nodes = mutableListOf<Node>()
    protected val edges = mutableListOf<Edge>()



    /**
     *
     * Operations on the lists or selected items
     *
     */
    protected open fun getComponentIterable(): Iterable<GraphComponent> {
        return nodes union edges
    }

    protected open fun deselectAll() {
        Node.selected = null
        Edge.selected = null
    }

    protected open fun getSelectedComponent(): GraphComponent? {
        return if ( Node.selected !== null) Node.selected else Edge.selected
    }

    protected open fun drawComponents(canvas: Canvas) {

        for (node in nodes) {
            node.draw(canvas)
        }

        for ( edge in edges ) {
            edge.draw(canvas)
        }
    }



    /**
     *
     * Functions to retrieve components when clicked
     *
     */
    protected fun getClickedComponent(x: Float, y: Float): GraphComponent? {
        return getComponentIterable().find { it.contains(x, y) }
    }

    protected fun getClickedNode(x: Float, y: Float): Node? {
        return nodes.find { it.contains(x, y) }
    }

    protected fun getClickedEdge(x: Float, y: Float): Edge? {
        return edges.find { it.contains(x, y) }
    }


    protected fun getClickedComponent(e: MotionEvent): GraphComponent? {
        return getClickedComponent(e.x, e.y)
    }

    protected fun getClickedNode(e: MotionEvent): Node? {
        return getClickedNode(e.x, e.y)
    }

    protected fun getClickedEdge(e: MotionEvent): Edge? {
        return getClickedEdge(e.x, e.y)
    }




    /**
     *
     * Subclass specific methods
     *
     */
    protected abstract fun createNode(x: Float, y: Float)
    protected abstract fun createEdge(firstNode: Node, node: Node)
    protected abstract fun removeNode(node: Node)
    protected abstract fun removeEdge(edge: Edge)



    /**
     *
     * Gestures event listeners
     *
     */
    private fun firstPointerDown(e: MotionEvent) {
        getClickedComponent(e)?.select()
        graphInvalidate()
    }

    private fun firstPointerUp() {
        deselectAll()
        graphInvalidate()
    }


    private fun firstPointerMove(e: MotionEvent) {
        getSelectedComponent()?.update(e)
        graphInvalidate()
    }

    protected open fun secondPointerDown(x: Float, y: Float) {

        val firstNode = Node.selected
        val node = getClickedNode(x, y)

        if ( node != null ) {

            if (firstNode != null) {

                createEdge(firstNode, node)
                graphInvalidate()

            }
        }

    }

    protected open fun longPress(e: MotionEvent) {
    }

    protected open fun doubleTap(e: MotionEvent) {
        val node = getClickedNode(e)

        if ( node == null ) {
            createNode(e.x, e.y)
            graphInvalidate()
        }
    }

    protected open fun fling() {
        val node = Node.selected
        val edge = Edge.selected

        when {
            node != null -> {
                removeNode(node)
                graphInvalidate()
            }

            edge !== null -> {
                removeEdge(edge)
                graphInvalidate()
            }
        }

    }




    /**
     *
     *  Init Graph Gestures listeners
     *
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initGraphGestures() {

        val gestures = GestureDetectorCompat(this, GraphGestures())
        graphView.setOnTouchListener {view, motionEvent ->
            gestures.onTouchEvent(motionEvent)

            if (motionEvent != null ) {
                when( motionEvent.actionMasked ) {
                    MotionEvent.ACTION_DOWN -> firstPointerDown(motionEvent)
                    MotionEvent.ACTION_MOVE -> firstPointerMove(motionEvent)
                    MotionEvent.ACTION_POINTER_DOWN -> {
                        val pointerIndex = motionEvent.actionIndex
                        if ( pointerIndex == 1 ) {
                            secondPointerDown(motionEvent.getX(pointerIndex),
                                    motionEvent.getY(pointerIndex))
                        }
                    }
                    MotionEvent.ACTION_UP -> firstPointerUp()
                }

            }
            true
        }

    }

    private inner class GraphGestures : GestureDetector.SimpleOnGestureListener() {

        private val VELOCITY_TRESHOLD = 7000

        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            if (e != null) {
                doubleTap(e)
            }
            return e != null
        }

        override fun onLongPress(e: MotionEvent?) {
            if (e != null) {
                longPress(e)
            }
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {

            if ( e1 != null && e2 != null) {
                val velocity = Math.sqrt(Math.pow(velocityX.toDouble(), 2.0) +
                        Math.pow(velocityY.toDouble(), 2.0))

                if ( velocity > VELOCITY_TRESHOLD) {
                    fling()
                }

            }

            return e1 != null && e2 != null
        }

    }

}
