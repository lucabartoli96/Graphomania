package g.frith.graphomania

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_input_dialog.view.*


class InputDialog : DialogFragment() {

    private var okButton: Button? = null

    private var listener: OnFragmentInteractionListener? = null

    private fun initInput(view: View) {

        view.errMsg.setTextColor(Color.RED)
        view.errMsg.text = getString(R.string.wrong_input)
        view.errMsg.visibility = View.GONE

        view.input.addTextChangedListener( object : TextWatcher {

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                view.errMsg.visibility = View.GONE
                var isAdmitted = true

                if ( s !== null ) {

                    for ( c in s ) {
                        isAdmitted = isAdmitted && c in 'a'..'z'
                    }

                }

                if ( !isAdmitted ) {
                    view.errMsg.visibility = View.VISIBLE
                }

                okButton?.isEnabled = !s.isNullOrEmpty() && isAdmitted
            }

            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }


        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater

        Log.d("oncreate", "Called")

        builder.setTitle(getString(R.string.choose_input))
        val view = inflater.inflate(R.layout.fragment_input_dialog, null)

        initInput(view)

        builder.setView(view)
                .setPositiveButton(R.string.ok,  { _, _ ->
                    listener?.onInputChosen(view.input.text.toString())
                })
                .setNegativeButton(R.string.cancel, { _, _ ->
                    dialog.cancel()
                })

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        okButton?.isEnabled = false
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
        fun onInputChosen(string: String)
        //fun onInputCanceled()
    }

    companion object {
        @JvmStatic
        fun newInstance() = InputDialog()
    }
}
