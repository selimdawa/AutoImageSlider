package io.selimdawa.autoimageslider.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.os.ConfigurationCompat
import androidx.core.text.TextUtilsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.controller.DrawController
import io.selimdawa.autoimageslider.view.draw.data.Indicator
import io.selimdawa.autoimageslider.view.draw.data.IndicatorShape
import io.selimdawa.autoimageslider.view.draw.data.Orientation
import io.selimdawa.autoimageslider.view.draw.data.PositionSavedState
import io.selimdawa.autoimageslider.view.draw.data.RtlMode
import io.selimdawa.autoimageslider.view.utils.CoordinatesUtils

class PageIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IndicatorManager.Listener {

    private var manager = IndicatorManager(this)
    private var viewPager2: ViewPager2? = null
    private var isInteractionEnabled = true

    init {
        if (id == NO_ID) id = generateViewId()
        manager.drawer().initAttributes(context, attrs)
        manager.indicator().let {
            it.paddingLeft = paddingLeft
            it.paddingTop = paddingTop
            it.paddingRight = paddingRight
            it.paddingBottom = paddingBottom
            isInteractionEnabled = it.isInteractiveAnimation
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(pos: Int, off: Float, offPx: Int) {
            val ind = manager.indicator()
            if (isViewMeasured && ind.isInteractiveAnimation && ind.animationType != IndicatorAnimationType.NONE) {
                val progress = CoordinatesUtils.getProgress(ind, pos, off, isRtl)
                setProgress(progress.first, progress.second)
            }
        }

        override fun onPageSelected(pos: Int) {
            val adapter = viewPager2?.adapter
            val realPos = (adapter as? InfinitePagerAdapter<*>)?.getRealPosition(pos) ?: pos
            val sel = if (isRtl) (count - 1) - realPos else realPos
            selection = sel
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                manager.indicator().isInteractiveAnimation = isInteractionEnabled
            }
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = updateState()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) =
            updateState()

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = updateState()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = updateState()
    }

    var count: Int
        get() = manager.indicator().count
        set(value) {
            manager.indicator().let {
                if (value >= 0 && it.count != value) {
                    it.count = value; updateVisibility(); requestLayout()
                }
            }
        }

    var radius: Int
        get() = manager.indicator().radius
        set(value) {
            manager.indicator().radius = maxOf(0, value); invalidate()
        }

    var padding: Int
        get() = manager.indicator().padding
        set(value) {
            manager.indicator().padding = maxOf(0, value); invalidate()
        }

    var selectedColor: Int
        get() = manager.indicator().selectedColor
        set(value) {
            manager.indicator().selectedColor = value; invalidate()
        }

    var unselectedColor: Int
        get() = manager.indicator().unselectedColor
        set(value) {
            manager.indicator().unselectedColor = value; invalidate()
        }

    var animationDuration: Long
        get() = manager.indicator().animationDuration
        set(value) {
            manager.indicator().animationDuration = value
        }

    var indicatorShape: IndicatorShape
        get() = manager.indicator().indicatorShape ?: IndicatorShape.CIRCLE
        set(value) {
            manager.indicator().indicatorShape = value; invalidate()
        }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        releaseViewPager(); super.onDetachedFromWindow()
    }

    override fun onMeasure(w: Int, h: Int) =
        manager.drawer().measureViewSize(w, h).let { setMeasuredDimension(it.first, it.second) }

    override fun onDraw(canvas: Canvas) = manager.drawer().draw(canvas)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?) = true.also { manager.drawer().touch(ev) }
    override fun onIndicatorUpdated() = invalidate()

    var isInteractiveAnimation: Boolean
        get() = manager.indicator().isInteractiveAnimation
        set(value) {
            manager.indicator().isInteractiveAnimation = value
        }

    fun setDynamicCount(dynamic: Boolean) {
        manager.indicator().isDynamicCount = dynamic
    }

    fun setOrientation(o: Orientation?) {
        o?.let { manager.indicator().orientation = it; requestLayout() }
    }

    fun setAnimationType(t: IndicatorAnimationType?) {
        manager.onValueUpdated(null); manager.indicator()
            .let { it.animationType = t ?: IndicatorAnimationType.NONE; invalidate() }
    }

    private var registeredAdapter: RecyclerView.Adapter<*>? = null

    fun setViewPager(pager: ViewPager2?) {
        releaseViewPager()
        viewPager2 = pager?.apply {
            registerOnPageChangeCallback(onPageChangeCallback)
            registeredAdapter = adapter?.also {
                try {
                    it.registerAdapterDataObserver(adapterDataObserver)
                } catch (_: Exception) {
                }
            }
        }
        updateState()
    }

    fun releaseViewPager() {
        try {
            viewPager2?.unregisterOnPageChangeCallback(onPageChangeCallback)
            registeredAdapter?.unregisterAdapterDataObserver(adapterDataObserver)
        } catch (_: Exception) {
        }
        registeredAdapter = null
        viewPager2 = null
    }

    fun setRtlMode(mode: RtlMode?) {
        manager.indicator().rtlMode = mode ?: RtlMode.Off
        updateState()
    }

    var selection: Int
        get() = manager.indicator().selectedPosition
        set(value) {
            val ind = manager.indicator()
            val adj = value.coerceIn(0, maxOf(0, ind.count - 1))
            if (adj != ind.selectedPosition) {
                ind.isInteractiveAnimation = false; ind.lastSelectedPosition = ind.selectedPosition
                ind.selectingPosition = adj; ind.selectedPosition = adj; manager.animate().basic()
            }
        }

    fun setProgress(pos: Int, progress: Float) {
        val ind = manager.indicator()
        if (!ind.isInteractiveAnimation) return
        val finalPos = pos.coerceIn(0, maxOf(0, ind.count - 1))
        val finalProg = progress.coerceIn(0f, 1f)

        if (finalProg == 1f) {
            ind.lastSelectedPosition = ind.selectedPosition
            ind.selectedPosition = finalPos
        }
        ind.selectingPosition = finalPos
        manager.animate().interactive(finalProg)
    }

    fun setClickListener(l: DrawController.ClickListener?) = manager.drawer().setClickListener(l)

    private fun updateState() {
        val pager = viewPager2 ?: return
        val adapter = pager.adapter ?: return
        val c = if (adapter is InfinitePagerAdapter<*>) adapter.realCount else adapter.itemCount
        val pos = pager.currentItem
        val realPos = (adapter as? InfinitePagerAdapter<*>)?.getRealPosition(pos) ?: pos
        val sel = if (c > 0) {
            val s = if (isRtl) (c - 1) - realPos else realPos
            s.coerceIn(0, c - 1)
        } else 0

        manager.indicator().let {
            it.selectedPosition = sel; it.selectingPosition = sel; it.lastSelectedPosition =
            sel; it.count = c; manager.animate().end(); updateVisibility(); requestLayout()
        }
    }

    private fun updateVisibility() {
        manager.indicator().let {
            if (it.isAutoVisibility) visibility =
                if (it.count > Indicator.MIN_COUNT) VISIBLE else INVISIBLE
        }
    }

    private val isRtl
        get() = manager.indicator().rtlMode == RtlMode.On || (manager.indicator().rtlMode == RtlMode.Auto && TextUtilsCompat.getLayoutDirectionFromLocale(
            ConfigurationCompat.getLocales(context.resources.configuration)[0]
        ) == LAYOUT_DIRECTION_RTL)
    private val isViewMeasured get() = measuredHeight != 0 || measuredWidth != 0

    override fun onSaveInstanceState() = PositionSavedState(super.onSaveInstanceState()).apply {
        manager.indicator().let {
            selectedPosition = it.selectedPosition; selectingPosition =
            it.selectingPosition; lastSelectedPosition = it.lastSelectedPosition
        }
    }

    override fun onRestoreInstanceState(s: Parcelable?) {
        if (s is PositionSavedState) {
            manager.indicator().let {
                it.selectedPosition = s.selectedPosition; it.selectingPosition =
                s.selectingPosition; it.lastSelectedPosition = s.lastSelectedPosition
            }; super.onRestoreInstanceState(s.superState)
        } else super.onRestoreInstanceState(s)
    }
}