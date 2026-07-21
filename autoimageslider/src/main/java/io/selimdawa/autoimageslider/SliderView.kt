package io.selimdawa.autoimageslider

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.selimdawa.autoimageslider.adapter.DefaultSliderAdapter
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter
import io.selimdawa.autoimageslider.view.PageIndicatorView
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.controller.DrawController
import io.selimdawa.autoimageslider.view.draw.data.IndicatorShape
import io.selimdawa.autoimageslider.view.draw.data.Orientation
import io.selimdawa.autoimageslider.view.draw.data.RtlMode
import io.selimdawa.autoimageslider.view.utils.DensityUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SliderView : FrameLayout {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        setupSlideView()
        attrs?.let { setUpAttributes(it) }
    }

    private var mFlagBackAndForth = false
    private var mJob: Job? = null
    private var mViewPager2: ViewPager2? = null

    var isAutoCycle = false
        set(value) {
            field = value; if (value) startAutoCycle() else stopAutoCycle()
        }

    var autoCycleDirection = AUTO_CYCLE_DIRECTION_RIGHT
    var scrollTimeInMillis = 2000
    var pagerIndicator: PageIndicatorView? = null; private set
    var sliderAdapter: RecyclerView.Adapter<*>? = null; private set
    var isInfiniteAdapter = true
    private var mPageListener: OnSliderPageListener? = null

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            checkPosition()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkPosition()
        }
    }

    private fun checkPosition() {
        val pager = mViewPager2 ?: return
        val adapter = sliderAdapter ?: return
        if (isInfiniteAdapter && adapter.itemCount > 0 && pager.currentItem == 0) {
            setCurrentPagePosition(0, false)
        }
    }

    var scrollTimeInSec: Int
        get() = scrollTimeInMillis / 1000
        set(value) {
            scrollTimeInMillis = value * 1000
        }

    var sliderAnimationDuration: Int = 400

    var currentPagePosition: Int
        get() = mViewPager2?.currentItem?.let {
            if (isInfiniteAdapter) (sliderAdapter as? InfinitePagerAdapter<*>)?.getRealPosition(
                it
            ) else it
        } ?: 0
        set(value) = setCurrentPagePosition(value, true)

    fun setCurrentPagePosition(value: Int, smoothScroll: Boolean) {
        val pager = mViewPager2 ?: return
        val adapter = sliderAdapter ?: return
        if (adapter.itemCount <= 0) return

        val target =
            if (isInfiniteAdapter) (adapter as? InfinitePagerAdapter<*>)?.getMiddlePosition(
                value
            ) else value

        if (smoothScroll) {
            pager.setCurrentItem(target ?: value, true)
        } else {
            pager.setCurrentItem(target ?: value, false)
        }
    }

    private fun setCurrentItemWithDuration(
        item: Int,
        duration: Long,
        interpolator: AccelerateDecelerateInterpolator = AccelerateDecelerateInterpolator()
    ) {
        val pager = mViewPager2 ?: return
        if (duration <= 0 || pager.width <= 0) {
            pager.setCurrentItem(item, true)
            return
        }

        val pxToDrag: Int = (item - pager.currentItem) * pager.width
        if (pxToDrag == 0) return

        val animator = ValueAnimator.ofInt(0, pxToDrag)
        var previousValue = 0

        animator.addUpdateListener { valueAnimator ->
            val currentValue = valueAnimator.animatedValue as Int
            val currentPxToDrag = (currentValue - previousValue).toFloat()
            if (pager.isFakeDragging) {
                pager.fakeDragBy(-currentPxToDrag)
            }
            previousValue = currentValue
        }

        animator.addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {
                pager.beginFakeDrag()
            }

            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (pager.isFakeDragging) pager.endFakeDrag()
            }

            override fun onAnimationCancel(animation: android.animation.Animator) {
                if (pager.isFakeDragging) pager.endFakeDrag()
            }

            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })

        animator.interpolator = interpolator
        animator.duration = duration
        animator.start()
    }

    var indicatorSelectedColor: Int
        get() = pagerIndicator?.selectedColor ?: 0
        set(value) {
            pagerIndicator?.selectedColor = value
        }

    var indicatorUnselectedColor: Int
        get() = pagerIndicator?.unselectedColor ?: 0
        set(value) {
            pagerIndicator?.unselectedColor = value
        }

    var indicatorRadius: Int
        get() = pagerIndicator?.radius ?: 0
        set(value) {
            pagerIndicator?.radius = value
        }

    var indicatorShape: IndicatorShape
        get() = pagerIndicator?.indicatorShape ?: IndicatorShape.CIRCLE
        set(value) {
            pagerIndicator?.indicatorShape = value
        }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideView() {
        mViewPager2 = ViewPager2(context).apply {
            id = generateViewId()
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    val realPos =
                        if (isInfiniteAdapter) (sliderAdapter as? InfinitePagerAdapter<*>)?.getRealPosition(
                            position
                        ) ?: position else position
                    mPageListener?.onSliderPageChanged(realPos)
                }
            })
            // Detect user touch to pause auto-cycle
            try {
                (getChildAt(0) as? RecyclerView)?.setOnTouchListener { v, event ->
                    if (isAutoCycle) {
                        when (event.action) {
                            MotionEvent.ACTION_MOVE -> stopAutoCycle()
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                v.performClick()
                                startAutoCycle()
                            }
                        }
                    }
                    false
                }
            } catch (_: Exception) {
            }
        }
        addView(mViewPager2, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    private fun setUpAttributes(attrs: AttributeSet) {
        context.withStyledAttributes(attrs, R.styleable.SliderView) {
            scrollTimeInSec = getInt(R.styleable.SliderView_sliderScrollTimeInSec, 2)
            autoCycleDirection = getInt(R.styleable.SliderView_sliderAutoCycleDirection, 0)
            sliderAnimationDuration = getInt(R.styleable.SliderView_sliderAnimationDuration, 400)
            val autoCycleEnabled = getBoolean(R.styleable.SliderView_sliderAutoCycleEnabled, true)
            isAutoCycle =
                getBoolean(R.styleable.SliderView_sliderStartAutoCycle, false) || autoCycleEnabled

            if (getBoolean(R.styleable.SliderView_sliderIndicatorEnabled, true)) {
                initIndicator()
                pagerIndicator?.apply {
                    setOrientation(
                        if (getInt(
                                R.styleable.SliderView_sliderIndicatorOrientation, 0
                            ) == 0
                        ) Orientation.HORIZONTAL else Orientation.VERTICAL
                    )
                    indicatorRadius = getDimension(
                        R.styleable.SliderView_sliderIndicatorRadius,
                        DensityUtils.dpToPx(4).toFloat()
                    ).toInt()
                    Log.d(
                        "SliderView",
                        "Indicator radius: ${DensityUtils.pxToDp(indicatorRadius.toFloat())}dp"
                    )
                    padding = getDimension(
                        R.styleable.SliderView_sliderIndicatorPadding,
                        DensityUtils.dpToPx(6).toFloat()
                    ).toInt()
                    val margin = getDimension(
                        R.styleable.SliderView_sliderIndicatorMargin,
                        DensityUtils.dpToPx(12).toFloat()
                    ).toInt()
                    setIndicatorMargins(
                        getDimension(
                            R.styleable.SliderView_sliderIndicatorMarginLeft, margin.toFloat()
                        ).toInt(), getDimension(
                            R.styleable.SliderView_sliderIndicatorMarginTop, margin.toFloat()
                        ).toInt(), getDimension(
                            R.styleable.SliderView_sliderIndicatorMarginRight, margin.toFloat()
                        ).toInt(), getDimension(
                            R.styleable.SliderView_sliderIndicatorMarginBottom, margin.toFloat()
                        ).toInt()
                    )
                    setIndicatorGravity(
                        getInt(
                            R.styleable.SliderView_sliderIndicatorGravity,
                            Gravity.CENTER or Gravity.BOTTOM
                        )
                    )
                    selectedColor = getColor(
                        R.styleable.SliderView_sliderIndicatorSelectedColor, "#ffffff".toColorInt()
                    )
                    unselectedColor = getColor(
                        R.styleable.SliderView_sliderIndicatorUnselectedColor,
                        "#33ffffff".toColorInt()
                    )
                    animationDuration = getInt(
                        R.styleable.SliderView_sliderIndicatorAnimationDuration, 350
                    ).toLong()
                    setRtlMode(
                        RtlMode.entries.getOrNull(
                            getInt(
                                R.styleable.SliderView_sliderIndicatorRtlMode, RtlMode.Off.ordinal
                            )
                        ) ?: RtlMode.Auto
                    )
                    this@SliderView.indicatorShape = IndicatorShape.entries.getOrNull(
                        getInt(
                            R.styleable.SliderView_sliderIndicatorShape,
                            IndicatorShape.CIRCLE.ordinal
                        )
                    ) ?: IndicatorShape.CIRCLE
                }
            }
        }
    }

    private fun initIndicator() {
        if (pagerIndicator == null) {
            pagerIndicator = PageIndicatorView(context).apply {
                setDynamicCount(true)
                setViewPager(mViewPager2)
            }
            addView(
                pagerIndicator,
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                    setMargins(20, 20, 20, 20)
                })
        }
    }

    @Suppress("unused")
    fun setSliderAdapter(adapter: RecyclerView.Adapter<*>, infinite: Boolean = true) {
        // Unregister from old adapter if any
        try {
            this.sliderAdapter?.unregisterAdapterDataObserver(adapterDataObserver)
        } catch (_: Exception) {
        }

        val wrappedAdapter = if (infinite) InfinitePagerAdapter(adapter) else adapter
        this.sliderAdapter = wrappedAdapter
        this.isInfiniteAdapter = infinite
        mViewPager2?.adapter = this.sliderAdapter

        // Register observer to handle initial position when items are added later
        try {
            this.sliderAdapter?.registerAdapterDataObserver(adapterDataObserver)
        } catch (_: Exception) {
        }

        val count =
            if (infinite) (wrappedAdapter as InfinitePagerAdapter<*>).realCount else wrappedAdapter.itemCount
        if (infinite && count > 0) setCurrentPagePosition(0, false)

        pagerIndicator?.setViewPager(mViewPager2)
    }

    @Suppress("unused")
    fun setSliderAdapter(urls: List<String>) {
        setSliderAdapter(DefaultSliderAdapter(urls), true)
    }

    fun setSliderTransformAnimation(animation: SliderAnimations) {
        mViewPager2?.setPageTransformer(SmartTransformer(animation))
    }

    @Suppress("unused")
    fun setCustomSliderTransformAnimation(transformer: ViewPager2.PageTransformer) {
        mViewPager2?.setPageTransformer(transformer)
    }

    fun setIndicatorAnimation(type: IndicatorAnimationType) {
        pagerIndicator?.setAnimationType(type)
    }

    fun startAutoCycle() {
        stopAutoCycle()
        if (!isAttachedToWindow) return
        val lifecycleOwner = findViewTreeLifecycleOwner() ?: return
        mJob = lifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(scrollTimeInMillis.milliseconds)
                if (isActive) slideNext()
            }
        }
    }

    fun stopAutoCycle() {
        mJob?.cancel()
        mJob = null
    }

    private fun slideNext() {
        val pager = mViewPager2 ?: return
        val adapter = sliderAdapter
        val count = adapter?.itemCount ?: 0
        if (count <= 1) return

        val nextPos = when (autoCycleDirection) {
            AUTO_CYCLE_DIRECTION_BACK_AND_FORTH -> {
                val current = pager.currentItem
                val realCount =
                    if (isInfiniteAdapter) (adapter as? InfinitePagerAdapter<*>)?.realCount
                        ?: 0 else count
                val realPos =
                    if (isInfiniteAdapter) (adapter as? InfinitePagerAdapter<*>)?.getRealPosition(
                        current
                    ) ?: 0 else current

                if (realPos == 0) mFlagBackAndForth = true
                if (realPos == realCount - 1) mFlagBackAndForth = false
                current + (if (mFlagBackAndForth) 1 else -1)
            }

            AUTO_CYCLE_DIRECTION_LEFT -> pager.currentItem - 1
            else -> pager.currentItem + 1
        }
        setCurrentItemWithDuration(nextPos, sliderAnimationDuration.toLong())
    }

    @Suppress("unused")
    fun slideToNextPosition() {
        slideNext()
    }

    @Suppress("unused")
    fun slideToPreviousPosition() {
        val pager = mViewPager2 ?: return
        setCurrentItemWithDuration(pager.currentItem - 1, sliderAnimationDuration.toLong())
    }

    fun setIndicatorGravity(gravity: Int) = pagerIndicator?.let {
        (it.layoutParams as LayoutParams).gravity = gravity; it.requestLayout()
    }

    fun setIndicatorMargins(l: Int, t: Int, r: Int, b: Int) = pagerIndicator?.let {
        (it.layoutParams as LayoutParams).setMargins(
            l, t, r, b
        ); it.requestLayout()
    }

    fun setOnIndicatorClickListener(l: DrawController.ClickListener?) =
        pagerIndicator?.setClickListener(l)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isAutoCycle) startAutoCycle()
    }

    override fun onDetachedFromWindow() {
        stopAutoCycle()
        super.onDetachedFromWindow()
    }

    @Suppress("unused")
    fun setCurrentPageListener(listener: OnSliderPageListener?) {
        this.mPageListener = listener
    }

    interface OnSliderPageListener {
        fun onSliderPageChanged(position: Int)
    }

    companion object {
        const val AUTO_CYCLE_DIRECTION_RIGHT = 0
        const val AUTO_CYCLE_DIRECTION_LEFT = 1
        const val AUTO_CYCLE_DIRECTION_BACK_AND_FORTH = 2
    }
}