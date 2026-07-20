package io.selimdawa.autoimageslider.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class SliderViewAdapter<VH : SliderViewAdapter.ViewHolder> : RecyclerView.Adapter<VH>() {

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    abstract fun onBind(viewHolder: VH, position: Int)

    override fun onBindViewHolder(holder: VH, position: Int) {
        onBind(holder, position)
    }
}