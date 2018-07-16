package g.frith.graphomania

import android.os.AsyncTask
import java.util.*


class Procedure<A, B, C>(init: Procedure<A, B, C>.()->Unit) {

    private val checkPoints = mutableMapOf<String, Pair<(Array<out B?>)->Unit, Long>>()

    private var startAction: (()->Unit)? = null
    private var endAction: ((C)->Unit)? = null
    private var currentAnimation: Animation? = null

    private lateinit var code: Animation.(Array<out A>)-> C

    init {
        apply(init)
    }

    fun start(action: () -> Unit) {
        startAction = action
    }

    fun end(action: (C) -> Unit) {
        endAction = action
    }

    fun abort() {
        val storeAnimation = currentAnimation
        if (storeAnimation !== null && storeAnimation.status != AsyncTask.Status.FINISHED) {
            storeAnimation.cancel(true)
        }
    }

    fun checkPoint(checkPoint: String, time: Long = 500, action: (Array<out B?>)->Unit) {
        checkPoints[checkPoint] = Pair(action, time)
    }

    fun procedure(p: Animation.(Array<out A>)->C) {
        code = p
    }

    operator fun invoke(vararg args: A) {
        startAction?.invoke()
        currentAnimation = Animation()
        currentAnimation?.execute(*args)
    }

    inner class Animation : AsyncTask<A, B, C>() {

        private val reached = LinkedList<String>()

        fun checkPoint(checkPoint: String, vararg args: B) {
            reached.add(checkPoint)
            publishProgress(*args)
            Thread.sleep(checkPoints[checkPoint]?.second ?: 500)
        }

        override fun doInBackground(vararg args: A): C {
            return code(args)
        }

        override fun onPostExecute(result: C) {
            endAction?.invoke(result)
        }

        override fun onProgressUpdate(vararg args: B?) {
            synchronized(reached) {
                val checkPoint = reached.poll()
                checkPoints[checkPoint]?.first?.invoke(args)
            }
        }

        fun procedure(vararg args: A): C {
            return code(args)
        }

    }
}