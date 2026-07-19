package io.selimdawa.autoimageslider.view

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.os.Parcelable
import android.util.AttributeSet
import android.view.*
import androidx.core.os.ConfigurationCompat
import androidx.core.text.TextUtilsCompat
import androidx.core.view.isEmpty
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import io.selimdawa.autoimageslider.SliderPager
import io.selimdawa.autoimageslider.adapter.InfinitePagerAdapter
import io.selimdawa.autoimageslider.view.animation.type.IndicatorAnimationType
import io.selimdawa.autoimageslider.view.draw.controller.DrawController
import io.selimdawa.autoimageslider.view.draw.data.*
import io.selimdawa.autoimageslider.view.utils.*

@Suppress("unused")
class PageIndicatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defRes: Int = 0
) : View(context, attrs, defStyleAttr, defRes), SliderPager.OnPageChangeListener, 
    IndicatorManager.Listener, SliderPager.OnAdapterChangeListener {

    private var manager = IndicatorManager(this)
    private var setObserver: DataSetObserver? = null
    private var viewPager: SliderPager? = null
    private var isInteractionEnabled = false

    var count: Int
        get() = manager.indicator().count
        set(value) { manager.indicator().let { if (value >= 0 && it.count != value) { it.count = value; updateVisibility(); requestLayout() } } }

    fun setRadius(radiusPx: Float) { manager.indicator().radius = radiusPx.toInt(); invalidate() }

    var radius: Int
        get() = manager.indicator().radius
        set(value) { manager.indicator().radius = DensityUtils.dpToPx(maxOf(0, value)); invalidate() }

    fun setPadding(paddingPx: Float) { manager.indicator().padding = paddingPx.toInt(); invalidate() }

    var padding: Int
        get() = manager.indicator().padding
        set(value) { manager.indicator().padding = DensityUtils.dpToPx(maxOf(0, value)); invalidate() }

    var scaleFactor: Float
        get() = manager.indicator().scaleFactor
        set(value) { manager.indicator().scaleFactor = value.coerceIn(0.3f, 1.0f) }

    fun setStrokeWidth(strokePx: Float) { manager.indicator().stroke = strokePx.toInt(); invalidate() }
    fun setStrokeWidth(strokeDp: Int) { manager.indicator().stroke = DensityUtils.dpToPx(strokeDp); invalidate() }
    val strokeWidth get() = manager.indicator().stroke

    var selectedColor: Int
        get() = manager.indicator().selectedColor
        set(value) { manager.indicator().selectedColor = value; invalidate() }

    var unselectedColor: Int
        get() = manager.indicator().unselectedColor
        set(value) { manager.indicator().unselectedColor = value; invalidate() }

    var animationDuration: Long
        get() = manager.indicator().animationDuration
        set(value) { manager.indicator().animationDuration = value }

    init {
        if (id == NO_ID) id = generateViewId()
        manager.drawer().initAttributes(context, attrs)
        manager.indicator().let {
            it.paddingLeft = paddingLeft; it.paddingTop = paddingTop
            it.paddingRight = paddingRight; it.paddingBottom = paddingBottom
            isInteractionEnabled = it.isInteractiveAnimation
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); findViewPager(parent) }
    override fun onDetachedFromWindow() { unRegisterSetObserver(); super.onDetachedFromWindow() }
    override fun onMeasure(w: Int, h: Int) = manager.drawer().measureViewSize(w, h).let { setMeasuredDimension(it.first, it.second) }
    override fun onDraw(canvas: Canvas) = manager.drawer().draw(canvas)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent?) = true.also { manager.drawer().touch(ev) }
    override fun onIndicatorUpdated() = invalidate()
    override fun onPageScrolled(pos: Int, off: Float, offPx: Int) {
        val ind = manager.indicator()
        val adapter = viewPager?.adapter
        val realPos = if (adapter is InfinitePagerAdapter) adapter.getRealPosition(pos) else pos
        if (isViewMeasured && ind.isInteractiveAnimation && ind.animationType != IndicatorAnimationType.NONE) {
            val progress = CoordinatesUtils.getProgress(ind, realPos, off, isRtl)
            setProgress(progress.first ?: 0, progress.second ?: 0f)
        }
    }

    override fun onPageSelected(pos: Int) {
        val realPos = viewPager?.currentItem ?: pos
        if (isViewMeasured) selection = if (isRtl) (count - 1) - realPos else realPos
    }
    override fun onPageScrollStateChanged(state: Int) { if (state == ViewPager.SCROLL_STATE_IDLE) manager.indicator().isInteractiveAnimation = isInteractionEnabled }
    override fun onAdapterChanged(p: ViewPager, old: PagerAdapter?, new: PagerAdapter?) = updateState()

    fun setDynamicCount(dynamic: Boolean) { manager.indicator().isDynamicCount = dynamic; if (dynamic) registerSetObserver() else unRegisterSetObserver() }
    fun setOrientation(o: Orientation?) { o?.let { manager.indicator().orientation = it; requestLayout() } }
    fun setAnimationType(t: IndicatorAnimationType?) { manager.onValueUpdated(null); manager.indicator().let { it.animationType = t ?: IndicatorAnimationType.NONE; invalidate() } }
    fun setInteractiveAnimation(isInteractive: Boolean) { manager.indicator().isInteractiveAnimation = isInteractive; isInteractionEnabled = isInteractive }
    fun setAutoVisibility(autoVisibility: Boolean) { if (!autoVisibility) visibility = VISIBLE; manager.indicator().isAutoVisibility = autoVisibility; updateVisibility() }

    fun setViewPager(pager: SliderPager?) {
        releaseViewPager()
        viewPager = pager?.apply { addOnPageChangeListener(this@PageIndicatorView); addOnAdapterChangeListener(this@PageIndicatorView) }
        manager.indicator().let { it.viewPagerId = pager?.id ?: NO_ID; if (pager != null) { setDynamicCount(it.isDynamicCount); updateState() } }
    }

    fun releaseViewPager() { viewPager?.removeOnPageChangeListener(this); viewPager = null }
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

    fun setSelected(position: Int) { val anim = manager.indicator().animationType; manager.indicator().animationType = IndicatorAnimationType.NONE; selection = position; manager.indicator().animationType = anim }
    fun clearSelection() { manager.indicator().let { it.isInteractiveAnimation = false; it.lastSelectedPosition = -1; it.selectingPosition = -1; it.selectedPosition = -1; manager.animate().basic() } }

    fun setProgress(pos: Int, progress: Float) {
        val ind = manager.indicator()
        if (!ind.isInteractiveAnimation) return
        val finalPos = pos.coerceIn(0, maxOf(0, ind.count - 1))
        val finalProg = progress.coerceIn(0f, 1f)
        if (finalProg == 1f) { ind.lastSelectedPosition = ind.selectedPosition; ind.selectedPosition = finalPos }
        ind.selectingPosition = finalPos; manager.animate().interactive(finalProg)
    }

    fun setClickListener(l: DrawController.ClickListener?) = manager.drawer().setClickListener(l)

    private fun registerSetObserver() {
        val adapter = viewPager?.adapter ?: return
        if (setObserver == null) {
            setObserver = object : DataSetObserver() { override fun onChanged() = updateState() }
            runCatching { adapter.registerDataSetObserver(setObserver!!) }
        }
    }

    private fun unRegisterSetObserver() { viewPager?.adapter?.let { runCatching { it.unregisterDataSetObserver(setObserver!!); setObserver = null } } }

    private fun updateState() {
        val adapter = viewPager?.adapter ?: return
        val isInf = adapter is InfinitePagerAdapter
        val c = if (isInf) adapter.realCount else adapter.count
        val pos = viewPager!!.currentItem
        val sel = if (isRtl) (c - 1) - pos else pos
        manager.indicator().let { it.selectedPosition = sel; it.selectingPosition = sel; it.lastSelectedPosition = sel; it.count = c; manager.animate().end(); updateVisibility(); requestLayout() }
    }

    private fun updateVisibility() { manager.indicator().let { if (it.isAutoVisibility) visibility = if (it.count > Indicator.MIN_COUNT) VISIBLE else INVISIBLE } }
    private val isRtl get() = manager.indicator().rtlMode == RtlMode.On || (manager.indicator().rtlMode == RtlMode.Auto && TextUtilsCompat.getLayoutDirectionFromLocale(ConfigurationCompat.getLocales(context.resources.configuration)[0]) == LAYOUT_DIRECTION_RTL)
    private val isViewMeasured get() = measuredHeight != 0 || measuredWidth != 0
    private fun findViewPager(p: ViewParent?) {
        if (p !is ViewGroup || p.isEmpty()) return
        val found = p.findViewById<View>(manager.indicator().viewPagerId) as? SliderPager
        if (found != null) setViewPager(found) else findViewPager(p.parent)
    }

    override fun onSaveInstanceState() = PositionSavedState(super.onSaveInstanceState()).apply { manager.indicator().let { selectedPosition = it.selectedPosition; selectingPosition = it.selectingPosition; lastSelectedPosition = it.lastSelectedPosition } }
    override fun onRestoreInstanceState(s: Parcelable?) {
        if (s is PositionSavedState) { manager.indicator().let { it.selectedPosition = s.selectedPosition; it.selectingPosition = s.selectingPosition; it.lastSelectedPosition = s.lastSelectedPosition }; super.onRestoreInstanceState(s.superState) }
        else super.onRestoreInstanceState(s)
    }
}