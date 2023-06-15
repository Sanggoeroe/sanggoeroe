package com.capstone.sanggoroe.ui.profile

import android.app.Activity.RESULT_OK
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.bumptech.glide.Glide
import com.capstone.sanggoroe.data.Constants
import com.capstone.sanggoroe.data.Constants.REQUEST_CODE_CAMERA
import com.capstone.sanggoroe.data.Constants.REQUEST_IMAGE_CAPTURE
import com.capstone.sanggoroe.databinding.FragmentEditProfileBinding
import com.capstone.sanggoroe.model.UserProfile
import com.capstone.sanggoroe.view.main.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.profileImage.setOnClickListener {
            intentCamera()
        }

        // TODO: Replace these with the actual skills

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, Constants.SKILLS)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.skill1Spinner.adapter = adapter
        binding.skill2Spinner.adapter = adapter
        binding.skill3Spinner.adapter = adapter

        // Membuat referensi ke pengguna saat ini dan database Firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        // Mendapatkan data pengguna dari Firestore
        currentUser?.let {
            db.collection("users").document(it.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // Mengatur data profil ke tampilan
                        val userProfile = document.toObject(UserProfile::class.java)
                        userProfile?.let {
                            binding.editName.setText(it.username)
                            binding.editDescription.setText(it.description)
                            // Berdasarkan implementasi Anda, atur nilai spinner untuk keterampilan
                            imageUri = Uri.parse(it.profileImageUrl)
                            Glide.with(this).load(imageUri).into(binding.profileImage)
                        }
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }


        binding.saveButton.setOnClickListener {
            saveUserProfile(it)
        }
    }

    private fun saveUserProfile(view: View) {
        val name = binding.editName.text.toString()
        val description = binding.editDescription.text.toString()
        val skill1 = binding.skill1Spinner.selectedItem.toString()
        val skill2 = binding.skill2Spinner.selectedItem.toString()
        val skill3 = binding.skill3Spinner.selectedItem.toString()

        val imageUriString = if (imageUri != null) imageUri!!.toString() else null

        // Membuat objek UserProfile baru dengan data yang diubah
        val updatedProfile = UserProfile(name, description, skill1, skill2, skill3, imageUriString)

        // Mendapatkan referensi ke database
        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid != null) {
            db.collection("users").document(uid)
                .set(updatedProfile)
                .addOnSuccessListener {
                    Snackbar.make(view, "Profil berhasil diperbarui", Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating profile", e)
                    Snackbar.make(view, "Terjadi kesalahan saat memperbarui profil", Snackbar.LENGTH_LONG).show()
                }
        } else {
            Snackbar.make(view, "Tidak dapat mengupdate profil", Snackbar.LENGTH_LONG).show()
        }
    }


    private fun intentCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it)?.also {
                    startActivityForResult(intent, REQUEST_CODE_CAMERA)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
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

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setBottomNavigationVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).setBottomNavigationVisibility(View.VISIBLE)
    }
}
