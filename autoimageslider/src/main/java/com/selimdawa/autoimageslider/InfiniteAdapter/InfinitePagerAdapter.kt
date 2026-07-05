package com.selimdawa.autoimageslider.InfiniteAdapter

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.SliderViewAdapter
import kotlin.math.max

class InfinitePagerAdapter(private val adapter: SliderViewAdapter<*>) : PagerAdapter() {

    val realCount get() = runCatching { adapter.count }.getOrDefault(0)

    override fun getCount() = if (realCount < 1) 0 else realCount * INFINITE_SCROLL_LIMIT

    fun getMiddlePosition(item: Int) = item + (max(0, realCount) * (INFINITE_SCROLL_LIMIT / 2))

    fun getRealPosition(virtualPosition: Int) =
        if (realCount > 0) virtualPosition % realCount else 0

    override fun instantiateItem(container: ViewGroup, virtualPosition: Int) =
        adapter.instantiateItem(container, getRealPosition(virtualPosition))

    override fun destroyItem(container: ViewGroup, virtualPosition: Int, `object`: Any) =
        adapter.destroyItem(container, getRealPosition(virtualPosition), `object`)

    override fun startUpdate(container: ViewGroup) = adapter.startUpdate(container)

    override fun finishUpdate(container: ViewGroup) = adapter.finishUpdate(container)

    override fun isViewFromObject(view: View, `object`: Any) =
        adapter.isViewFromObject(view, `object`)

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) =
        adapter.restoreState(bundle, classLoader)

    override fun saveState() = adapter.saveState()

    override fun getPageTitle(virtualPosition: Int) =
        adapter.getPageTitle(getRealPosition(virtualPosition))

    override fun getPageWidth(position: Int) = adapter.getPageWidth(position)

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) =
        adapter.setPrimaryItem(container, position, `object`)

    override fun unregisterDataSetObserver(observer: DataSetObserver) =
        adapter.unregisterDataSetObserver(observer)

    override fun registerDataSetObserver(observer: DataSetObserver) =
        adapter.registerDataSetObserver(observer)

    override fun getItemPosition(`object`: Any) = adapter.getItemPosition(`object`)

    companion object {
        const val INFINITE_SCROLL_LIMIT = 32400
    }
}