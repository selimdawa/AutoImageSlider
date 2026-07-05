package com.selimdawa.autoimageslider

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.IndicatorView.PageIndicatorView
import com.selimdawa.autoimageslider.IndicatorView.animation.type.BaseAnimation
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController.ClickListener
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.draw.data.RtlMode
import com.selimdawa.autoimageslider.IndicatorView.utils.DensityUtils
import com.selimdawa.autoimageslider.InfiniteAdapter.InfinitePagerAdapter
import com.selimdawa.autoimageslider.SliderViewAdapter.DataSetListener

class SliderView : FrameLayout, Runnable, OnTouchListener, DataSetListener,
    SliderPager.OnPageChangeListener {

    private val mHandler = Handler(Looper.getMainLooper())
    private var mFlagBackAndForth = false

    var isAutoCycle: Boolean = false
    var autoCycleDirection: Int = 0
    var scrollTimeInMillis: Int = 0
    var pagerIndicator: PageIndicatorView? = null
        private set
    private var mPagerAdapter: SliderViewAdapter<*>? = null
    private var mSliderPager: SliderPager? = null
    private var mInfinitePagerAdapter: InfinitePagerAdapter? = null
    private var mPageListener: OnSliderPageListener? = null
    private var mIsInfiniteAdapter = true
    private var mIsIndicatorEnabled = true
    private var mPreviousPosition = -1

    constructor(context: Context) : super(context) {
        setupSlideView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupSlideView(context)
        setUpAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        setupSlideView(context)
        setUpAttributes(context, attrs)
    }

    private fun setUpAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.SliderView, 0, 0) {
            val indicatorEnabled = getBoolean(R.styleable.SliderView_sliderIndicatorEnabled, true)
            val sliderAnimationDuration = getInt(
                R.styleable.SliderView_sliderAnimationDuration, SliderPager.DEFAULT_SCROLL_DURATION
            )
            val sliderScrollTimeInSec = getInt(R.styleable.SliderView_sliderScrollTimeInSec, 2)
            val sliderAutoCycleEnabled =
                getBoolean(R.styleable.SliderView_sliderAutoCycleEnabled, true)
            val sliderStartAutoCycle =
                getBoolean(R.styleable.SliderView_sliderStartAutoCycle, false)
            val sliderAutoCycleDirection = getInt(
                R.styleable.SliderView_sliderAutoCycleDirection, AUTO_CYCLE_DIRECTION_RIGHT
            )

            setSliderAnimationDuration(sliderAnimationDuration)
            this@SliderView.scrollTimeInSec = sliderScrollTimeInSec
            this@SliderView.isAutoCycle = sliderAutoCycleEnabled
            this@SliderView.autoCycleDirection = sliderAutoCycleDirection
            this@SliderView.isAutoCycle = sliderStartAutoCycle
            setIndicatorEnabled(indicatorEnabled)

            if (mIsIndicatorEnabled) {
                initIndicator()
                val indicatorOrientation = getInt(
                    R.styleable.SliderView_sliderIndicatorOrientation,
                    Orientation.HORIZONTAL.ordinal
                )
                val orientation = if (indicatorOrientation == 0) {
                    Orientation.HORIZONTAL
                } else {
                    Orientation.VERTICAL
                }
                val indicatorRadius = getDimension(
                    R.styleable.SliderView_sliderIndicatorRadius, DensityUtils.dpToPx(2).toFloat()
                ).toInt()
                val indicatorPadding = getDimension(
                    R.styleable.SliderView_sliderIndicatorPadding, DensityUtils.dpToPx(3).toFloat()
                ).toInt()
                val indicatorMargin = getDimension(
                    R.styleable.SliderView_sliderIndicatorMargin, DensityUtils.dpToPx(12).toFloat()
                ).toInt()
                val indicatorMarginLeft = getDimension(
                    R.styleable.SliderView_sliderIndicatorMarginLeft,
                    DensityUtils.dpToPx(12).toFloat()
                ).toInt()
                val indicatorMarginTop = getDimension(
                    R.styleable.SliderView_sliderIndicatorMarginTop,
                    DensityUtils.dpToPx(12).toFloat()
                ).toInt()
                val indicatorMarginRight = getDimension(
                    R.styleable.SliderView_sliderIndicatorMarginRight,
                    DensityUtils.dpToPx(12).toFloat()
                ).toInt()
                val indicatorMarginBottom = getDimension(
                    R.styleable.SliderView_sliderIndicatorMarginBottom,
                    DensityUtils.dpToPx(12).toFloat()
                ).toInt()
                val indicatorGravity = getInt(
                    R.styleable.SliderView_sliderIndicatorGravity, Gravity.CENTER or Gravity.BOTTOM
                )
                val indicatorUnselectedColor = getColor(
                    R.styleable.SliderView_sliderIndicatorUnselectedColor, "#33ffffff".toColorInt()
                )
                val indicatorSelectedColor = getColor(
                    R.styleable.SliderView_sliderIndicatorSelectedColor, "#ffffff".toColorInt()
                )
                val indicatorAnimationDuration = getInt(
                    R.styleable.SliderView_sliderIndicatorAnimationDuration,
                    BaseAnimation.DEFAULT_ANIMATION_TIME
                )
                val indicatorRtlMode = getInt(
                    R.styleable.SliderView_sliderIndicatorRtlMode, RtlMode.Off.ordinal
                )
                val rtlMode: RtlMode = RtlMode.entries.getOrNull(indicatorRtlMode) ?: RtlMode.Auto

                setIndicatorOrientation(orientation)
                this@SliderView.indicatorRadius = indicatorRadius
                setIndicatorPadding(indicatorPadding)
                setIndicatorMargin(indicatorMargin)
                setIndicatorMarginCustom(
                    indicatorMarginLeft,
                    indicatorMarginTop,
                    indicatorMarginRight,
                    indicatorMarginBottom
                )
                setIndicatorGravity(indicatorGravity)
                setIndicatorMargins(
                    indicatorMarginLeft,
                    indicatorMarginTop,
                    indicatorMarginRight,
                    indicatorMarginBottom
                )
                this@SliderView.indicatorUnselectedColor = indicatorUnselectedColor
                this@SliderView.indicatorSelectedColor = indicatorSelectedColor
                setIndicatorAnimationDuration(indicatorAnimationDuration.toLong())
                setIndicatorRtlMode(rtlMode)
            }
        }
    }

    private fun initIndicator() {
        if (this.pagerIndicator == null) {
            this.pagerIndicator = PageIndicatorView(context)
            val params = LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
            params.setMargins(20, 20, 20, 20)
            addView(this.pagerIndicator, 1, params)
        }
        pagerIndicator?.setViewPager(mSliderPager)
        pagerIndicator?.setDynamicCount(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideView(context: Context) {
        val pager = SliderPager(context)
        mSliderPager = pager
        pager.overScrollMode = OVER_SCROLL_IF_CONTENT_SCROLLS
        pager.id = generateViewId()
        val sliderParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        addView(pager, 0, sliderParams)
        pager.setOnTouchListener(this)
        pager.addOnPageChangeListener(this)
    }

    fun setOnIndicatorClickListener(listener: ClickListener?) {
        pagerIndicator?.setClickListener(listener)
    }

    fun setCurrentPageListener(listener: OnSliderPageListener?) {
        this.mPageListener = listener
    }

    fun setSliderAdapter(pagerAdapter: SliderViewAdapter<*>) {
        mPagerAdapter = pagerAdapter
        val infiniteAdapter = InfinitePagerAdapter(pagerAdapter)
        mInfinitePagerAdapter = infiniteAdapter
        mSliderPager?.adapter = infiniteAdapter
        pagerAdapter.dataSetChangedListener(this)
        this.currentPagePosition = 0
    }

    fun setSliderAdapter(pagerAdapter: SliderViewAdapter<*>, infiniteAdapter: Boolean) {
        mIsInfiniteAdapter = infiniteAdapter
        if (!infiniteAdapter) {
            mPagerAdapter = pagerAdapter
            mSliderPager?.adapter = pagerAdapter
        } else {
            setSliderAdapter(pagerAdapter)
        }
    }

    fun setInfiniteAdapterEnabled(enabled: Boolean) {
        val adapter = mPagerAdapter
        if (adapter != null) {
            setSliderAdapter(adapter, enabled)
        }
    }

    val sliderPager: SliderPager
        get() = mSliderPager ?: SliderPager(context)

    val sliderAdapter: PagerAdapter?
        get() = mPagerAdapter

    fun setOffscreenPageLimit(limit: Int) {
        mSliderPager?.offscreenPageLimit = limit
    }

    var scrollTimeInSec: Int
        get() = this.scrollTimeInMillis / 1000
        set(time) {
            this.scrollTimeInMillis = time * 1000
        }

    fun setSliderTransformAnimation(animation: SliderAnimations) {
        mSliderPager?.setPageTransformer(false, SmartTransformer(animation))
    }

    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        if (this.isAutoCycle) {
            if (event.action == MotionEvent.ACTION_MOVE) {
                stopAutoCycle()
            } else if (event.action == MotionEvent.ACTION_UP) {
                mHandler.postDelayed({ startAutoCycle() }, 2000)
            }
        }
        return false
    }

    fun setCustomSliderTransformAnimation(animation: SliderPager.PageTransformer?) {
        mSliderPager?.setPageTransformer(false, animation)
    }

    fun setSliderAnimationDuration(duration: Int) {
        mSliderPager?.setScrollDuration(duration)
    }

    fun setSliderAnimationDuration(duration: Int, interpolator: Interpolator?) {
        mSliderPager?.setScrollDuration(duration, interpolator)
    }

    var currentPagePosition: Int
        get() {
            if (this.sliderAdapter != null) {
                return this.sliderPager.currentItem
            } else {
                throw NullPointerException("Adapter not set")
            }
        }
        set(position) {
            mSliderPager?.setCurrentItem(position, true)
        }

    fun setPageIndicatorView(indicatorView: PageIndicatorView?) {
        this.pagerIndicator = indicatorView
        initIndicator()
    }

    fun setIndicatorEnabled(enabled: Boolean) {
        this.mIsIndicatorEnabled = enabled
        if (this.pagerIndicator == null && enabled) {
            initIndicator()
        }
    }

    fun setIndicatorAnimationDuration(duration: Long) {
        pagerIndicator?.animationDuration = duration
    }

    fun setIndicatorGravity(gravity: Int) {
        val indicator = pagerIndicator ?: return
        val layoutParams = indicator.layoutParams as LayoutParams
        layoutParams.gravity = gravity
        indicator.layoutParams = layoutParams
    }

    fun setIndicatorPadding(padding: Int) {
        pagerIndicator?.setPadding(padding.toFloat())
    }

    fun setIndicatorMargins(left: Int, top: Int, right: Int, bottom: Int) {
        val indicator = pagerIndicator ?: return
        val layoutParams = indicator.layoutParams as LayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        indicator.layoutParams = layoutParams
    }

    fun setIndicatorOrientation(orientation: Orientation?) {
        pagerIndicator?.setOrientation(orientation)
    }

    fun setIndicatorAnimation(animation: IndicatorAnimationType?) {
        pagerIndicator?.setAnimationType(animation)
    }

    fun setIndicatorVisibility(visibility: Boolean) {
        pagerIndicator?.visibility = if (visibility) VISIBLE else GONE
    }

    private val adapterItemsCount: Int
        get() {
            return try {
                this.sliderAdapter?.count ?: 0
            } catch (_: NullPointerException) {
                Log.e(
                    TAG,
                    "getAdapterItemsCount: Slider Adapter is null so, it can't get count of items"
                )
                0
            }
        }

    fun startAutoCycle() {
        mHandler.removeCallbacks(this)
        mHandler.postDelayed(this, scrollTimeInMillis.toLong())
    }

    fun stopAutoCycle() {
        mHandler.removeCallbacks(this)
    }

    var indicatorRadius: Int
        get() = pagerIndicator?.radius ?: 0
        set(pagerIndicatorRadius) {
            this.pagerIndicator?.radius = pagerIndicatorRadius
        }

    fun setIndicatorRtlMode(rtlMode: RtlMode?) {
        pagerIndicator?.setRtlMode(rtlMode)
    }

    fun setIndicatorMargin(margin: Int) {
        val indicator = pagerIndicator ?: return
        val layoutParams = indicator.layoutParams as LayoutParams
        layoutParams.setMargins(margin, margin, margin, margin)
        indicator.layoutParams = layoutParams
    }

    fun setIndicatorMarginCustom(left: Int, top: Int, right: Int, bottom: Int) {
        val indicator = pagerIndicator ?: return
        val layoutParams = indicator.layoutParams as LayoutParams
        layoutParams.setMargins(left, top, right, bottom)
        indicator.layoutParams = layoutParams
    }

    var indicatorSelectedColor: Int
        get() = this.pagerIndicator?.selectedColor ?: 0
        set(color) {
            this.pagerIndicator?.selectedColor = color
        }

    var indicatorUnselectedColor: Int
        get() = this.pagerIndicator?.unselectedColor ?: 0
        set(color) {
            this.pagerIndicator?.unselectedColor = color
        }

    override fun run() {
        try {
            slideToNextPosition()
        } finally {
            if (this.isAutoCycle) {
                mHandler.postDelayed(this, scrollTimeInMillis.toLong())
            }
        }
    }

    fun slideToNextPosition() {
        val pager = mSliderPager ?: return
        val currentPosition = pager.currentItem
        val itemsCount = this.adapterItemsCount
        if (itemsCount > 1) {
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_BACK_AND_FORTH) {
                if (currentPosition % (itemsCount - 1) == 0 && mPreviousPosition != itemsCount - 1 && mPreviousPosition != 0) {
                    mFlagBackAndForth = !mFlagBackAndForth
                }
                if (mFlagBackAndForth) {
                    pager.setCurrentItem(currentPosition + 1, true)
                } else {
                    pager.setCurrentItem(currentPosition - 1, true)
                }
            }
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_LEFT) {
                pager.setCurrentItem(currentPosition - 1, true)
            }
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_RIGHT) {
                pager.setCurrentItem(currentPosition + 1, true)
            }
        }
        mPreviousPosition = currentPosition
    }

    fun slideToPreviousPosition() {
        val pager = mSliderPager ?: return
        val currentPosition = pager.currentItem
        val itemsCount = this.adapterItemsCount

        if (itemsCount > 1) {
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_BACK_AND_FORTH) {
                if (currentPosition % (itemsCount - 1) == 0 && mPreviousPosition != itemsCount - 1 && mPreviousPosition != 0) {
                    mFlagBackAndForth = !mFlagBackAndForth
                }
                if (mFlagBackAndForth && currentPosition < mPreviousPosition) {
                    pager.setCurrentItem(currentPosition - 1, true)
                } else {
                    pager.setCurrentItem(currentPosition + 1, true)
                }
            }
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_LEFT) {
                pager.setCurrentItem(currentPosition + 1, true)
            }
            if (this.autoCycleDirection == AUTO_CYCLE_DIRECTION_RIGHT) {
                pager.setCurrentItem(currentPosition - 1, true)
            }
        }
        mPreviousPosition = currentPosition
    }

    override fun dataSetChanged() {
        if (mIsInfiniteAdapter) {
            mInfinitePagerAdapter?.notifyDataSetChanged()
            mSliderPager?.setCurrentItem(0, false)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        mPageListener?.onSliderPageChanged(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}

    interface OnSliderPageListener {
        fun onSliderPageChanged(position: Int)
    }

    companion object {
        const val AUTO_CYCLE_DIRECTION_RIGHT: Int = 0
        const val AUTO_CYCLE_DIRECTION_LEFT: Int = 1
        const val AUTO_CYCLE_DIRECTION_BACK_AND_FORTH: Int = 2
        const val TAG: String = "Slider View : "
    }
}