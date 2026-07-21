package com.flatcode.autoimageslider

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.flatcode.autoimageslider.databinding.ActivityMainBinding
import io.selimdawa.autoimageslider.SliderAnimations
import io.selimdawa.autoimageslider.SliderView
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.controller.DrawController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: SliderAdapterExample

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SliderApp", "MainActivity onCreate started")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SliderAdapterExample(this)

        binding.imageSlider.apply {
            setSliderAdapter(this@MainActivity.adapter)
            setIndicatorAnimation(IndicatorAnimationType.DROP)
            setSliderTransformAnimation(SliderAnimations.SIMPLE)
            autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
            indicatorSelectedColor = ContextCompat.getColor(this@MainActivity, R.color.white)
            indicatorUnselectedColor = ContextCompat.getColor(this@MainActivity, R.color.gray)
            scrollTimeInSec = 3
            isAutoCycle = true
            startAutoCycle()

            setCurrentPageListener(object : SliderView.OnSliderPageListener {
                override fun onSliderPageChanged(position: Int) {
                    Log.d("MainActivity", "onSliderPageChanged: $position")
                }
            })


            setOnIndicatorClickListener(object : DrawController.ClickListener {
                override fun onIndicatorClicked(position: Int) {
                    Log.i("GGG", "onIndicatorClicked: $currentPagePosition")
                }
            })
        }

        binding.btnAddItem.setOnClickListener { addNewItem() }
        binding.btnRemoveItem.setOnClickListener { removeLastItem() }
        binding.btnRenewItems.setOnClickListener { renewItems() }

        renewItems()
    }

    private fun renewItems() {
        val sliderItemList = MutableList(5) { i ->
            val url = if (i % 2 == 0) {
                "https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
            } else {
                "https://images.pexels.com/photos/747964/pexels-photo-747964.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260"
            }
            SliderItem(getString(R.string.slider_item_format, i), url)
        }
        adapter.renewItems(sliderItemList)
    }

    private fun removeLastItem() {
        val lastIndex = adapter.itemCount - 1
        if (lastIndex >= 0) {
            adapter.deleteItem(lastIndex)
        }
    }

    private fun addNewItem() {
        adapter.addItem(
            SliderItem(
                getString(R.string.manual_item_description),
                "https://images.pexels.com/photos/929778/pexels-photo-929778.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260"
            )
        )
    }
}