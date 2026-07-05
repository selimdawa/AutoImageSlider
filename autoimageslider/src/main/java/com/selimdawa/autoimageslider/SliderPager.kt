package com.selimdawa.autoimageslider

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
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
import androidx.core.view.accessibility.AccessibilityEventCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible
import androidx.customview.view.AbsSavedState
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.InfiniteAdapter.InfinitePagerAdapter
import java.lang.annotation.Inherited
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

open class SliderPager : ViewGroup {

    private var mExpectedAdapterCount = 0

    class ItemInfo {
        var `object`: Any? = null
        var position: Int = 0
        var scrolling: Boolean = false
        var widthFactor: Float = 0f
        var offset: Float = 0f
    }

    private val mItems = ArrayList<ItemInfo>()
    private val mTempItem = ItemInfo()

    private val mTempRect = Rect()

    var mAdapter: PagerAdapter? = null
    var mCurItem: Int = 0
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
    private var mOffscreenPageLimit: Int = DEFAULT_OFFSCREEN_PAGES

    private var mIsBeingDragged = false
    private var mIsUnableToDrag = false
    private var mDefaultGutterSize = 0
    private var mGutterSize = 0
    private var mTouchSlop = 0

    private var mLastMotionX = 0f
    private var mLastMotionY = 0f
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f

    private var mActivePointerId: Int = INVALID_POINTER

    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mFlingDistance = 0
    private var mCloseEnough = 0

    var isFakeDragging: Boolean = false
        private set
    private var mFakeDragBeginTime: Long = 0

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
    private val mEndScrollRunnable = Runnable {
        setScrollState(SCROLL_STATE_IDLE)
        populate()
    }

    private var mScrollState: Int = SCROLL_STATE_IDLE

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

    @Retention(AnnotationRetention.RUNTIME)
    @Target(AnnotationTarget.CLASS)
    annotation class DecorView {}

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initSliderPager()
    }

    fun initSliderPager() {
        setWillNotDraw(false)
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        val currentContext = context
        mScroller = OwnScroller(currentContext, DEFAULT_SCROLL_DURATION, sInterpolator)
        val configuration = ViewConfiguration.get(currentContext)
        val density = currentContext.resources.displayMetrics.density

        mTouchSlop = configuration.scaledPagingTouchSlop
        mMinimumVelocity = (MIN_FLING_VELOCITY * density).toInt()
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mLeftEdge = EdgeEffect(currentContext)
        mRightEdge = EdgeEffect(currentContext)

        mFlingDistance = (MIN_DISTANCE_FOR_FLING * density).toInt()
        mCloseEnough = (CLOSE_ENOUGH * density).toInt()
        mDefaultGutterSize = (DEFAULT_GUTTER_SIZE * density).toInt()

        ViewCompat.setAccessibilityDelegate(this, MyAccessibilityDelegate())

        if (ViewCompat.getImportantForAccessibility(this) == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            ViewCompat.setImportantForAccessibility(
                this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { v, originalInsets ->
            val applied = ViewCompat.onApplyWindowInsets(v, originalInsets)
            if (applied.isConsumed) {
                applied
            } else {
                val res = Rect()
                res.left = applied.systemWindowInsetLeft
                res.top = applied.systemWindowInsetTop
                res.right = applied.systemWindowInsetRight
                res.bottom = applied.systemWindowInsetBottom

                var i = 0
                val count = childCount
                while (i < count) {
                    val child = getChildAt(i)
                    if (child != null) {
                        val childInsets = ViewCompat.dispatchApplyWindowInsets(child, applied)
                        res.left = min(childInsets.systemWindowInsetLeft, res.left)
                        res.top = min(childInsets.systemWindowInsetTop, res.top)
                        res.right = min(childInsets.systemWindowInsetRight, res.right)
                        res.bottom =
                            min(childInsets.systemWindowInsetBottom, res.bottom)
                    }
                    i++
                }

                applied.replaceSystemWindowInsets(res.left, res.top, res.right, res.bottom)
            }
        }
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(mEndScrollRunnable)
        val scroller = mScroller
        if (scroller != null && !scroller.isFinished) {
            scroller.abortAnimation()
        }
        super.onDetachedFromWindow()
    }

    fun setScrollState(newState: Int) {
        if (mScrollState == newState) {
            return
        }

        mScrollState = newState
        if (mPageTransformer != null) {
            enableLayers(newState != SCROLL_STATE_IDLE)
        }
        dispatchOnScrollStateChanged(newState)
    }

    private fun setAdapterViewPagerObserver(observer: PagerObserver?) {
        try {
            val setViewPagerObserver = PagerAdapter::class.java.getDeclaredMethod(
                "setViewPagerObserver", DataSetObserver::class.java
            )
            setViewPagerObserver.isAccessible = true
            setViewPagerObserver.invoke(mAdapter, observer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeNonDecorViews() {
        var i = 0
        while (i < childCount) {
            val child = getChildAt(i)
            if (child != null) {
                val lp = child.layoutParams as LayoutParams
                if (!lp.isDecor) {
                    removeViewAt(i)
                    i--
                }
            }
            i++
        }
    }

    var adapter: PagerAdapter?
        get() = mAdapter
        set(adapter) {
            val currentAdapter = mAdapter
            if (currentAdapter != null) {
                setAdapterViewPagerObserver(null)
                currentAdapter.startUpdate(this)
                for (ii in mItems) {
                    val obj = ii.`object`
                    if (obj != null) {
                        currentAdapter.destroyItem(this, ii.position, obj)
                    }
                }
                currentAdapter.finishUpdate(this)
                mItems.clear()
                removeNonDecorViews()
                mCurItem = 0
                scrollTo(0, 0)
            }

            val oldAdapter = mAdapter
            mAdapter = adapter
            mExpectedAdapterCount = 0

            val newAdapter = mAdapter
            if (newAdapter != null) {
                var observer = mObserver
                if (observer == null) {
                    observer = PagerObserver()
                    mObserver = observer
                }
                setAdapterViewPagerObserver(observer)
                try {
                    newAdapter.registerDataSetObserver(observer)
                } catch (ignored: Exception) {
                }
                mPopulatePending = false
                val wasFirstLayout = mFirstLayout
                mFirstLayout = true
                mExpectedAdapterCount = newAdapter.count
                if (mRestoredCurItem >= 0) {
                    newAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader)
                    setCurrentItemInternal(mRestoredCurItem, false, true)
                    mRestoredCurItem = -1
                    mRestoredAdapterState = null
                    mRestoredClassLoader = null
                } else if (!wasFirstLayout) {
                    populate()
                } else {
                    requestLayout()
                }
            }

            val listeners = mAdapterChangeListeners
            if (listeners != null && listeners.isNotEmpty()) {
                var i = 0
                val count = listeners.size
                while (i < count) {
                    listeners[i]?.onAdapterChanged(this, oldAdapter, adapter)
                    i++
                }
            }
        }

    fun addOnAdapterChangeListener(listener: OnAdapterChangeListener) {
        var listeners = mAdapterChangeListeners
        if (listeners == null) {
            listeners = ArrayList()
            mAdapterChangeListeners = listeners
        }
        listeners.add(listener)
    }

    fun removeOnAdapterChangeListener(listener: OnAdapterChangeListener) {
        mAdapterChangeListeners?.remove(listener)
    }

    private val clientWidth: Int
        get() = measuredWidth - paddingLeft - paddingRight

    fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        var targetItem = item
        val currentAdapter = mAdapter
        if (currentAdapter is InfinitePagerAdapter) {
            targetItem = currentAdapter.getMiddlePosition(targetItem)
        }
        mPopulatePending = false
        setCurrentItemInternal(targetItem, smoothScroll, false)
    }

    var currentItem: Int
        get() {
            val currentAdapter = mAdapter
            if (currentAdapter is InfinitePagerAdapter) {
                if (currentAdapter.realCount > 0) {
                    return currentAdapter.getRealPosition(mCurItem)
                }
            }
            return mCurItem
        }
        set(item) {
            mPopulatePending = false
            setCurrentItem(item, !mFirstLayout)
        }

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean) {
        setCurrentItemInternal(item, smoothScroll, always, 0)
    }

    fun setCurrentItemInternal(item: Int, smoothScroll: Boolean, always: Boolean, velocity: Int) {
        var targetItem = item
        val currentAdapter = mAdapter
        if (currentAdapter == null || currentAdapter.count <= 0) {
            setScrollingCacheEnabled(false)
            return
        }
        if (!always && mCurItem == targetItem && mItems.size != 0) {
            setScrollingCacheEnabled(false)
            return
        }

        val maxCount = currentAdapter.count
        if (targetItem < 0) {
            targetItem = 0
        } else if (targetItem >= maxCount) {
            targetItem = maxCount - 1
        }
        val pageLimit = mOffscreenPageLimit
        if (targetItem > (mCurItem + pageLimit) || targetItem < (mCurItem - pageLimit)) {
            for (ii in mItems) {
                ii.scrolling = true
            }
        }
        val dispatchSelected = mCurItem != targetItem

        if (mFirstLayout) {
            mCurItem = targetItem
            triggerOnPageChangeEvent(targetItem)
            requestLayout()
        } else {
            populate(targetItem)
            scrollToItem(targetItem, smoothScroll, velocity, dispatchSelected)
        }
    }

    private fun scrollToItem(
        item: Int, smoothScroll: Boolean, velocity: Int, dispatchSelected: Boolean
    ) {
        val curInfo = infoForPosition(item)
        var destX = 0
        if (curInfo != null) {
            val width = this.clientWidth
            destX = (width * kotlin.math.max(
                mFirstOffset, kotlin.math.min(curInfo.offset, mLastOffset)
            )).toInt()
        }
        if (smoothScroll) {
            smoothScrollTo(destX, 0, velocity)
            if (dispatchSelected) {
                triggerOnPageChangeEvent(item)
            }
        } else {
            if (dispatchSelected) {
                triggerOnPageChangeEvent(item)
            }
            completeScroll(false)
            scrollTo(destX, 0)
            pageScrolled(destX)
        }
    }

    @Deprecated(
        "Use addOnPageChangeListener and removeOnPageChangeListener instead."
    )
    fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        mOnPageChangeListener = listener
    }

    fun addOnPageChangeListener(listener: OnPageChangeListener) {
        var listeners = mOnPageChangeListeners
        if (listeners == null) {
            listeners = ArrayList()
            mOnPageChangeListeners = listeners
        }
        listeners.add(listener)
    }

    fun removeOnPageChangeListener(listener: OnPageChangeListener) {
        mOnPageChangeListeners?.remove(listener)
    }

    fun clearOnPageChangeListeners() {
        mOnPageChangeListeners?.clear()
    }

    fun setPageTransformer(
        reverseDrawingOrder: Boolean, transformer: PageTransformer?
    ) {
        setPageTransformer(reverseDrawingOrder, transformer, LAYER_TYPE_HARDWARE)
    }

    fun setPageTransformer(
        reverseDrawingOrder: Boolean, transformer: PageTransformer?, pageLayerType: Int
    ) {
        val hasTransformer = transformer != null
        val needsPopulate = hasTransformer != (mPageTransformer != null)
        mPageTransformer = transformer
        isChildrenDrawingOrderEnabled = hasTransformer
        if (hasTransformer) {
            mDrawingOrder = if (reverseDrawingOrder) DRAW_ORDER_REVERSE else DRAW_ORDER_FORWARD
            mPageTransformerLayerType = pageLayerType
        } else {
            mDrawingOrder = DRAW_ORDER_DEFAULT
        }
        if (needsPopulate) populate()
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        val index = if (mDrawingOrder == DRAW_ORDER_REVERSE) childCount - 1 - i else i
        val orderedChildren = mDrawingOrderedChildren

        if (orderedChildren == null || orderedChildren.size != childCount) {
            sortChildDrawingOrder()
        }

        val targetChild = mDrawingOrderedChildren?.get(index)
        val lp = targetChild?.layoutParams as? LayoutParams
        return lp?.childIndex ?: index
    }

    fun setInternalPageChangeListener(listener: OnPageChangeListener?): OnPageChangeListener? {
        val oldListener = mInternalPageChangeListener
        mInternalPageChangeListener = listener
        return oldListener
    }

    var offscreenPageLimit: Int
        get() = mOffscreenPageLimit
        set(limit) {
            var finalLimit = limit
            if (finalLimit < DEFAULT_OFFSCREEN_PAGES) {
                Log.w(
                    TAG,
                    "Requested offscreen page limit $finalLimit too small; defaulting to $DEFAULT_OFFSCREEN_PAGES"
                )
                finalLimit = DEFAULT_OFFSCREEN_PAGES
            }
            if (finalLimit != mOffscreenPageLimit) {
                mOffscreenPageLimit = finalLimit
                populate()
            }
        }

    var pageMargin: Int
        get() = mPageMargin
        set(marginPixels) {
            val oldMargin = mPageMargin
            mPageMargin = marginPixels

            val currentWidth = width
            recomputeScrollPosition(currentWidth, currentWidth, marginPixels, oldMargin)

            requestLayout()
        }

    fun setPageMarginDrawable(d: Drawable?) {
        mMarginDrawable = d
        if (d != null) refreshDrawableState()
        setWillNotDraw(d == null)
        invalidate()
    }

    fun setPageMarginDrawable(@DrawableRes resId: Int) {
        setPageMarginDrawable(ContextCompat.getDrawable(context, resId))
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return super.verifyDrawable(who) || who === mMarginDrawable
    }

    fun setScrollDuration(millis: Int, interpolator: Interpolator?) {
        mScroller = if (interpolator != null) {
            OwnScroller(context, millis, interpolator)
        } else {
            OwnScroller(context, millis)
        }
    }

    fun setScrollDuration(millis: Int) {
        setScrollDuration(millis, null)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        val d = mMarginDrawable
        if (d != null && d.isStateful) {
            d.state = drawableState
        }
    }

    fun distanceInfluenceForSnapDuration(f: Float): Float {
        var value = f
        value -= 0.5f
        value *= (0.3f * kotlin.math.PI.toFloat() / 2.0f)
        return kotlin.math.sin(value.toDouble()).toFloat()
    }

    @JvmOverloads
    fun smoothScrollTo(x: Int, y: Int, velocity: Int = 0) {
        var currentVelocity = velocity
        if (childCount == 0) {
            setScrollingCacheEnabled(false)
            return
        }

        val sx: Int
        val scroller = mScroller
        val wasScrolling = scroller != null && !scroller.isFinished
        if (wasScrolling && scroller != null) {
            sx = if (mIsScrollStarted) scroller.currX else scroller.startX
            scroller.abortAnimation()
            setScrollingCacheEnabled(false)
        } else {
            sx = scrollX
        }
        val sy = scrollY
        val dx = x - sx
        val dy = y - sy
        if (dx == 0 && dy == 0) {
            completeScroll(false)
            populate()
            setScrollState(SCROLL_STATE_IDLE)
            return
        }

        setScrollingCacheEnabled(true)
        setScrollState(SCROLL_STATE_SETTLING)

        val width = clientWidth
        val halfWidth = width / 2
        val distanceRatio = minOf(1f, 1.0f * kotlin.math.abs(dx) / width)
        val distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio)

        var duration: Int
        currentVelocity = kotlin.math.abs(currentVelocity)
        if (currentVelocity > 0) {
            duration =
                4 * kotlin.math.round(1000 * kotlin.math.abs(distance / currentVelocity)).toInt()
        } else {
            val adapter = mAdapter
            val pageWidth = if (adapter != null) width * adapter.getPageWidth(mCurItem) else 0f
            val pageDelta = kotlin.math.abs(dx).toFloat() / (pageWidth + mPageMargin)
            duration = ((pageDelta + 1) * 100).toInt()
        }
        duration = minOf(duration, MAX_SETTLE_DURATION)

        mIsScrollStarted = false
        mScroller?.startScroll(sx, sy, dx, dy, duration)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun addNewItem(position: Int, index: Int): ItemInfo {
        val ii = ItemInfo()
        ii.position = position
        ii.`object` = mAdapter?.instantiateItem(this, position) ?: Any()
        ii.widthFactor = mAdapter?.getPageWidth(position) ?: 1f
        if (index !in mItems.indices) {
            mItems.add(ii)
        } else {
            mItems.add(index, ii)
        }
        return ii
    }

    fun dataSetChanged() {
        val adapter = mAdapter ?: return
        val adapterCount = adapter.count
        mExpectedAdapterCount = adapterCount
        var needPopulate = mItems.size < mOffscreenPageLimit * 2 + 1 && mItems.size < adapterCount
        var newCurrItem = mCurItem

        var isUpdating = false
        var i = 0
        while (i < mItems.size) {
            val ii = mItems[i]
            val obj = ii.`object`
            if (obj == null) {
                i++
                continue
            }
            val newPos = adapter.getItemPosition(obj)

            if (newPos == PagerAdapter.POSITION_UNCHANGED) {
                i++
                continue
            }

            if (newPos == PagerAdapter.POSITION_NONE) {
                mItems.removeAt(i)
                i--

                if (!isUpdating) {
                    adapter.startUpdate(this)
                    isUpdating = true
                }

                adapter.destroyItem(this, ii.position, obj)
                needPopulate = true

                if (mCurItem == ii.position) {
                    newCurrItem = kotlin.math.max(0, kotlin.math.min(mCurItem, adapterCount - 1))
                    needPopulate = true
                }
                i++
                continue
            }

            if (ii.position != newPos) {
                if (ii.position == mCurItem) {
                    newCurrItem = newPos
                }

                ii.position = newPos
                needPopulate = true
            }
            i++
        }

        if (isUpdating) {
            adapter.finishUpdate(this)
        }

        mItems.sortWith(COMPARATOR)

        if (needPopulate) {
            val count = childCount
            for (j in 0 until count) {
                val child = getChildAt(j)
                if (child != null) {
                    val lp = child.layoutParams as LayoutParams
                    if (!lp.isDecor) {
                        lp.widthFactor = 0f
                    }
                }
            }

            setCurrentItemInternal(newCurrItem, false, true)
            requestLayout()
        }
    }

    fun populate(newCurrentItem: Int = mCurItem) {
        var oldCurInfo: ItemInfo? = null
        if (mCurItem != newCurrentItem) {
            oldCurInfo = infoForPosition(mCurItem)
            mCurItem = newCurrentItem
        }

        val adapter = mAdapter
        if (adapter == null) {
            sortChildDrawingOrder()
            return
        }

        if (mPopulatePending) {
            if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...")
            sortChildDrawingOrder()
            return
        }

        if (windowToken == null) {
            return
        }

        adapter.startUpdate(this)

        val pageLimit = mOffscreenPageLimit
        val startPos = kotlin.math.max(0, mCurItem - pageLimit)
        val N = adapter.count
        val endPos = kotlin.math.min(N - 1, mCurItem + pageLimit)

        if (N != mExpectedAdapterCount) {
            val resName: String? = try {
                resources.getResourceName(id)
            } catch (e: Resources.NotFoundException) {
                Integer.toHexString(id)
            }
            throw IllegalStateException(
                "The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: $mExpectedAdapterCount, found: $N Pager id: $resName Pager class: $javaClass Problematic adapter: ${adapter.javaClass}"
            )
        }

        var curIndex = 0
        var curItem: ItemInfo? = null
        while (curIndex < mItems.size) {
            val ii = mItems[curIndex]
            if (ii.position >= mCurItem) {
                if (ii.position == mCurItem) curItem = ii
                break
            }
            curIndex++
        }

        if (curItem == null && N > 0) {
            curItem = addNewItem(mCurItem, curIndex)
        }

        if (curItem != null) {
            var extraWidthLeft = 0f
            var itemIndex = curIndex - 1
            var ii = if (itemIndex >= 0) mItems[itemIndex] else null
            val clientWidth = this.clientWidth
            val leftWidthNeeded =
                if (clientWidth <= 0) 0f else 2f - curItem.widthFactor + paddingLeft.toFloat() / clientWidth.toFloat()

            for (pos in mCurItem - 1 downTo 0) {
                if (extraWidthLeft >= leftWidthNeeded && pos < startPos) {
                    if (ii == null) {
                        break
                    }
                    val obj = ii.`object`
                    if (pos == ii.position && !ii.scrolling && obj != null) {
                        mItems.removeAt(itemIndex)
                        adapter.destroyItem(this, pos, obj)
                        if (DEBUG) {
                            Log.i(
                                TAG,
                                "populate() - destroyItem() with pos: $pos view: ${obj as? View}"
                            )
                        }
                        itemIndex--
                        curIndex--
                        ii = if (itemIndex >= 0) mItems[itemIndex] else null
                    }
                } else if (ii != null && pos == ii.position) {
                    extraWidthLeft += ii.widthFactor
                    itemIndex--
                    ii = if (itemIndex >= 0) mItems[itemIndex] else null
                } else {
                    ii = addNewItem(pos, itemIndex + 1)
                    extraWidthLeft += ii.widthFactor
                    curIndex++
                    ii = if (itemIndex >= 0) mItems[itemIndex] else null
                }
            }

            var extraWidthRight = curItem.widthFactor
            itemIndex = curIndex + 1
            if (extraWidthRight < 2f) {
                ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                val rightWidthNeeded =
                    if (clientWidth <= 0) 0f else paddingRight.toFloat() / clientWidth.toFloat() + 2f
                for (pos in mCurItem + 1 until N) {
                    if (extraWidthRight >= rightWidthNeeded && pos > endPos) {
                        if (ii == null) {
                            break
                        }
                        val obj = ii.`object`
                        if (pos == ii.position && !ii.scrolling && obj != null) {
                            mItems.removeAt(itemIndex)
                            adapter.destroyItem(this, pos, obj)
                            if (DEBUG) {
                                Log.i(
                                    TAG,
                                    "populate() - destroyItem() with pos: $pos view: ${obj as? View}"
                                )
                            }
                            ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                        }
                    } else if (ii != null && pos == ii.position) {
                        extraWidthRight += ii.widthFactor
                        itemIndex++
                        ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    } else {
                        ii = addNewItem(pos, itemIndex)
                        itemIndex++
                        extraWidthRight += ii.widthFactor
                        ii = if (itemIndex < mItems.size) mItems[itemIndex] else null
                    }
                }
            }

            calculatePageOffsets(curItem, curIndex, oldCurInfo)

            val curObj = curItem.`object`
            if (curObj != null) {
                adapter.setPrimaryItem(this, mCurItem, curObj)
            }
        }

        if (DEBUG) {
            Log.i(TAG, "Current page list:")
            for (i in mItems.indices) {
                Log.i(TAG, "#$i: page ${mItems[i].position}")
            }
        }

        adapter.finishUpdate(this)

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null) {
                val lp = child.layoutParams as LayoutParams
                lp.childIndex = i
                if (!lp.isDecor && lp.widthFactor == 0f) {
                    val ii = infoForChild(child)
                    if (ii != null) {
                        lp.widthFactor = ii.widthFactor
                        lp.position = ii.position
                    }
                }
            }
        }
        sortChildDrawingOrder()

        if (hasFocus()) {
            val currentFocused = findFocus()
            var ii = if (currentFocused != null) infoForAnyChild(currentFocused) else null
            if (ii == null || ii.position != mCurItem) {
                for (i in 0 until childCount) {
                    val child = getChildAt(i)
                    if (child != null) {
                        ii = infoForChild(child)
                        if (ii != null && ii.position == mCurItem) {
                            if (child.requestFocus(FOCUS_FORWARD)) {
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sortChildDrawingOrder() {
        if (mDrawingOrder != DRAW_ORDER_DEFAULT) {
            var orderedChildren = mDrawingOrderedChildren
            if (orderedChildren == null) {
                orderedChildren = ArrayList()
                mDrawingOrderedChildren = orderedChildren
            } else {
                orderedChildren.clear()
            }
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child != null) {
                    orderedChildren.add(child)
                }
            }
            orderedChildren.sortWith(sPositionComparator)
        }
    }

    private fun triggerOnPageChangeEvent(position: Int) {
        val listeners = mOnPageChangeListeners
        if (listeners != null) {
            val currentAdapter = mAdapter
            for (eachListener in listeners) {
                if (eachListener != null) {
                    if (currentAdapter is InfinitePagerAdapter) {
                        val n = currentAdapter.getRealPosition(position)
                        eachListener.onPageSelected(n)
                    } else {
                        eachListener.onPageSelected(position)
                    }
                }
            }
        }
        mInternalPageChangeListener?.onPageSelected(position)
    }

    private fun calculatePageOffsets(curItem: ItemInfo, curIndex: Int, oldCurInfo: ItemInfo?) {
        val adapter = mAdapter ?: return
        val N = adapter.count
        val width = this.clientWidth
        val marginOffset = if (width > 0) mPageMargin.toFloat() / width else 0f

        if (oldCurInfo != null) {
            val oldCurPosition = oldCurInfo.position
            if (oldCurPosition < curItem.position) {
                var itemIndex = 0
                var offset = oldCurInfo.offset + oldCurInfo.widthFactor + marginOffset
                var pos = oldCurPosition + 1
                while (pos <= curItem.position && itemIndex < mItems.size) {
                    var ii = mItems[itemIndex]
                    while (pos > ii.position && itemIndex < mItems.size - 1) {
                        itemIndex++
                        ii = mItems[itemIndex]
                    }
                    while (pos < ii.position) {
                        offset += adapter.getPageWidth(pos) + marginOffset
                        pos++
                    }
                    ii.offset = offset
                    offset += ii.widthFactor + marginOffset
                    pos++
                }
            } else if (oldCurPosition > curItem.position) {
                var itemIndex = mItems.size - 1
                var offset = oldCurInfo.offset
                var pos = oldCurPosition - 1
                while (pos >= curItem.position && itemIndex >= 0) {
                    var ii = mItems[itemIndex]
                    while (pos < ii.position && itemIndex > 0) {
                        itemIndex--
                        ii = mItems[itemIndex]
                    }
                    while (pos > ii.position) {
                        offset -= adapter.getPageWidth(pos) + marginOffset
                        pos--
                    }
                    offset -= ii.widthFactor + marginOffset
                    ii.offset = offset
                    pos--
                }
            }
        }

        val itemCount = mItems.size
        var offset = curItem.offset
        var pos = curItem.position - 1
        mFirstOffset = if (curItem.position == 0) curItem.offset else -Float.MAX_VALUE
        mLastOffset =
            if (curItem.position == N - 1) curItem.offset + curItem.widthFactor - 1 else Float.MAX_VALUE

        var prevIndex = curIndex - 1
        while (prevIndex >= 0) {
            val ii = mItems[prevIndex]
            while (pos > ii.position) {
                offset -= adapter.getPageWidth(pos--) + marginOffset
            }
            offset -= ii.widthFactor + marginOffset
            ii.offset = offset
            if (ii.position == 0) mFirstOffset = offset
            prevIndex--
            pos--
        }

        offset = curItem.offset + curItem.widthFactor + marginOffset
        pos = curItem.position + 1

        var nextIndex = curIndex + 1
        while (nextIndex < itemCount) {
            val ii = mItems[nextIndex]
            while (pos < ii.position) {
                offset += adapter.getPageWidth(pos++) + marginOffset
            }
            if (ii.position == N - 1) {
                mLastOffset = offset + ii.widthFactor - 1
            }
            ii.offset = offset
            offset += ii.widthFactor + marginOffset
            nextIndex++
            pos++
        }

        mNeedCalculatePageOffsets = false
    }

    class SavedState : AbsSavedState {
        var position: Int = 0
        var adapterState: Parcelable? = null
        var loader: ClassLoader? = null

        constructor(superState: Parcelable) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(position)
            out.writeParcelable(adapterState, flags)
        }

        override fun toString(): String {
            return "FragmentPager.SavedState{${Integer.toHexString(System.identityHashCode(this))} position=$position}"
        }

        internal constructor(`in`: Parcel, loader: ClassLoader?) : super(`in`, loader) {
            val currentLoader = loader ?: javaClass.classLoader
            position = `in`.readInt()
            adapterState = `in`.readParcelable(currentLoader)
            this.loader = currentLoader
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> =
                object : ClassLoaderCreator<SavedState?> {
                    override fun createFromParcel(`in`: Parcel, loader: ClassLoader?): SavedState {
                        return SavedState(`in`, loader)
                    }

                    override fun createFromParcel(`in`: Parcel): SavedState {
                        return SavedState(`in`, null)
                    }

                    override fun newArray(size: Int): Array<SavedState?> {
                        return arrayOfNulls(size)
                    }
                }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState() ?: Bundle()
        val ss = SavedState(superState)
        ss.position = mCurItem
        ss.adapterState = mAdapter?.saveState()
        return ss
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        if (mAdapter != null) {
            mAdapter?.restoreState(state.adapterState, state.loader)
            setCurrentItemInternal(state.position, false, true)
        } else {
            mRestoredCurItem = state.position
            mRestoredAdapterState = state.adapterState
            mRestoredClassLoader = state.loader
        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams?) {
        var finalParams = params
        if (!checkLayoutParams(finalParams)) {
            finalParams = generateLayoutParams(finalParams)
        }
        val lp = finalParams as LayoutParams
        lp.isDecor = lp.isDecor or isDecorView(child)
        if (mInLayout) {
            check(!lp.isDecor) { "Cannot add pager decor view during layout" }
            lp.needsMeasure = true
            addViewInLayout(child, index, finalParams)
        } else {
            super.addView(child, index, finalParams)
        }

        if (USE_CACHE) {
            child.isDrawingCacheEnabled = child.visibility != GONE
        }
    }

    override fun removeView(view: View?) {
        if (mInLayout) {
            removeViewInLayout(view)
        } else {
            super.removeView(view)
        }
    }

    fun infoForChild(child: View): ItemInfo? {
        val adapter = mAdapter ?: return null
        for (i in mItems.indices) {
            val ii = mItems[i]
            val obj = ii.`object`
            if (obj != null && adapter.isViewFromObject(child, obj)) {
                return ii
            }
        }
        return null
    }

    fun infoForAnyChild(child: View): ItemInfo? {
        var currentChild = child
        var parent = currentChild.parent
        while (parent !== this) {
            if (parent == null || parent !is View) {
                return null
            }
            currentChild = parent
            parent = currentChild.parent
        }
        return infoForChild(currentChild)
    }

    fun infoForPosition(position: Int): ItemInfo? {
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.position == position) {
                return ii
            }
        }
        return null
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec)
        )

        val measuredWidthVal = measuredWidth
        val maxGutterSize = measuredWidthVal / 10
        mGutterSize = kotlin.math.min(maxGutterSize, mDefaultGutterSize)

        var childWidthSize = measuredWidthVal - paddingLeft - paddingRight
        var childHeightSize = measuredHeight - paddingTop - paddingBottom

        var size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child != null && child.visibility != GONE) {
                val lp = child.layoutParams as? LayoutParams
                if (lp != null && lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    var widthMode = MeasureSpec.AT_MOST
                    var heightMode = MeasureSpec.AT_MOST
                    val consumeVertical = vgrav == Gravity.TOP || vgrav == Gravity.BOTTOM
                    val consumeHorizontal = hgrav == Gravity.LEFT || hgrav == Gravity.RIGHT

                    if (consumeVertical) {
                        widthMode = MeasureSpec.EXACTLY
                    } else if (consumeHorizontal) {
                        heightMode = MeasureSpec.EXACTLY
                    }

                    var widthSize = childWidthSize
                    var heightSize = childHeightSize
                    if (lp.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        widthMode = MeasureSpec.EXACTLY
                        if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                            widthSize = lp.width
                        }
                    }
                    if (lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        heightMode = MeasureSpec.EXACTLY
                        if (lp.height != ViewGroup.LayoutParams.MATCH_PARENT) {
                            heightSize = lp.height
                        }
                    }
                    val widthSpec = MeasureSpec.makeMeasureSpec(widthSize, widthMode)
                    val heightSpec = MeasureSpec.makeMeasureSpec(heightSize, heightMode)
                    child.measure(widthSpec, heightSpec)

                    if (consumeVertical) {
                        childHeightSize -= child.measuredHeight
                    } else if (consumeHorizontal) {
                        childWidthSize -= child.measuredWidth
                    }
                }
            }
        }

        mChildWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY)
        mChildHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY)

        mInLayout = true
        populate()
        mInLayout = false

        size = childCount
        for (i in 0 until size) {
            val child = getChildAt(i)
            if (child != null && child.visibility != GONE) {
                if (DEBUG) {
                    Log.v(TAG, "Measuring #$i $child: $mChildWidthMeasureSpec")
                }

                val lp = child.layoutParams as? LayoutParams
                if (lp == null || !lp.isDecor) {
                    val widthSpec = MeasureSpec.makeMeasureSpec(
                        (childWidthSize * (lp?.widthFactor ?: 1f)).toInt(), MeasureSpec.EXACTLY
                    )
                    child.measure(widthSpec, mChildHeightMeasureSpec)
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w != oldw) {
            recomputeScrollPosition(w, oldw, mPageMargin, mPageMargin)
        }
    }

    private fun recomputeScrollPosition(width: Int, oldWidth: Int, margin: Int, oldMargin: Int) {
        val scroller = mScroller
        if (oldWidth > 0 && mItems.isNotEmpty()) {
            if (scroller != null && !scroller.isFinished) {
                scroller.finalX = this.currentItem * this.clientWidth
            } else {
                val widthWithMargin = width - paddingLeft - paddingRight + margin
                val oldWidthWithMargin = (oldWidth - paddingLeft - paddingRight + oldMargin)
                val xpos = scrollX
                val pageOffset = xpos.toFloat() / oldWidthWithMargin
                val newOffsetPixels = (pageOffset * widthWithMargin).toInt()

                scrollTo(newOffsetPixels, scrollY)
            }
        } else {
            val ii = infoForPosition(mCurItem)
            val scrollOffset = if (ii != null) kotlin.math.min(ii.offset, mLastOffset) else 0f
            val scrollPos = (scrollOffset * (width - paddingLeft - paddingRight)).toInt()
            if (scrollPos != scrollX) {
                completeScroll(false)
                scrollTo(scrollPos, scrollY)
            }
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        val height = b - t
        var currentPaddingLeft = paddingLeft
        var currentPaddingTop = paddingTop
        var currentPaddingRight = paddingRight
        var currentPaddingBottom = paddingBottom
        val currentScrollX = scrollX

        var decorCount = 0

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null && child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                var childLeft = 0
                var childTop = 0
                if (lp.isDecor) {
                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    val vgrav = lp.gravity and Gravity.VERTICAL_GRAVITY_MASK
                    when (hgrav) {
                        Gravity.LEFT -> {
                            childLeft = currentPaddingLeft
                            currentPaddingLeft += child.measuredWidth
                        }

                        Gravity.CENTER_HORIZONTAL -> childLeft = kotlin.math.max(
                            (width - child.measuredWidth) / 2, currentPaddingLeft
                        )

                        Gravity.RIGHT -> {
                            childLeft = width - currentPaddingRight - child.measuredWidth
                            currentPaddingRight += child.measuredWidth
                        }

                        else -> childLeft = currentPaddingLeft
                    }
                    when (vgrav) {
                        Gravity.TOP -> {
                            childTop = currentPaddingTop
                            currentPaddingTop += child.measuredHeight
                        }

                        Gravity.CENTER_VERTICAL -> childTop = kotlin.math.max(
                            (height - child.measuredHeight) / 2, currentPaddingTop
                        )

                        Gravity.BOTTOM -> {
                            childTop = height - currentPaddingBottom - child.measuredHeight
                            currentPaddingBottom += child.measuredHeight
                        }

                        else -> childTop = currentPaddingTop
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

        val childWidth = width - currentPaddingLeft - currentPaddingRight
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null && child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                val ii = if (!lp.isDecor) infoForChild(child) else null
                if (ii != null) {
                    val loff = (childWidth * ii.offset).toInt()
                    val childLeft = currentPaddingLeft + loff
                    val childTop = currentPaddingTop
                    if (lp.needsMeasure) {
                        lp.needsMeasure = false
                        val widthSpec = MeasureSpec.makeMeasureSpec(
                            (childWidth * lp.widthFactor).toInt(), MeasureSpec.EXACTLY
                        )
                        val heightSpec = MeasureSpec.makeMeasureSpec(
                            (height - currentPaddingTop - currentPaddingBottom), MeasureSpec.EXACTLY
                        )
                        child.measure(widthSpec, heightSpec)
                    }
                    if (DEBUG) {
                        Log.v(
                            TAG,
                            "Positioning #$i $child f=${ii.`object`}:$childLeft,$childTop ${child.measuredWidth}x${child.measuredHeight}"
                        )
                    }
                    child.layout(
                        childLeft,
                        childTop,
                        childLeft + child.measuredWidth,
                        childTop + child.measuredHeight
                    )
                }
            }
        }
        mTopPageBounds = currentPaddingTop
        mBottomPageBounds = height - currentPaddingBottom
        mDecorChildCount = decorCount

        if (mFirstLayout) {
            scrollToItem(mCurItem, false, 0, false)
        }
        mFirstLayout = false
    }

    override fun computeScroll() {
        mIsScrollStarted = true
        if (!mScroller!!.isFinished() && mScroller!!.computeScrollOffset()) {
            val oldX = getScrollX()
            val oldY = scrollY
            val scroller = mScroller
            if (scroller != null) {
                val x = scroller.currX
                val y = scroller.currY

                if (oldX != x || oldY != y) {
                    scrollTo(x, y)
                    if (!pageScrolled(x)) {
                        scroller.abortAnimation()
                        scrollTo(0, y)
                    }
                }
            }

            ViewCompat.postInvalidateOnAnimation(this)
            return
        }

        completeScroll(true)
    }

    private fun pageScrolled(xpos: Int): Boolean {
        if (mItems.isEmpty()) {
            if (mFirstLayout) {
                return false
            }
            mCalledSuper = false
            onPageScrolled(0, 0f, 0)
            check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
            return false
        }
        val ii = infoForCurrentScrollPosition() ?: return false
        val width = this.clientWidth
        val widthWithMargin = width + mPageMargin
        val marginOffset = mPageMargin.toFloat() / width
        val currentPage = ii.position
        val pageOffset = (((xpos.toFloat() / width) - ii.offset) / (ii.widthFactor + marginOffset))
        val offsetPixels = (pageOffset * widthWithMargin).toInt()

        mCalledSuper = false
        onPageScrolled(currentPage, pageOffset, offsetPixels)
        check(mCalledSuper) { "onPageScrolled did not call superclass implementation" }
        return true
    }

    @CallSuper
    protected fun onPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        if (mDecorChildCount > 0) {
            val currentScrollX = scrollX
            var currentPaddingLeft = paddingLeft
            var currentPaddingRight = paddingRight
            val currentWidth = width
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child != null) {
                    val lp = child.layoutParams as LayoutParams
                    if (!lp.isDecor) continue

                    val hgrav = lp.gravity and Gravity.HORIZONTAL_GRAVITY_MASK
                    var childLeft = 0
                    when (hgrav) {
                        Gravity.LEFT -> {
                            childLeft = currentPaddingLeft
                            currentPaddingLeft += child.width
                        }

                        Gravity.CENTER_HORIZONTAL -> childLeft = kotlin.math.max(
                            (currentWidth - child.measuredWidth) / 2, currentPaddingLeft
                        )

                        Gravity.RIGHT -> {
                            childLeft = currentWidth - currentPaddingRight - child.measuredWidth
                            currentPaddingRight += child.measuredWidth
                        }

                        else -> childLeft = currentPaddingLeft
                    }
                    childLeft += currentScrollX

                    val childOffset = childLeft - child.left
                    if (childOffset != 0) {
                        child.offsetLeftAndRight(childOffset)
                    }
                }
            }
        }

        dispatchOnPageScrolled(position, offset, offsetPixels)

        val transformer = mPageTransformer
        if (transformer != null) {
            val currentScrollX = scrollX
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child != null) {
                    val lp = child.layoutParams as LayoutParams

                    if (lp.isDecor) continue
                    val transformPos = (child.left - currentScrollX).toFloat() / this.clientWidth
                    transformer.transformPage(child, transformPos)
                }
            }
        }

        mCalledSuper = true
    }

    private fun dispatchOnPageScrolled(position: Int, offset: Float, offsetPixels: Int) {
        mOnPageChangeListener?.onPageScrolled(position, offset, offsetPixels)
        val listeners = mOnPageChangeListeners
        if (listeners != null) {
            var i = 0
            val z = listeners.size
            while (i < z) {
                listeners[i]?.onPageScrolled(position, offset, offsetPixels)
                i++
            }
        }
        mInternalPageChangeListener?.onPageScrolled(position, offset, offsetPixels)
    }

    private fun dispatchOnPageSelected(position: Int) {
        mOnPageChangeListener?.onPageSelected(position)
        val listeners = mOnPageChangeListeners
        if (listeners != null) {
            var i = 0
            val z = listeners.size
            while (i < z) {
                listeners[i]?.onPageSelected(position)
                i++
            }
        }
        mInternalPageChangeListener?.onPageSelected(position)
    }

    private fun dispatchOnScrollStateChanged(state: Int) {
        mOnPageChangeListener?.onPageScrollStateChanged(state)
        val listeners = mOnPageChangeListeners
        if (listeners != null) {
            var i = 0
            val z = listeners.size
            while (i < z) {
                listeners[i]?.onPageScrollStateChanged(state)
                i++
            }
        }
        mInternalPageChangeListener?.onPageScrollStateChanged(state)
    }

    private fun completeScroll(postEvents: Boolean) {
        var needPopulate = mScrollState == SCROLL_STATE_SETTLING
        if (needPopulate) {
            setScrollingCacheEnabled(false)
            val scroller = mScroller
            val wasScrolling = scroller != null && !scroller.isFinished
            if (wasScrolling && scroller != null) {
                scroller.abortAnimation()
                val oldX = scrollX
                val oldY = scrollY
                val x = scroller.currX
                val y = scroller.currY
                if (oldX != x || oldY != y) {
                    scrollTo(x, y)
                    if (x != oldX) {
                        pageScrolled(x)
                    }
                }
            }
        }
        mPopulatePending = false
        for (i in mItems.indices) {
            val ii = mItems[i]
            if (ii.scrolling) {
                needPopulate = true
                ii.scrolling = false
            }
        }
        if (needPopulate) {
            if (postEvents) {
                ViewCompat.postOnAnimation(this, mEndScrollRunnable)
            } else {
                mEndScrollRunnable.run()
            }
        }
    }

    private fun isGutterDrag(x: Float, dx: Float): Boolean {
        return (x < mGutterSize && dx > 0) || (x > width - mGutterSize && dx < 0)
    }

    private fun enableLayers(enable: Boolean) {
        val count = childCount
        for (i in 0 until count) {
            val layerType = if (enable) mPageTransformerLayerType else LAYER_TYPE_NONE
            getChildAt(i)?.setLayerType(layerType, null)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            val action = ev.action and MotionEvent.ACTION_MASK

            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                if (DEBUG) Log.v(TAG, "Intercept done!")
                resetTouch()
                return false
            }

            if (action != MotionEvent.ACTION_DOWN) {
                if (mIsBeingDragged) {
                    if (DEBUG) Log.v(TAG, "Intercept returning true!")
                    return true
                }
                if (mIsUnableToDrag) {
                    if (DEBUG) Log.v(TAG, "Intercept returning false!")
                    return false
                }
            }

            when (action) {
                MotionEvent.ACTION_MOVE -> {
                    val activePointerId = mActivePointerId
                    if (activePointerId != INVALID_POINTER) {
                        val pointerIndex = ev.findPointerIndex(activePointerId)
                        if (pointerIndex in 0 until ev.pointerCount) {
                            val x = ev.getX(pointerIndex)
                            val dx = x - mLastMotionX
                            val xDiff = kotlin.math.abs(dx)
                            val y = ev.getY(pointerIndex)
                            val yDiff = kotlin.math.abs(y - mInitialMotionY)
                            if (DEBUG) Log.v(TAG, "Moved x to $x,$y diff=$xDiff,$yDiff")

                            if (dx != 0f && !isGutterDrag(mLastMotionX, dx) && canScroll(
                                    this, false, dx.toInt(), x.toInt(), y.toInt()
                                )
                            ) {
                                mLastMotionX = x
                                mLastMotionY = y
                                mIsUnableToDrag = true
                                return false
                            }
                            if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
                                if (DEBUG) Log.v(TAG, "Starting drag!")
                                mIsBeingDragged = true
                                requestParentDisallowInterceptTouchEvent(true)
                                setScrollState(SCROLL_STATE_DRAGGING)
                                mLastMotionX =
                                    if (dx > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                                mLastMotionY = y
                                setScrollingCacheEnabled(true)
                            } else if (yDiff > mTouchSlop) {
                                if (DEBUG) Log.v(TAG, "Starting unable to drag!")
                                mIsUnableToDrag = true
                            }
                            if (mIsBeingDragged) {
                                if (performDrag(x)) {
                                    ViewCompat.postInvalidateOnAnimation(this)
                                }
                            }
                        }
                    }
                }

                MotionEvent.ACTION_DOWN -> {
                    val initialX = ev.x
                    mInitialMotionX = initialX
                    mLastMotionX = initialX
                    val initialY = ev.y
                    mInitialMotionY = initialY
                    mLastMotionY = initialY
                    mActivePointerId = ev.getPointerId(0)
                    mIsUnableToDrag = false

                    mIsScrollStarted = true
                    val scroller = mScroller
                    if (scroller != null) {
                        scroller.computeScrollOffset()
                        if (mScrollState == SCROLL_STATE_SETTLING && kotlin.math.abs(scroller.finalX - scroller.currX) > mCloseEnough) {
                            scroller.abortAnimation()
                            mPopulatePending = false
                            populate()
                            mIsBeingDragged = true
                            requestParentDisallowInterceptTouchEvent(true)
                            setScrollState(SCROLL_STATE_DRAGGING)
                        } else {
                            completeScroll(false)
                            mIsBeingDragged = false
                        }
                    } else {
                        completeScroll(false)
                        mIsBeingDragged = false
                    }

                    if (DEBUG) {
                        Log.v(
                            TAG,
                            "Down at $mLastMotionX,$mLastMotionY mIsBeingDragged=$mIsBeingDragged mIsUnableToDrag=$mIsUnableToDrag"
                        )
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> onSecondaryPointerUp(ev)
            }

            var tracker = mVelocityTracker
            if (tracker == null) {
                tracker = VelocityTracker.obtain()
                mVelocityTracker = tracker
            }
            tracker?.addMovement(ev)

            return mIsBeingDragged
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (this.isFakeDragging) {
            return true
        }

        if (ev.action == MotionEvent.ACTION_DOWN && ev.edgeFlags != 0) {
            return false
        }

        val adapter = mAdapter
        if (adapter == null || adapter.count == 0) {
            return false
        }

        var tracker = mVelocityTracker
        if (tracker == null) {
            tracker = VelocityTracker.obtain()
            mVelocityTracker = tracker
        }
        tracker?.addMovement(ev)

        val action = ev.action
        var needsInvalidate = false

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                mScroller?.abortAnimation()
                mPopulatePending = false
                populate()

                val initialX = ev.x
                mInitialMotionX = initialX
                mLastMotionX = initialX
                val initialY = ev.y
                mInitialMotionY = initialY
                mLastMotionY = initialY
                mActivePointerId = ev.getPointerId(0)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mIsBeingDragged) {
                    val pointerIndex = ev.findPointerIndex(mActivePointerId)
                    if (pointerIndex == -1) {
                        needsInvalidate = resetTouch()
                        return true
                    }
                    val x = ev.getX(pointerIndex)
                    val xDiff = kotlin.math.abs(x - mLastMotionX)
                    val y = ev.getY(pointerIndex)
                    val yDiff = kotlin.math.abs(y - mLastMotionY)
                    if (DEBUG) {
                        Log.v(TAG, "Moved x to $x,$y diff=$xDiff,$yDiff")
                    }
                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        if (DEBUG) Log.v(TAG, "Starting drag!")
                        mIsBeingDragged = true
                        requestParentDisallowInterceptTouchEvent(true)
                        mLastMotionX =
                            if (x - mInitialMotionX > 0) mInitialMotionX + mTouchSlop else mInitialMotionX - mTouchSlop
                        mLastMotionY = y
                        setScrollState(SCROLL_STATE_DRAGGING)
                        setScrollingCacheEnabled(true)

                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                if (mIsBeingDragged) {
                    val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                    if (activePointerIndex in 0 until ev.pointerCount) {
                        val x = ev.getX(activePointerIndex)
                        needsInvalidate = needsInvalidate or performDrag(x)
                    }
                }
            }

            MotionEvent.ACTION_UP -> if (mIsBeingDragged) {
                val velocityTracker = mVelocityTracker
                if (velocityTracker != null) {
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialVelocity = velocityTracker.getXVelocity(mActivePointerId).toInt()
                    mPopulatePending = true
                    val width = this.clientWidth
                    val currentScrollX = scrollX
                    val ii = infoForCurrentScrollPosition()
                    if (ii != null && width > 0) {
                        val marginOffset = mPageMargin.toFloat() / width
                        val currentPage = ii.position
                        val pageOffset =
                            (((currentScrollX.toFloat() / width) - ii.offset) / (ii.widthFactor + marginOffset))
                        val activePointerIndex = ev.findPointerIndex(mActivePointerId)
                        if (activePointerIndex in 0 until ev.pointerCount) {
                            val x = ev.getX(activePointerIndex)
                            val totalDelta = (x - mInitialMotionX).toInt()
                            val nextPage = determineTargetPage(
                                currentPage, pageOffset, initialVelocity, totalDelta
                            )
                            setCurrentItemInternal(nextPage, true, true, initialVelocity)
                        }
                    }
                }
                needsInvalidate = resetTouch()
            }

            MotionEvent.ACTION_CANCEL -> if (mIsBeingDragged) {
                scrollToItem(mCurItem, true, 0, false)
                needsInvalidate = resetTouch()
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = ev.actionIndex
                mLastMotionX = ev.getX(index)
                mActivePointerId = ev.getPointerId(index)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(ev)
                val pointerIndex = ev.findPointerIndex(mActivePointerId)
                if (pointerIndex in 0 until ev.pointerCount) {
                    mLastMotionX = ev.getX(pointerIndex)
                }
            }
        }
        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
        return true
    }

    private fun resetTouch(): Boolean {
        mActivePointerId = INVALID_POINTER
        endDrag()
        mLeftEdge?.onRelease()
        mRightEdge?.onRelease()
        val needsInvalidate: Boolean =
            mLeftEdge?.isFinished == true || mRightEdge?.isFinished == true
        return needsInvalidate
    }

    private fun requestParentDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private fun performDrag(x: Float): Boolean {
        var needsInvalidate = false

        val deltaX = mLastMotionX - x
        mLastMotionX = x

        val oldScrollX = scrollX.toFloat()
        var scrollXVal = oldScrollX + deltaX
        val width = this.clientWidth

        var leftBound = width * mFirstOffset
        var rightBound = width * mLastOffset
        var leftAbsolute = true
        var rightAbsolute = true

        if (mItems.isNotEmpty()) {
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                leftAbsolute = false
                leftBound = firstItem.offset * width
            }
            val adapterCount = mAdapter?.count ?: 0
            if (lastItem.position != adapterCount - 1) {
                rightAbsolute = false
                rightBound = lastItem.offset * width
            }

            if (scrollXVal < leftBound) {
                if (leftAbsolute) {
                    val over = leftBound - scrollXVal
                    mLeftEdge?.onPull(abs(over) / width)
                    needsInvalidate = true
                }
                scrollXVal = leftBound
            } else if (scrollXVal > rightBound) {
                if (rightAbsolute) {
                    val over = scrollXVal - rightBound
                    mRightEdge?.onPull(abs(over) / width)
                    needsInvalidate = true
                }
                scrollXVal = rightBound
            }
        }
        mLastMotionX += scrollXVal - scrollXVal.toInt()
        scrollTo(scrollXVal.toInt(), scrollY)
        pageScrolled(scrollXVal.toInt())

        return needsInvalidate
    }

    private fun infoForCurrentScrollPosition(): ItemInfo? {
        val width = this.clientWidth
        val scrollOffset = if (width > 0) scrollX.toFloat() / width else 0f
        val marginOffset = if (width > 0) mPageMargin.toFloat() / width else 0f
        var lastPos = -1
        var lastOffset = 0f
        var lastWidth = 0f
        var first = true

        var lastItem: ItemInfo? = null
        var i = 0
        while (i < mItems.size) {
            var ii = mItems[i]
            val offset: Float
            if (!first && ii.position != lastPos + 1) {
                ii = mTempItem
                ii.offset = lastOffset + lastWidth + marginOffset
                ii.position = lastPos + 1
                ii.widthFactor = mAdapter?.getPageWidth(ii.position) ?: 1f
                i--
            }
            offset = ii.offset

            val leftBound = offset
            val rightBound = offset + ii.widthFactor + marginOffset
            if (first || scrollOffset >= leftBound) {
                if (scrollOffset < rightBound || i == mItems.size - 1) {
                    return ii
                }
            } else {
                return lastItem
            }
            first = false
            lastPos = ii.position
            lastOffset = offset
            lastWidth = ii.widthFactor
            lastItem = ii
            i++
        }

        return lastItem
    }

    private fun determineTargetPage(
        currentPage: Int, pageOffset: Float, velocity: Int, deltaX: Int
    ): Int {
        var targetPage: Int
        if (abs(deltaX) > mFlingDistance && abs(velocity) > mMinimumVelocity) {
            targetPage = if (velocity > 0) currentPage else currentPage + 1
        } else {
            val truncator = if (currentPage >= mCurItem) 0.4f else 0.6f
            targetPage = currentPage + (pageOffset + truncator).toInt()
        }

        if (mItems.isNotEmpty()) {
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]

            targetPage = max(firstItem.position, min(targetPage, lastItem.position))
        }

        return targetPage
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        var needsInvalidate = false

        val overScrollMode = overScrollMode
        val adapter = mAdapter
        if (overScrollMode == OVER_SCROLL_ALWAYS || (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && adapter != null && adapter.count > 1)) {
            val leftEdge = mLeftEdge
            if (leftEdge != null && !leftEdge.isFinished) {
                canvas.withSave {
                    val height = height - paddingTop - paddingBottom
                    val width = width

                    canvas.rotate(270f)
                    canvas.translate((-height + paddingTop).toFloat(), mFirstOffset * width)
                    leftEdge.setSize(height, width)
                    needsInvalidate = false or leftEdge.draw(canvas)
                }
            }
            val rightEdge = mRightEdge
            if (rightEdge != null && !rightEdge.isFinished) {
                canvas.withSave {
                    val width = width
                    val height = height - paddingTop - paddingBottom

                    canvas.rotate(90f)
                    canvas.translate(-paddingTop.toFloat(), -(mLastOffset + 1) * width)
                    rightEdge.setSize(height, width)
                    needsInvalidate = needsInvalidate or rightEdge.draw(canvas)
                }
            }
        } else {
            mLeftEdge?.finish()
            mRightEdge?.finish()
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val adapter = mAdapter
        val marginDrawable = mMarginDrawable
        if (mPageMargin > 0 && marginDrawable != null && mItems.size > 0 && adapter != null) {
            val currentScrollX = scrollX
            val currentWidth = width

            val marginOffset = mPageMargin.toFloat() / currentWidth
            var itemIndex = 0
            var ii = mItems[0]
            var offset = ii.offset
            val itemCount = mItems.size
            val firstPos = ii.position
            val lastPos = mItems[itemCount - 1].position
            for (pos in firstPos until lastPos) {
                while (pos > ii.position && itemIndex < itemCount - 1) {
                    ii = mItems[++itemIndex]
                }

                val drawAt: Float
                if (pos == ii.position) {
                    drawAt = (ii.offset + ii.widthFactor) * currentWidth
                    offset = ii.offset + ii.widthFactor + marginOffset
                } else {
                    val widthFactor = adapter.getPageWidth(pos)
                    drawAt = (offset + widthFactor) * currentWidth
                    offset += widthFactor + marginOffset
                }

                if (drawAt + mPageMargin > currentScrollX) {
                    marginDrawable.setBounds(
                        kotlin.math.round(drawAt).toInt(),
                        mTopPageBounds,
                        kotlin.math.round(drawAt + mPageMargin).toInt(),
                        mBottomPageBounds
                    )
                    marginDrawable.draw(canvas)
                }

                if (drawAt > currentScrollX + currentWidth) {
                    break
                }
            }
        }
    }

    fun beginFakeDrag(): Boolean {
        if (mIsBeingDragged) {
            return false
        }
        this.isFakeDragging = true
        setScrollState(SCROLL_STATE_DRAGGING)
        mLastMotionX = 0f
        mInitialMotionX = mLastMotionX
        var tracker = mVelocityTracker
        if (tracker == null) {
            tracker = VelocityTracker.obtain()
            mVelocityTracker = tracker
        } else {
            tracker.clear()
        }
        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        mVelocityTracker?.addMovement(ev)
        ev.recycle()
        mFakeDragBeginTime = time
        return true
    }

    fun endFakeDrag() {
        check(this.isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }

        if (mAdapter != null) {
            val velocityTracker = mVelocityTracker
            if (velocityTracker != null) {
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = velocityTracker.getXVelocity(mActivePointerId).toInt()
                mPopulatePending = true
                val width = this.clientWidth
                val currentScrollX = scrollX
                val ii = infoForCurrentScrollPosition()
                if (ii != null) {
                    val currentPage = ii.position
                    val pageOffset =
                        ((currentScrollX.toFloat() / width) - ii.offset) / ii.widthFactor
                    val totalDelta = (mLastMotionX - mInitialMotionX).toInt()
                    val nextPage = determineTargetPage(
                        currentPage, pageOffset, initialVelocity, totalDelta
                    )
                    setCurrentItemInternal(nextPage, true, true, initialVelocity)
                }
            }
        }
        endDrag()

        this.isFakeDragging = false
    }

    fun fakeDragBy(xOffset: Float) {
        check(this.isFakeDragging) { "No fake drag in progress. Call beginFakeDrag first." }

        val adapter = mAdapter ?: return

        mLastMotionX += xOffset

        val oldScrollX = scrollX.toFloat()
        var scrollXVal = oldScrollX - xOffset
        val width = this.clientWidth

        var leftBound = width * mFirstOffset
        var rightBound = width * mLastOffset

        if (mItems.isNotEmpty()) {
            val firstItem = mItems[0]
            val lastItem = mItems[mItems.size - 1]
            if (firstItem.position != 0) {
                leftBound = firstItem.offset * width
            }
            if (lastItem.position != adapter.count - 1) {
                rightBound = lastItem.offset * width
            }

            if (scrollXVal < leftBound) {
                scrollXVal = leftBound
            } else if (scrollXVal > rightBound) {
                scrollXVal = rightBound
            }
        }
        mLastMotionX += scrollXVal - scrollXVal.toInt()
        scrollTo(scrollXVal.toInt(), scrollY)
        pageScrolled(scrollXVal.toInt())

        val time = SystemClock.uptimeMillis()
        val ev = MotionEvent.obtain(
            mFakeDragBeginTime, time, MotionEvent.ACTION_MOVE, mLastMotionX, 0f, 0
        )
        mVelocityTracker?.addMovement(ev)
        ev.recycle()
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = ev.actionIndex
        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mLastMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
            mVelocityTracker?.clear()
        }
    }

    private fun endDrag() {
        mIsBeingDragged = false
        mIsUnableToDrag = false

        mVelocityTracker?.recycle()
        mVelocityTracker = null
    }

    private fun setScrollingCacheEnabled(enabled: Boolean) {
        if (mScrollingCacheEnabled != enabled) {
            mScrollingCacheEnabled = enabled
            if (USE_CACHE) {
                val count = childCount
                for (i in 0 until count) {
                    val child = getChildAt(i)
                    if (child != null && child.visibility != GONE) {
                        child.isDrawingCacheEnabled = enabled
                    }
                }
            }
        }
    }

    override fun canScrollHorizontally(direction: Int): Boolean {
        if (mAdapter == null) {
            return false
        }

        val width = this.clientWidth
        val currentScrollX = scrollX
        return if (direction < 0) {
            currentScrollX > (width * mFirstOffset).toInt()
        } else if (direction > 0) {
            currentScrollX < (width * mLastOffset).toInt()
        } else {
            false
        }
    }

    protected fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val currentScrollX = v.scrollX
            val currentScrollY = v.scrollY
            val count = v.childCount
            for (i in count - 1 downTo 0) {
                val child = v.getChildAt(i)
                if (child != null && x + currentScrollX >= child.left && x + currentScrollX < child.right && y + currentScrollY >= child.top && y + currentScrollY < child.bottom && canScroll(
                        child,
                        true,
                        dx,
                        x + currentScrollX - child.left,
                        y + currentScrollY - child.top
                    )
                ) {
                    return true
                }
            }
        }

        return checkV && v.canScrollHorizontally(-dx)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        return super.dispatchKeyEvent(event) || executeKeyEvent(event)
    }

    fun executeKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> handled =
                    if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                        pageLeft()
                    } else {
                        arrowScroll(FOCUS_LEFT)
                    }

                KeyEvent.KEYCODE_DPAD_RIGHT -> handled =
                    if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                        pageRight()
                    } else {
                        arrowScroll(FOCUS_RIGHT)
                    }

                KeyEvent.KEYCODE_TAB -> if (event.hasNoModifiers()) {
                    handled = arrowScroll(FOCUS_FORWARD)
                } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                    handled = arrowScroll(FOCUS_BACKWARD)
                }
            }
        }
        return handled
    }

    fun arrowScroll(direction: Int): Boolean {
        var currentFocused = findFocus()
        if (currentFocused === this) {
            currentFocused = null
        } else if (currentFocused != null) {
            var isChild = false
            var parentView = currentFocused.parent
            while (parentView is ViewGroup) {
                if (parentView === this) {
                    isChild = true
                    break
                }
                parentView = parentView.parent
            }
            if (!isChild) {
                val sb = StringBuilder()
                sb.append(currentFocused.javaClass.simpleName)
                var currentParent = currentFocused.parent
                while (currentParent is ViewGroup) {
                    sb.append(" => ").append(currentParent.javaClass.simpleName)
                    currentParent = currentParent.parent
                }
                Log.e(
                    TAG,
                    "arrowScroll tried to find focus based on non-child current focused view $sb"
                )
                currentFocused = null
            }
        }

        var handled = false

        val nextFocused = FocusFinder.getInstance().findNextFocus(
            this, currentFocused, direction
        )
        if (nextFocused != null && nextFocused !== currentFocused) {
            if (direction == FOCUS_LEFT) {
                val nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left
                val currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left
                handled = if (currentFocused != null && nextLeft >= currLeft) {
                    pageLeft()
                } else {
                    nextFocused.requestFocus()
                }
            } else if (direction == FOCUS_RIGHT) {
                val nextLeft = getChildRectInPagerCoordinates(mTempRect, nextFocused).left
                val currLeft = getChildRectInPagerCoordinates(mTempRect, currentFocused).left
                handled = if (currentFocused != null && nextLeft <= currLeft) {
                    pageRight()
                } else {
                    nextFocused.requestFocus()
                }
            }
        } else if (direction == FOCUS_LEFT || direction == FOCUS_BACKWARD) {
            handled = pageLeft()
        } else if (direction == FOCUS_RIGHT || direction == FOCUS_FORWARD) {
            handled = pageRight()
        }
        if (handled) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction))
        }
        return handled
    }

    private fun getChildRectInPagerCoordinates(outRect: Rect?, child: View?): Rect {
        var finalRect = outRect ?: Rect()
        if (child == null) {
            finalRect.set(0, 0, 0, 0)
            return finalRect
        }
        finalRect.left = child.left
        finalRect.right = child.right
        finalRect.top = child.top
        finalRect.bottom = child.bottom

        var parent = child.parent
        while (parent is ViewGroup && parent !== this) {
            val group = parent
            finalRect.left += group.left
            finalRect.right += group.right
            finalRect.top += group.top
            finalRect.bottom += group.bottom

            parent = group.parent
        }
        return finalRect
    }

    fun pageLeft(): Boolean {
        if (mCurItem > 0) {
            setCurrentItem(mCurItem - 1, true)
            return true
        }
        return false
    }

    fun pageRight(): Boolean {
        val adapter = mAdapter
        if (adapter != null && mCurItem < (adapter.count - 1)) {
            setCurrentItem(mCurItem + 1, true)
            return true
        }
        return false
    }

    override fun addFocusables(views: ArrayList<View?>, direction: Int, focusableMode: Int) {
        val focusableCount = views.size
        val currentDescendantFocusability = descendantFocusability

        if (currentDescendantFocusability != FOCUS_BLOCK_DESCENDANTS) {
            val count = childCount
            for (i in 0 until count) {
                val child = getChildAt(i)
                if (child != null && child.visibility == VISIBLE) {
                    val ii = infoForChild(child)
                    if (ii != null && ii.position == mCurItem) {
                        child.addFocusables(views, direction, focusableMode)
                    }
                }
            }
        }

        if (currentDescendantFocusability != FOCUS_AFTER_DESCENDANTS || (focusableCount == views.size)) {
            if (!isFocusable) {
                return
            }
            if ((focusableMode and FOCUSABLES_TOUCH_MODE) == FOCUSABLES_TOUCH_MODE && isInTouchMode && !isFocusableInTouchMode) {
                return
            }
            views.add(this)
        }
    }

    override fun addTouchables(views: ArrayList<View?>?) {
        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null && child.visibility == VISIBLE) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem) {
                    child.addTouchables(views)
                }
            }
        }
    }

    override fun onRequestFocusInDescendants(
        direction: Int, previouslyFocusedRect: Rect?
    ): Boolean {
        val index: Int
        val increment: Int
        val end: Int
        val count = childCount
        if ((direction and FOCUS_FORWARD) != 0) {
            index = 0
            increment = 1
            end = count
        } else {
            index = count - 1
            increment = -1
            end = -1
        }
        var i = index
        while (i != end) {
            val child = getChildAt(i)
            if (child != null && child.isVisible) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem) {
                    if (child.requestFocus(direction, previouslyFocusedRect)) {
                        return true
                    }
                }
            }
            i += increment
        }
        return false
    }

    @SuppressLint("WrongConstant")
    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        if (event.eventType == AccessibilityEventCompat.TYPE_VIEW_SCROLLED) {
            return super.dispatchPopulateAccessibilityEvent(event)
        }

        val count = childCount
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child != null && child.isVisible) {
                val ii = infoForChild(child)
                if (ii != null && ii.position == mCurItem && child.dispatchPopulateAccessibilityEvent(
                        event
                    )
                ) {
                    return true
                }
            }
        }

        return false
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return generateDefaultLayoutParams()
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    internal inner class MyAccessibilityDelegate : AccessibilityDelegateCompat() {
        override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
            super.onInitializeAccessibilityEvent(host, event)
            event.className = SliderPager::class.java.name
            event.isScrollable = canScroll()
            val adapter = mAdapter
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED && adapter != null) {
                event.itemCount = adapter.count
                event.fromIndex = mCurItem
                event.toIndex = mCurItem
            }
        }

        override fun onInitializeAccessibilityNodeInfo(
            host: View, info: AccessibilityNodeInfoCompat
        ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = SliderPager::class.java.name
            info.isScrollable = canScroll()
            if (canScrollHorizontally(1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD)
            }
            if (canScrollHorizontally(-1)) {
                info.addAction(AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD)
            }
        }

        override fun performAccessibilityAction(host: View, action: Int, args: Bundle?): Boolean {
            if (super.performAccessibilityAction(host, action, args)) {
                return true
            }
            when (action) {
                AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD -> {
                    if (canScrollHorizontally(1)) {
                        this@SliderPager.currentItem = mCurItem + 1
                        return true
                    }
                    return false
                }

                AccessibilityNodeInfoCompat.ACTION_SCROLL_BACKWARD -> {
                    if (canScrollHorizontally(-1)) {
                        this@SliderPager.currentItem = mCurItem - 1
                        return true
                    }
                    return false
                }
            }
            return false
        }

        private fun canScroll(): Boolean {
            val adapter = mAdapter
            return adapter != null && adapter.count > 1
        }
    }

    private inner class PagerObserver : DataSetObserver() {
        override fun onChanged() {
            dataSetChanged()
        }

        override fun onInvalidated() {
            dataSetChanged()
        }
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

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, durationScrollMillis)
        }
    }

    class LayoutParams : ViewGroup.LayoutParams {
        var isDecor: Boolean = false
        var gravity: Int = 0
        var widthFactor: Float = 0f
        var needsMeasure: Boolean = false
        var position: Int = 0
        var childIndex: Int = 0

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
            if (llp.isDecor != rlp.isDecor) {
                return if (llp.isDecor) 1 else -1
            }
            return llp.position - rlp.position
        }
    }

    companion object {
        const val DEFAULT_SCROLL_DURATION: Int = 250
        private const val TAG = "SliderPager"
        private const val DEBUG = false
        private const val USE_CACHE = false
        private const val DEFAULT_OFFSCREEN_PAGES = 1
        private const val MAX_SETTLE_DURATION = 600
        private const val MIN_DISTANCE_FOR_FLING = 25
        private const val DEFAULT_GUTTER_SIZE = 16
        private const val MIN_FLING_VELOCITY = 400

        val LAYOUT_ATTRS: IntArray = intArrayOf(
            android.R.attr.layout_gravity
        )

        private val COMPARATOR = Comparator<ItemInfo?> { lhs, rhs ->
            if (lhs == null || rhs == null) 0 else lhs.position - rhs.position
        }

        private val sInterpolator = Interpolator { t ->
            val adjustedT = t - 1.0f
            adjustedT * adjustedT * adjustedT * adjustedT * adjustedT + 1.0f
        }

        private const val INVALID_POINTER = -1
        private const val CLOSE_ENOUGH = 2

        private const val DRAW_ORDER_DEFAULT = 0
        private const val DRAW_ORDER_FORWARD = 1
        private const val DRAW_ORDER_REVERSE = 2
        private val sPositionComparator = ViewPositionComparator()

        const val SCROLL_STATE_IDLE: Int = 0
        const val SCROLL_STATE_DRAGGING: Int = 1
        const val SCROLL_STATE_SETTLING: Int = 2

        private fun isDecorView(view: View): Boolean {
            val clazz = view.javaClass
            return clazz.getAnnotation(DecorView::class.java) != null
        }
    }
}