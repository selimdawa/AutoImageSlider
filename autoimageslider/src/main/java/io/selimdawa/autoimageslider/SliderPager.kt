package io.selimdawa.autoimageslider

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter

@Suppress("unused")
open class SliderPager @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private var mScroller: OwnScroller? = null

    init {
        setScrollDuration(DEFAULT_SCROLL_DURATION)
    }

    fun setScrollDuration(millis: Int, interpolator: Interpolator? = sInterpolator) {
        runCatching {
            val scrollerField = ViewPager::class.java.getDeclaredField("mScroller")
            scrollerField.isAccessible = true
            mScroller = OwnScroller(context, millis, interpolator ?: sInterpolator)
            scrollerField.set(this, mScroller)
        }
    }

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        val adapter = adapter
        val target = if (adapter is InfinitePagerAdapter) adapter.getMiddlePosition(item) else item
        super.setCurrentItem(target, smoothScroll)
    }

    override fun getCurrentItem(): Int {
        val adapter = adapter
        val current = super.getCurrentItem()
        return if (adapter is InfinitePagerAdapter) adapter.getRealPosition(current) else current
    }

    interface OnPageChangeListener : ViewPager.OnPageChangeListener
    interface OnAdapterChangeListener : ViewPager.OnAdapterChangeListener
    interface PageTransformer : ViewPager.PageTransformer

    internal class OwnScroller(context: Context, var durationScrollMillis: Int, interpolator: Interpolator) :
        Scroller(context, interpolator) {
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, durationScrollMillis)
        }
    }

    companion object {
        const val DEFAULT_SCROLL_DURATION = 250
        private val sInterpolator = Interpolator { t -> (t - 1f).let { it * it * it * it * it + 1f } }
    }
}