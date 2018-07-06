package g.frith.graphomania

import android.graphics.Canvas
import android.view.MotionEvent

abstract class AbstractLoopedGraphActivity : AbstractGraphActivity() {


    companion object {
        const val LOOP_CURVE = 50f
    }

    /**
     *
     * Concrete "looped" components classes
     *
     */
    protected open class LoopedNode(x: Float, y: Float): Node(x, y) {
        var loop: Loop? = null
    }


    protected open class Loop(val node: Node, var angle: Float) : GraphComponent() {

        companion object {
            var selected: Loop? = null
        }

        init {
            (node as LoopedNode).loop = this
        }

        override fun select() {
            selected = this
        }

        override fun isSelected(): Boolean {
            return this === selected
        }

        override fun update(fingerX: Float, fingerY: Float) {
            angle = Math.toDegrees(
                    getInclination(node.x, node.y, fingerX, fingerY).toDouble()
            ).toFloat()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawLoop(node.x, node.y,  LOOP_CURVE, angle,
                    NODE_RADIUS, getArcPaint())
        }

        override fun contains(fingerX: Float, fingerY: Float): Boolean {

            val radius = LOOP_CURVE + ENLARGE_TOUCH

            val pX = getRotatedX(node.x, node.y, node.x + 2* LOOP_CURVE, node.y, angle)
            val pY = getRotatedY(node.x, node.y, node.x + 2* LOOP_CURVE, node.y, angle)

            return fingerX in (pX - radius)..(pX + radius) &&
                    fingerY in (pY - radius)..(pY + radius)
        }

    }


    /**
     *
     * loops completes graph data structure
     *
     */
    protected val loops = mutableListOf<Loop>()



    /**
     *
     * Extension of base class Abstract Graph, to include loops
     *
     */
    override fun getComponentIterable(): Iterable<GraphComponent> {
        return super.getComponentIterable() union loops
    }


    override fun deselectAll() {
        super.deselectAll()
        Loop.selected = null
    }

    override fun getSelectedComponent(): GraphComponent? {
        val component = super.getSelectedComponent()
        return if ( component !== null ) component else Loop.selected
    }

    override fun drawComponents(canvas: Canvas) {
        super.drawComponents(canvas)

        for ( loop in loops ) {
            loop.draw(canvas)
        }
    }


    /**
     *  Retrieve loops when clicked
     */
    protected fun getClickedLoop(e: MotionEvent): Loop? {
        return loops.find { it.contains(e.x, e.y) }
    }



    /**
     *
     * Subclass specific methods
     *
     */
    protected abstract fun createLoop(node: Node)
    protected abstract fun removeLoop(loop: Loop)



    /**
     *
     * Gestures listeners
     *
     */
    override fun secondPointerDown(x: Float, y: Float) {

        val firstNode = Node.selected
        val node = getClickedNode(x, y)

        if ( node != null ) {

            if (firstNode != null) {
                createEdge(firstNode, node)
            } else {
                createLoop(node)
            }
        }

    }


    override fun fling() {
        val node = Node.selected
        val edge = Edge.selected
        val loop = Loop.selected

        when {
            node != null -> {
                removeNode(node)
            }

            edge !== null -> {
                removeEdge(edge)
            }

            loop !== null -> {
                removeLoop(loop)
            }
        }

    }
}
