package g.frith.graphomania

import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.fragment_symbol_picker_dialog.view.*


private const val SELECTED = "selected"
private const val DISABLED = "disabled"


class SymbolPickerDialog : DialogFragment() {

    private var okButton: Button? = null

    private lateinit var disabled: CharArray
    private var symbols = mutableSetOf<Char>()

    private var listener: OnFragmentInteractionListener? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            disabled = it.getCharArray(DISABLED)
            symbols.addAll(it.getCharArray(SELECTED).toMutableList())
        }
    }

    private fun getLinearLayout(orientation: Int): LinearLayout {
        val ll = LinearLayout(context)
        ll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        )
        ll.orientation = orientation
        return ll
    }

    private fun getToggleButton(symbol: Char): ToggleButton {
        val button = ToggleButton(context)
        //button.text = symbol.toString()
        button.text = symbol.toString()
        button.textOff = symbol.toString()
        button.textOn = symbol.toString()
        button.layoutParams = LinearLayout.LayoutParams(100, 100)
        button.typeface = Typeface.DEFAULT

        when ( symbol ) {
            in symbols -> {
                button.isChecked = true
                button.setBackgroundColor(Color.LTGRAY)
            }
            in disabled -> {
                button.isEnabled = false
            }
            else -> {
                button.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        return button
    }

    private fun getSymbolsPad(): LinearLayout {

        val pad = getLinearLayout(LinearLayout.VERTICAL)

        var i = 0
        var row: LinearLayout? = null

        for ( c in  'a'..'z' ) {

            if ( i == 0 ) {

                if ( row !== null ) {
                    pad.addView(row)
                }

                row = getLinearLayout(LinearLayout.HORIZONTAL)
            }

            val button = getToggleButton(c)

            button.setOnCheckedChangeListener { buttonView, isChecked ->

                val symbol = buttonView.text[0]

                if ( isChecked ) {
                    symbols.add(symbol)
                } else {
                    symbols.remove(symbol)
                }

                buttonView.setBackgroundColor(if ( isChecked ) Color.LTGRAY else Color.TRANSPARENT)
                okButton?.isEnabled = !symbols.isEmpty()
            }

            row?.addView(button)

            i = (i + 1) % 6

        }

        if ( i != 0 ) {
            pad.addView(row)
        }

        return pad
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.fragment_symbol_picker_dialog, null)

        view.symbolsContainer.addView(getSymbolsPad())

        builder.setView(view)
                .setPositiveButton(R.string.ok,  { dialog, id ->
                    listener?.onSymbolsPicked(symbols.toList())
                })
                .setNegativeButton(R.string.cancel, { dialog, id ->
                    listener?.onSymbolsCancel()
                    getDialog().cancel()
                })

        return  builder.create()
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        okButton?.isEnabled = !symbols.isEmpty()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onSymbolsPicked(symbols: List<Char>)
        fun onSymbolsCancel()
    }

    companion object {
        @JvmStatic
        fun newInstance(disabled: List<Char>, selected: List<Char>? = null) =
                SymbolPickerDialog().apply {
                    arguments = Bundle().apply {
                        this.putCharArray(
                                DISABLED,
                                disabled.toCharArray()
                        )
                        this.putCharArray(
                                SELECTED,
                                selected?.toCharArray() ?: CharArray(0)
                        )
                    }
                }
    }
}
