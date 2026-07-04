package com.flatcode.autoimageslider

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.autoimageslider.databinding.ActivityMainBinding
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController
import com.selimdawa.autoimageslider.SliderAnimations
import com.selimdawa.autoimageslider.SliderView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SliderAdapterExample

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SliderAdapterExample(this)

        binding.imageSlider.apply {
            setSliderAdapter(this@MainActivity.adapter)
            setIndicatorAnimation(IndicatorAnimationType.WORM)
            setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION)
            autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            indicatorSelectedColor = Color.WHITE
            indicatorUnselectedColor = Color.GRAY
            scrollTimeInSec = 3
            isAutoCycle = true
            startAutoCycle()

            setOnIndicatorClickListener(object : DrawController.ClickListener {
                override fun onIndicatorClicked(position: Int) {
                    Log.i("GGG", "onIndicatorClicked: $currentPagePosition")
                }
            })
        }

        binding.btnAddItem.setOnClickListener { addNewItem() }
        binding.btnRemoveItem.setOnClickListener { removeLastItem() }
        binding.btnRenewItems.setOnClickListener { renewItems() }
    }

    private fun renewItems() {
        val sliderItemList = MutableList(5) { i ->
            SliderItem().apply {
                description = "Slider Item $i"
                imageUrl = if (i % 2 == 0) {
                    "https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
                } else {
                    "https://images.pexels.com/photos/747964/pexels-photo-747964.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260"
                }
            }
        }
        adapter.renewItems(sliderItemList)
    }

    private fun removeLastItem() {
        val lastIndex = adapter.count - 1
        if (lastIndex >= 0) {
            adapter.deleteItem(lastIndex)
        }
    }

    private fun addNewItem() {
        val sliderItem = SliderItem().apply {
            description = "Slider Item Added Manually"
            imageUrl =
                "https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
        }
        adapter.addItem(sliderItem)
    }
}