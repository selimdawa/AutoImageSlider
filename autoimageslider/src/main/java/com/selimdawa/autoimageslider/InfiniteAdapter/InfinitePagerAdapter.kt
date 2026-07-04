package com.selimdawa.autoimageslider.InfiniteAdapter

import android.database.DataSetObserver
import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.selimdawa.autoimageslider.SliderViewAdapter
import kotlin.math.max

class InfinitePagerAdapter(private val adapter: SliderViewAdapter<*>) : PagerAdapter() {

    val realAdapter: PagerAdapter
        get() = this.adapter

    override fun getCount(): Int {
        if (this.realCount < 1) {
            return 0
        }
        return this.realCount * INFINITE_SCROLL_LIMIT
    }

    val realCount: Int
        get() {
            return runCatching { this.realAdapter.count }.getOrDefault(0)
        }

    fun getMiddlePosition(item: Int): Int {
        val midpoint: Int = max(0, this.realCount) * (INFINITE_SCROLL_LIMIT / 2)
        return item + midpoint
    }

    override fun instantiateItem(container: ViewGroup, virtualPosition: Int): Any {
        if (this.realCount < 1) {
            return adapter.instantiateItem(container, 0)
        }
        return adapter.instantiateItem(container, getRealPosition(virtualPosition))
    }

    override fun destroyItem(container: ViewGroup, virtualPosition: Int, `object`: Any) {
        if (this.realCount < 1) {
            adapter.destroyItem(container, 0, `object`)
            return
        }
        adapter.destroyItem(container, getRealPosition(virtualPosition), `object`)
    }

    override fun startUpdate(container: ViewGroup) {
        adapter.startUpdate(container)
    }

    override fun finishUpdate(container: ViewGroup) {
        adapter.finishUpdate(container)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return adapter.isViewFromObject(view, `object`)
    }

    override fun restoreState(bundle: Parcelable?, classLoader: ClassLoader?) {
        adapter.restoreState(bundle, classLoader)
    }

    override fun saveState(): Parcelable? {
        return adapter.saveState()
    }

    override fun getPageTitle(virtualPosition: Int): CharSequence? {
        return adapter.getPageTitle(getRealPosition(virtualPosition))
    }

    override fun getPageWidth(position: Int): Float {
        return adapter.getPageWidth(position)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        adapter.setPrimaryItem(container, position, `object`)
    }

    override fun unregisterDataSetObserver(observer: DataSetObserver) {
        adapter.unregisterDataSetObserver(observer)
    }

    override fun registerDataSetObserver(observer: DataSetObserver) {
        adapter.registerDataSetObserver(observer)
    }

    // FIXED SIGNATURE: Changed type from Any? to Any to perfectly match PagerAdapter
    override fun getItemPosition(`object`: Any): Int {
        return adapter.getItemPosition(`object`)
    }

    fun getRealPosition(virtualPosition: Int): Int {
        if (this.realCount > 0) {
            return virtualPosition % this.realCount
        }
        return 0
    }

    companion object {
        const val INFINITE_SCROLL_LIMIT: Int = 32400
    }
}