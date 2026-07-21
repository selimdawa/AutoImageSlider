package io.selimdawa.autoimageslider.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import androidx.core.os.ConfigurationCompat
import androidx.core.text.TextUtilsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.selimdawa.autoimageslider.R
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter
import io.selimdawa.autoimageslider.view.animation.AnimationManager
import io.selimdawa.autoimageslider.view.animation.UpdateListener
import io.selimdawa.autoimageslider.view.draw.IndicatorDrawer
import io.selimdawa.autoimageslider.view.model.Indicator
import io.selimdawa.autoimageslider.view.model.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.model.IndicatorShape
import io.selimdawa.autoimageslider.view.model.Orientation
import io.selimdawa.autoimageslider.view.model.PositionSavedState
import io.selimdawa.autoimageslider.view.model.RtlMode
import io.selimdawa.autoimageslider.view.model.Value
import io.selimdawa.autoimageslider.view.utils.CoordinatesUtils
import io.selimdawa.autoimageslider.view.utils.DensityUtils
import kotlin.math.min

class PageIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), UpdateListener {

    private val indicator = Indicator()
    private val drawer = IndicatorDrawer(indicator)
    private val animation = AnimationManager(indicator, this)

    private var viewPager2: ViewPager2? = null
    private var isInteractionEnabled = true

    init {
        if (id == NO_ID) id = generateViewId()
        initAttributes(context, attrs)
        indicator.apply {
            paddingLeft = this@PageIndicatorView.paddingLeft
            paddingTop = this@PageIndicatorView.paddingTop
            paddingRight = this@PageIndicatorView.paddingRight
            paddingBottom = this@PageIndicatorView.paddingBottom
        }
    }

    private fun initAttributes(context: Context, attrs: AttributeSet?) {
        context.withStyledAttributes(attrs, R.styleable.PageIndicatorView, 0, 0) {
            val count = getInt(
                R.styleable.PageIndicatorView_piv_count, Indicator.COUNT_NONE
            ).let { if (it == Indicator.COUNT_NONE) Indicator.DEFAULT_COUNT else it }
            val position = getInt(R.styleable.PageIndicatorView_piv_select, 0).coerceIn(
                0, (count - 1).coerceAtLeast(0)
            )

            indicator.apply {
                this.count = count
                selectedPosition = position; selectingPosition = position; lastSelectedPosition =
                position
                unselectedColor = getColor(
                    R.styleable.PageIndicatorView_piv_unselectedColor, "#33ffffff".toColorInt()
                )
                selectedColor = getColor(
                    R.styleable.PageIndicatorView_piv_selectedColor, "#ffffff".toColorInt()
                )
                animationDuration =
                    getInt(R.styleable.PageIndicatorView_piv_animationDuration, 350).toLong()
                isInteractiveAnimation =
                    getBoolean(R.styleable.PageIndicatorView_piv_interactiveAnimation, true)
                isInteractionEnabled = isInteractiveAnimation

                animationType = IndicatorAnimationType.entries.getOrNull(
                    getInt(
                        R.styleable.PageIndicatorView_piv_animationType,
                        IndicatorAnimationType.NONE.ordinal
                    )
                ) ?: IndicatorAnimationType.NONE

                rtlMode = RtlMode.entries.getOrNull(
                    getInt(R.styleable.PageIndicatorView_piv_rtl_mode, RtlMode.Off.ordinal)
                ) ?: RtlMode.Auto

                indicatorShape = IndicatorShape.entries.getOrNull(
                    getInt(
                        R.styleable.PageIndicatorView_piv_indicatorShape,
                        IndicatorShape.CIRCLE.ordinal
                    )
                ) ?: IndicatorShape.CIRCLE

                radius = getDimension(
                    R.styleable.PageIndicatorView_piv_radius, DensityUtils.dpToPx(4).toFloat()
                ).toInt()
                padding = getDimension(
                    R.styleable.PageIndicatorView_piv_padding, DensityUtils.dpToPx(6).toFloat()
                ).toInt()
                scaleFactor =
                    getFloat(R.styleable.PageIndicatorView_piv_scaleFactor, 0.7f).coerceIn(
                        0.3f, 1.0f
                    )
                stroke = if (animationType == IndicatorAnimationType.FILL) getDimension(
                    R.styleable.PageIndicatorView_piv_strokeWidth, DensityUtils.dpToPx(1).toFloat()
                ).toInt() else 0

                isAutoVisibility =
                    getBoolean(R.styleable.PageIndicatorView_piv_autoVisibility, true)
                isDynamicCount = getBoolean(R.styleable.PageIndicatorView_piv_dynamicCount, false)
                orientation = if (getInt(
                        R.styleable.PageIndicatorView_piv_orientation, 0
                    ) == 0
                ) Orientation.HORIZONTAL else Orientation.VERTICAL
            }
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(pos: Int, off: Float, offPx: Int) {
            if (isViewMeasured && indicator.animationType != IndicatorAnimationType.NONE) {
                if (indicator.isInteractiveAnimation) {
                    val count = indicator.count
                    if (count <= 0) return
                    
                    val from = pos % count
                    val progress = CoordinatesUtils.getProgress(indicator, pos, off, isRtl)
                    
                    // Stateless update: from is always pos, to is always pos + 1
                    indicator.selectedPosition = from
                    setProgress(progress.first, progress.second)
                }
            }
        }

        override fun onPageSelected(pos: Int) {
            val adapter = viewPager2?.adapter
            val realPos = (adapter as? InfinitePagerAdapter<*>)?.getRealPosition(pos) ?: pos
            val sel = if (isRtl) (count - 1) - realPos else realPos

            if (viewPager2?.scrollState == ViewPager2.SCROLL_STATE_IDLE || !indicator.isInteractiveAnimation || indicator.animationType == IndicatorAnimationType.NONE) {
                selection = sel
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                indicator.isInteractiveAnimation = isInteractionEnabled
                updateState()
            } else if (state == ViewPager2.SCROLL_STATE_DRAGGING || state == ViewPager2.SCROLL_STATE_SETTLING) {
                indicator.isInteractiveAnimation = true
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = updateState()
        override fun onItemRangeChanged(p: Int, c: Int, payload: Any?) = updateState()
        override fun onItemRangeInserted(p: Int, c: Int) = updateState()
        override fun onItemRangeRemoved(p: Int, c: Int) = updateState()
    }

    var count: Int
        get() = indicator.count
        set(value) {
            if (value >= 0 && indicator.count != value) {
                indicator.count = value; updateVisibility(); requestLayout()
            }
        }

    var radius: Int
        get() = indicator.radius
        set(value) {
            indicator.radius = maxOf(0, value); invalidate()
        }

    var padding: Int
        get() = indicator.padding
        set(value) {
            indicator.padding = maxOf(0, value); invalidate()
        }

    var selectedColor: Int
        get() = indicator.selectedColor
        set(value) {
            indicator.selectedColor = value; invalidate()
        }

    var unselectedColor: Int
        get() = indicator.unselectedColor
        set(value) {
            indicator.unselectedColor = value; invalidate()
        }

    var animationDuration: Long
        get() = indicator.animationDuration
        set(value) {
            indicator.animationDuration = value
        }

    var indicatorShape: IndicatorShape
        get() = indicator.indicatorShape ?: IndicatorShape.CIRCLE
        set(value) {
            indicator.indicatorShape = value; invalidate()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val count = indicator.count
        val r = indicator.radius
        val isH = indicator.orientation == Orientation.HORIZONTAL
        var dW = 0
        var dH = 0

        if (count != 0) {
            val w = (r * 2 * count) + (indicator.padding * (count - 1))
            val h = r * 2
            dW = if (isH) w else h; dH = if (isH) h else w
        }
        if (indicator.animationType == IndicatorAnimationType.DROP) {
            if (isH) dH *= 2 else dW *= 2
        }

        dW += paddingLeft + paddingRight; dH += paddingTop + paddingBottom

        val w = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(widthMeasureSpec)
            MeasureSpec.AT_MOST -> min(dW, MeasureSpec.getSize(widthMeasureSpec))
            else -> dW
        }.coerceAtLeast(0)

        val h = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> min(dH, MeasureSpec.getSize(heightMeasureSpec))
            else -> dH
        }.coerceAtLeast(0)

        indicator.width = w; indicator.height = h
        setMeasuredDimension(w, h)
    }

    override fun onDraw(canvas: Canvas) = drawer.draw(canvas)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?) = true.also { drawer.touch(ev) }

    override fun onValueUpdated(value: Value?) {
        drawer.updateValue(value); invalidate()
    }

    fun setDynamicCount(dynamic: Boolean) {
        indicator.isDynamicCount = dynamic
    }

    fun setOrientation(o: Orientation?) {
        indicator.orientation = o; requestLayout()
    }

    fun setAnimationType(t: IndicatorAnimationType?) {
        onValueUpdated(null)
        indicator.animationType = t ?: IndicatorAnimationType.NONE
        invalidate()
    }

    fun setViewPager(pager: ViewPager2?) {
        releaseViewPager()
        viewPager2 = pager?.apply {
            registerOnPageChangeCallback(onPageChangeCallback)
            adapter?.registerAdapterDataObserver(adapterDataObserver)
        }
        updateState()
    }

    fun releaseViewPager() {
        viewPager2?.unregisterOnPageChangeCallback(onPageChangeCallback)
        try {
            viewPager2?.adapter?.unregisterAdapterDataObserver(adapterDataObserver)
        } catch (_: Exception) {
        }
        viewPager2 = null
    }

    fun setRtlMode(mode: RtlMode?) {
        indicator.rtlMode = mode ?: RtlMode.Off; updateState()
    }

    var selection: Int
        get() = indicator.selectedPosition
        set(value) {
            val adj = value.coerceIn(0, maxOf(0, indicator.count - 1))
            if (adj != indicator.selectedPosition) {
                indicator.isInteractiveAnimation = false
                indicator.lastSelectedPosition = indicator.selectedPosition
                indicator.selectingPosition = adj
                indicator.selectedPosition = adj
                animation.basic()
            }
        }

    fun setProgress(pos: Int, progress: Float) {
        if (!indicator.isInteractiveAnimation) return
        val finalPos = pos.coerceIn(0, maxOf(0, indicator.count - 1))
        val finalProg = progress.coerceIn(0f, 1f)
        
        indicator.selectingPosition = finalPos
        animation.interactive(finalProg)
    }

    fun setClickListener(l: IndicatorDrawer.ClickListener?) = drawer.setClickListener(l)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (indicator.viewPagerId != NO_ID) {
            val pager = (parent as? View)?.findViewById<ViewPager2>(indicator.viewPagerId)
            if (pager != null) setViewPager(pager)
        }
    }

    override fun onDetachedFromWindow() {
        releaseViewPager()
        super.onDetachedFromWindow()
    }

    private fun updateState() {
        val pager = viewPager2 ?: return
        val adapter = pager.adapter ?: return
        val c = if (adapter is InfinitePagerAdapter<*>) adapter.realCount else adapter.itemCount
        val pos = pager.currentItem
        val realPos = (adapter as? InfinitePagerAdapter<*>)?.getRealPosition(pos) ?: pos
        val sel = if (c > 0) (if (isRtl) (c - 1) - realPos else realPos).coerceIn(0, c - 1) else 0

        indicator.apply {
            selectedPosition = sel; selectingPosition = sel; lastSelectedPosition = sel
            count = c; animation.end(); updateVisibility(); requestLayout()
        }
    }

    private fun updateVisibility() {
        if (indicator.isAutoVisibility) visibility = if (indicator.count > 1) VISIBLE else INVISIBLE
    }

    private val isRtl
        get() = indicator.rtlMode == RtlMode.On || (indicator.rtlMode == RtlMode.Auto && TextUtilsCompat.getLayoutDirectionFromLocale(
            ConfigurationCompat.getLocales(context.resources.configuration)[0]
        ) == LAYOUT_DIRECTION_RTL)

    private val isViewMeasured get() = measuredHeight != 0 || measuredWidth != 0

    override fun onSaveInstanceState() = PositionSavedState(super.onSaveInstanceState()).apply {
        selectedPosition = indicator.selectedPosition
        selectingPosition = indicator.selectingPosition
        lastSelectedPosition = indicator.lastSelectedPosition
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PositionSavedState) {
            indicator.apply {
                selectedPosition = state.selectedPosition
                selectingPosition = state.selectingPosition
                lastSelectedPosition = state.lastSelectedPosition
            }
            super.onRestoreInstanceState(state.superState)
        } else super.onRestoreInstanceState(state)
    }
}