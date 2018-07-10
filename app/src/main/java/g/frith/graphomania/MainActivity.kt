package g.frith.graphomania

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.view.View
import kotlinx.android.synthetic.main.fragment_new_project.view.*
import java.io.File
import java.util.regex.Pattern


class MainActivity : AppCompatActivity() {

    private val filePattern = Pattern.compile("^([a-z]+)_(\\w+).json$")
    private val namePattern = Regex("^\\w+$")
    private val fileName = { type: String, name: String -> "${type}_$name.json" }

    private val types = mapOf(
            "automata" to AutomataActivity::class.java,
            "graph" to GraphActivity::class.java
    )

    private fun launchProject(type: String, name: String) {
        val intent = Intent(this, types[type])
        intent.putExtra("name", name)
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newProject.setOnClickListener { showNewProjectDialog() }
        loadProject.setOnClickListener { showLoadProjectDialog() }

    }


    private fun showNewProjectDialog() {
        alert ("New Project",
                "Pick a type and a name") {

            onShow {
                Log.d("new project", yesButton.toString())
                yesButton?.isEnabled = false
            }

            view(R.layout.fragment_new_project) {
                errorMsg.visibility = View.GONE
                typeSpinner.drowDown(types.keys.toTypedArray())
                nameInput.onTextChanged {
                    errorMsg.visibility = View.GONE

                    if (it.isEmpty()) {
                        yesButton?.isEnabled = false
                    } else if (!it.matches(namePattern)) {
                        errorMsg.text = getString(R.string.dont_match)
                        errorMsg.visibility = View.VISIBLE
                        yesButton?.isEnabled = false
                    } else {
                        val exists = File(filesDir, fileName(
                                typeSpinner.selectedItem.toString(),
                                it.toString()
                        )).exists()

                        if (exists) {
                            errorMsg.text = getString(R.string.name_exists)
                            errorMsg.visibility = View.VISIBLE
                            yesButton?.isEnabled = false
                        } else {
                            yesButton?.isEnabled = true
                        }
                    }
                }

                positiveButton(R.string.ok, {
                    launchProject(
                            typeSpinner.selectedItem.toString(),
                            nameInput.text.toString()
                    )
                })

                negativeButton(R.string.cancel)
            }

        }.show()
    }

    private fun showLoadProjectDialog() {

        val typeList = mutableListOf<String>()
        val nameList = mutableListOf<String>()
        val list = mutableListOf<String>()

        for (file in fileList()) {
            val matcher = filePattern.matcher(file)
            if (matcher.matches()) {
                //PER CANCELLARE
                //File(filesDir, file).delete()
                val type = matcher.group(1)
                val name = matcher.group(2)

                typeList.add(type)
                nameList.add(name)

                list.add("$name ($type)")
            }
        }

        alert(R.string.choose_project, list) {

            onItemSelected {
                launchProject(typeList[it], nameList[it])
            }

        }.show()
    }
}
