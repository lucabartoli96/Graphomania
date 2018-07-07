package g.frith.graphomania


class Procedure(val runnable: Procedure.()->Unit) {

    private val checkPoints = mutableMapOf<String, Pair<()->Unit, Long>>()

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

    fun put(checkPoint: String, action: ()->Unit, time: Long = 500): Procedure {
        checkPoints[checkPoint] = Pair(action, time)
        return this
    }

    fun remove(checkPoint: String): Procedure {
        checkPoints.remove(checkPoint)
        return this
    }

    fun reached(checkPoint: String, time: Long = 500) {
        checkPoints[checkPoint]?.first?.invoke()
        Thread.sleep(checkPoints[checkPoint]?.second ?: time)
    }

    operator fun invoke() {
        Thread {
            startAction?.invoke()
            runnable()
            endAction?.invoke()
        }.start()
    }

}