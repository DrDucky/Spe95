package com.pomplarg.spe95.utils

import android.view.View

fun View.rotateFab(rotate: Boolean): Boolean {
    this.animate().duration = 200
    this.animate().rotation(if (rotate) 135f else 0f)
    return rotate
}

fun fabShowIn(view: View) {
    view.visibility = View.VISIBLE
    view.alpha = 0f
    view.translationY = view.height.toFloat()
    view.animate()
        .setDuration(200L)
        .translationY(0f)
        .alpha(1f)
        .start()
}

fun fabShowOut(view: View) {
    view.visibility = View.VISIBLE
    view.alpha = 1f
    view.translationY = 0f
    view.animate()
        .setDuration(200L)
        .translationY(view.height.toFloat())
        .alpha(0f)
        .start()
}

fun fabInit(view: View) {
    view.visibility = View.GONE
    view.alpha = 0f
    view.translationY = view.height.toFloat()
}