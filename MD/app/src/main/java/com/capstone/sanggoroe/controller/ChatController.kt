package com.capstone.sanggoroe.controller

import android.content.Context
import android.widget.Toast
import com.capstone.sanggoroe.R
import com.capstone.sanggoroe.adapter.FirebaseMessageAdapter
import com.capstone.sanggoroe.model.Message
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class ChatController(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val currentUser: FirebaseUser? = auth.currentUser
    private val db: FirebaseDatabase = Firebase.database
    private val messagesRef: DatabaseReference = db.reference.child(MESSAGES_CHILD)
    private lateinit var messageAdapter: FirebaseMessageAdapter

    fun sendMessage(messageText: String) {
        val firebaseUser = currentUser ?: return
        val friendlyMessage = Message(
            messageText,
            firebaseUser.displayName.toString(),
            firebaseUser.photoUrl.toString(),
            Date().time
        )
        messagesRef.push().setValue(friendlyMessage) { error, _ ->
            if (error != null) {
                Toast.makeText(
                    context, context.getString(R.string.send_error),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.send_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun createMessageAdapter(): FirebaseMessageAdapter {
        val options = FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(messagesRef, Message::class.java)
            .build()
        messageAdapter = FirebaseMessageAdapter(options, currentUser?.displayName)
        return messageAdapter
    }

    fun startListening() {
        messageAdapter.startListening()
    }

    fun stopListening() {
        messageAdapter.stopListening()
    }


    companion object {
        private const val MESSAGES_CHILD = "messages"
    }
}