package com.loic.spe95.signin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.loic.spe95.data.Result
import com.loic.spe95.data.SingleLiveEvent
import com.loic.spe95.signin.data.LoginRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class LoginViewModel(
    private val repository: LoginRepository
) :
    ViewModel(), CoroutineScope {
    // set coroutine context
    private val compositeJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + compositeJob

    // -- Coroutine jobs
    private var getUserJob: Job? = null

    private val _loginSucceeded = SingleLiveEvent<Any>()
    val loginSucceed = _loginSucceeded

    // VM attributes
    val _email: MutableLiveData<String> = MutableLiveData()
    val email: LiveData<String> = _email
    var _emailError: MutableLiveData<String> = MutableLiveData()

    val _password: MutableLiveData<String> = MutableLiveData()
    val password: LiveData<String> = _password
    var _passwordError: MutableLiveData<String> = MutableLiveData()

    var loginError: MutableLiveData<String> = MutableLiveData()

    /**
     * Login into Firebase
     */
    fun login() {
        if (getUserJob?.isActive == true) getUserJob?.cancel()
        getUserJob = launch {
            when (val result =
                repository.loginUser(email.value!!, password.value!!)) {
                is Result.Success -> _loginSucceeded.call()
                is Result.Error   -> loginError.value = result.exception.toString()
                //is Result2.Canceled -> _snackbarText.value = R.string.canceled
            }
        }
    }

    fun onEmailTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty())
            _emailError.value = "Champ obligatoire"
        else
            _emailError.value = null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
            _emailError.value = "Email KO"
        } else {
            _emailError.value = null
        }
    }

    fun onPasswordTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty())
            _passwordError.value = "Champ obligatoire"
        else
            _passwordError.value = null
    }
}