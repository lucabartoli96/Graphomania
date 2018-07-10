package g.frith.graphomania

import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import android.view.View
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Canvas


const val QUALITY = 100

fun View.getBitmap(): Bitmap {
    val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(b)
    draw(canvas)
    return b
}

private fun getPublicAlbumStorageDir(albumName: String): File? {
    val dir = File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), albumName)
    if ( !dir.exists() ) {
        if (!dir.mkdirs()) {
            Log.e("Screenshots", "Directory not created")
        }
    }
    return dir
}

private fun getFileName(dir: File?, name: String): File? {

    return if ( dir !== null ) {
        var file = File(dir, "$name.png")

        var i = 1
        while (file.exists()) {
            file = File(dir, "$name($i).png")
            i++
        }

        file
    } else {
        null
    }
}


fun saveScreenshot(albumName: String, name: String, bitmap: Bitmap) {

    val dir = getPublicAlbumStorageDir(albumName)
    val file = getFileName(dir, name)

    try {
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, fos)
        fos.flush()
        fos.close()
    } catch (e: IOException) {
        Log.e("Screenshots", e.message, e)
    }
}