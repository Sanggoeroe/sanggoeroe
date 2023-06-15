package com.capstone.sanggoroe.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.sanggoroe.controller.ChatController
import com.capstone.sanggoroe.databinding.FragmentChatBinding
import com.capstone.sanggoroe.view.main.MainActivity

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatController: ChatController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatController = ChatController(requireContext())

        setupUI()
    }

    private fun setupUI() {
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageEditText.text.toString()
            chatController.sendMessage(messageText)
            binding.messageEditText.setText("")
        }

        val manager = LinearLayoutManager(requireContext())
        manager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = manager

        val adapter = chatController.createMessageAdapter()
        binding.messageRecyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        chatController.startListening()
        (activity as MainActivity).setBottomNavigationVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        chatController.stopListening()
        (activity as MainActivity).setBottomNavigationVisibility(View.VISIBLE)
    }

}