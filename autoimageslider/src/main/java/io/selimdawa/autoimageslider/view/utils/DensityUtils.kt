package io.selimdawa.autoimageslider.view.utils

import android.content.res.Resources
import android.util.TypedValue

@Suppress("unused")
object DensityUtils {
    fun dpToPx(dp: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

    fun pxToDp(px: Float) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().displayMetrics
    ).toInt()
}