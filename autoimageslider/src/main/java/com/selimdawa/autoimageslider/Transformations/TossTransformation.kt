package com.selimdawa.autoimageslider.Transformations

import android.view.View
import com.selimdawa.autoimageslider.SliderPager
import kotlin.math.abs
import kotlin.math.max

class TossTransformation : SliderPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.translationX = -position * page.width
        page.cameraDistance = 20000f

        if (position < 0.5 && position > -0.5) {
            page.visibility = View.VISIBLE
        } else {
            page.visibility = View.INVISIBLE
        }

        if (position < -1) {     // [-Infinity,-1)
            // This page is way off-screen to the left.
            page.alpha = 0f
        } else if (position <= 0) {    // [-1,0]
            page.alpha = 1f
            page.scaleX = max(0.4f, (1 - abs(position)))
            page.scaleY = max(0.4f, (1 - abs(position)))
            page.rotationX = 1080 * (1 - abs(position) + 1)
            page.translationY = -1000 * abs(position)
        } else if (position <= 1) {    // (0,1]
            page.alpha = 1f
            page.scaleX = max(0.4f, (1 - abs(position)))
            page.scaleY = max(0.4f, (1 - abs(position)))
            page.rotationX = -1080 * (1 - abs(position) + 1)
            page.translationY = -1000 * abs(position)
        } else {    // (1,+Infinity]
            // This page is way off-screen to the right.
            page.alpha = 0f
        }
    }
}