package io.selimdawa.autoimageslider.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import coil.load
import io.selimdawa.autoimageslider.R

class DefaultSliderAdapter(private val imageUrls: List<String>) :
    SliderViewAdapter<DefaultSliderAdapter.DefaultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DefaultViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.default_slider_item, parent, false)
        return DefaultViewHolder(view)
    }

    override fun onBind(viewHolder: DefaultViewHolder, position: Int) {
        viewHolder.imageView.load(imageUrls[position])
    }

    override fun getItemCount() = imageUrls.size

    class DefaultViewHolder(view: View) : ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.iv_auto_image_slider)
    }
}