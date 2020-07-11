package com.example.veedio.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.veedio.PhotoActivity
import com.example.veedio.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*
import com.example.veedio.data.Photo
import com.example.veedio.extrass.inflate

class RecyclerAdapter(private val photos: ArrayList<Photo>):
    RecyclerView.Adapter<RecyclerAdapter.PhotoHolder>() {


    class PhotoHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var photo: Photo? = null

        init{
            v.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val context = itemView.context
            val showPhotoIntent = Intent(context, PhotoActivity::class.java)
            showPhotoIntent.putExtra(PHOTO_KEY, photo)
            context.startActivity(showPhotoIntent)
        }

        companion object {
            private val PHOTO_KEY = "PHOTO"
        }

        fun bindPhoto(photo: Photo){
            this.photo = photo
            Picasso.with(view.context).load(photo.url).into(view.itemImage)
            view.itemDate.text = photo.humanDate
            view.itemDescription.text = photo.explanation
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row)
        return  PhotoHolder(inflatedView)

    }

    override fun getItemCount(): Int {
        return photos.size
    }

    override fun onBindViewHolder(holder: RecyclerAdapter.PhotoHolder, position: Int) {
        val itemPhoto = photos[position]
        holder.bindPhoto(itemPhoto)
    }

}