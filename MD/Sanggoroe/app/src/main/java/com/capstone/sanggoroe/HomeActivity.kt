package com.capstone.sanggoroe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import com.capstone.sanggoroe.adapter.UserAdapter
import com.capstone.sanggoroe.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
//    private val userController = UserController()
//    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
