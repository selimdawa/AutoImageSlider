package com.selimdawa.autoimageslider.Transformations

import android.view.View
import com.selimdawa.autoimageslider.SliderPager
import kotlin.math.abs
import kotlin.math.max

class CubeOutScalingTransformation : SliderPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {

        if (position < -1) {    // [-Infinity,-1)
            page.alpha = 0f
        } else if (position <= 0) {    // [-1,0]
            page.alpha = 1f
            page.pivotX = page.width.toFloat()
            page.rotationY = -90 * abs(position)
        } else if (position <= 1) {    // (0,1]
            page.alpha = 1f
            page.pivotX = 0f
            page.rotationY = 90 * abs(position)
        } else {    // (1,+Infinity]
            page.alpha = 0f
        }

        if (abs(position) <= 0.5) {
            page.scaleY = max(0.4f, 1 - abs(position))
        } else if (abs(position) <= 1) {
            page.scaleY = max(0.4f, abs(position))
        }
    }
}