package com.capstone.sanggoroe.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.sanggoroe.databinding.ActivityLoginBinding
import com.capstone.sanggoroe.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        binding.signUpText.setOnClickListener {
            Intent(this, RegisterActivity::class.java).also {
                startActivity(it)
            }
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if(email.isEmpty()) {
                binding.emailEditText.error = "Email is required"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }
            if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = "Please enter a valid email"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.passwordEditText.error = "Password is required"
                binding.passwordEditText.requestFocus()
                return@setOnClickListener
            }
            if (password.length < 8) {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                binding.passwordEditText.requestFocus()
                return@setOnClickListener
            }
            loginUser(email, password)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful) {
                    Intent(this, MainActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(it)
                    }
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            Intent(this, MainActivity::class.java).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(it)
            }
        }
    }
//    private lateinit var loginViewModel: LoginViewModel
//

//
//
//        val viewModelFactory = ViewModelFactory(Auth)
//        loginViewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
//
//
//        binding.loginButton.setOnClickListener {
//            val email = binding.emailEditText.text.toString()
//            val password = binding.passwordEditText.text.toString()
//
//            if (validateInput(email, password)) {
//                loginUser(email, password)
//            }
//        }
//    }
//
//    private fun validateInput(email: String, password: String): Boolean {
//        if (email.isEmpty()) {
//            binding.emailEditText.error = "Email is required"
//            binding.emailEditText.requestFocus()
//            return false
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.emailEditText.error = "Please provide a valid email"
//            binding.emailEditText.requestFocus()
//            return false
//        }
//
//        if (password.isEmpty()) {
//            binding.passwordEditText.error = "Password is required"
//            binding.passwordEditText.requestFocus()
//            return false
//        }
//
//        if (password.length < 6) {
//            binding.passwordEditText.error = "Password must be at least 6 characters"
//            binding.passwordEditText.requestFocus()
//            return false
//        }
//
//        return true
//    }
//
//    private fun loginUser(email: String, password: String) {
//        loginViewModel.login(email, password).observe(this) { result ->
//            when (result) {
//                is Result.Success -> {
//                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
//                    // Perform necessary actions upon successful login
//                }
//                is Result.Error -> {
//                    Toast.makeText(
//                        this,
//                        "Login failed: ${result.error.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                is Result.Loading -> {
//                    // Show loading indicator or perform other actions during login
//                }
//            }
//        }
//    }
}


