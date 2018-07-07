package g.frith.graphomania


class Procedure(val runnable: Procedure.()->Unit) {

    private val checkPoints = mutableMapOf<String, ()->Unit>()

    private var startAction: (()->Unit)? = null
    private var endAction: (()->Unit)? = null


    fun start(action: () -> Unit): Procedure {
        startAction = action
        return this
    }

    fun end(action: () -> Unit): Procedure {
        endAction = action
        return this
    }

    fun put(checkPoint: String, action: ()->Unit): Procedure {
        checkPoints.put(checkPoint, action)
        return this
    }

    fun remove(checkPoint: String): Procedure {
        checkPoints.remove(checkPoint)
        return this
    }

    fun reached(checkPoint: String) {
        checkPoints[checkPoint]?.invoke()
        Thread.sleep(500)
    }

    operator fun invoke() {
        Thread {
            startAction?.invoke()
            runnable()
            endAction?.invoke()
        }.start()
    }

}