package g.frith.graphomania

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v7.app.AlertDialog
import java.io.File
import java.util.regex.Pattern


class MainActivity : AppCompatActivity(), ProjecManager {

    private val filePattern = Pattern.compile("^([a-z]+)_(\\w+).json$")

    private val types = mapOf(
            "automata" to AutomataActivity::class.java,
            "graph" to GraphActivity::class.java
    )

    override fun launchProject(type: String, name: String) {
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
        val ft =  supportFragmentManager.beginTransaction()
        NewProjectDialog.newInstance(types.keys.toTypedArray())
                .show(ft, "New Project")
    }

    private fun showLoadProjectDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose a project")

        val typeList = mutableListOf<String>()
        val nameList = mutableListOf<String>()

        val optionsList = mutableListOf<String>()

        for (file in fileList()) {
            Log.d("Main", file)
            val matcher = filePattern.matcher(file)
            if (matcher.matches()) {
                //PER CANCELLARE
                //File(filesDir, file).delete()
                val type = matcher.group(1)
                val name = matcher.group(2)

                typeList.add(type)
                nameList.add(name)

                optionsList.add("$name ($type)")
            }
        }

        builder.setItems(optionsList.toTypedArray(), { dialog, which ->
            launchProject(typeList[which], nameList[which])
        })

        builder.create().show()
    }
}
