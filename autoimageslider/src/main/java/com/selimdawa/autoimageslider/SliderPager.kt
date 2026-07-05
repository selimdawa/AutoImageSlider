package com.selimdawa.autoimageslider

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.os.SystemClock
import android.util.AttributeSet
import android.util.Log
import android.view.FocusFinder
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.animation.Interpolator
import android.widget.EdgeEffect
import android.widget.Scroller
import androidx.annotation.CallSuper
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.withSave
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.customview.view.AbsSavedState
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.Adapter.InfinitePagerAdapter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Suppress("unused")
open class SliderPager : ViewGroup {
    private var mExpectedAdapterCount = 0

    class ItemInfo {
        var `object`: Any? = null
        var position = 0
        var scrolling = false
        var widthFactor = 0f
        var offset = 0f
    }

    private val mItems = ArrayList<ItemInfo>()
    private val mTempItem = ItemInfo()
    private val mTempRect = Rect()
    var mAdapter: PagerAdapter? = null
    var mCurItem = 0
    private var mRestoredCurItem = -1
    private var mRestoredAdapterState: Parcelable? = null
    private var mRestoredClassLoader: ClassLoader? = null
    private var mScroller: Scroller? = null
    private var mIsScrollStarted = false
    private var mObserver: PagerObserver? = null
    private var mPageMargin = 0
    private var mMarginDrawable: Drawable? = null
    private var mTopPageBounds = 0
    private var mBottomPageBounds = 0
    private var mFirstOffset = -Float.MAX_VALUE
    private var mLastOffset = Float.MAX_VALUE
    private var mChildWidthMeasureSpec = 0
    private var mChildHeightMeasureSpec = 0
    private var mInLayout = false
    private var mScrollingCacheEnabled = false
    private var mPopulatePending = false
    private var mOffscreenPageLimit = DEFAULT_OFFSCREEN_PAGES
    private var mIsBeingDragged = false
    private var mIsUnableToDrag = false
    private var mDefaultGutterSize = 0
    private var mGutterSize = 0
    private var mTouchSlop = 0
    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f
    private var mActivePointerId = INVALID_POINTER
    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mFlingDistance = 0
    private var mCloseEnough = 0
    var isFakeDragging = false; private set
    private var mFakeDragBeginTime = 0L
    private var mLeftEdge: EdgeEffect? = null
    private var mRightEdge: EdgeEffect? = null
    private var mFirstLayout = true
    private var mNeedCalculatePageOffsets = false
    private var mCalledSuper = false
    private var mDecorChildCount = 0
    private var mOnPageChangeListeners: MutableList<OnPageChangeListener?>? = null
    private var mOnPageChangeListener: OnPageChangeListener? = null
    private var mInternalPageChangeListener: OnPageChangeListener? = null
    private var mAdapterChangeListeners: MutableList<OnAdapterChangeListener?>? = null
    private var mPageTransformer: PageTransformer? = null
    private var mPageTransformerLayerType = 0
    private var mDrawingOrder = 0
    private var mDrawingOrderedChildren: ArrayList<View?>? = null
    private val mEndScrollRunnable = Runnable { setScrollState(SCROLL_STATE_IDLE); populate() }
    private var mScrollState = SCROLL_STATE_IDLE

    interface OnPageChangeListener {
        fun onPageScrolled(position: Int, positionOffset: Float, @Px positionOffsetPixels: Int)
        fun onPageSelected(position: Int)
        fun onPageScrollStateChanged(state: Int)
    }

    class SimpleOnPageChangeListener : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int, positionOffset: Float, positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {}
        override fun onPageScrollStateChanged(state: Int) {}
    }

    interface PageTransformer {
        fun transformPage(page: View, position: Float)
    }

    interface OnAdapterChangeListener {
        fun onAdapterChanged(
            viewPager: SliderPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?
        )
    }

    class DecorView(context: Context) : android.widget.FrameLayout(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initSliderPager()
    }

    fun initSliderPager() {
        setWillNotDraw(false)
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        mScroller = OwnScroller(context, DEFAULT_SCROLL_DURATION, sInterpolator)
        val configuration = ViewConfiguration.get(context)
        val density = context.resources.displayMetrics.density
        mTouchSlop = configuration.scaledPagingTouchSlop
        mMinimumVelocity = (MIN_FLING_VELOCITY * density).toInt()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mLeftEdge = EdgeEffect(context); mRightEdge = EdgeEffect(context)
        mFlingDistance = (MIN_DISTANCE_FOR_FLING * density).toInt()
        mCloseEnough = (CLOSE_ENOUGH * density).toInt()
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()
        ViewCompat.setAccessibilityDelegate(this, MyAccessibilityDelegate())
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        ViewCompat.setOnApplyWindowInsetsListener(this) { v, originalInsets ->
            val applied = ViewCompat.onApplyWindowInsets(v, originalInsets)
            if (applied.isConsumed) applied else {
                val insets =
                    applied.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                val res = Rect(insets.left, insets.top, insets.right, insets.bottom)
                for (i in 0..<childCount) {
                    getChildAt(i)?.let {
                        val childInsets = ViewCompat.dispatchApplyWindowInsets(it, applied)
                            .getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                        res.left = min(childInsets.left, res.left); res.top =
                        min(childInsets.top, res.top)
                        res.right = min(childInsets.right, res.right); res.bottom =
                        min(childInsets.bottom, res.bottom)
                    }
                }
                androidx.core.view.WindowInsetsCompat.Builder(applied).setInsets(
                    androidx.core.view.WindowInsetsCompat.Type.systemBars(),
                    androidx.core.graphics.Insets.of(res.left, res.top, res.right, res.bottom)
                ).build()
            }
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(mEndScrollRunnable)
        mScroller?.takeIf { !it.isFinished }?.abortAnimation()
        super.onDetachedFromWindow()
    }

    fun setScrollState(newState: Int) {
        if (mScrollState == newState) return
        mScrollState = newState
        if (mPageTransformer != null) enableLayers(newState != SCROLL_STATE_IDLE)
        dispatchOnScrollStateChanged(newState)
    }

    private fun setAdapterViewPagerObserver(observer: PagerObserver?) = runCatching {
        PagerAdapter::class.java.getDeclaredMethod(
            "setViewPagerObserver", DataSetObserver::class.java
        ).apply {
            isAccessible = true; invoke(mAdapter, observer)
        }
    }.getOrNull()

    private fun removeNonDecorViews() {
        var i = 0
        while (i < childCount) {
            val child = getChildAt(i)
            if (child != null && !(child.layoutParams as LayoutParams).isDecor) {
                removeViewAt(i); i--
            }
            i++
        }
    }

    var adapter: PagerAdapter?
        get() = mAdapter
        set(value) {
            val oldAdapter = mAdapter; mAdapter = value; mExpectedAdapterCount = 0
            if (oldAdapter != null) {
                setAdapterViewPagerObserver(null); oldAdapter.startUpdate(this)
                for (ii in mItems) ii.`object`?.let {
                    oldAdapter.destroyItem(
                        this, ii.position, it
                    )
                }
                oldAdapter.finishUpdate(this); mItems.clear(); removeNonDecorViews(); mCurItem =
                    0; scrollTo(0, 0)
            }
            if (value != null) {
                val obs = mObserver ?: PagerObserver().also { mObserver = it }
                setAdapterViewPagerObserver(obs); runCatching { value.registerDataSetObserver(obs) }
                mPopulatePending = false
                val wasFirst = mFirstLayout; mFirstLayout = true
                mExpectedAdapterCount = value.count
                if (mRestoredCurItem >= 0) {
                    value.restoreState(mRestoredAdapterState, mRestoredClassLoader)
                    setCurrentItemInternal(mRestoredCurItem, smoothScroll = false, always = true)
                    mRestoredCurItem = -1; mRestoredAdapterState = null; mRestoredClassLoader = null
                } else if (!wasFirst) populate() else requestLayout()
            }
            mAdapterChangeListeners?.forEach { it?.onAdapterChanged(this, oldAdapter, value) }
        }

    fun addOnAdapterChangeListener(listener: OnAdapterChangeListener) {
        (mAdapterChangeListeners
            ?: ArrayList<OnAdapterChangeListener?>().also { mAdapterChangeListeners = it }).add(
            listener
        )
    }

    fun removeOnAdapterChangeListener(listener: OnAdapterChangeListener) {
        mAdapterChangeListeners?.remove(listener)
    }

    private val clientWidth get() = measuredWidth - paddingLeft - paddingRight

    fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        var targetItem = item
        if (mAdapter is InfinitePagerAdapter) targetItem =
            (mAdapter as InfinitePagerAdapter).getMiddlePosition(targetItem)
        mPopulatePending = false; setCurrentItemInternal(targetItem, smoothScroll, false)
    }

    var currentItem: Int
        get() = if (mAdapter is InfinitePagerAdapter && (mAdapter as InfinitePagerAdapter).realCount > 0) (mAdapter as InfinitePagerAdapter).getRealPosition(
            mCurItem
        ) else mCurItem
        set(item) {
            mPopulatePending = false; setCurrentItem(item, !mFirstLayout)
        }

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean) =
        setCurrentItemInternal(item, smoothScroll, always, 0)

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean, velocity: Int) {
        val adapter = mAdapter ?: run { setScrollingCacheEnabled(false); return }
        if (adapter.count <= 0 || (!always && mCurItem == item && mItems.isNotEmpty())) {
            setScrollingCacheEnabled(false); return
        }
        val targetItem = item.coerceIn(0, adapter.count - 1)
        if (targetItem > (mCurItem + mOffscreenPageLimit) || targetItem < (mCurItem - mOffscreenPageLimit)) mItems.forEach {
            it.scrolling = true
        }
        val dispatchSelected = mCurItem != targetItem
        if (mFirstLayout) {
            mCurItem = targetItem; triggerOnPageChangeEvent(targetItem); requestLayout()
        } else {
            populate(targetItem); scrollToItem(targetItem, smoothScroll, velocity, dispatchSelected)
        }
    }

    private fun scrollToItem(
        item: Int, smoothScroll: Boolean, velocity: Int, dispatchSelected: Boolean
    ) {
        var destX = 0
        infoForPosition(item)?.let {
            destX = (clientWidth * max(mFirstOffset, min(it.offset, mLastOffset))).toInt()
        }
        if (dispatchSelected) triggerOnPageChangeEvent(item)
        if (smoothScroll) smoothScrollTo(destX, 0, velocity) else {
            completeScroll(false); scrollTo(destX, 0); pageScrolled(destX)
        }
    }

    @Deprecated("Use addOnPageChangeListener and removeOnPageChangeListener instead.")
    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    fun addOnPageChangeListener(listener: OnPageChangeListener) {
        (mOnPageChangeListeners ?: ArrayList<OnPageChangeListener?>().also {
            mOnPageChangeListeners = it
        }).add(listener)
    }

    fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        mOnPageChangeListeners?.remove(listener)
    }

    fun clearOnPageChangeListeners() {
        mOnPageChangeListeners?.clear()
    }

    fun setPageTransformer(reverseDrawingOrder: Boolean, transformer: PageTransformer?) =
        setPageTransformer(reverseDrawingOrder, transformer, LAYER_TYPE_HARDWARE)

    fun setPageTransformer(
        reverseDrawingOrder: Boolean, transformer: PageTransformer?, pageLayerType: Int
    ) {
        val hasTransformer = transformer != null
        val needsPopulate = hasTransformer != (mPageTransformer != null)
        mPageTransformer = transformer; isChildrenDrawingOrderEnabled = hasTransformer
        mDrawingOrder =
            if (hasTransformer) (if (reverseDrawingOrder) DRAW_ORDER_REVERSE else DRAW_ORDER_FORWARD) else DRAW_ORDER_DEFAULT
        if (hasTransformer) mPageTransformerLayerType = pageLayerType
        if (needsPopulate) populate()
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        val index = if (mDrawingOrder == DRAW_ORDER_REVERSE) childCount - 1 - i else i
        if (mDrawingOrderedChildren?.size != childCount) sortChildDrawingOrder()
        return (mDrawingOrderedChildren?.get(index)?.layoutParams as? LayoutParams)?.childIndex
            ?: index
    }

    fun setInternalPageChangeListener(listener: OnPageChangeListener?): OnPageChangeListener? {
        val old = mInternalPageChangeListener; mInternalPageChangeListener = listener; return old
    }

    var offscreenPageLimit: Int
        get() = mOffscreenPageLimit
        set(value) {
            val finalLimit = if (value < DEFAULT_OFFSCREEN_PAGES) Log.w(
                TAG,
                "Requested offscreen page limit $value too small; defaulting to $DEFAULT_OFFSCREEN_PAGES"
            ).run { DEFAULT_OFFSCREEN_PAGES } else value
            if (finalLimit != mOffscreenPageLimit) {
                mOffscreenPageLimit = finalLimit; populate()
            }
        }

    var pageMargin: Int
        get() = mPageMargin
        set(value) {
            val old = mPageMargin; mPageMargin = value; recomputeScrollPosition(
                width, width, value, old
            ); requestLayout()
        }

    fun setPageMarginDrawable(d: Drawable?) {
        mMarginDrawable =
            d; if (d != null) refreshDrawableState(); setWillNotDraw(d == null); invalidate()
    }

    fun setPageMarginDrawable(@DrawableRes resId: Int) =
        setPageMarginDrawable(ContextCompat.getDrawable(context, resId))

    override fun verifyDrawable(who: Drawable) =
        super.verifyDrawable(who) || who === mMarginDrawable

    fun setScrollDuration(millis: Int, interpolator: Interpolator?) {
        mScroller =
            if (interpolator != null) OwnScroller(context, millis, interpolator) else OwnScroller(
                context, millis
            )
    }

    fun setScrollDuration(millis: Int) = setScrollDuration(millis, null)

    override fun drawableStateChanged() {
        super.drawableStateChanged(); mMarginDrawable?.takeIf { it.isStateful }
            ?.apply { state = drawableState }
    }

    fun distanceInfluenceForSnapDuration(f: Float) =
        kotlin.math.sin(((f - 0.5f) * (0.3f * kotlin.math.PI.toFloat() / 2f)).toDouble()).toFloat()

    fun smoothScrollTo(x: Int, y: Int) {
        smoothScrollTo(x, y, 0)
    }

    fun smoothScrollTo(x: Int, y: Int, velocity: Int) {
        if (isEmpty()) {
            setScrollingCacheEnabled(false)
            return
        }
        val sx = mScroller?.let {
            if (!it.isFinished) {
                (if (mIsScrollStarted) it.currX else it.startX).also { _ ->
                    it.abortAnimation()
                    setScrollingCacheEnabled(false)
                }
            } else null
        } ?: scrollX
        val dx = x - sx
        val dy = y - scrollY
        if (dx == 0 && dy == 0) {
            completeScroll(false)
            populate()
            setScrollState(SCROLL_STATE_IDLE)
            return
        }
        setScrollingCacheEnabled(true)
        setScrollState(SCROLL_STATE_SETTLING)
        val w = clientWidth
        val distance =
            (w / 2) + (w / 2) * distanceInfluenceForSnapDuration(minOf(1f, 1f * abs(dx) / w))
        val vAbs = abs(velocity)
        val duration = minOf(
            if (vAbs > 0) {
                4 * round(1000 * abs(distance / vAbs)).toInt()
            } else {
                (((abs(dx).toFloat() / ((mAdapter?.getPageWidth(mCurItem)
                    ?: 0f) * w + mPageMargin)) + 1) * 100).toInt()
            }, MAX_SETTLE_DURATION
        )
        mIsScrollStarted = false
        mScroller?.startScroll(sx, scrollY, dx, dy, duration)
        postInvalidateOnAnimation()
    }

    fun addNewItem(position: Int, index: Int) = ItemInfo().apply {
        this.position = position; `object` =
        mAdapter?.instantiateItem(this@SliderPager, position) ?: Any(); widthFactor =
        mAdapter?.getPageWidth(position) ?: 1f
        if (index !in mItems.indices) mItems.add(this) else mItems.add(index, this)
    }

    fun dataSetChanged() {
        val adapter = mAdapter ?: return
        val adapterCount = adapter.count; mExpectedAdapterCount = adapterCount
        var needPopulate = mItems.size < mOffscreenPageLimit * 2 + 1 && mItems.size < adapterCount
        var newCurrItem = mCurItem
        var isUpdating = false
        var i = 0
        while (i < mItems.size) {
            val ii = mItems[i]
            val obj = ii.`object` ?: run { i++; return@run }
            when (val newPos = adapter.getItemPosition(obj)) {
                PagerAdapter.POSITION_UNCHANGED -> i++
                PagerAdapter.POSITION_NONE -> {
                    mItems.removeAt(i); i--; if (!isUpdating) {
                        adapter.startUpdate(this); isUpdating = true
                    }; adapter.destroyItem(this, ii.position, obj); needPopulate = true
                    if (mCurItem == ii.position) {
                        newCurrItem = max(0, min(mCurItem, adapterCount - 1)); needPopulate = true
                    }; i++
                }

                else -> {
                    if (ii.position != newPos) {
                        if (ii.position == mCurItem) newCurrItem = newPos; ii.position =
                            newPos; needPopulate = true
                    }; i++
                }
            }
        }
        if (isUpdating) adapter.finishUpdate(this)
        mItems.sortWith(COMPARATOR)
        if (needPopulate) {
            for (j in 0 until childCount) {
                (getChildAt(j)?.layoutParams as? LayoutParams)?.let {
                    if (!it.isDecor) it.widthFactor = 0f
                }
            }
            setCurrentItemInternal(
                newCurrItem, smoothScroll = false, always = true
            ); requestLayout()
        }
    }

    fun populate(newCurrentItem: Int = mCurItem) {
        val oldCurInfo = if (mCurItem != newCurrentItem) infoForPosition(mCurItem).also {
            mCurItem = newCurrentItem
        } else null
        val adapter = mAdapter ?: run { sortChildDrawingOrder(); return }
        if (mPopulatePending) {
            sortChildDrawingOrder(); return
        }
        if (windowToken == null) return
        adapter.startUpdate(this)

        val pageLimit = mOffscreenPageLimit
        val startPos = max(0, mCurItem - pageLimit)
        val n = adapter.count
        val endPos = min(n - 1, mCurItem + pageLimit)
        if (n != mExpectedAdapterCount) {
            val resName =
                runCatching { resources.getResourceName(id) }.getOrElse { Integer.toHexString(id) }
            throw IllegalStateException("The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: $mExpectedAdapterCount, found: $n Pager id: $resName Pager class: $javaClass Problematic adapter: ${adapter.javaClass}")
        }
        var curIndex = mItems.indexOfFirst { it.position >= mCurItem }.coerceAtLeast(0)
        val curItem =
            mItems.getOrNull(curIndex)?.takeIf { it.position == mCurItem } ?: if (n > 0) addNewItem(
                mCurItem, curIndex
            ) else null
        if (curItem != null) {
            var extraWidthLeft = 0f
            var itemIndex = curIndex - 1
            var ii = mItems.getOrNull(itemIndex)
            val cWidth = clientWidth
            val leftWidthNeeded =
                if (cWidth <= 0) 0f else 2f - curItem.widthFactor + paddingLeft.toFloat() / cWidth.toFloat()
            for (pos in mCurItem - 1 downTo 0) {
                if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                    val obj = ii?.`object` ?: break
                    if (pos == ii.position && !ii.scrolling) {
                        mItems.removeAt(itemIndex); adapter.destroyItem(
                            this, pos, obj
                        ); itemIndex--; curIndex--; ii = mItems.getOrNull(itemIndex)
                    }
                } else if (ii != null && pos == ii.position) {
                    extraWidthLeft += ii.widthFactor; itemIndex--; ii = mItems.getOrNull(itemIndex)
                } else {
                    ii = addNewItem(
                        pos, itemIndex + 1
                    ); extraWidthLeft += ii.widthFactor; curIndex++; ii =
                        mItems.getOrNull(itemIndex)
                }
            }
            var extraWidthRight = curItem.widthFactor; itemIndex = curIndex + 1
            if (extraWidthRight < 2f) {
                ii = mItems.getOrNull(itemIndex)
                val rightWidthNeeded =
                    if (cWidth <= 0) 0f else paddingRight.toFloat() / cWidth.toFloat() + 2f
                for (pos in mCurItem + 1 until n) {
                    if (extraWidthRight >= rightWidthNeeded && pos > endPos) {
                        val obj = ii?.`object` ?: break
                        if (pos == ii.position && !ii.scrolling) {
                            mItems.removeAt(itemIndex); adapter.destroyItem(this, pos, obj); ii =
                                mItems.getOrNull(itemIndex)
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraWidthRight += ii.widthFactor; itemIndex++; ii =
                            mItems.getOrNull(itemIndex)
                    } else {
                        ii = addNewItem(
                            pos, itemIndex
                        ); itemIndex++; extraWidthRight += ii.widthFactor; ii =
                            mItems.getOrNull(itemIndex)
                    }
                }
            }
            calculatePageOffsets(curItem, curIndex, oldCurInfo)
            curItem.`object`?.let { adapter.setPrimaryItem(this, mCurItem, it) }
        }
        adapter.finishUpdate(this)
        for (i in 0 until childCount) {
            getChildAt(i)?.let { child ->
                val lp = child.layoutParams as LayoutParams; lp.childIndex = i
                if (!lp.isDecor && lp.widthFactor == 0f) {
                    infoForChild(child)?.let {
                        lp.widthFactor = it.widthFactor; lp.position = it.position
                    }
                }
            }
        }
        sortChildDrawingOrder()
        if (hasFocus()) {
            val focused = findFocus()
            val iiAny = focused?.let { infoForAnyChild(it) }
            if (iiAny == null || iiAny.position != mCurItem) {
                for (i in 0 until childCount) {
                    getChildAt(i)?.let { child ->
                        infoForChild(child)?.let {
                            if (it.position == mCurItem && child.requestFocus(
                                    FOCUS_FORWARD
                                )
                            ) return@let
                        }
                    }
                }
            }
        }
    }

    private fun sortChildDrawingOrder() {
        if (mDrawingOrder != DRAW_ORDER_DEFAULT) {
            val list =
                mDrawingOrderedChildren ?: ArrayList<View?>().also { mDrawingOrderedChildren = it }
            list.clear(); for (i in 0 until childCount) getChildAt(i)?.let { list.add(it) }; list.sortWith(
                sPositionComparator
            )
        }
    }

    private fun triggerOnPageChangeEvent(position: Int) {
        mOnPageChangeListeners?.forEach { listener ->
            listener?.onPageSelected(
                if (mAdapter is InfinitePagerAdapter) (mAdapter as InfinitePagerAdapter).getRealPosition(
                    position
                ) else position
            )
        }
        mInternalPageChangeListener?.onPageSelected(position)
    }

    private fun calculatePageOffsets(curItem: ItemInfo, curIndex: Int, oldCurInfo: ItemInfo?) {
        val adapter = mAdapter ?: return
        val n = adapter.count
        val w = clientWidth
        val mOffset = if (w > 0) mPageMargin.toFloat() / w else 0f
        if (oldCurInfo != null) {
            val oldPos = oldCurInfo.position
            if (oldPos < curItem.position) {
                var idx = 0
                var off = oldCurInfo.offset + oldCurInfo.widthFactor + mOffset
                var pos = oldPos + 1
                while (pos <= curItem.position && idx < mItems.size) {
                    var ii = mItems[idx]; while (pos > ii.position && idx < mItems.size - 1) ii =
                        mItems[++idx]
                    while (pos < ii.position) {
                        off += adapter.getPageWidth(pos) + mOffset; pos++
                    }
                    ii.offset = off; off += ii.widthFactor + mOffset; pos++
                }
            } else if (oldPos > curItem.position) {
                var idx = mItems.size - 1
                var off = oldCurInfo.offset
                var pos = oldPos - 1
                while (pos >= curItem.position && idx >= 0) {
                    var ii = mItems[idx]; while (pos < ii.position && idx > 0) ii = mItems[--idx]
                    while (pos > ii.position) {
                        off -= adapter.getPageWidth(pos) + mOffset; pos--
                    }
                    off -= ii.widthFactor + mOffset; ii.offset = off; pos--
                }
            }
        }
        var off = curItem.offset
        var pLeft = curItem.position - 1
        mFirstOffset = if (curItem.position == 0) curItem.offset else -Float.MAX_VALUE
        mLastOffset =
            if (curItem.position == n - 1) curItem.offset + curItem.widthFactor - 1 else Float.MAX_VALUE
        var prevIdx = curIndex - 1
        while (prevIdx >= 0) {
            val ii =
                mItems[prevIdx]; while (pLeft > ii.position) off -= adapter.getPageWidth(pLeft--) + mOffset
            off -= ii.widthFactor + mOffset; ii.offset = off; if (ii.position == 0) mFirstOffset =
                off
            prevIdx--; pLeft--
        }
        off = curItem.offset + curItem.widthFactor + mOffset
        var pRight = curItem.position + 1
        var nextIdx = curIndex + 1
        while (nextIdx < mItems.size) {
            val ii = mItems[nextIdx]; while (pRight < ii.position) off += adapter.getPageWidth(
                pRight++
            ) + mOffset
            if (ii.position == n - 1) mLastOffset = off + ii.widthFactor - 1
            ii.offset = off; off += ii.widthFactor + mOffset; nextIdx++; pRight++
        }
        mNeedCalculatePageOffsets = false
    }

    class SavedState : AbsSavedState {
        var position = 0
        var adapterState: Parcelable? = null
        var loader: ClassLoader? = null

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags); out.writeInt(position); out.writeParcelable(
                adapterState, flags
            )
        }

        override fun toString() =
            "FragmentPager.SavedState{${Integer.toHexString(System.identityHashCode(this))} position=$position}"

        internal constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`, loader) {
            val currentLoader = loader ?: javaClass.classLoader
            position = `in`.readInt()
            adapterState = androidx.core.os.ParcelCompat.readParcelable(
                `in`, currentLoader, Parcelable::class.java
            )
            this.loader = currentLoader
        }

        companion object {
            @JvmField
            val CREATOR = object : ClassLoaderCreator<SavedState?> {
                override fun createFromParcel(`in`: Parcel, l: ClassLoader?) = SavedState(`in`, l)
                override fun createFromParcel(`in`: Parcel) = SavedState(`in`, null)
                override fun newArray(size: Int) = arrayOfNulls<SavedState?>(size)
            }
        }
    }

    override fun onSaveInstanceState() = SavedState(super.onSaveInstanceState() ?: Bundle()).apply {
        position = mCurItem; adapterState = mAdapter?.saveState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) return
        super.onRestoreInstanceState(state.superState)
        if (mAdapter != null) {
            mAdapter?.restoreState(
                state.adapterState, state.loader
            ); setCurrentItemInternal(state.position, smoothScroll = false, always = true)
        } else {
            mRestoredCurItem = state.position; mRestoredAdapterState =
                state.adapterState; mRestoredClassLoader = state.loader
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        val lp =
            (if (!checkLayoutParams(params)) generateLayoutParams(params) else params) as LayoutParams
        lp.isDecor = lp.isDecor or isDecorView(child)
        if (mInLayout) {
            check(!lp.isDecor) { "Cannot add pager decor view during layout" }; lp.needsMeasure =
                true; addViewInLayout(child, index, lp)
        } else super.addView(child, index, lp)
    }

    override fun removeView(view: View?) =
        if (mInLayout) removeViewInLayout(view) else super.removeView(view)

    fun infoForChild(child: View) = mAdapter?.let { adapter ->
        mItems.firstOrNull {
            it.`object`?.let { obj ->
                adapter.isViewFromObject(
                    child, obj
                )
            } == true
        }
    }

    fun infoForAnyChild(child: View): ItemInfo? {
        var v = child
        var p = v.parent
        while (p !== this) {
            if (p == null || p !is View) return null; v = p; p = v.parent
        }
        return infoForChild(v)
    }

    fun infoForPosition(position: Int) = mItems.firstOrNull { it.position == position }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow(); mFirstLayout = true
    }

    @SuppressLint("RtlHardcoded")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec)
        )
        mGutterSize = min(measuredWidth / 10, mDefaultGutterSize)
        var cWidth = measuredWidth - paddingLeft - paddingRight
        var cHeight = measuredHeight - paddingTop - paddingBottom
        for (i in 0 until childCount) {
            getChildAt(i)?.takeIf { it.visibility != GONE }?.let { child ->
                (child.layoutParams as? LayoutParams)?.takeIf { it.isDecor }?.let { lp ->
                    val hg = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vg = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    var wMode =
                        if (vg == Gravity.TOP || vg == Gravity.BOTTOM) MeasureSpec.EXACTLY else MeasureSpec.AT_MOST
                    var hMode =
                        if (hg == Gravity.LEFT || hg == Gravity.RIGHT) MeasureSpec.EXACTLY else MeasureSpec.AT_MOST
                    var wSize = cWidth
                    var hSize = cHeight
                    if (lp.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        wMode =
                            MeasureSpec.EXACTLY; if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) wSize =
                            lp.width
                    }
                    if (lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        hMode =
                            MeasureSpec.EXACTLY; if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) hSize =
                            lp.height
                    }
                    child.measure(
                        MeasureSpec.makeMeasureSpec(wSize, wMode),
                        MeasureSpec.makeMeasureSpec(hSize, hMode)
                    )
                    if (vg == Gravity.TOP || vg == Gravity.BOTTOM) cHeight -= child.measuredHeight else cWidth -= child.measuredWidth
                }
            }
        }
        mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(cWidth, MeasureSpec.EXACTLY)
        mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(cHeight, MeasureSpec.EXACTLY)
        mInLayout = true; populate(); mInLayout = false
        for (i in 0 until childCount) {
            getChildAt(i)?.takeIf { it.visibility != GONE }?.let { child ->
                val lp = child.layoutParams as? LayoutParams
                if (lp == null || !lp.isDecor) child.measure(
                    MeasureSpec.makeMeasureSpec(
                        (cWidth * (lp?.widthFactor ?: 1f)).toInt(), MeasureSpec.EXACTLY
                    ), mChildHeightMeasureSpec
                )
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw) recomputeScrollPosition(w, oldw, mPageMargin, mPageMargin)
    }

    private fun recomputeScrollPosition(width: Int, oldWidth: Int, margin: Int, oldMargin: Int) {
        if (oldWidth > 0 && mItems.isNotEmpty()) {
            mScroller?.takeIf { !it.isFinished }?.apply { finalX = currentItem * clientWidth }
                ?: run {
                    val expos = scrollX
                    scrollTo(
                        ((expos.toFloat() / (oldWidth - paddingLeft - paddingRight + oldMargin)) * (width - paddingLeft - paddingRight + margin)).toInt(),
                        scrollY
                    )
                }
        } else {
            val scrollPos = ((infoForPosition(mCurItem)?.let { min(it.offset, mLastOffset) }
                ?: 0f) * (width - paddingLeft - paddingRight)).toInt()
            if (scrollPos != scrollX) {
                completeScroll(false); scrollTo(scrollPos, scrollY)
            }
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        val height = b - t
        var pLeft = paddingLeft
        var pTop = paddingTop
        var pRight = paddingRight
        var pBottom = paddingBottom
        val currentScrollX = scrollX
        var decorCount = 0

        for (i in 0 until count) {
            getChildAt(i)?.takeIf { it.visibility != GONE }?.let { child ->
                val lp = child.layoutParams as LayoutParams
                if (lp.isDecor) {
                    var childLeft = when (lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                        Gravity.LEFT -> pLeft.also { pLeft += child.measuredWidth }
                        Gravity.CENTER_HORIZONTAL -> max((width - child.measuredWidth) / 2, pLeft)
                        Gravity.RIGHT -> (width - pRight - child.measuredWidth).also { pRight += child.measuredWidth }
                        else -> pLeft
                    }
                    val childTop = when (lp.gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                        Gravity.TOP -> pTop.also { pTop += child.measuredHeight }
                        Gravity.CENTER_VERTICAL -> max((height - child.measuredHeight) / 2, pTop)
                        Gravity.BOTTOM -> (height - pBottom - child.measuredHeight).also { pBottom += child.measuredHeight }
                        else -> pTop
                    }
                    childLeft += currentScrollX
                    child.layout(
                        childLeft,
                        childTop,
                        childLeft + child.measuredWidth,
                        childTop + child.measuredHeight
                    )
                    decorCount++
                }
            }
        }

        val childWidth = width - pLeft - pRight
        for (i in 0 until count) {
            getChildAt(i)?.takeIf { it.visibility != GONE }?.let { child ->
                val lp = child.layoutParams as LayoutParams
                val ii = if (!lp.isDecor) infoForChild(child) else null
                if (ii != null) {
                    val childLeft = pLeft + (childWidth * ii.offset).toInt()
                    if (lp.needsMeasure) {
                        lp.needsMeasure = false
                        child.measure(
                            MeasureSpec.makeMeasureSpec(
                                (childWidth * lp.widthFactor).toInt(), MeasureSpec.EXACTLY
                            ), MeasureSpec.makeMeasureSpec(
                                height - pTop - pBottom, MeasureSpec.EXACTLY
                            )
                        )
                    }
                    child.layout(
                        childLeft,
                        pTop,
                        childLeft + child.measuredWidth,
                        pTop + child.measuredHeight
                    )
                }
            }
        }
        mTopPageBounds = pTop; mBottomPageBounds = height - pBottom; mDecorChildCount = decorCount
        if (mFirstLayout) scrollToItem(mCurItem, false, 0, false)
        mFirstLayout = false
    }

    override fun computeScroll() {
        mIsScrollStarted = true
        if (mScroller?.let { !it.isFinished && it.computeScrollOffset() } == true) {
            val oldX = scrollX
            val oldY = scrollY
            val x = mScroller?.currX ?: 0
            val y = mScroller?.currY ?: 0
            if (oldX != x || oldY != y) {
                scrollTo(x, y); if (!pageScrolled(x)) {
                    mScroller?.abortAnimation(); scrollTo(0, y)
                }
            }
            postInvalidateOnAnimation(); return
        }
        completeScroll(true)
    }

    private fun pageScrolled(expos: Int): Boolean {
        if (mItems.isEmpty()) {
            if (mFirstLayout) return false
            mCalledSuper = false; onPageScrolled(0, 0f, 0)
            check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }; return false
        }
        val ii = infoForCurrentScrollPosition() ?: return false
        val w = clientWidth
        val pageOffset =
            (((expos.toFloat() / w) - ii.offset) / (ii.widthFactor + mPageMargin.toFloat() / w))
        mCalledSuper = false; onPageScrolled(
            ii.position, pageOffset, (pageOffset * (w + mPageMargin)).toInt()
        )
        check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }; return true
    }

    @SuppressLint("RtlHardcoded")
    @CallSuper
    protected open fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        if (mDecorChildCount > 0) {
            val sX = scrollX
            var pLeft = paddingLeft
            var pRight = paddingRight
            val w = width
            val layoutDirection = this.layoutDirection

            for (i in 0 until childCount) {
                getChildAt(i)?.let { child ->
                    val lp = child.layoutParams as LayoutParams
                    if (lp.isDecor) {
                        val absoluteGravity =
                            Gravity.getAbsoluteGravity(lp.gravity, layoutDirection)
                        val childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                            Gravity.LEFT -> pLeft.also { pLeft += child.width }
                            Gravity.CENTER_HORIZONTAL -> max((w - child.measuredWidth) / 2, pLeft)
                            Gravity.RIGHT -> (w - pRight - child.measuredWidth).also { pRight += child.width }
                            else -> pLeft
                        } + sX
                        (childLeft - child.left).takeIf { it != 0 }
                            ?.let { child.offsetLeftAndRight(it) }
                    }
                }
            }
        }
        dispatchOnPageScrolled(position, offset, offsetPixels)
        mPageTransformer?.let { transformer ->
            val sX = scrollX
            val w = clientWidth.toFloat()
            for (i in 0 until childCount) {
                getChildAt(i)?.let {
                    if (!(it.layoutParams as LayoutParams).isDecor) transformer.transformPage(
                        it, (it.left - sX) / w
                    )
                }
            }
        }
        mCalledSuper = true
    }

    private fun dispatchOnPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        mOnPageChangeListener?.onPageScrolled(position, offset, offsetPixels)
        mOnPageChangeListeners?.forEach { it?.onPageScrolled(position, offset, offsetPixels) }
        mInternalPageChangeListener?.onPageScrolled(position, offset, offsetPixels)
    }

    private fun dispatchOnPageSelected(position: Int) {
        mOnPageChangeListener?.onPageSelected(position)
        mOnPageChangeListeners?.forEach { it?.onPageSelected(position) }
        mInternalPageChangeListener?.onPageSelected(position)
    }

    private fun dispatchOnScrollStateChanged(state: Int) {
        mOnPageChangeListener?.onPageScrollStateChanged(state)
        mOnPageChangeListeners?.forEach { it?.onPageScrollStateChanged(state) }
        mInternalPageChangeListener?.onPageScrollStateChanged(state)
    }

    private fun completeScroll(postEvents: Boolean) {
        var needPopulate = mScrollState == SCROLL_STATE_SETTLING
        if (needPopulate) {
            setScrollingCacheEnabled(false)
            mScroller?.takeIf { !it.isFinished }?.apply {
                abortAnimation()
                val oldX = scrollX
                val x = currX
                val y = currY
                if (oldX != x || scrollY != y) {
                    scrollTo(x, y); if (x != oldX) pageScrolled(x)
                }
            }
        }
        mPopulatePending = false; mItems.forEach {
            if (it.scrolling) {
                needPopulate = true; it.scrolling = false
            }
        }
        if (needPopulate) if (postEvents) postOnAnimation(mEndScrollRunnable) else mEndScrollRunnable.run()
    }

    private fun isGutterDrag(x: Float, dx: Float) =
        (x < mGutterSize && dx > 0) || (x > width - mGutterSize && dx < 0)

    private fun enableLayers(enable: Boolean) {
        val layer = if (enable) mPageTransformerLayerType else LAYER_TYPE_NONE
        for (i in 0 until childCount) getChildAt(i)?.setLayerType(layer, null)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean = runCatching {
        val action = ev.action and MotionEvent.ACTION_MASK
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) return false.also { resetTouch() }
        if (action != MotionEvent.ACTION_DOWN) {
            if (mIsBeingDragged) return true
            if (mIsUnableToDrag) return false
        }
        when (action) {
            MotionEvent.ACTION_MOVE -> mActivePointerId.takeIf { it != INVALID_POINTER }
                ?.let { id ->
                    val pIdx = ev.findPointerIndex(id)
                    if (pIdx in 0 until ev.pointerCount) {
                        val x = ev.getX(pIdx)
                        val dx = x - mLastMotionX
                        val xDiff = abs(dx)
                        val y = ev.getY(pIdx)
                        val yDiff = abs(y - mInitialMotionY)
                        if (dx != 0f && !isGutterDrag(mLastMotionX, dx) && canScroll(
                                this, false, dx.toInt(), x.toInt(), y.toInt()
                            )
                        ) {
                            mLastMotionX = x; mLastMotionY = y; mIsUnableToDrag = true; return false
                        }
                        if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                            mIsBeingDragged =
                                true; requestParentDisallowInterceptTouchEvent(); setScrollState(
                                SCROLL_STATE_DRAGGING
                            )
                            mLastMotionX =
                                if (dx > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                            mLastMotionY = y; setScrollingCacheEnabled(true)
                        } else if (yDiff > mTouchSlop) mIsUnableToDrag = true
                        if (mIsBeingDragged && performDrag(x)) postInvalidateOnAnimation()
                    }
                }

            MotionEvent.ACTION_DOWN -> {
                mInitialMotionX = ev.x; mLastMotionX = ev.x; mInitialMotionY = ev.y; mLastMotionY =
                    ev.y
                mActivePointerId = ev.getPointerId(0); mIsUnableToDrag = false; mIsScrollStarted =
                    true
                mScroller?.apply {
                    computeScrollOffset()
                    if (mScrollState == SCROLL_STATE_SETTLING && abs(finalX - currX) > mCloseEnough) {
                        abortAnimation(); mPopulatePending = false; populate(); mIsBeingDragged =
                            true
                        requestParentDisallowInterceptTouchEvent(); setScrollState(
                            SCROLL_STATE_DRAGGING
                        )
                    } else {
                        completeScroll(false); mIsBeingDragged = false
                    }
                } ?: run { completeScroll(false); mIsBeingDragged = false }
            }

            MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
        }
        (mVelocityTracker ?: VelocityTracker.obtain().also { mVelocityTracker = it })?.addMovement(
            ev
        )
        mIsBeingDragged
    }.getOrDefault(false)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isFakeDragging) return true
        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) return false
        val adapter = mAdapter ?: return false
        if (adapter.count == 0) return false
        (mVelocityTracker ?: VelocityTracker.obtain().also { mVelocityTracker = it })?.addMovement(
            ev
        )
        var needsInvalidate = false
        when (ev.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mScroller?.abortAnimation(); mPopulatePending = false; populate()
                mInitialMotionX = ev.x; mLastMotionX = ev.x; mInitialMotionY = ev.y; mLastMotionY =
                    ev.y
                mActivePointerId = ev.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingDragged) {
                    val pIdx = ev.findPointerIndex(mActivePointerId)
                    if (pIdx == -1) {
                        resetTouch(); return true
                    }
                    val x = ev.getX(pIdx)
                    val y = ev.getY(pIdx)
                    if (abs(x - mLastMotionX) > mTouchSlop && abs(x - mLastMotionX) > abs(y - mLastMotionY)) {
                        mIsBeingDragged =
                            true; requestParentDisallowInterceptTouchEvent(); setScrollState(
                            SCROLL_STATE_DRAGGING
                        ); setScrollingCacheEnabled(true)
                        mLastMotionX =
                            if (x - mInitialMotionX > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                        mLastMotionY = y; parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                if (mIsBeingDragged) {
                    val activeIdx = ev.findPointerIndex(mActivePointerId)
                    if (activeIdx in 0 until ev.pointerCount) needsInvalidate =
                        performDrag(ev.getX(activeIdx))
                }
            }

            MotionEvent.ACTION_UP -> {
                performClick()
                if (mIsBeingDragged) {
                    mVelocityTracker?.apply {
                        computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                        val initialVelocity = getXVelocity(mActivePointerId).toInt()
                        mPopulatePending = true
                        val w = clientWidth
                        infoForCurrentScrollPosition()?.let { ii ->
                            if (w > 0) {
                                val activeIdx = ev.findPointerIndex(mActivePointerId)
                                if (activeIdx in 0 until ev.pointerCount) {
                                    val nextPage = determineTargetPage(
                                        ii.position,
                                        (((scrollX.toFloat() / w) - ii.offset) / (ii.widthFactor + mPageMargin.toFloat() / w)),
                                        initialVelocity,
                                        (ev.getX(activeIdx) - mInitialMotionX).toInt()
                                    )
                                    setCurrentItemInternal(
                                        nextPage,
                                        smoothScroll = true,
                                        always = true,
                                        velocity = initialVelocity
                                    )
                                }
                            }
                        }
                    }
                    needsInvalidate = resetTouch()
                }
            }

            MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                scrollToItem(mCurItem, true, 0, false); needsInvalidate = resetTouch()
            }

            MotionEvent.ACTION_POINTER_DOWN -> ev.actionIndex.let {
                mLastMotionX = ev.getX(it); mActivePointerId = ev.getPointerId(it)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                val pIdx = ev.findPointerIndex(mActivePointerId)
                if (pIdx in 0 until ev.pointerCount) mLastMotionX = ev.getX(pIdx)
            }
        }
        if (needsInvalidate) postInvalidateOnAnimation()
        return true
    }

    private fun resetTouch(): Boolean {
        mActivePointerId =
            INVALID_POINTER; endDrag(); mLeftEdge?.onRelease(); mRightEdge?.onRelease()
        return mLeftEdge?.isFinished == false || mRightEdge?.isFinished == false
    }

    private fun requestParentDisallowInterceptTouchEvent() =
        parent?.requestDisallowInterceptTouchEvent(true)

    private fun performDrag(x: Float): Boolean {
        var needsInvalidate = false
        val deltaX = mLastMotionX - x; mLastMotionX = x
        var scrollXVal = scrollX.toFloat() + deltaX
        val w = clientWidth
        var leftBound = w * mFirstOffset
        var rightBound = w * mLastOffset
        var leftAbsolute = true
        var rightAbsolute = true
        if (mItems.isNotEmpty()) {
            val first = mItems[0]
            val last = mItems[mItems.size - 1]
            if (first.position != 0) {
                leftAbsolute = false; leftBound = first.offset * w
            }
            if (last.position != (mAdapter?.count ?: 0) - 1) {
                rightAbsolute = false; rightBound = last.offset * w
            }
            if (scrollXVal < leftBound) {
                if (leftAbsolute) {
                    mLeftEdge?.onPull(abs(leftBound - scrollXVal) / w); needsInvalidate = true
                }
                scrollXVal = leftBound
            } else if (scrollXVal > rightBound) {
                if (rightAbsolute) {
                    mRightEdge?.onPull(abs(scrollXVal - rightBound) / w); needsInvalidate = true
                }
                scrollXVal = rightBound
            }
        }
        mLastMotionX += scrollXVal - scrollXVal.toInt(); scrollTo(
            scrollXVal.toInt(), scrollY
        ); pageScrolled(scrollXVal.toInt())
        return needsInvalidate
    }

    private fun infoForCurrentScrollPosition(): ItemInfo? {
        val w = clientWidth
        val scrollOffset = if (w > 0) scrollX.toFloat() / w else 0f
        val marginOffset = if (w > 0) mPageMargin.toFloat() / w else 0f
        var lastPos = -1
        var lastOffset = 0f
        var lastWidth = 0f
        var first = true
        var lastItem: ItemInfo? = null
        var i = 0
        while (i < mItems.size) {
            var ii = mItems[i]
            if (!first && ii.position != lastPos + 1) {
                ii = mTempItem; ii.offset = lastOffset + lastWidth + marginOffset; ii.position =
                    lastPos + 1
                ii.widthFactor = mAdapter?.getPageWidth(ii.position) ?: 1f; i--
            }
            val offset = ii.offset
            if (first || scrollOffset >= offset) {
                if (scrollOffset < offset + ii.widthFactor + marginOffset || i == mItems.size - 1) return ii
            } else return lastItem
            first = false; lastPos = ii.position; lastOffset = offset; lastWidth =
                ii.widthFactor; lastItem = ii; i++
        }
        return lastItem
    }

    private fun determineTargetPage(
        currentPage: Int, pageOffset: Float, velocity: Int, deltaX: Int
    ): Int {
        var targetPage =
            if (abs(deltaX) > mFlingDistance && abs(velocity) > mMinimumVelocity) (if (velocity > 0) currentPage else currentPage + 1) else currentPage + (pageOffset + (if (currentPage >= mCurItem) 0.4f else 0.6f)).toInt()
        if (mItems.isNotEmpty()) targetPage =
            max(mItems[0].position, min(targetPage, mItems[mItems.size - 1].position))
        return targetPage
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var needsInvalidate = false
        val mode = overScrollMode
        if (mode == OVER_SCROLL_ALWAYS || (mode == OVER_SCROLL_IF_CONTENT_SCROLLS && mAdapter?.let { it.count > 1 } == true)) {
            mLeftEdge?.takeIf { !it.isFinished }?.let { edge ->
                canvas.withSave {
                    val h =
                        height - paddingTop - paddingBottom; canvas.rotate(270f); canvas.translate(
                    (-h + paddingTop).toFloat(), mFirstOffset * width
                )
                    edge.setSize(h, width); needsInvalidate = edge.draw(canvas)
                }
            }
            mRightEdge?.takeIf { !it.isFinished }?.let { edge ->
                canvas.withSave {
                    val h =
                        height - paddingTop - paddingBottom; canvas.rotate(90f); canvas.translate(
                    -paddingTop.toFloat(), -(mLastOffset + 1) * width
                )
                    edge.setSize(h, width); needsInvalidate = needsInvalidate or edge.draw(canvas)
                }
            }
        } else {
            mLeftEdge?.finish(); mRightEdge?.finish()
        }
        if (needsInvalidate) postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val adapter = mAdapter
        val marginDrawable = mMarginDrawable
        if (mPageMargin > 0 && marginDrawable != null && mItems.isNotEmpty() && adapter != null) {
            val sX = scrollX
            val w = width
            val marginOffset = mPageMargin.toFloat() / w
            var idx = 0
            var ii = mItems[0]
            var offset = ii.offset
            for (pos in ii.position until mItems[mItems.size - 1].position) {
                while (pos > ii.position && idx < mItems.size - 1) ii = mItems[++idx]
                val drawAt = if (pos == ii.position) ((ii.offset + ii.widthFactor) * w).also {
                    offset = ii.offset + ii.widthFactor + marginOffset
                } else ((offset + adapter.getPageWidth(pos)) * w).also {
                    offset += adapter.getPageWidth(
                        pos
                    ) + marginOffset
                }
                if (drawAt + mPageMargin > sX) {
                    marginDrawable.setBounds(
                        round(drawAt).toInt(),
                        mTopPageBounds,
                        round(drawAt + mPageMargin).toInt(),
                        mBottomPageBounds
                    )
                    marginDrawable.draw(canvas)
                }
                if (drawAt > sX + w) break
            }
        }
    }

    fun beginFakeDrag(): Boolean {
        if (mIsBeingDragged) return false
        isFakeDragging = true; setScrollState(SCROLL_STATE_DRAGGING); mLastMotionX =
            0f; mInitialMotionX = 0f
        val tracker = mVelocityTracker ?: VelocityTracker.obtain()
            .also { mVelocityTracker = it }; tracker.clear()
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        mVelocityTracker?.addMovement(ev); ev.recycle(); mFakeDragBeginTime = time; return true
    }

    fun endFakeDrag() {
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }
        if (mAdapter != null) {
            mVelocityTracker?.apply {
                computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val v = getXVelocity(mActivePointerId).toInt()
                mPopulatePending = true
                val w = clientWidth
                infoForCurrentScrollPosition()?.let { ii ->
                    val nextPage = determineTargetPage(
                        ii.position,
                        ((scrollX.toFloat() / w) - ii.offset) / ii.widthFactor,
                        v,
                        (mLastMotionX - mInitialMotionX).toInt()
                    )
                    setCurrentItemInternal(
                        nextPage, smoothScroll = true, always = true, velocity = v
                    )
                }
            }
        }
        endDrag(); isFakeDragging = false
    }

    fun fakeDragBy(xOffset: Float) {
        check(isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }
        val adapter = mAdapter ?: return; mLastMotionX += xOffset
        var scrollXVal = scrollX.toFloat() - xOffset
        val w = clientWidth
        var leftBound = w * mFirstOffset
        var rightBound = w * mLastOffset
        if (mItems.isNotEmpty()) {
            val first = mItems[0]
            val last = mItems[mItems.size - 1]
            if (first.position != 0) leftBound = first.offset * w
            if (last.position != adapter.count - 1) rightBound = last.offset * w
            scrollXVal = scrollXVal.coerceIn(leftBound, rightBound)
        }
        mLastMotionX += scrollXVal - scrollXVal.toInt(); scrollTo(
            scrollXVal.toInt(), scrollY
        ); pageScrolled(scrollXVal.toInt())
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(
            mFakeDragBeginTime, time, MotionEvent.ACTION_MOVE, mLastMotionX, 0f, 0
        )
        mVelocityTracker?.addMovement(ev); ev.recycle()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pIdx = ev.actionIndex
        if (ev.getPointerId(pIdx) == mActivePointerId) {
            val newIdx = if (pIdx == 0) 1 else 0; mLastMotionX = ev.getX(newIdx)
            mActivePointerId = ev.getPointerId(newIdx); mVelocityTracker?.clear()
        }
    }

    private fun endDrag() {
        mIsBeingDragged = false; mIsUnableToDrag =
            false; mVelocityTracker?.recycle(); mVelocityTracker = null
    }

    private fun setScrollingCacheEnabled(enabled: Boolean) {
        if (mScrollingCacheEnabled != enabled) mScrollingCacheEnabled = enabled
    }

    override fun canScrollHorizontally(direction: Int) = mAdapter?.let {
        val w = clientWidth
        val sX =
            scrollX; if (direction < 0) sX > (w * mFirstOffset).toInt() else if (direction > 0) sX < (w * mLastOffset).toInt() else false
    } ?: false

    protected fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val sX = v.scrollX
            val sY = v.scrollY
            for (i in v.childCount - 1 downTo 0) {
                val child = v.getChildAt(i)
                if (child != null && x + sX >= child.left && x + sX < child.right && y + sY >= child.top && y + sY < child.bottom && canScroll(
                        child, true, dx, x + sX - child.left, y + sY - child.top
                    )
                ) return true
            }
        }
        return checkV && v.canScrollHorizontally(-dx)
    }

    override fun dispatchKeyEvent(event: KeyEvent) =
        super.dispatchKeyEvent(event) || executeKeyEvent(event)

    fun executeKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> handled =
                    if (event.hasModifiers(KeyEvent.META_ALT_ON)) pageLeft() else arrowScroll(
                        FOCUS_LEFT
                    )

                KeyEvent.KEYCODE_DPAD_RIGHT -> handled =
                    if (event.hasModifiers(KeyEvent.META_ALT_ON)) pageRight() else arrowScroll(
                        FOCUS_RIGHT
                    )

                KeyEvent.KEYCODE_TAB -> if (event.hasNoModifiers()) handled =
                    arrowScroll(FOCUS_FORWARD) else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) handled =
                    arrowScroll(FOCUS_BACKWARD)
            }
        }
        return handled
    }

    fun arrowScroll(direction: Int): Boolean {
        var focused = findFocus().takeIf { it !== this }
        if (focused != null) {
            var isChild = false
            var p = focused.parent
            while (p is ViewGroup) {
                if (p === this) {
                    isChild = true; break
                }; p = p.parent
            }
            if (!isChild) {
                val sb = StringBuilder(focused.javaClass.simpleName)
                var cp = focused.parent
                while (cp is ViewGroup) {
                    sb.append(" => ").append(cp.javaClass.simpleName); cp = cp.parent
                }
                Log.e(TAG, "arrowScroll tried to find focus based on non-child view $sb"); focused =
                    null
            }
        }
        var handled = false
        val next = FocusFinder.getInstance().findNextFocus(this, focused, direction)
        if (next != null && next !== focused) {
            val nextL = getChildRectInPagerCoordinates(mTempRect, next).left
            val currL = getChildRectInPagerCoordinates(mTempRect, focused).left
            if (direction == FOCUS_LEFT) handled =
                if (focused != null && nextL >= currL) pageLeft() else next.requestFocus()
            else if (direction == FOCUS_RIGHT) handled =
                if (focused != null && nextL <= currL) pageRight() else next.requestFocus()
        } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) handled = pageLeft()
        else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) handled = pageRight()
        if (handled) playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction))
        return handled
    }

    private fun getChildRectInPagerCoordinates(outRect: Rect?, child: View?): Rect {
        val r = outRect ?: Rect(); if (child == null) return r.apply { set(0, 0, 0, 0) }
        r.set(child.left, child.top, child.right, child.bottom)
        var p = child.parent
        while (p is ViewGroup && p !== this) {
            r.left += p.left; r.right += p.right; r.top += p.top; r.bottom += p.bottom; p = p.parent
        }
        return r
    }

    fun pageLeft() = (mCurItem > 0).also { if (it) setCurrentItem(mCurItem - 1, true) }
    fun pageRight() = (mAdapter != null && mCurItem < mAdapter!!.count - 1).also {
        if (it) setCurrentItem(
            mCurItem + 1, true
        )
    }

    override fun addFocusables(views: ArrayList<View?>, direction: Int, focusableMode: Int) {
        val focusableCount = views.size
        if (descendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            for (i in 0 until childCount) getChildAt(i)?.takeIf { it.isVisible }?.let { c ->
                infoForChild(c)?.takeIf { it.position == mCurItem }
                    ?.let { c.addFocusables(views, direction, focusableMode) }
            }
        }
        if (descendantFocusability != FOCUS_AFTER_DESCENDANTS || focusableCount == views.size) {
            if (!isFocusable || ((focusableMode and FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE && isInTouchMode && !isFocusableInTouchMode)) return
            views.add(this)
        }
    }

    override fun addTouchables(views: ArrayList<View?>?) {
        for (i in 0 until childCount) getChildAt(i)?.takeIf { it.isVisible }?.let { c ->
            infoForChild(c)?.takeIf { it.position == mCurItem }?.let { c.addTouchables(views) }
        }
    }

    override fun onRequestFocusInDescendants(
        direction: Int, previouslyFocusedRect: Rect?
    ): Boolean {
        val count = childCount
        val start = if ((direction and FOCUS_FORWARD) != 0) 0 else count - 1
        val inc = if ((direction and FOCUS_FORWARD) != 0) 1 else -1
        val end = if ((direction and FOCUS_FORWARD) != 0) count else -1
        var i = start; while (i != end) {
            getChildAt(i)?.takeIf { it.isVisible }?.let { c ->
                infoForChild(c)?.takeIf { it.position == mCurItem }
                    ?.let { if (c.requestFocus(direction, previouslyFocusedRect)) return true }
            }
            i += inc
        }
        return false
    }

    @SuppressLint("WrongConstant")
    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) return super.dispatchPopulateAccessibilityEvent(
            event
        )
        for (i in 0 until childCount) {
            getChildAt(i)?.takeIf { it.isVisible }?.let { child ->
                infoForChild(child)?.takeIf {
                    it.position == mCurItem && child.dispatchPopulateAccessibilityEvent(event)
                }?.let { return true }
            }
        }
        return false
    }

    override fun generateDefaultLayoutParams() = LayoutParams()
    override fun generateLayoutParams(p: ViewGroup.LayoutParams?) = generateDefaultLayoutParams()
    override fun checkLayoutParams(p: ViewGroup.LayoutParams?) =
        p is LayoutParams && super.checkLayoutParams(p)

    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)

    internal inner class MyAccessibilityDelegate : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            event.className = SliderPager::class.java.name; event.isScrollable = canScroll()
            mAdapter?.takeIf { event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED }?.let {
                event.itemCount = it.count; event.fromIndex = mCurItem; event.toIndex = mCurItem
            }
        }

        override fun onInitializeAccessibilityNodeInfo(
            host: View, info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info); info.className =
                SliderPager::class.java.name; info.isScrollable = canScroll()
            if (canScrollHorizontally(1)) info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
            if (canScrollHorizontally(-1)) info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?) =
            super.performAccessibilityAction(host, action, args) || when (action) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> canScrollHorizontally(1).also {
                    if (it) this@SliderPager.currentItem = mCurItem + 1
                }

                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> canScrollHorizontally(-1).also {
                    if (it) this@SliderPager.currentItem = mCurItem - 1
                }

                else -> false
            }

        private fun canScroll() = mAdapter?.let { it.count > 1 } ?: false
    }

    private inner class PagerObserver : DataSetObserver() {
        override fun onChanged() = dataSetChanged()
        override fun onInvalidated() = dataSetChanged()
    }

    internal inner class OwnScroller : Scroller {
        private val durationScrollMillis: Int

        constructor(context: Context?, durationScroll: Int) : super(context, sInterpolator) {
            this.durationScrollMillis = durationScroll
        }

        constructor(context: Context?, durationScroll: Int, interpolator: Interpolator?) : super(
            context, interpolator
        ) {
            this.durationScrollMillis = durationScroll
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) =
            super.startScroll(startX, startY, dx, dy, durationScrollMillis)
    }

    class LayoutParams : ViewGroup.LayoutParams {
        var isDecor = false
        var gravity = 0
        var widthFactor = 0f
        var needsMeasure = false
        var position = 0
        var childIndex = 0

        constructor() : super(MATCH_PARENT, MATCH_PARENT)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            context.withStyledAttributes(attrs, LAYOUT_ATTRS) {
                gravity = getInteger(0, Gravity.TOP)
            }
        }
    }

    internal class ViewPositionComparator : Comparator<View?> {
        override fun compare(lhs: View?, rhs: View?): Int {
            if (lhs == null || rhs == null) return 0
            val llp = lhs.layoutParams as LayoutParams
            val rlp = rhs.layoutParams as LayoutParams
            return if (llp.isDecor != rlp.isDecor) (if (llp.isDecor) 1 else -1) else llp.position - rlp.position
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    companion object {
        const val DEFAULT_SCROLL_DURATION = 250
        private const val TAG = "SliderPager"
        private const val DEBUG = false
        private const val USE_CACHE = false
        private const val DEFAULT_OFFSCREEN_PAGES = 1
        private const val MAX_SETTLE_DURATION = 600
        private const val MIN_DISTANCE_FOR_FLING = 25
        private const val DEFAULT_GUTTER_SIZE = 16
        private const val MIN_FLING_VELOCITY = 400

        val LAYOUT_ATTRS = intArrayOf(android.R.attr.layout_gravity)

        private val COMPARATOR =
            Comparator<ItemInfo?> { lhs, rhs -> if (lhs == null || rhs == null) 0 else lhs.position - rhs.position }

        private val sInterpolator =
            Interpolator { t -> (t - 1.0f).let { it * it * it * it * it + 1.0f } }

        private const val INVALID_POINTER = -1
        private const val CLOSE_ENOUGH = 2
        private const val DRAW_ORDER_DEFAULT = 0
        private const val DRAW_ORDER_FORWARD = 1
        private const val DRAW_ORDER_REVERSE = 2
        private val sPositionComparator = ViewPositionComparator()

        const val SCROLL_STATE_IDLE = 0
        const val SCROLL_STATE_DRAGGING = 1
        const val SCROLL_STATE_SETTLING = 2

        private fun isDecorView(view: View) = view is DecorView
    }
}