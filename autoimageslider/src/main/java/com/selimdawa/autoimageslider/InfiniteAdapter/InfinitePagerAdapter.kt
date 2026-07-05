package com.selimdawa.autoimageslider.InfiniteAdapter

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.SliderViewAdapter
import kotlin.math.max

class InfinitePagerAdapter(private val adapter: SliderViewAdapter<*>) : PagerAdapter() {

    val realCount: Int get() = runCatching { adapter.count }.getOrDefault(0)

    override fun getCount(): Int = if (realCount < 1) 0 else realCount * INFINITE_SCROLL_LIMIT

    fun getMiddlePosition(item: Int): Int = item + (max(0, realCount) * (INFINITE_SCROLL_LIMIT / 2))

    fun getRealPosition(virtualPosition: Int): Int =
        if (realCount > 0) virtualPosition % realCount else 0

    override fun instantiateItem(container: ViewGroup, virtualPosition: Int): Any {
        val pos = if (realCount < 1) 0 else getRealPosition(virtualPosition)
        return adapter.instantiateItem(container, pos)
    }

    override fun destroyItem(container: ViewGroup, virtualPosition: Int, `object`: Any) {
        val pos = if (realCount < 1) 0 else getRealPosition(virtualPosition)
        adapter.destroyItem(container, pos, `object`)
    }

    override fun startUpdate(container: ViewGroup) = adapter.startUpdate(container)

    override fun finishUpdate(container: ViewGroup) = adapter.finishUpdate(container)

    override fun isViewFromObject(view: View, `object`: Any): Boolean =
        adapter.isViewFromObject(view, `object`)

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) =
        adapter.restoreState(bundle, classLoader)

    override fun saveState(): Parcelable? = adapter.saveState()

    override fun getPageTitle(virtualPosition: Int): CharSequence? =
        adapter.getPageTitle(getRealPosition(virtualPosition))

    override fun getPageWidth(position: Int): Float = adapter.getPageWidth(position)

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) =
        adapter.setPrimaryItem(container, position, `object`)

    override fun unregisterDataSetObserver(observer: DataSetObserver) =
        adapter.unregisterDataSetObserver(observer)

    override fun registerDataSetObserver(observer: DataSetObserver) =
        adapter.registerDataSetObserver(observer)

    override fun getItemPosition(`object`: Any): Int = adapter.getItemPosition(`object`)

    companion object {
        const val INFINITE_SCROLL_LIMIT: Int = 32400
    }
}