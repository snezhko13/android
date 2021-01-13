package com.example.project1.ui.login

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.project1.MainActivity
import com.example.project1.R
import com.example.project1.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer
            binding.registerButton.isEnabled = loginState.isDataValid
            binding.loginButton.isEnabled = loginState.isDataValid
            if (loginState.usernameError != null) {
                binding.username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                binding.password.error = getString(loginState.passwordError)
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            binding.registerButton.setOnClickListener {
                register()
            }
            binding.loginButton.setOnClickListener {
                login()
            }
        }
    }

    private fun login() {
        loading.visibility = View.VISIBLE

        FirebaseAuth.getInstance().signInWithEmailAndPassword(
            username.text.toString(),
            password.text.toString()
        )
            .addOnCompleteListener(this@LoginActivity) { task ->
                loading.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d("INFO", "User logged in")
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.w(
                        "INFO",
                        "signInWithEmail:failure",
                        task.exception
                    )
                    Toast.makeText(
                        this@LoginActivity, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun register() {
        loading.visibility = View.VISIBLE
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            username.text.toString(),
            password.text.toString()
        )
            .addOnCompleteListener(this@LoginActivity) { task ->
                loading.visibility = View.GONE
                if (task.isSuccessful) {
                    Log.d("INFO", "User registered")
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Log.w(ContentValues.TAG, "User registration failed", task.exception)
                    Toast.makeText(
                        this@LoginActivity,
                        "Registration failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}


fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}
