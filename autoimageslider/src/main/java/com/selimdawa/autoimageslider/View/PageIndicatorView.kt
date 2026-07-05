package com.selimdawa.autoimageslider.View

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.core.text.TextUtilsCompat
import androidx.core.view.isEmpty
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.selimdawa.autoimageslider.View.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.View.draw.controller.DrawController.ClickListener
import com.selimdawa.autoimageslider.View.draw.data.Indicator
import com.selimdawa.autoimageslider.View.draw.data.Orientation
import com.selimdawa.autoimageslider.View.draw.data.PositionSavedState
import com.selimdawa.autoimageslider.View.draw.data.RtlMode
import com.selimdawa.autoimageslider.View.utils.CoordinatesUtils
import com.selimdawa.autoimageslider.View.utils.DensityUtils
import com.selimdawa.autoimageslider.Adapter.InfinitePagerAdapter
import com.selimdawa.autoimageslider.SliderPager

@Suppress("unused")
class PageIndicatorView : View, SliderPager.OnPageChangeListener, IndicatorManager.Listener,
    SliderPager.OnAdapterChangeListener {

    private var manager: IndicatorManager? = null
    private var setObserver: DataSetObserver? = null
    private var viewPager: SliderPager? = null
    private var isInteractionEnabled = false

    constructor(context: Context?) : super(context) {
        init(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(attrs)
    }

    constructor(
        context: Context?, attrs: AttributeSet?, defStyle: Int, defRes: Int
    ) : super(context, attrs, defStyle, defRes) {
        init(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow(); findViewPager(parent)
    }

    override fun onDetachedFromWindow() {
        unRegisterSetObserver(); super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState() = PositionSavedState(super.onSaveInstanceState()).apply {
        manager?.indicator()?.let {
            selectedPosition = it.selectedPosition; selectingPosition =
            it.selectingPosition; lastSelectedPosition = it.lastSelectedPosition
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PositionSavedState) {
            manager?.indicator()?.let {
                it.selectedPosition = state.selectedPosition; it.selectingPosition =
                state.selectingPosition; it.lastSelectedPosition = state.lastSelectedPosition
            }
            super.onRestoreInstanceState(state.superState)
        } else super.onRestoreInstanceState(state)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pair = manager?.drawer()?.measureViewSize(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(pair?.first ?: 0, pair?.second ?: 0)
    }

    override fun onDraw(canvas: Canvas) {
        manager?.drawer()?.draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?) = true.also { manager?.drawer()?.touch(event) }

    override fun onIndicatorUpdated() = invalidate()
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) =
        onPageScroll(position, positionOffset)

    override fun onPageSelected(position: Int) = onPageSelect(position)
    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) manager?.indicator()?.isInteractiveAnimation =
            isInteractionEnabled
    }

    override fun onAdapterChanged(
        viewPager: SliderPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?
    ) = updateState()

    var count: Int
        get() = manager?.indicator()?.count ?: 0
        set(value) {
            val ind = manager?.indicator() ?: return
            if (value >= 0 && ind.count != value) {
                ind.count = value; updateVisibility(); requestLayout()
            }
        }

    fun setDynamicCount(dynamicCount: Boolean) {
        manager?.indicator()?.isDynamicCount = dynamicCount
        if (dynamicCount) registerSetObserver() else unRegisterSetObserver()
    }

    fun setRadius(radiusPx: Float) {
        manager?.indicator()?.radius = radiusPx.coerceAtLeast(0f).toInt()
        invalidate()
    }

    var radius: Int
        get() = manager?.indicator()?.radius ?: 0
        set(value) {
            manager?.indicator()?.radius = DensityUtils.dpToPx(value.coerceAtLeast(0))
            invalidate()
        }

    fun setPadding(paddingPx: Float) {
        manager?.indicator()?.padding = paddingPx.coerceAtLeast(0f).toInt()
        invalidate()
    }

    var padding: Int
        get() = manager?.indicator()?.padding ?: 0
        set(value) {
            manager?.indicator()?.padding = DensityUtils.dpToPx(value.coerceAtLeast(0))
            invalidate()
        }

    var scaleFactor: Float
        get() = manager?.indicator()?.scaleFactor ?: 0f
        set(value) {
            manager?.indicator()?.scaleFactor = value.coerceIn(0.3f, 1.0f)
        }

    fun setStrokeWidth(strokePx: Float) {
        val r = manager?.indicator()?.radius ?: 0
        manager?.indicator()?.stroke = strokePx.coerceIn(0f, r.toFloat()).toInt()
        invalidate()
    }

    fun setStrokeWidth(strokeDp: Int) {
        val r = manager?.indicator()?.radius ?: 0
        manager?.indicator()?.stroke = DensityUtils.dpToPx(strokeDp).coerceIn(0, r)
        invalidate()
    }

    val strokeWidth get() = manager?.indicator()?.stroke ?: 0

    var selectedColor: Int
        get() = manager?.indicator()?.selectedColor ?: 0
        set(value) {
            manager?.indicator()?.selectedColor = value; invalidate()
        }

    var unselectedColor: Int
        get() = manager?.indicator()?.unselectedColor ?: 0
        set(value) {
            manager?.indicator()?.unselectedColor = value; invalidate()
        }

    fun setAutoVisibility(autoVisibility: Boolean) {
        if (!autoVisibility) visibility = VISIBLE
        manager?.indicator()?.isAutoVisibility = autoVisibility
        updateVisibility()
    }

    fun setOrientation(orientation: Orientation?) {
        if (orientation != null) {
            manager?.indicator()?.orientation = orientation; requestLayout()
        }
    }

    var animationDuration: Long
        get() = manager?.indicator()?.animationDuration ?: 0L
        set(value) {
            manager?.indicator()?.animationDuration = value
        }

    fun setAnimationType(type: IndicatorAnimationType?) {
        manager?.onValueUpdated(null)
        manager?.indicator()
            ?.let { it.animationType = type ?: IndicatorAnimationType.NONE; invalidate() }
    }

    fun setInteractiveAnimation(isInteractive: Boolean) {
        manager?.indicator()?.isInteractiveAnimation = isInteractive
        isInteractionEnabled = isInteractive
    }

    fun setViewPager(pager: SliderPager?) {
        releaseViewPager()
        if (pager == null) return
        viewPager = pager.apply {
            addOnPageChangeListener(this@PageIndicatorView); addOnAdapterChangeListener(this@PageIndicatorView)
        }
        manager?.indicator()
            ?.let { it.viewPagerId = pager.id; setDynamicCount(it.isDynamicCount); updateState() }
    }

    fun releaseViewPager() {
        viewPager?.removeOnPageChangeListener(this); viewPager = null
    }

    fun setRtlMode(mode: RtlMode?) {
        val ind = manager?.indicator() ?: return
        ind.rtlMode = mode ?: RtlMode.Off
        val pager = viewPager ?: return
        val pos = if (isRtl) (ind.count - 1) - ind.selectedPosition else pager.currentItem
        ind.lastSelectedPosition = pos; ind.selectingPosition = pos; ind.selectedPosition = pos
        invalidate()
    }

    var selection: Int
        get() = manager?.indicator()?.selectedPosition ?: 0
        set(value) {
            val ind = manager?.indicator() ?: return
            val adjusted = adjustPosition(value)
            if (adjusted == ind.selectedPosition || adjusted == ind.selectingPosition) return
            ind.isInteractiveAnimation = false; ind.lastSelectedPosition = ind.selectedPosition
            ind.selectingPosition = adjusted; ind.selectedPosition = adjusted
            manager?.animate()?.basic()
        }

    fun setSelected(position: Int) {
        val ind = manager?.indicator() ?: return
        val anim = ind.animationType; ind.animationType = IndicatorAnimationType.NONE
        selection = position; ind.animationType = anim
    }

    fun clearSelection() {
        manager?.indicator()?.let {
            it.isInteractiveAnimation = false; it.lastSelectedPosition = Indicator.COUNT_NONE
            it.selectingPosition = Indicator.COUNT_NONE; it.selectedPosition = Indicator.COUNT_NONE
            manager?.animate()?.basic()
        }
    }

    fun setProgress(selectingPosition: Int, progress: Float) {
        val ind = manager?.indicator() ?: return
        if (!ind.isInteractiveAnimation) return
        val finalPos = selectingPosition.coerceIn(0, (ind.count - 1).coerceAtLeast(0))
        val finalProg = progress.coerceIn(0f, 1f)
        if (finalProg == 1f) {
            ind.lastSelectedPosition = ind.selectedPosition; ind.selectedPosition = finalPos
        }
        ind.selectingPosition = finalPos
        manager?.animate()?.interactive(finalProg)
    }

    fun setClickListener(listener: ClickListener?) = manager?.drawer()?.setClickListener(listener)

    private fun init(attrs: AttributeSet?) {
        if (id == NO_ID) id = generateViewId()
        manager = IndicatorManager(this).apply {
            drawer().initAttributes(context, attrs)
            indicator().let {
                it.paddingLeft = this@PageIndicatorView.paddingLeft; it.paddingTop =
                this@PageIndicatorView.paddingTop
                it.paddingRight = this@PageIndicatorView.paddingRight; it.paddingBottom =
                this@PageIndicatorView.paddingBottom
                isInteractionEnabled = it.isInteractiveAnimation
            }
        }
    }

    private fun registerSetObserver() {
        val pager = viewPager
        val adapter = pager?.adapter
        if (setObserver != null || pager == null || adapter == null) return
        setObserver = object : DataSetObserver() {
            override fun onChanged() = updateState()
        }
        runCatching { adapter.registerDataSetObserver(setObserver!!) }
    }

    private fun unRegisterSetObserver() {
        val adapter = viewPager?.adapter
        if (setObserver == null || adapter == null) return
        runCatching { adapter.unregisterDataSetObserver(setObserver!!); setObserver = null }
    }

    private fun updateState() {
        val pager = viewPager
        val adapter = pager?.adapter
        if (pager == null || adapter == null) return
        val isInfinite = adapter is InfinitePagerAdapter
        val count = if (isInfinite) adapter.realCount else adapter.count
        val rawPos = pager.currentItem
        val position = if (isInfinite) (if (count > 0) rawPos % count else 0) else rawPos
        val selectedPos = if (isRtl) (count - 1) - position else position

        manager?.indicator()?.let {
            it.selectedPosition = selectedPos; it.selectingPosition =
            selectedPos; it.lastSelectedPosition = selectedPos
            it.count = count; manager?.animate()?.end(); updateVisibility(); requestLayout()
        }
    }

    private fun updateVisibility() {
        val ind = manager?.indicator() ?: return
        if (ind.isAutoVisibility) visibility =
            if (ind.count > Indicator.MIN_COUNT) VISIBLE else INVISIBLE
    }

    private fun onPageSelect(position: Int) {
        val ind = manager?.indicator() ?: return
        if (isViewMeasured) selection = if (isRtl) (ind.count - 1) - position else position
    }

    private fun onPageScroll(position: Int, positionOffset: Float) {
        val ind = manager?.indicator() ?: return
        if (isViewMeasured && ind.isInteractiveAnimation && ind.animationType != IndicatorAnimationType.NONE) {
            val progressPair = CoordinatesUtils.getProgress(ind, position, positionOffset, isRtl)
            setProgress(progressPair.first ?: 0, progressPair.second ?: 0f)
        }
    }

    private val isRtl
        get() = when (manager?.indicator()?.rtlMode) {
            RtlMode.On -> true
            RtlMode.Off -> false
            RtlMode.Auto -> TextUtilsCompat.getLayoutDirectionFromLocale(
                androidx.core.os.ConfigurationCompat.getLocales(
                    context.resources.configuration
                ).get(0)
            ) == LAYOUT_DIRECTION_RTL

            else -> false
        }

    private val isViewMeasured get() = measuredHeight != 0 || measuredWidth != 0

    private fun findViewPager(parent: ViewParent?) {
        if (parent == null || parent !is ViewGroup || parent.isEmpty()) return
        val found = findViewPager(parent, manager?.indicator()?.viewPagerId ?: NO_ID)
        if (found != null) setViewPager(found) else findViewPager(parent.parent)
    }

    private fun findViewPager(viewGroup: ViewGroup, id: Int) =
        viewGroup.findViewById<View?>(id) as? SliderPager

    private fun adjustPosition(position: Int): Int {
        val ind = manager?.indicator() ?: return 0
        return position.coerceIn(0, (ind.count - 1).coerceAtLeast(0))
    }
}