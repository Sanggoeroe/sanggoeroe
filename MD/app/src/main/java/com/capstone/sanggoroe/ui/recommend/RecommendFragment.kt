package com.capstone.sanggoroe.ui.recommend

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.sanggoroe.adapter.PostAdapter
import com.capstone.sanggoroe.data.api.ApiConfig
import com.capstone.sanggoroe.databinding.FragmentRecommendBinding
import com.capstone.sanggoroe.model.Post
import com.capstone.sanggoroe.model.RecommendResponse
import com.capstone.sanggoroe.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecommendFragment : Fragment() {

    private var _binding: FragmentRecommendBinding? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

    private val recommendedPosts = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter
    private lateinit var userSkills: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecommendBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        userSkills = emptyList() // Inisialisasi userSkills dengan list kosong

        postAdapter = PostAdapter(requireContext(), recommendedPosts, userSkills)
        binding.rvListRecommendation.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListRecommendation.adapter = postAdapter

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        val user = auth.currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    val userProfile = document.toObject(UserProfile::class.java)
                    if (userProfile != null) {
                        userSkills = listOfNotNull(userProfile.skill1, userProfile.skill2, userProfile.skill3)
                        loadRecommendations(userProfile.skill1, userProfile.skill2, userProfile.skill3)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user profile: ", exception)
                }
        }
    }

    private fun loadRecommendations(skill1: String?, skill2: String?, skill3: String?) {
        val skills = listOfNotNull(skill1, skill2, skill3).joinToString(",")

        ApiConfig.apiService.getRecommendations(skills).enqueue(object :
            Callback<RecommendResponse> {
            override fun onResponse(call: Call<RecommendResponse>, response: Response<RecommendResponse>) {
                if(response.isSuccessful) {
                    val recommendResponse = response.body()?.recommendResponse ?: emptyList()
                    loadPosts(recommendResponse.map { it.jobID })
                    Toast.makeText(requireContext(), "Berikut adalah rekomendasi yang sesuai dengan skill Anda", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                Log.e(TAG, "Error getting recommendations", t)
                Toast.makeText(requireContext(), "Error getting recommendations", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadPosts(recommendPostIds: List<Int>) {
        FirebaseFirestore.getInstance().collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val newPostList = mutableListOf<Post>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    if (post.jobID in recommendPostIds) {
                        newPostList.add(post)
                    }
                }
                postAdapter.setItem(newPostList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    // ... // The rest of your code stays the same

    companion object {
        private const val TAG = "RecommendFragment"
    }
}
