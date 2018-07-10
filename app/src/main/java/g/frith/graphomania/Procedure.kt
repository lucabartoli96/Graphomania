package g.frith.graphomania

import android.os.Handler
import android.os.Looper


class Procedure(init: Procedure.()->Unit) {

    private val checkPoints = mutableMapOf<String, Pair<()->Unit, Long>>()

    private var startAction: (()->Unit)? = null
    private var endAction: (()->Unit)? = null

    private lateinit var code: (Array<out Any>)->Unit

    init {
        apply(init)
    }

    fun start(action: () -> Unit) {
        startAction = action
    }

    fun end(action: () -> Unit) {
        endAction = action
    }

    fun checkPoint(checkPoint: String, time: Long = 500, action: ()->Unit) {
        checkPoints[checkPoint] = Pair(action, time)
    }

    fun checkPoint(checkPoint: String) {
        checkPoints[checkPoint]?.first?.invoke()
        Thread.sleep(checkPoints[checkPoint]?.second ?: 500)
    }

    fun remove(checkPoint: String) {
        checkPoints.remove(checkPoint)
    }

    fun procedure(p: ((Array<out Any>)->Unit)?) {
        if ( p !== null ) {
            code = p
        }
    }

    fun procedure(vararg args: Any) {
        code.invoke(args)
    }

    operator fun invoke(vararg args: Any) {
        Thread {
            startAction?.invoke()
            code(args)
            endAction?.invoke()
        }.start()
    }

}