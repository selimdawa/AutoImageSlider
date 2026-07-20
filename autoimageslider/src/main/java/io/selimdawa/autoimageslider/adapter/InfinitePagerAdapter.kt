package io.selimdawa.autoimageslider.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class InfinitePagerAdapter<VH : RecyclerView.ViewHolder>(
    private val adapter: RecyclerView.Adapter<VH>
) : RecyclerView.Adapter<VH>() {

    init {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                notifyDataSetChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                notifyDataSetChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyDataSetChanged()
            }
        })
    }

    val realCount get() = adapter.itemCount

    override fun getItemCount() = if (realCount < 1) 0 else INFINITE_SCROLL_LIMIT

    fun getMiddlePosition(item: Int) =
        (INFINITE_SCROLL_LIMIT / 2) - ((INFINITE_SCROLL_LIMIT / 2) % max(1, realCount)) + item

    fun getRealPosition(virtualPos: Int) = if (realCount > 0) virtualPos % realCount else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        adapter.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: VH, position: Int) =
        adapter.onBindViewHolder(holder, getRealPosition(position))

    override fun getItemViewType(position: Int) = adapter.getItemViewType(getRealPosition(position))
    override fun getItemId(position: Int) = adapter.getItemId(getRealPosition(position))

    companion object {
        const val INFINITE_SCROLL_LIMIT = 32400
    }
}