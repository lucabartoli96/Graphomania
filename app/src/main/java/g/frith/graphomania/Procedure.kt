package g.frith.graphomania


class Procedure(val init: Procedure.()->Unit) {

    private val checkPoints = mutableMapOf<String, Pair<()->Unit, Long>>()

    private var startAction: (()->Unit)? = null
    private var endAction: (()->Unit)? = null

    private lateinit var code: (Array<out Any>)->Unit

    fun start(action: () -> Unit): Procedure {
        startAction = action
        return this
    }

    fun end(action: () -> Unit): Procedure {
        endAction = action
        return this
    }

    fun checkPoint(checkPoint: String, time: Long = 500, action: ()->Unit): Procedure {
        checkPoints[checkPoint] = Pair(action, time)
        return this
    }

    fun checkPoint(checkPoint: String) {
        checkPoints[checkPoint]?.first?.invoke()
        Thread.sleep(checkPoints[checkPoint]?.second ?: 500)
    }

    fun remove(checkPoint: String): Procedure {
        checkPoints.remove(checkPoint)
        return this
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