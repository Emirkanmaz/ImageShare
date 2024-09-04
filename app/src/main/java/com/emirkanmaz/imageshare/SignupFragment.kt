package com.emirkanmaz.imageshare

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.emirkanmaz.imageshare.databinding.FragmentSignupBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val action = SignupFragmentDirections.actionSignupFragmentToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }


        binding.signupButton.setOnClickListener { signup(it) }
        binding.signinButton.setOnClickListener { signin(it) }


    }

    private fun signup(view: View) {
        val email = binding.editTextEmailAddress.text.toString()
        val password = binding.editTextPassword.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userMap = hashMapOf<String, Any>(
                        "email" to auth.currentUser!!.email.toString(),
                        "createdAt" to Timestamp.now()
                    )

                    db.collection("users").document(auth.currentUser!!.uid).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Signup Successful", Toast.LENGTH_LONG).show()

                            val action = SignupFragmentDirections.actionSignupFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }

                } else {
                    Toast.makeText(requireContext(), task.exception?.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun signin(view: View) {
        val email = binding.editTextEmailAddress.text.toString()
        val password = binding.editTextPassword.text.toString()

        if (email.isNotBlank() && password.isNotBlank()) {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Signin Successful", Toast.LENGTH_LONG).show()
                    val action = SignupFragmentDirections.actionSignupFragmentToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}