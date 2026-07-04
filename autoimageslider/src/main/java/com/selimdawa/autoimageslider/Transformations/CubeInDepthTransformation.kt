package com.selimdawa.autoimageslider.Transformations

import android.view.View
import com.selimdawa.autoimageslider.SliderPager
import kotlin.math.abs
import kotlin.math.max

class CubeInDepthTransformation : SliderPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        page.cameraDistance = 20000f

        if (position < -1) {
            page.alpha = 0f
        } else if (position <= 0) {
            page.alpha = 1f
            page.pivotX = page.width.toFloat()
            page.rotationY = 90 * abs(position)
        } else if (position <= 1) {
            page.alpha = 1f
            page.pivotX = 0f
            page.rotationY = -90 * abs(position)
        } else {
            page.alpha = 0f
        }

        if (abs(position) <= 0.5) {
            page.scaleY = max(.4f, 1 - abs(position))
        } else if (abs(position) <= 1) {
            page.scaleY = max(.4f, 1 - abs(position))
        }
    }
}