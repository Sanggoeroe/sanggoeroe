package com.capstone.sanggoroe.ui.profile

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.capstone.sanggoroe.R
import com.capstone.sanggoroe.databinding.FragmentProfileBinding
import com.capstone.sanggoroe.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: FragmentProfileBinding
    private lateinit var imageUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        loadUserProfile()

        binding.profileImage.setOnClickListener {
            intentCamera()
        }

        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_profile_to_editProfileFragment)
        }
    }

    private fun loadUserProfile() {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val docRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                        val userProfile = document.toObject(UserProfile::class.java)
                        userProfile?.let {
                            bindUserProfile(it)
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }
    }


    private fun bindUserProfile(userProfile: UserProfile) {
        binding.username.text = userProfile.username
        binding.desciption.text = userProfile.description
        Glide.with(this).load(userProfile.profileImageUrl).into(binding.profileImage)

        // Clear existing skill views
        binding.linearLayout.removeAllViews()

        val skills = listOf(userProfile.skill1, userProfile.skill2, userProfile.skill3)
        for (skill in skills) {
            val skillView = TextView(context)
            skillView.apply {
                text = skill
                setTextColor(ContextCompat.getColor(context, R.color.white))
                setBackgroundResource(R.drawable.skill_background)
                setPadding(8.dpToPx(context), 8.dpToPx(context), 8.dpToPx(context), 8.dpToPx(context))
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                params.setMargins(0, 0, 8.dpToPx(context), 0)
                layoutParams = params
            }
            binding.linearLayout.addView(skillView)
        }
    }

    // Extension function to convert dp to pixel
    fun Int.dpToPx(context: Context): Int {
        val scale = context.resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }


    private fun intentCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it)?.also {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            uploadImage(imageBitmap)
        }
    }


    private fun uploadImage(imageBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img/${FirebaseAuth.getInstance().currentUser?.uid}")
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener{
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            imageUri = it
                            binding.profileImage.setImageBitmap(imageBitmap)
                        }
                    }
                }
            }

    }


    companion object
    {
        private const val REQUEST_IMAGE_CAPTURE = 100
        private const val REQUEST_PERMISSION = 200
    }

}
