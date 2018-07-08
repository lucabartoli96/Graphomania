package g.frith.graphomania

import android.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class alert(build: alert.() -> Unit) {

    private val builder = null

    var title: String = ""
    set(value) {
        field = value
    }
}


fun editText(build: EditText.()->Unit) : EditText {

    return object : EditText(null) {

        private val actions = mutableMapOf<(CharSequence)->Boolean, (CharSequence)->Unit> ()

        infix fun ((CharSequence)->Boolean).then(action: (CharSequence)->Unit) {
            actions[this] = action
        }

        var onChanged: ((CharSequence) -> Unit)? = null

        init {

            this.addTextChangedListener ( object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                    if ( p0 !== null ) {
                        for ( key in actions.keys ) {
                            if ( key(p0) ) {
                                actions[key]?.invoke(p0)
                            }
                        }
                        onChanged?.invoke(p0)
                    }
                }


            })

        }

    }.apply(build)
}