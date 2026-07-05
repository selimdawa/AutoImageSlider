package com.selimdawa.autoimageslider.Adapter

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import kotlin.math.max

class InfinitePagerAdapter(private val adapter: SliderViewAdapter<*>) : PagerAdapter() {
    val realCount get() = runCatching { adapter.count }.getOrDefault(0)

    override fun getCount() = if (realCount < 1) 0 else realCount * INFINITE_SCROLL_LIMIT
    fun getMiddlePosition(item: Int) = item + (max(0, realCount) * (INFINITE_SCROLL_LIMIT / 2))
    fun getRealPosition(virtualPos: Int) = if (realCount > 0) virtualPos % realCount else 0

    override fun instantiateItem(container: ViewGroup, virtualPos: Int) =
        adapter.instantiateItem(container, getRealPosition(virtualPos))

    override fun destroyItem(container: ViewGroup, virtualPos: Int, obj: Any) =
        adapter.destroyItem(container, getRealPosition(virtualPos), obj)

    override fun startUpdate(container: ViewGroup) = adapter.startUpdate(container)
    override fun finishUpdate(container: ViewGroup) = adapter.finishUpdate(container)
    override fun isViewFromObject(view: View, obj: Any) = adapter.isViewFromObject(view, obj)
    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) =
        adapter.restoreState(bundle, classLoader)

    override fun saveState() = adapter.saveState()
    override fun getPageTitle(virtualPos: Int) = adapter.getPageTitle(getRealPosition(virtualPos))
    override fun getPageWidth(position: Int) = adapter.getPageWidth(position)
    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) =
        adapter.setPrimaryItem(container, position, obj)

    override fun unregisterDataSetObserver(observer: DataSetObserver) =
        adapter.unregisterDataSetObserver(observer)

    override fun registerDataSetObserver(observer: DataSetObserver) =
        adapter.registerDataSetObserver(observer)

    override fun getItemPosition(obj: Any) = adapter.getItemPosition(obj)

    companion object {
        const val INFINITE_SCROLL_LIMIT = 32400
    }
}