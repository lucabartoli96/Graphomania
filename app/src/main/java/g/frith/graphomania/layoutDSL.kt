package g.frith.graphomania

import android.R
import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.*


fun Activity.horizontal(init: LinearLayout.()->Unit): LinearLayout {
    val ll = LinearLayout(this)
    ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    )
    ll.orientation = LinearLayout.HORIZONTAL
    ll.apply(init)
    return ll
}

fun Activity.vertical(init: LinearLayout.()->Unit): LinearLayout {
    val ll = LinearLayout(this)
    ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    )
    ll.orientation = LinearLayout.VERTICAL
    ll.apply(init)
    return ll
}

fun ViewGroup.horizontal(init: LinearLayout.()->Unit) {
    val ll = LinearLayout(context)
    ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    )
    ll.orientation = LinearLayout.HORIZONTAL
    addView(ll)
    ll.apply(init)
}

fun ViewGroup.vertical(init: LinearLayout.()->Unit) {
    val ll = LinearLayout(context)
    ll.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
    )
    ll.orientation = LinearLayout.VERTICAL
    addView(ll)
    ll.apply(init)
}


fun ViewGroup.toggleButton(init: ToggleButton.()->Unit) {
    val tB = ToggleButton(context)
    addView(tB)
    tB.apply(init)
}


fun <T> Spinner.drowDown(options: Array<T>) {
    val simpleAdapter = ArrayAdapter<T>(
            context, R.layout.simple_spinner_item, options
    )
    simpleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    adapter = simpleAdapter
}


fun EditText.onTextChanged( handler: (CharSequence)->Unit ) {

    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if ( p0 !== null) {
                handler(p0)
            }
        }
    })
}

