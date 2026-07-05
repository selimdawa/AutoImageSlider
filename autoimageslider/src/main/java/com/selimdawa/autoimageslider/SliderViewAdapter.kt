package com.selimdawa.autoimageslider

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import java.util.LinkedList
import java.util.Queue

abstract class SliderViewAdapter<VH : SliderViewAdapter.ViewHolder> : PagerAdapter() {
    private var dataSetListener: DataSetListener? = null
    private val destroyedItems: Queue<VH> = LinkedList()

    abstract class ViewHolder(val itemView: View)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val viewHolder = destroyedItems.poll() ?: onCreateViewHolder(container)
        container.addView(viewHolder.itemView)
        onBindViewHolder(viewHolder, position)
        return viewHolder
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView((`object` as VH).itemView)
        destroyedItems.add(`object`)
    }

    override fun isViewFromObject(view: View, `object`: Any) = (`object` as VH).itemView == view

    override fun getItemPosition(`object`: Any) = POSITION_NONE

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        dataSetListener?.dataSetChanged()
    }

    abstract fun onCreateViewHolder(parent: ViewGroup): VH
    abstract fun onBindViewHolder(viewHolder: VH, position: Int)

    internal fun dataSetChangedListener(listener: DataSetListener?) {
        dataSetListener = listener
    }

    internal interface DataSetListener {
        fun dataSetChanged()
    }
}