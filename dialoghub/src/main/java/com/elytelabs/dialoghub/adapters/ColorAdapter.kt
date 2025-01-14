package com.elytelabs.dialoghub.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R

class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var colors: List<Int> = emptyList()
    private var transparency: Int = 255
    private var onItemClickListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        val transparentColor = Color.argb(transparency, Color.red(color), Color.green(color), Color.blue(color))
        holder.colorView.setBackgroundColor(transparentColor)
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(transparentColor)
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    fun setColors(colors: List<Int>) {
        this.colors = colors
        notifyDataSetChanged()
    }

    fun setTransparency(transparency: Int) {
        this.transparency = transparency
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById(R.id.colorView)
    }
}