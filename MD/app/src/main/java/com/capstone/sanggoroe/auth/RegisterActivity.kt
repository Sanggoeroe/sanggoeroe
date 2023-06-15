package com.capstone.sanggoroe.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capstone.sanggoroe.databinding.ActivityRegisterBinding
import com.capstone.sanggoroe.view.main.MainActivity
import com.capstone.sanggoroe.data.Result
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
//    private lateinit var registerViewModel: RegisterViewModel
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()

        binding.registerButton.setOnClickListener {
            val username = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty()) {
                binding.emailEditText.error = "Email is required"
                binding.emailEditText.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
            if (username.isEmpty()) {
                binding.nameEditText.error = "Name is required"
                binding.nameEditText.requestFocus()
                return@setOnClickListener
            }
            
            registerUser(username, email, password)
        }

        binding.loginTextView.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Intent(this, MainActivity::class.java).also {
                        it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    finish()
                } else {
                    Toast.makeText(
                        baseContext, "Registration failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }
//
//    private fun validateInput(email: String, password: String): Boolean {
//        if (email.isEmpty()) {
//            binding.edtEmail.error = "Email is required"
//            binding.edtEmail.requestFocus()
//            return false
//        }
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
//            binding.edtEmail.error = "Please provide a valid email"
//            binding.edtEmail.requestFocus()
//            return false
//        }
//
//        if (password.isEmpty()) {
//            binding.edtPassword.error = "Password is required"
//            binding.edtPassword.requestFocus()
//            return false
//        }
//
//        if (password.length < 6) {
//            binding.edtPassword.error = "Password must be at least 6 characters"
//            binding.edtPassword.requestFocus()
//            return false
//        }
//
//        return true
//    }
//
//    private fun registerUser(email: String, password: String) {
//        registerViewModel.register(email, password).observe(this) { result ->
//            when (result) {
//                is Result.Success -> {
//                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
//                    // Perform necessary actions upon successful registration
//                }
//                is Result.Error -> {
//                    Toast.makeText(
//                        this,
//                        "Registration failed: ${result.error.message}",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                is Result.Loading -> {
//                    // Show loading indicator or perform other actions during registration
//                }
//            }
//        }
//    }
}
