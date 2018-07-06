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


    private val types = mapOf(
            "automata" to AutomataActivity::class.java
    )

    override fun launchProject(type: String, name: String) {
        val intent = Intent(this, types[type])
        intent.putExtra("name", name)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        newProject.setOnClickListener {
            val ft =  supportFragmentManager.beginTransaction()
            NewProjectFragment.newInstance(types.keys.toTypedArray())
                    .show(ft, "NewProject")
        }

        loadProject.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setTitle("Choose a project")

            val pattern = Pattern.compile("^([a-z]+)_(\\w+).json$")

            val typeList = mutableListOf<String>()
            val nameList = mutableListOf<String>()

            val optionsList = mutableListOf<String>()

            for ( file in fileList() ) {
                Log.d("Main", file)
                val matcher = pattern.matcher(file)
                if ( matcher.matches() ) {
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

            val dialog = builder.create()
            dialog.show()
        }

    }
}
