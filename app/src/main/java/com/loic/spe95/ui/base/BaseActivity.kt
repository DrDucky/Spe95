package com.loic.spe95.ui.base

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // Any activity may require to hide the virtual keyboard. Therefore, in the BaseActivity
    fun showKeyboard(show: Boolean) {
        if (!show) {
            val imm: InputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            //Find the currently focused view, so we can grab the correct window token from it.
            var view: View? = currentFocus
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = View(this)
            }
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }
        }
    }
}