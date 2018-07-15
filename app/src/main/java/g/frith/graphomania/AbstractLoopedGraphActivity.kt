package g.frith.graphomania

import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.PointF
import android.util.Log
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


    protected open class Loop(val node: Node, var angle: Float,
                              val delay: Long? = null) : GraphComponent() {

        companion object {
            var selected: Loop? = null
        }

        init {
            (node as LoopedNode).loop = this
        }

        private var firstTime = true
        private var animatingFract = 0f
        private var animation: ValueAnimator

        init {

            animation = Animator.get().animateInt(1, 100,
                    EDGE_ANIM_DURATION, delay) {
                animatingFract = it/100f
            }
        }

        protected val fract: Float
            get() {
                return when {
                    firstTime -> {
                        animation.start()
                        firstTime = false
                        1f/100f
                    }
                    animation.isRunning -> {
                        animatingFract
                    }
                    else -> {
                        1f
                    }
                }
            }

        protected fun isDelayed(): Boolean {
            return delay !== null && animation.isStarted && !animation.isRunning
        }

        protected fun isRunning(): Boolean {
            return firstTime || animation.isRunning
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
     * Extension of base class Abstract Graph, to include loops
     *
     */
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
        for ( node in nodes ) {
            (node as LoopedNode).loop?.draw(canvas)
        }
    }


    /**
     *
     * Retrieve components when clicked extended to loops
     *
     */
    override fun getClickedComponent(x: Float, y: Float): GraphComponent? {
        return super.getClickedComponent(x, y) ?: getClickedLoop(x, y)
    }


    protected fun getClickedLoop(x: Float, y: Float): Loop? {
        for ( node in nodes ) {
            val loop = (node as LoopedNode).loop
            if ( loop !== null && loop.contains(x, y) ) {
                return loop
            }
        }
        return null
    }

    protected fun getClickedLoop(e: MotionEvent): Loop? {
        return getClickedLoop(e.x, e.y)
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
