package io.selimdawa.autoimageslider

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.*
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.viewpager.widget.PagerAdapter
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter
import io.selimdawa.autoimageslider.adapter.SliderViewAdapter
import io.selimdawa.autoimageslider.view.PageIndicatorView
import io.selimdawa.autoimageslider.view.animation.type.BaseAnimation
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.controller.DrawController
import io.selimdawa.autoimageslider.view.draw.data.Orientation
import io.selimdawa.autoimageslider.view.draw.data.RtlMode
import io.selimdawa.autoimageslider.view.utils.DensityUtils

@Suppress("unused")
class SliderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Runnable, View.OnTouchListener,
    SliderViewAdapter.DataSetListener, SliderPager.OnPageChangeListener {

    private val mHandler = Handler(Looper.getMainLooper())
    private var mFlagBackAndForth = false
    private var mPreviousPosition = -1
    private var mSliderPager: SliderPager? = null

    var isAutoCycle = false
    var autoCycleDirection = AUTO_CYCLE_DIRECTION_RIGHT
    var scrollTimeInMillis = 2000
    var pagerIndicator: PageIndicatorView? = null; private set
    var sliderAdapter: PagerAdapter? = null; private set
    var isInfiniteAdapter = true
    private var mPageListener: OnSliderPageListener? = null

    var scrollTimeInSec: Int
        get() = scrollTimeInMillis / 1000
        set(value) { scrollTimeInMillis = value * 1000 }

    var currentPagePosition: Int
        get() = mSliderPager?.currentItem ?: 0
        set(value) { mSliderPager?.setCurrentItem(value, true) }

    var indicatorRadius: Int
        get() = pagerIndicator?.radius ?: 0
        set(value) { pagerIndicator?.radius = value }

    var indicatorSelectedColor: Int
        get() = pagerIndicator?.selectedColor ?: 0
        set(value) { pagerIndicator?.selectedColor = value }

    var indicatorUnselectedColor: Int
        get() = pagerIndicator?.unselectedColor ?: 0
        set(value) { pagerIndicator?.unselectedColor = value }

    init {
        setupSlideView()
        attrs?.let { setUpAttributes(it) }
    }

    private fun setupSlideView() {
        mSliderPager = SliderPager(context).apply {
            overScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS
            id = generateViewId()
            setOnTouchListener(this@SliderView)
            addOnPageChangeListener(this@SliderView)
        }
        addView(mSliderPager, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun setUpAttributes(attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.SliderView) {
            setSliderAnimationDuration(getInt(R.styleable.SliderView_sliderAnimationDuration, 250))
            scrollTimeInSec = getInt(R.styleable.SliderView_sliderScrollTimeInSec, 2)
            autoCycleDirection = getInt(R.styleable.SliderView_sliderAutoCycleDirection, 0)
            val autoCycleEnabled = getBoolean(R.styleable.SliderView_sliderAutoCycleEnabled, true)
            isAutoCycle = getBoolean(R.styleable.SliderView_sliderStartAutoCycle, false) || autoCycleEnabled
            
            if (isAutoCycle) startAutoCycle()
            
            if (getBoolean(R.styleable.SliderView_sliderIndicatorEnabled, true)) {
                initIndicator()
                pagerIndicator?.apply {
                    setOrientation(if (getInt(R.styleable.SliderView_sliderIndicatorOrientation, 0) == 0) Orientation.HORIZONTAL else Orientation.VERTICAL)
                    radius = getDimension(R.styleable.SliderView_sliderIndicatorRadius, DensityUtils.dpToPx(2).toFloat()).toInt()
                    padding = getDimension(R.styleable.SliderView_sliderIndicatorPadding, DensityUtils.dpToPx(3).toFloat()).toInt()
                    val margin = getDimension(R.styleable.SliderView_sliderIndicatorMargin, DensityUtils.dpToPx(12).toFloat()).toInt()
                    setIndicatorMargins(
                        getDimension(R.styleable.SliderView_sliderIndicatorMarginLeft, margin.toFloat()).toInt(),
                        getDimension(R.styleable.SliderView_sliderIndicatorMarginTop, margin.toFloat()).toInt(),
                        getDimension(R.styleable.SliderView_sliderIndicatorMarginRight, margin.toFloat()).toInt(),
                        getDimension(R.styleable.SliderView_sliderIndicatorMarginBottom, margin.toFloat()).toInt()
                    )
                    setIndicatorGravity(getInt(R.styleable.SliderView_sliderIndicatorGravity, Gravity.CENTER or Gravity.BOTTOM))
                    selectedColor = getColor(R.styleable.SliderView_sliderIndicatorSelectedColor, "#ffffff".toColorInt())
                    unselectedColor = getColor(R.styleable.SliderView_sliderIndicatorUnselectedColor, "#33ffffff".toColorInt())
                    animationDuration = getInt(R.styleable.SliderView_sliderIndicatorAnimationDuration, BaseAnimation.DEFAULT_ANIMATION_TIME).toLong()
                    setRtlMode(RtlMode.entries.getOrNull(getInt(R.styleable.SliderView_sliderIndicatorRtlMode, RtlMode.Off.ordinal)) ?: RtlMode.Auto)
                }
            }
        }
    }

    private fun initIndicator() {
        if (pagerIndicator == null) {
            pagerIndicator = PageIndicatorView(context).apply {
                setDynamicCount(true)
                setViewPager(mSliderPager)
            }
            addView(pagerIndicator, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                setMargins(20, 20, 20, 20)
            })
        }
    }

    fun setSliderAdapter(adapter: SliderViewAdapter<*>) {
        setSliderAdapter(adapter, true)
    }

    fun setSliderAdapter(adapter: SliderViewAdapter<*>, infinite: Boolean) {
        this.sliderAdapter = adapter
        this.isInfiniteAdapter = infinite
        mSliderPager?.adapter = if (infinite) InfinitePagerAdapter(adapter) else adapter
        adapter.dataSetChangedListener(this)
        currentPagePosition = 0
    }

    fun setInfiniteAdapterEnabled(enabled: Boolean) {
        val adapter = sliderAdapter as? SliderViewAdapter<*>
        if (adapter != null) setSliderAdapter(adapter, enabled)
    }

    fun setOffscreenPageLimit(limit: Int) { mSliderPager?.offscreenPageLimit = limit }

    fun setSliderTransformAnimation(animation: SliderAnimations) {
        mSliderPager?.setPageTransformer(false, SmartTransformer(animation))
    }

    fun setCustomSliderTransformAnimation(transformer: SliderPager.PageTransformer?) {
        mSliderPager?.setPageTransformer(false, transformer)
    }

    fun setSliderAnimationDuration(duration: Int) = mSliderPager?.setScrollDuration(duration)

    fun setSliderAnimationDuration(duration: Int, interpolator: Interpolator?) {
        mSliderPager?.setScrollDuration(duration, interpolator)
    }

    fun setPageIndicatorView(indicatorView: PageIndicatorView?) {
        this.pagerIndicator = indicatorView
        initIndicator()
    }

    fun setIndicatorEnabled(enabled: Boolean) {
        if (enabled && pagerIndicator == null) initIndicator()
        pagerIndicator?.visibility = if (enabled) VISIBLE else GONE
    }

    fun setIndicatorAnimationDuration(duration: Long) { pagerIndicator?.animationDuration = duration }

    fun setIndicatorGravity(gravity: Int) = pagerIndicator?.let { (it.layoutParams as LayoutParams).gravity = gravity; it.requestLayout() }

    fun setIndicatorPadding(padding: Int) { pagerIndicator?.padding = padding }

    fun setIndicatorMargins(l: Int, t: Int, r: Int, b: Int) = pagerIndicator?.let { (it.layoutParams as LayoutParams).setMargins(l, t, r, b); it.requestLayout() }

    fun setIndicatorMargin(margin: Int) = setIndicatorMargins(margin, margin, margin, margin)

    fun setIndicatorMarginCustom(l: Int, t: Int, r: Int, b: Int) = setIndicatorMargins(l, t, r, b)

    fun setIndicatorOrientation(o: Orientation?) { pagerIndicator?.setOrientation(o) }

    fun setIndicatorAnimation(animation: IndicatorAnimationType?) = pagerIndicator?.setAnimationType(animation)

    fun setIndicatorVisibility(visibility: Boolean) { pagerIndicator?.visibility = if (visibility) VISIBLE else GONE }

    fun setIndicatorRtlMode(rtlMode: RtlMode?) { pagerIndicator?.setRtlMode(rtlMode) }

    fun setOnIndicatorClickListener(l: DrawController.ClickListener?) = pagerIndicator?.setClickListener(l)

    fun setCurrentPageListener(listener: OnSliderPageListener?) { this.mPageListener = listener }

    fun startAutoCycle() {
        mHandler.removeCallbacks(this)
        mHandler.postDelayed(this, scrollTimeInMillis.toLong())
    }

    fun stopAutoCycle() = mHandler.removeCallbacks(this)

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (isAutoCycle) {
            if (event.action == MotionEvent.ACTION_MOVE) stopAutoCycle()
            else if (event.action == MotionEvent.ACTION_UP) mHandler.postDelayed({ startAutoCycle() }, 2000)
        }
        return false
    }

    override fun run() {
        try { slideToNextPosition() } finally { if (isAutoCycle) mHandler.postDelayed(this, scrollTimeInMillis.toLong()) }
    }

    fun slideToNextPosition() { slide(1) }

    fun slideToPreviousPosition() { slide(-1) }

    private fun slide(direction: Int) {
        val pager = mSliderPager ?: return
        val count = try { sliderAdapter?.count ?: 0 } catch (e: Exception) { 0 }
        if (count <= 1) return

        val nextPos = when (autoCycleDirection) {
            AUTO_CYCLE_DIRECTION_BACK_AND_FORTH -> {
                if (pager.currentItem % (count - 1) == 0 && mPreviousPosition != count - 1 && mPreviousPosition != 0) mFlagBackAndForth = !mFlagBackAndForth
                pager.currentItem + (if (mFlagBackAndForth) direction else -direction)
            }
            AUTO_CYCLE_DIRECTION_LEFT -> pager.currentItem - direction
            else -> pager.currentItem + direction
        }
        mPreviousPosition = pager.currentItem
        pager.setCurrentItem(nextPos, true)
    }

    override fun dataSetChanged() {
        if (isInfiniteAdapter) (mSliderPager?.adapter as? InfinitePagerAdapter)?.notifyDataSetChanged()
        mSliderPager?.setCurrentItem(0, false)
    }

    override fun onPageSelected(position: Int) {
        val realPos = mSliderPager?.currentItem ?: position
        mPageListener?.onSliderPageChanged(realPos)
    }
    override fun onPageScrolled(pos: Int, off: Float, offPx: Int) {}
    override fun onPageScrollStateChanged(state: Int) {}

    interface OnSliderPageListener { fun onSliderPageChanged(position: Int) }

    companion object {
        const val AUTO_CYCLE_DIRECTION_RIGHT = 0
        const val AUTO_CYCLE_DIRECTION_LEFT = 1
        const val AUTO_CYCLE_DIRECTION_BACK_AND_FORTH = 2
    }
}