package com.flatcode.autoimageslider

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import coil.load
import com.flatcode.autoimageslider.databinding.ImageSliderLayoutItemBinding
import io.selimdawa.autoimageslider.adapter.SliderViewAdapter

class SliderAdapterExample(private val context: Context) :
    SliderViewAdapter<SliderAdapterExample.SliderAdapterVH>() {

    private var mSliderItems: MutableList<SliderItem> = mutableListOf()

    fun renewItems(sliderItems: MutableList<SliderItem>) {
        this.mSliderItems = sliderItems
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        if (position in mSliderItems.indices) {
            this.mSliderItems.removeAt(position)
            notifyDataSetChanged()
        }
    }

    fun addItem(sliderItem: SliderItem) {
        this.mSliderItems.add(sliderItem)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup): SliderAdapterVH {
        return SliderAdapterVH(
            ImageSliderLayoutItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(viewHolder: SliderAdapterVH, position: Int) {
        val sliderItem = mSliderItems[position]

        with(viewHolder.binding) {
            tvAutoImageSlider.text = sliderItem.description
            tvAutoImageSlider.textSize = 16f
            tvAutoImageSlider.setTextColor(Color.WHITE)

            ivAutoImageSlider.load(sliderItem.imageUrl)

            root.setOnClickListener {
                Toast.makeText(context, "This is item in position $position", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun getCount(): Int = mSliderItems.size

    class SliderAdapterVH(val binding: ImageSliderLayoutItemBinding) : ViewHolder(binding.root)
}