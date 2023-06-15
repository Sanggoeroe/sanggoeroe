package com.capstone.sanggoroe.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capstone.sanggoroe.R
import com.capstone.sanggoroe.adapter.PostAdapter
import com.capstone.sanggoroe.data.api.ApiConfig
import com.capstone.sanggoroe.databinding.FragmentHomeBinding
import com.capstone.sanggoroe.model.Post
import com.capstone.sanggoroe.model.RecommendResponse
import com.capstone.sanggoroe.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth
    private val binding get() = _binding!!

    private val postList = mutableListOf<Post>()
    private lateinit var postAdapter: PostAdapter
    private lateinit var userSkills: List<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

//    private fun setupRecyclerView() {
//        postAdapter = PostAdapter(requireContext(), postList)
//        binding.rvListHome.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvListHome.adapter = postAdapter
//    }


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
                        postAdapter = PostAdapter(requireContext(), postList, userSkills)
                        binding.rvListHome.layoutManager = LinearLayoutManager(requireContext())
                        binding.rvListHome.adapter = postAdapter
                        loadPosts()
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
                val recommendResponse = response.body()
                Log.d(TAG, "onResponse: ${response}")
                Toast.makeText(requireContext(), "Berikut adalah rekomendasi yang sesuai dengan skill Anda", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<RecommendResponse>, t: Throwable) {
                Log.e(TAG, "Error getting recommendations", t)
                Toast.makeText(requireContext(), "Error getting recommendations", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun loadPosts() {
        FirebaseFirestore.getInstance().collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val newPostList = mutableListOf<Post>()
                for (document in documents) {
                    val post = document.toObject(Post::class.java)
                    newPostList.add(post)
                }
                postAdapter.setItem(newPostList)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bottom_nav_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.navigation_home -> {
                findNavController().navigate(R.id.navigation_home)
                true
            }
            R.id.navigation_chat -> {
                findNavController().navigate(R.id.navigation_chat)
                true
            }
            R.id.navigation_post -> {
                findNavController().navigate(R.id.navigation_post)
                true
            }
            R.id.navigation_notifications -> {
                findNavController().navigate(R.id.navigation_notifications)
                true
            }
            R.id.navigation_profile -> {
                findNavController().navigate(R.id.navigation_profile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "HomeFragment"
    }
}