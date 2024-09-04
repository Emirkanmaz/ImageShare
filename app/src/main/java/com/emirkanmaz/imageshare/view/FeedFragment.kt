package com.emirkanmaz.imageshare.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.emirkanmaz.imageshare.R
import com.emirkanmaz.imageshare.adapter.PostAdapter
import com.emirkanmaz.imageshare.databinding.FragmentFeedBinding
import com.emirkanmaz.imageshare.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var popup: PopupMenu
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    val postList = mutableListOf<Post>()
    private var adapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        db = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { floatingButtonMenu(it) }

        popup = PopupMenu(context, binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_popup, popup.menu)
        popup.setOnMenuItemClickListener(this)

        listenToAllPosts()

        adapter = PostAdapter(postlist = postList)
        binding.feedRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter = adapter

    }

    private fun listenToAllPosts() {
        val postsCollectionGroup = db.collectionGroup("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)

        postsCollectionGroup.addSnapshotListener { snapshots, error ->
            if (!isAdded) return@addSnapshotListener

            if (error != null) {
                println(error)
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            if (snapshots != null && !snapshots.isEmpty) {
                postList.clear()

                for (document in snapshots.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let { postList.add(it) }
                }

                adapter?.notifyDataSetChanged()
            }
        }
    }


    private fun floatingButtonMenu(view: View?) {
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (item?.itemId == R.id.exitItem){
            auth.signOut()
            val action = FeedFragmentDirections.actionFeedFragmentToSignupFragment()
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.feedFragment, true)
                .build()
            Navigation.findNavController(requireView()).navigate(action, navOptions)
        } else if(item?.itemId == R.id.postItem){
            val action = FeedFragmentDirections.actionFeedFragmentToUploadFragment()
            Navigation.findNavController(requireView()).navigate(action)
        }
        return true
    }
}