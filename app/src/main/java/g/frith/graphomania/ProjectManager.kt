package g.frith.graphomania

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_new_project.view.*
import android.text.Editable
import android.widget.Button
import java.io.File


private const val OPTIONS = "options"


interface ProjecManager {
    fun launchProject(type: String, name: String)
}


class NewProjectDialog : DialogFragment() {

    private val fileName = { type: String, name: String -> "${type}_$name.json" }

    private var listener: ProjecManager? = null
    private lateinit var options: Array<String>
    private lateinit var okButton: Button

    private val pattern = Regex("^\\w+$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            options = it.getStringArray(OPTIONS)
        }
    }


    private fun initWidgets(view: View) {

        val adapter = ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        view.typeSpinner.adapter = adapter

        view.nameInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {


                view.errorMsg.visibility = View.GONE

                if (s.isEmpty()) {
                    okButton.isEnabled = false
                } else if (!s.matches(pattern)) {
                    view.errorMsg.text = getString(R.string.dont_match)
                    view.errorMsg.visibility = View.VISIBLE
                    okButton.isEnabled = false
                } else {
                    val exists = File(activity!!.filesDir, fileName(
                            view.typeSpinner.selectedItem.toString(),
                            s.toString()
                    )).exists()

                    if (exists) {
                        view.errorMsg.text = getString(R.string.name_exists)
                        okButton.isEnabled = false
                        view.errorMsg.visibility = View.VISIBLE
                    } else {
                        okButton.isEnabled = true
                    }
                }

                okButton.isEnabled = !s.isEmpty()
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity!!.layoutInflater

        val view = inflater.inflate(R.layout.fragment_new_project, null)

        initWidgets(view)

        builder.setView(view)
                .setPositiveButton(R.string.ok,  { dialog, id ->
                    listener?.launchProject(
                            view.typeSpinner.selectedItem.toString(),
                            view.nameInput.text.toString()
                    )
                })
                .setNegativeButton(R.string.cancel, { dialog, id ->
                    dialog.cancel()
                })

        return  builder.create()
    }

    override fun onStart() {
        super.onStart()
        okButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
        okButton.isEnabled = false
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ProjecManager) {
            listener = context
        } else {
            throw RuntimeException(context.toString() +
                    " must implement ProjectManager")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }


    companion object {
        @JvmStatic
        fun newInstance(options: Array<String>) = NewProjectDialog().apply {
            arguments = Bundle().apply {
                putStringArray(OPTIONS, options)
            }
        }
    }
}
