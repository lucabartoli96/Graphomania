package g.frith.graphomania

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout

fun Activity.alert(title: CharSequence? = null,
                   message: CharSequence? = null,
                   func: (AlertDialogHelper.() -> Unit)? = null): AlertDialog {
    return if ( func !== null ) {
        AlertDialogHelper(this, title, message).apply(func)
    } else {
        AlertDialogHelper(this, title, message)
    }.create()

}

fun Activity.alert(titleResource: Int = 0,
                   messageResource: Int = 0,
                   func: (AlertDialogHelper.() -> Unit)? = null): AlertDialog {
    val title = if (titleResource == 0) null else getString(titleResource)
    val message = if (messageResource == 0) null else getString(messageResource)

    return if ( func !== null ) {
        AlertDialogHelper(this, title, message).apply(func)
    } else {
        AlertDialogHelper(this, title, message)
    }.create()

}

fun Fragment.alert(title: CharSequence? = null,
                   message: CharSequence? = null,
                   func: (AlertDialogHelper.() -> Unit)? = null): AlertDialog {
    return if ( func !== null ) {
        AlertDialogHelper(context!!, title, message).apply(func)
    } else {
        AlertDialogHelper(context!!, title, message)
    }.create()
}

fun Fragment.alert(titleResource: Int = 0,
                   messageResource: Int = 0,
                   func: (AlertDialogHelper.() -> Unit)? = null): AlertDialog {

    val title = if (titleResource == 0) null else getString(titleResource)
    val message = if (messageResource == 0) null else getString(messageResource)
    return if ( func !== null ) {
        AlertDialogHelper(context!!, title, message).apply(func)
    } else {
        AlertDialogHelper(context!!, title, message)
    }.create()
}


@SuppressLint("InflateParams")
class AlertDialogHelper(context: Context,
                        title: CharSequence?,
                        message: CharSequence?) {

    private val ctx = context
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context)
    private var dialog: AlertDialog? = null
    private var dialogInit: (View.()->Unit)? = null

    private var view: View? = null

    private var yesButtonText: CharSequence? = null
    private var noButtonText: CharSequence? = null
    private var yesButtonFunc: (View.()->Unit)? = null
    private var noButtonFunc: (View.()->Unit)? = null

    private var items: MutableList<String>? = null
    private var itemSelected: ((Int)->Unit)? = null

    val yesButton: Button?
        get() = dialog?.getButton(Dialog.BUTTON_POSITIVE)
    val noButton: Button?
        get() = dialog?.getButton(Dialog.BUTTON_NEGATIVE)
    val neutralButton: Button?
        get() = dialog?.getButton(Dialog.BUTTON_NEUTRAL)



    init {
        builder.setTitle(title)
        builder.setMessage(message)
    }

    fun onShow(init: View.()->Unit) {
        dialogInit = init
    }


    fun positiveButton(@StringRes textResource: Int, func: (View.() -> Unit)? = null) {
        yesButtonText = ctx.getString(textResource)
        yesButtonFunc = func
    }

    fun positiveButton(text: CharSequence, func: (View.() -> Unit)? = null) {
        yesButtonText = text
        yesButtonFunc = func
    }

    fun negativeButton(@StringRes textResource: Int, func: (View.() -> Unit)? = null) {
        noButtonText = ctx.getString(textResource)
        noButtonFunc = func
    }

    fun negativeButton(text: CharSequence, func: (View.() -> Unit)? = null) {
        noButtonText = text
        noButtonFunc = func
    }

    fun onCancel(func: () -> Unit) {
        builder.setOnCancelListener { func() }
    }

    fun view(@LayoutRes layoutRes: Int, func: (View.() -> Unit)? = null) {
        when {
            view !== null -> throw Exception("View already set")
            items !== null -> throw Exception("List already set")
            else -> {
                view = LayoutInflater.from(ctx).inflate(layoutRes, null)
                func?.let { view?.apply(it) }
            }
        }
    }

    fun view(func: () -> View) {
        when {
            view !== null -> throw Exception("View is already set, cannot set content twice")
            items !== null -> throw Exception("List already set, cannot set content twice")
            else -> {
                view = func()
            }
        }
    }

    fun list(fill: MutableList<String>.()->Unit) {
        when {
            view !== null -> throw Exception("View already set, cannot set content twice")
            items !== null -> throw Exception("List already set, cannot set content twice")
            else -> {
                items = mutableListOf()
                items?.apply(fill)
            }
        }
    }

    fun onItemSelected(handler: (Int)->Unit) {
        when {
            view !== null -> throw Exception("No list is set")
            items === null -> throw Exception("List not set yet")
            else -> {
                itemSelected = handler
            }
        }
    }

    fun create(): AlertDialog {

        val storeItems = items

        dialog = if ( storeItems !== null ) {
            Log.d("create", storeItems.toString())
            builder.setItems(storeItems.toTypedArray(), {_, which ->
                itemSelected?.invoke(which)
            })
        } else {

            if( view == null ) {
                view = LinearLayout(ctx)
                view?.let {
                    it.layoutParams = ViewGroup.LayoutParams(0, 0)
                }
            }

            builder.setView(view)
            builder
                    .setPositiveButton(yesButtonText, {_, _ ->
                        yesButtonFunc?.let {
                            view?.apply(it)
                        }
                    })
                    .setNegativeButton(noButtonText, {_, _ ->
                        noButtonFunc?.let {
                            view?.apply(it)
                        }
                    })
        }.create()

        dialog?.setOnShowListener {
            dialogInit?.let { view?.apply(it) }
        }

        return dialog!!
    }

}