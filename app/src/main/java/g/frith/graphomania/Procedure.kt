package g.frith.graphomania

import kotlin.concurrent.thread


fun procedure() {

}

class Procedure(val runnable: Procedure.()->Unit) {

    private var procedure = Thread { runnable() }
    private val checkPoints = mutableMapOf<String, ()->Unit>()

    private var delay = 100

    fun put(checkPoint: String, action: ()->Unit) {
        checkPoints.put(checkPoint, action)
    }

    fun remove(checkPoint: String) {
        checkPoints.remove(checkPoint)
    }

    fun reached(checkPoint: String) {
        checkPoints[checkPoint]?.invoke()
    }

    operator fun invoke() {
        procedure.start()
    }

}