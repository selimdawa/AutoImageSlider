package com.flatcode.autoimageslider

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import coil.load
import com.flatcode.autoimageslider.databinding.ImageSliderLayoutItemBinding
import io.selimdawa.autoimageslider.adapter.SliderViewAdapter

class SliderAdapterExample(private val context: Context) :
    SliderViewAdapter<SliderAdapterExample.SliderAdapterVH>() {

    private var mSliderItems: MutableList<SliderItem> = mutableListOf()

    fun renewItems(newSliderItems: MutableList<SliderItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = mSliderItems.size
            override fun getNewListSize() = newSliderItems.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mSliderItems[oldItemPosition].imageUrl == newSliderItems[newItemPosition].imageUrl
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return mSliderItems[oldItemPosition] == newSliderItems[newItemPosition]
            }
        })
        mSliderItems = newSliderItems
        diffResult.dispatchUpdatesTo(this)
    }

    fun deleteItem(position: Int) {
        if (position in mSliderItems.indices) {
            mSliderItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun addItem(sliderItem: SliderItem) {
        mSliderItems.add(sliderItem)
        notifyItemInserted(mSliderItems.size - 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliderAdapterVH {
        return SliderAdapterVH(
            ImageSliderLayoutItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBind(viewHolder: SliderAdapterVH, position: Int) {
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

    override fun getItemCount(): Int = mSliderItems.size

    class SliderAdapterVH(val binding: ImageSliderLayoutItemBinding) : ViewHolder(binding.root)
}