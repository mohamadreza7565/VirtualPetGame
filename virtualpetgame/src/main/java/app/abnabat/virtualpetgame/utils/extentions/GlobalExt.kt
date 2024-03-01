package app.abnabat.virtualpetgame.utils.extentions

import android.view.View

fun View.invisible() {
    if (visibility != View.INVISIBLE)
        visibility = View.INVISIBLE
}

fun View.changeVisibility(visible: Boolean) {
    if (visible)
        visible()
    else {
        gone()
    }
}

fun View.gone() {
    if (visibility != View.GONE)
        visibility = View.GONE
}

fun View.visible() {
    if (visibility != View.VISIBLE)
        visibility = View.VISIBLE
}
