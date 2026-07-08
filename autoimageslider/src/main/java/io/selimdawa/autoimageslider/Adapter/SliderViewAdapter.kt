package io.selimdawa.autoimageslider.Adapter

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

    @Suppress("UNCHECKED_CAST")
    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView((obj as VH).itemView)
        destroyedItems.add(obj)
    }

    @Suppress("UNCHECKED_CAST")
    override fun isViewFromObject(view: View, obj: Any) = (obj as VH).itemView == view

    override fun getItemPosition(obj: Any) = POSITION_NONE

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