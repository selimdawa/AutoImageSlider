package com.selimdawa.autoimageslider.IndicatorView

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
import androidx.core.view.ViewCompat
import androidx.core.view.isNotEmpty
import androidx.core.view.size
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.selimdawa.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType
import com.selimdawa.autoimageslider.IndicatorView.draw.controller.DrawController.ClickListener
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Indicator
import com.selimdawa.autoimageslider.IndicatorView.draw.data.Orientation
import com.selimdawa.autoimageslider.IndicatorView.draw.data.PositionSavedState
import com.selimdawa.autoimageslider.IndicatorView.draw.data.RtlMode
import com.selimdawa.autoimageslider.IndicatorView.utils.CoordinatesUtils
import com.selimdawa.autoimageslider.IndicatorView.utils.DensityUtils
import com.selimdawa.autoimageslider.InfiniteAdapter.InfinitePagerAdapter
import com.selimdawa.autoimageslider.SliderPager

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

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewPager(parent)
    }

    override fun onDetachedFromWindow() {
        unRegisterSetObserver()
        super.onDetachedFromWindow()
    }

    public override fun onSaveInstanceState(): Parcelable {
        val currentSuperState = super.onSaveInstanceState()
        val positionSavedState = PositionSavedState(currentSuperState)

        manager?.indicator()?.let { indicator ->
            positionSavedState.selectedPosition = indicator.selectedPosition
            positionSavedState.selectingPosition = indicator.selectingPosition
            positionSavedState.lastSelectedPosition = indicator.lastSelectedPosition
        }

        return positionSavedState
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is PositionSavedState) {
            manager?.indicator()?.let { indicator ->
                indicator.selectedPosition = state.selectedPosition
                indicator.selectingPosition = state.selectingPosition
                indicator.lastSelectedPosition = state.lastSelectedPosition
            }
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val pair = manager?.drawer()?.measureViewSize(widthMeasureSpec, heightMeasureSpec)
        val width = pair?.first ?: 0
        val height = pair?.second ?: 0
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        manager!!.drawer().draw(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        manager!!.drawer().touch(event)
        return true
    }

    override fun onIndicatorUpdated() {
        invalidate()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        onPageScroll(position, positionOffset)
    }

    override fun onPageSelected(position: Int) {
        onPageSelect(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            manager?.indicator()?.isInteractiveAnimation = isInteractionEnabled
        }
    }

    override fun onAdapterChanged(
        viewPager: SliderPager,
        oldAdapter: PagerAdapter?,
        newAdapter: PagerAdapter?
    ) {
        updateState()
    }

    var count: Int
        get() = manager?.indicator()?.count ?: 0
        set(value) {
            val indicator = manager?.indicator() ?: return
            if (value >= 0 && indicator.count != value) {
                indicator.count = value
                updateVisibility()
                requestLayout()
            }
        }

    fun setDynamicCount(dynamicCount: Boolean) {
        manager?.indicator()?.isDynamicCount = dynamicCount

        if (dynamicCount) {
            registerSetObserver()
        } else {
            unRegisterSetObserver()
        }
    }

    fun setRadius(radiusPx: Float) {
        val finalRadiusPx = if (radiusPx < 0) {
            0f
        } else {
            radiusPx
        }

        manager?.indicator()?.radius = finalRadiusPx.toInt()
        invalidate()
    }

    var radius: Int
        get() = manager?.indicator()?.radius ?: 0
        set(radiusDp) {
            val finalRadiusDp = if (radiusDp < 0) {
                0
            } else {
                radiusDp
            }

            val radiusPx = DensityUtils.dpToPx(finalRadiusDp)
            manager?.indicator()?.radius = radiusPx
            invalidate()
        }

    fun setPadding(paddingPx: Float) {
        val finalPaddingPx = if (paddingPx < 0) {
            0f
        } else {
            paddingPx
        }

        manager?.indicator()?.padding = finalPaddingPx.toInt()
        invalidate()
    }

    var padding: Int
        get() = manager?.indicator()?.padding ?: 0
        set(paddingDp) {
            val finalPaddingDp = if (paddingDp < 0) {
                0
            } else {
                paddingDp
            }

            val paddingPx = DensityUtils.dpToPx(finalPaddingDp)
            manager?.indicator()?.padding = paddingPx
            invalidate()
        }

    var scaleFactor: Float
        get() = manager?.indicator()?.scaleFactor ?: 0f
        set(factor) {
            var finalFactor = factor
            if (finalFactor > 1.0f) {
                finalFactor = 1.0f
            } else if (finalFactor < 0.3f) {
                finalFactor = 0.3f
            }

            manager?.indicator()?.scaleFactor = finalFactor
        }

    fun setStrokeWidth(strokePx: Float) {
        var finalStrokePx = strokePx
        val radiusPx = manager?.indicator()?.radius ?: 0

        if (finalStrokePx < 0) {
            finalStrokePx = 0f
        } else if (finalStrokePx > radiusPx) {
            finalStrokePx = radiusPx.toFloat()
        }

        manager?.indicator()?.stroke = finalStrokePx.toInt()
        invalidate()
    }

    fun setStrokeWidth(strokeDp: Int) {
        var strokePx = DensityUtils.dpToPx(strokeDp)
        val radiusPx = manager?.indicator()?.radius ?: 0

        if (strokePx < 0) {
            strokePx = 0
        } else if (strokePx > radiusPx) {
            strokePx = radiusPx
        }

        manager?.indicator()?.stroke = strokePx
        invalidate()
    }

    val strokeWidth: Int
        get() = manager?.indicator()?.stroke ?: 0

    var selectedColor: Int
        get() = manager?.indicator()?.selectedColor ?: 0
        set(color) {
            manager?.indicator()?.selectedColor = color
            invalidate()
        }

    var unselectedColor: Int
        get() = manager?.indicator()?.unselectedColor ?: 0
        set(color) {
            manager?.indicator()?.unselectedColor = color
            invalidate()
        }

    fun setAutoVisibility(autoVisibility: Boolean) {
        if (!autoVisibility) {
            visibility = VISIBLE
        }

        manager?.indicator()?.isAutoVisibility = autoVisibility
        updateVisibility()
    }

    fun setOrientation(orientation: Orientation?) {
        if (orientation != null) {
            manager?.indicator()?.orientation = orientation
            requestLayout()
        }
    }

    var animationDuration: Long
        get() = manager?.indicator()?.animationDuration ?: 0L
        set(duration) {
            manager?.indicator()?.animationDuration = duration
        }

    fun setAnimationType(type: IndicatorAnimationType?) {
        manager?.onValueUpdated(null)

        val indicator = manager?.indicator() ?: return
        if (type != null) {
            indicator.animationType = type
        } else {
            indicator.animationType = IndicatorAnimationType.NONE
        }
        invalidate()
    }

    fun setInteractiveAnimation(isInteractive: Boolean) {
        manager?.indicator()?.isInteractiveAnimation = isInteractive
        this.isInteractionEnabled = isInteractive
    }

    fun setViewPager(pager: SliderPager?) {
        releaseViewPager()
        if (pager == null) {
            return
        }

        viewPager = pager
        pager.addOnPageChangeListener(this)
        pager.addOnAdapterChangeListener(this)

        val indicator = manager?.indicator()
        indicator?.viewPagerId = pager.id

        setDynamicCount(indicator?.isDynamicCount ?: false)
        updateState()
    }

    fun releaseViewPager() {
        viewPager?.removeOnPageChangeListener(this)
        viewPager = null
    }

    fun setRtlMode(mode: RtlMode?) {
        val indicator = manager?.indicator() ?: return
        if (mode == null) {
            indicator.rtlMode = RtlMode.Off
        } else {
            indicator.rtlMode = mode
        }

        val currentViewPager = viewPager ?: return

        val selectedPosition = indicator.selectedPosition
        var position = selectedPosition

        position = if (this.isRtl) {
            (indicator.count - 1) - selectedPosition
        } else {
            currentViewPager.currentItem
        }

        indicator.lastSelectedPosition = position
        indicator.selectingPosition = position
        indicator.selectedPosition = position
        invalidate()
    }

    var selection: Int
        get() = manager?.indicator()?.selectedPosition ?: 0
        set(position) {
            val indicator = manager?.indicator() ?: return
            val adjustedPosition = adjustPosition(position)

            if (adjustedPosition == indicator.selectedPosition || adjustedPosition == indicator.selectingPosition) {
                return
            }

            indicator.isInteractiveAnimation = false
            indicator.lastSelectedPosition = indicator.selectedPosition
            indicator.selectingPosition = adjustedPosition
            indicator.selectedPosition = adjustedPosition
            manager?.animate()?.basic()
        }

    fun setSelected(position: Int) {
        val indicator = manager?.indicator() ?: return
        val animationType = indicator.animationType
        indicator.animationType = IndicatorAnimationType.NONE

        this.selection = position
        indicator.animationType = animationType
    }

    fun clearSelection() {
        val indicator = manager?.indicator() ?: return
        indicator.isInteractiveAnimation = false
        indicator.lastSelectedPosition = Indicator.COUNT_NONE
        indicator.selectingPosition = Indicator.COUNT_NONE
        indicator.selectedPosition = Indicator.COUNT_NONE
        manager?.animate()?.basic()
    }

    fun setProgress(selectingPosition: Int, progress: Float) {
        var finalSelectingPosition = selectingPosition
        var finalProgress = progress
        val indicator = manager?.indicator() ?: return

        if (!indicator.isInteractiveAnimation) {
            return
        }

        val count = indicator.count
        if (count <= 0 || finalSelectingPosition < 0) {
            finalSelectingPosition = 0
        } else if (finalSelectingPosition > count - 1) {
            finalSelectingPosition = count - 1
        }

        if (finalProgress < 0) {
            finalProgress = 0f
        } else if (finalProgress > 1) {
            finalProgress = 1f
        }

        if (finalProgress == 1f) {
            indicator.lastSelectedPosition = indicator.selectedPosition
            indicator.selectedPosition = finalSelectingPosition
        }

        indicator.selectingPosition = finalSelectingPosition
        manager?.animate()?.interactive(finalProgress)
    }

    fun setClickListener(listener: ClickListener?) {
        manager?.drawer()?.setClickListener(listener)
    }

    private fun init(attrs: AttributeSet?) {
        setupId()
        initIndicatorManager(attrs)
    }

    private fun setupId() {
        if (id == NO_ID) id = generateViewId()
    }

    private fun initIndicatorManager(attrs: AttributeSet?) {
        manager = IndicatorManager(this)
        manager?.drawer()?.initAttributes(context, attrs)

        val indicator = manager?.indicator() ?: return
        indicator.paddingLeft = paddingLeft
        indicator.paddingTop = paddingTop
        indicator.paddingRight = paddingRight
        indicator.paddingBottom = paddingBottom
        isInteractionEnabled = indicator.isInteractiveAnimation
    }

    private fun registerSetObserver() {
        val currentViewPager = viewPager
        val adapter = currentViewPager?.adapter
        if (setObserver != null || currentViewPager == null || adapter == null) {
            return
        }

        setObserver = object : DataSetObserver() {
            override fun onChanged() {
                updateState()
            }
        }

        try {
            adapter.registerDataSetObserver(setObserver!!)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun unRegisterSetObserver() {
        val currentViewPager = viewPager
        val adapter = currentViewPager?.adapter
        if (setObserver == null || currentViewPager == null || adapter == null) {
            return
        }

        try {
            adapter.unregisterDataSetObserver(setObserver!!)
            setObserver = null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    private fun updateState() {
        val currentViewPager = viewPager
        val adapter = currentViewPager?.adapter
        if (currentViewPager == null || adapter == null) {
            return
        }

        val count: Int
        val position: Int
        if (adapter is InfinitePagerAdapter) {
            count = adapter.realCount
            position = if (count > 0) currentViewPager.currentItem % count else 0
        } else {
            count = adapter.count
            position = currentViewPager.currentItem
        }

        val selectedPos = if (this.isRtl) (count - 1) - position else position

        val indicator = manager?.indicator() ?: return
        indicator.selectedPosition = selectedPos
        indicator.selectingPosition = selectedPos
        indicator.lastSelectedPosition = selectedPos
        indicator.count = count
        manager?.animate()?.end()

        updateVisibility()
        requestLayout()
    }

    private fun updateVisibility() {
        val indicator = manager?.indicator() ?: return
        if (!indicator.isAutoVisibility) {
            return
        }

        val count = indicator.count
        val currentVisibility = visibility

        if (currentVisibility != VISIBLE && count > Indicator.Companion.MIN_COUNT) {
            visibility = VISIBLE
        } else if (currentVisibility != INVISIBLE && count <= Indicator.Companion.MIN_COUNT) {
            visibility = INVISIBLE
        }
    }

    private fun onPageSelect(position: Int) {
        var finalPosition = position
        val indicator = manager?.indicator() ?: return
        val canSelectIndicator = this.isViewMeasured
        val count = indicator.count

        if (canSelectIndicator) {
            if (this.isRtl) {
                finalPosition = (count - 1) - finalPosition
            }

            this.selection = finalPosition
        }
    }

    private fun onPageScroll(position: Int, positionOffset: Float) {
        val indicator = manager?.indicator() ?: return
        val animationType = indicator.animationType
        val interactiveAnimation = indicator.isInteractiveAnimation
        val canSelectIndicator =
            this.isViewMeasured && interactiveAnimation && animationType != IndicatorAnimationType.NONE

        if (!canSelectIndicator) {
            return
        }

        val progressPair = CoordinatesUtils.getProgress(
            indicator, position, positionOffset,
            this.isRtl
        )
        val selectingPosition: Int = progressPair.first ?: 0
        val selectingProgress: Float = progressPair.second ?: 0f
        setProgress(selectingPosition, selectingProgress)
    }

    private val isRtl: Boolean
        get() {
            return when (manager?.indicator()?.rtlMode) {
                RtlMode.On -> true
                RtlMode.Off -> false
                RtlMode.Auto -> TextUtilsCompat.getLayoutDirectionFromLocale(
                    context.resources.configuration.locale
                ) == ViewCompat.LAYOUT_DIRECTION_RTL

                else -> false
            }
        }

    private val isViewMeasured: Boolean
        get() = measuredHeight != 0 || measuredWidth != 0

    private fun findViewPager(viewParent: ViewParent?) {
        val isValidParent = viewParent != null &&
                viewParent is ViewGroup && viewParent.isNotEmpty()

        if (!isValidParent) {
            return
        }

        val viewPagerId = manager?.indicator()?.viewPagerId ?: NO_ID
        val foundViewPager = findViewPager(viewParent, viewPagerId)

        if (foundViewPager != null) {
            setViewPager(foundViewPager)
        } else {
            findViewPager(viewParent.parent)
        }
    }

    private fun findViewPager(viewGroup: ViewGroup, id: Int): SliderPager? {
        if (viewGroup.size <= 0) {
            return null
        }

        val view = viewGroup.findViewById<View?>(id)
        return if (view != null && view is SliderPager) {
            view
        } else {
            null
        }
    }

    private fun adjustPosition(position: Int): Int {
        var adjustedPosition = position
        val indicator = manager?.indicator() ?: return 0
        val count = indicator.count
        val lastPosition = count - 1

        if (adjustedPosition <= 0) {
            adjustedPosition = 0
        } else if (adjustedPosition > lastPosition) {
            adjustedPosition = lastPosition
        }

        return adjustedPosition
    }
}