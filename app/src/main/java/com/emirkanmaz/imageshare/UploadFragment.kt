package com.emirkanmaz.imageshare

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.emirkanmaz.imageshare.databinding.FragmentUploadBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.UUID

class UploadFragment : Fragment() {
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedImage: Uri? = null
    private var selectedBitmap: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        db = Firebase.firestore
        registerLaunchers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            it.getString("selectedImageUri")?.let { uri ->
                selectedImage = Uri.parse(uri)
            }

            it.getByteArray("selectedBitmap")?.let { bytes ->
                selectedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.uploadImageView.setImageBitmap(selectedBitmap)
            }
        }
        binding.uploadImageView.setOnClickListener { selectImage(it) }
        binding.postButton.setOnClickListener { postImage(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("selectedImageUri", selectedImage?.toString())
        selectedBitmap?.let {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            outState.putByteArray("selectedBitmap", outputStream.toByteArray())
        }
    }

    private fun postImage(view: View) {
        val uuid = UUID.randomUUID()
        val reference = storage.reference
        val imageReference = reference.child("images").child("$uuid.jpg")
        if (selectedImage != null) {
            imageReference.putFile(selectedImage!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        val userDocRef = db.collection("users").document(auth.currentUser!!.uid)

                        val postMap = hashMapOf(
                            "imageUrl" to imageUrl,
                            "timestamp" to Timestamp.now(),
                            "comment" to binding.descriptionEditText.text.toString()
                        )

                        userDocRef.collection("posts").add(postMap).addOnSuccessListener {
                            Toast.makeText(
                                requireContext(),
                                "Post successfully added",
                                Toast.LENGTH_LONG).show()
                            val action = UploadFragmentDirections.actionUploadFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)

                        }.addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                e.localizedMessage,
                                Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        task.exception?.localizedMessage.toString(),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    private fun selectImage(view: View) {
        val permission = if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(permission)) {
                Snackbar.make(view, "Need Permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Give Permission") {
                        permissionLauncher.launch(permission)
                    }.show()
            } else {
                permissionLauncher.launch(permission)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intent)
        }
    }

    private fun registerLaunchers() {
        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == AppCompatActivity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        selectedImage = uri
                        try {
                            selectedBitmap = if (Build.VERSION.SDK_INT >= 29) {
                                val source = ImageDecoder.createSource(
                                    requireActivity().contentResolver,
                                    uri
                                )
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(
                                    requireActivity().contentResolver,
                                    uri
                                )
                            }
                            binding.uploadImageView.setImageBitmap(selectedBitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intent)
                } else {
                    Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}