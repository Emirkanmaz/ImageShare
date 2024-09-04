package com.emirkanmaz.imageshare.adapter

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.emirkanmaz.imageshare.databinding.RecyclerRowBinding
import com.emirkanmaz.imageshare.model.Post
import com.squareup.picasso.Picasso
import java.util.Locale

class PostAdapter(private val postlist: MutableList<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(val binding: RecyclerRowBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postlist.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.binding.emailTextView.text = postlist[position].email
        holder.binding.descriptionTextView.text = postlist[position].comment
        holder.binding.timestampTextView.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(postlist[position].timestamp!!.toDate())
        Picasso.get()
            .load(postlist[position].imageUrl)
            .fit()
            .centerInside()
            .into(holder.binding.feedImageView)


    }

}