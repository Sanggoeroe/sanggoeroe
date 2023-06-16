package com.capstone.sanggoroe.ui.post

import android.Manifest
import android.R
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.capstone.sanggoroe.data.Constants
import com.capstone.sanggoroe.data.Constants.PERMISSION_CODE_CAMERA
import com.capstone.sanggoroe.data.Constants.PERMISSION_CODE_GALLERY
import com.capstone.sanggoroe.data.Constants.REQUEST_CODE_CAMERA
import com.capstone.sanggoroe.data.Constants.REQUEST_CODE_GALLERY
import com.capstone.sanggoroe.databinding.FragmentPostBinding
import com.capstone.sanggoroe.model.Post
import com.capstone.sanggoroe.model.RecommendResponseItem
import com.capstone.sanggoroe.model.UserProfile
import com.capstone.sanggoroe.view.main.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

class PostFragment : Fragment() {

    private var _binding: FragmentPostBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private lateinit var postImage: String
    private var recommendResponseItem: RecommendResponseItem? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPostBinding.inflate(inflater, container, false)

        // Set up the spinner
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            Constants.SKILLS
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.skillSpinner.adapter = adapter
        binding.skillSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSkill = parent?.getItemAtPosition(position) as String
                Toast.makeText(requireContext(), "Selected: $selectedSkill", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(requireContext(), "No Skill Selected", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cameraButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_CODE_CAMERA
                )
            } else {
                openCamera()
            }
        }

        binding.galleryButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_CODE_GALLERY
                )
            } else {
                openGallery()
            }
        }

        binding.postButton.setOnClickListener {
            uploadImage()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun uploadImage() {
        val ref =
            FirebaseStorage.getInstance().reference.child("post_images/${UUID.randomUUID()}")
        imageUri?.let {
            ref.putFile(it)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        postImage = uri.toString()
                        // After the image was successfully uploaded and we got its URL, we can save the post
                        savePost()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } ?: Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show()
    }

    private fun savePost() {
        val post = recommendResponseItem?.let { recommendItem ->
            Post(
                uid = FirebaseAuth.getInstance().currentUser?.uid!!,
                title = binding.titleEditText.text.toString(),
                content = binding.contentEditText.text.toString(),
                image = postImage,
                skills = recommendItem.position,
                timestamp = System.currentTimeMillis(),
                jobID = recommendItem.jobID
            )
        } ?: run {
            Post(
                uid = FirebaseAuth.getInstance().currentUser?.uid!!,
                title = binding.titleEditText.text.toString(),
                content = binding.contentEditText.text.toString(),
                image = postImage,
                skills = binding.skillSpinner.selectedItem.toString(),
                timestamp = System.currentTimeMillis()
            )
        }

        // Generate a new random document ID
        val docId = FirebaseFirestore.getInstance().collection("posts").document().id

        FirebaseFirestore.getInstance().collection("posts").document(docId).set(post)
            .addOnSuccessListener {
                if (isAdded) { // pengecekan apakah fragment masih terpasang ke activity
                    Toast.makeText(requireContext(), "Post published", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (isAdded) { // pengecekan apakah fragment masih terpasang ke activity
                    Toast.makeText(requireContext(), "Failed to publish post", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CODE_CAMERA)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            val photo = data?.extras?.get("data") as Bitmap
            // Create a file from the Bitmap
            val filesDir = requireContext().filesDir
            val imageFile = File(filesDir, "image" + ".jpg")
            val os: OutputStream
            try {
                os = FileOutputStream(imageFile)
                photo.compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.flush()
                os.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error writing Bitmap", e)
            }

            // Convert the file to Uri
            imageUri = Uri.fromFile(imageFile)

            // Set the ImageView with the new photo
            binding.previewImageView.setImageBitmap(photo)

        } else if (requestCode == REQUEST_CODE_GALLERY && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data

            // Load the selected image into the ImageView
            imageUri?.let {
                binding.previewImageView.setImageURI(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setBottomNavigationVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).setBottomNavigationVisibility(View.VISIBLE)
    }

    companion object {
        private const val TAG = "PostFragment"
    }
}
