package com.capstone.sanggoroe.adapter


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capstone.sanggoroe.R
import com.capstone.sanggoroe.model.Post
import com.capstone.sanggoroe.model.UserProfile
import com.capstone.sanggoroe.ui.home.HomeFragment
import com.capstone.sanggoroe.ui.post.DetailFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    private val context: Context,
    private val postList: MutableList<Post>,
    private val userSkills: List<String>
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profileImage)
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val postImageView: ImageView = view.findViewById(R.id.postImageView)
        val postContentTextView: TextView = view.findViewById(R.id.postContentTextView)
        val postTitleTextView: TextView = view.findViewById(R.id.titleContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.itemlist_home, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.postContentTextView.text = post.content
        holder.postTitleTextView.text = post.title

        // Load post image
        Glide.with(context).load(post.image).into(holder.postImageView)

        // Get user data from Firestore
        FirebaseFirestore.getInstance().collection("users").document(post.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(UserProfile::class.java)

                    // Load username and profile image
                    if (user != null) {
                        holder.usernameTextView.text = user.username
                        Glide.with(context).load(user.profileImageUrl)
                            .into(holder.profileImage)
                    }
                }
            }

        // Check if post is a recommendation
        if (post.skills in userSkills || post.skills in userSkills || post.skills in userSkills) {
            holder.timeTextView.text = "Rekomendasi"
        } else {
            holder.timeTextView.text = formatTimestamp(post.timestamp)
        }

    }

    override fun getItemCount(): Int {
        return postList.size
    }

    fun setItem(newPost: List<Post>) {
        this.postList.clear()
        this.postList.addAll(newPost)
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Long): String {
        val date = Date(timestamp)
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }
}