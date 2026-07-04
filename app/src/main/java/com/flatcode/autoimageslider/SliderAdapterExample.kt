package com.flatcode.autoimageslider

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.flatcode.autoimageslider.databinding.ImageSliderLayoutItemBinding
import com.selimdawa.autoimageslider.SliderViewAdapter

class SliderAdapterExample(private val context: Context) :
    SliderViewAdapter<SliderAdapterExample.SliderAdapterVH>() {

    private var mSliderItems: MutableList<SliderItem> = ArrayList()

    fun renewItems(sliderItems: MutableList<SliderItem>) {
        this.mSliderItems = sliderItems
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        this.mSliderItems.removeAt(position)
        notifyDataSetChanged()
    }

    fun addItem(sliderItem: SliderItem) {
        this.mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        val binding = ImageSliderLayoutItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SliderAdapterVH(binding)
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem = mSliderItems[position]

        with(viewHolder.binding) {
            tvAutoImageSlider.text = sliderItem.description
            tvAutoImageSlider.textSize = 16f
            tvAutoImageSlider.setTextColor(Color.WHITE)

            Glide.with(root.context).load(sliderItem.imageUrl).fitCenter().into(ivAutoImageSlider)

            root.setOnClickListener {
                Toast.makeText(context, "This is item in position $position", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getCount(): Int {
        return mSliderItems.size
    }

    class SliderAdapterVH(val binding: ImageSliderLayoutItemBinding) : ViewHolder(binding.root)
}