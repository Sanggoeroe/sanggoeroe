package com.capstone.sanggoroe.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capstone.sanggoroe.databinding.ActivityRegisterBinding
import com.capstone.sanggoroe.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.txtLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


        binding.btnRegister.setOnClickListener {
            val username = binding.edtUsername.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (validateInput(username, email, password)) {
                RegisterFirebase(username, email, password)
            }
        }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.edtUsername.error = "Username is required"
            binding.edtUsername.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            binding.edtEmail.error = "Email is required"
            binding.edtEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.edtEmail.error = "Please provide valid email"
            binding.edtEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            binding.edtPassword.error = "Password is required"
            binding.edtPassword.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.edtPassword.error = "Password harus lebih dari 6 karakter"
            binding.edtPassword.requestFocus()
            return false
        }

        return true
    }

    private fun RegisterFirebase(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    user!!.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Email verification sent", Toast.LENGTH_SHORT).show()
                            }
                        }

                    // Menyimpan username di Firestore
                    val db = FirebaseFirestore.getInstance()
                    val userData: MutableMap<String, Any> = HashMap()
                    userData["username"] = username
                    userData["email"] = email

                    db.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Username added", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to add username", Toast.LENGTH_SHORT).show()
                        }

                    Toast.makeText(this, "Register Success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Register Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
