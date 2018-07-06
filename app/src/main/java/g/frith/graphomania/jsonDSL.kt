package g.frith.graphomania

private fun <T> encodeAtomic(value: T): CharSequence {
    return when (value) {
        is CharSequence, is Char -> "\"$value\""
        is Iterable<*> -> {
            val subSb = StringBuilder()
            var first = true
            subSb.append("[")
            for (subValue in value) {

                if ( first ) {
                    first = false
                } else {
                    subSb.append(",")
                }

                subSb.append(encodeAtomic(subValue))
            }
            subSb.append("]")
        }
        else -> value.toString()
    }
}


abstract class Json


class JsonArr(vararg args: Any) : Json() {

    private val sb = StringBuilder()
    private var first = true

    init {
        sb.append("[")
        if ( !args.isEmpty()) {
            append(args)
        }
    }

    fun append(vararg args: Any) {

        for ( arg in args ) {
            if ( first ) {
                first = false
            } else {
                sb.append(",")
            }
            sb.append(encodeAtomic(arg))
        }
    }

    override fun toString(): String {
        return sb.toString() + "]"
    }

}

class JsonObj(init: JsonObj.() -> Unit) : Json() {

    private val sb = StringBuilder()
    private var first = true


    init {
        sb.append("{")
        this.init()
        sb.append("}")
    }


    infix fun <T> String.To(value: T) {

        if ( first ) {
            first = false
        } else {
            sb.append(",")
        }

        sb.append("\"$this\":")
        sb.append(encodeAtomic(value))

    }

    override fun toString(): String {
        return sb.toString()
    }
}