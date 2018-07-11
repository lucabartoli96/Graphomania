package g.frith.graphomania

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper


class Procedure<A, B, C>(init: Procedure<A, B, C>.()->Unit) {

    private val checkPoints = mutableMapOf<String, Pair<(Array<out B?>)->Unit, Long>>()

    private var startAction: (()->Unit)? = null
    private var endAction: ((C)->Unit)? = null
    private var currentCheckPoint: ((Array<out B?>)->Unit)? = null

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

    fun checkPoint(checkPoint: String, time: Long = 500, action: (Array<out B?>)->Unit) {
        checkPoints[checkPoint] = Pair(action, time)
    }

    fun procedure(p: Animation.(Array<out A>)->C) {
        code = p
    }

    operator fun invoke(vararg args: A) {
        startAction?.invoke()
        Animation().execute(*args)
    }

    inner class Animation : AsyncTask<A, B, C>() {

        fun checkPoint(checkPoint: String, vararg args: B) {
            currentCheckPoint = checkPoints[checkPoint]?.first
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
            currentCheckPoint?.invoke(args)
        }

        fun procedure(vararg args: A): C {
            return code(args)
        }

    }
}