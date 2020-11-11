package com.pomplarg.spe95.signin.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.pomplarg.spe95.BuildConfig
import com.pomplarg.spe95.MainActivity
import com.pomplarg.spe95.databinding.ActivitySigninBinding
import com.pomplarg.spe95.utils.hideKeyboard
import org.koin.androidx.viewmodel.ext.android.viewModel

class SignInActivity : AppCompatActivity() {
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideKeyboard(binding.root)

        binding.appVersion = BuildConfig.VERSION_NAME
        binding.vmLoginForm = loginViewModel
        binding.btnSignin.setOnClickListener(View.OnClickListener {
            hideKeyboard()
            loginViewModel.login()
        })

        //Observables
        loginViewModel.loginSucceed.observe(this, Observer {
            intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        })

        loginViewModel._emailError.observe(this, Observer {
            binding.email.error = loginViewModel._emailError.value
        })

        loginViewModel._passwordError.observe(this, Observer {
            binding.password.error = loginViewModel._passwordError.value
        })

        loginViewModel.loginError.observe(this, Observer {
            Snackbar.make(
                binding.root,
                "Login error",
                Snackbar.LENGTH_LONG
            ).show()
        })
    }
}