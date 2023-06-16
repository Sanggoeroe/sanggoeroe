package com.capstone.sanggoroe.ui.post

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.capstone.sanggoroe.databinding.FragmentDetailBinding
import com.capstone.sanggoroe.model.Post
import com.capstone.sanggoroe.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        // You should pass the post as a parameter to the fragment
        val post = arguments?.getParcelable<Post>("post")
        post?.let {
            loadUserProfile(it.uid) { userProfile ->
                updateUI(it, userProfile)
            }
        }

        return view
    }

    private fun loadUserProfile(userId: String, onResult: (UserProfile) -> Unit) {
        FirebaseFirestore.getInstance().collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(UserProfile::class.java)
                userProfile?.let {
                    onResult(it)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting user profile: ", exception)
            }
    }

    private fun updateUI(post: Post, userProfile: UserProfile) {
        binding.postTitle.text = post.title
        binding.postContent.text = post.content
        binding.uploadTime.text = post.timestamp.toString() // Better to format this timestamp into a readable format

        // Using Glide library to load image from URL
        Glide.with(this)
            .load(post.image)
            .into(binding.imageView)

        // Load user profile image, if available
        Glide.with(this)
            .load(userProfile.profileImageUrl)
            .into(binding.profilePic)

        binding.username.text = post.username

        // If skill exists in Post model, you can display it like this
        // Make sure you have "skill" field in your Post model
        binding.skillTitleDetail.text = "Tag Skill: ${post.skills}"
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "DetailFragment"

        fun newInstance(post: Post): DetailFragment {
            val fragment = DetailFragment()

            // Create an instance of Bundle
            val args = Bundle()

            // Store the post object into the Bundle
            args.putParcelable("post", post)

            // Attach the Bundle to the fragment
            fragment.arguments = args

            return fragment
        }
    }
}