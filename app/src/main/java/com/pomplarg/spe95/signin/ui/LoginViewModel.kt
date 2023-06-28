package com.pomplarg.spe95.signin.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pomplarg.spe95.data.Result
import com.pomplarg.spe95.signin.data.LoginRepository
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

    private val _loginSucceeded = MutableLiveData<Boolean>(false)
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
                email.value?.let { email ->
                    password.value?.let { password -> repository.loginUser(email, password) }
                }) {
                is Result.Success -> _loginSucceeded.value = true
                is Result.Error   -> loginError.value = result.exception.toString()
                else              -> {}//Nothing to do
            }
        }
    }

    fun onEmailTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty())
            _emailError.value = "Ce champ est obligatoire"
        else
            _emailError.value = null

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
            _emailError.value = "Entrez une adresse email valide"
        } else {
            _emailError.value = null
        }
    }

    fun onPasswordTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (s.isEmpty()) {
            _passwordError.value = "Ce champ est obligatoire"
        } else {
            _passwordError.value = null
        }
    }
}