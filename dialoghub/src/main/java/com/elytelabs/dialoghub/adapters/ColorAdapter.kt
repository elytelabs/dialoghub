package com.elytelabs.dialoghub.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.toolbox.ColorGenerator

import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem

class ColorAdapter : RecyclerView.Adapter<ColorAdapter.ViewHolder>() {

    private var colors: List<Int> = emptyList()
    private var transparency: Int = 255
    private var selectedColor: Int? = null
    private var onItemClickListener: ((Int) -> Unit)? = null
    private var lockProvider: ItemLockProvider? = null
    private var onLockedItemClickListener: ((LockableItem.Color, unlock: () -> Unit) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val color = colors[position]
        // Apply live alpha transparency to every swatch in the palette
        val transparentColor = Color.argb(
            transparency,
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        )
        holder.colorView.setBackgroundColor(transparentColor)

        val isLocked = lockProvider?.isColorLocked(color) == true
        holder.ivColorLockBadge.visibility = if (isLocked) View.VISIBLE else View.GONE

        val isSelected = if (!isLocked && selectedColor != null) {
            val selR = Color.red(selectedColor!!)
            val selG = Color.green(selectedColor!!)
            val selB = Color.blue(selectedColor!!)
            selR == Color.red(color) && selG == Color.green(color) && selB == Color.blue(color)
        } else {
            false
        }

        if (isSelected) {
            holder.selectedCheck.visibility = View.VISIBLE
            val checkColor = if (ColorGenerator.isDarkColor(color)) Color.WHITE else Color.BLACK
            holder.selectedCheck.setColorFilter(checkColor)
            holder.selectedOverlay.visibility = View.VISIBLE
        } else {
            holder.selectedCheck.visibility = View.GONE
            holder.selectedOverlay.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (isLocked) {
                onLockedItemClickListener?.invoke(LockableItem.Color(color)) {
                    selectedColor = color
                    notifyItemRangeChanged(0, itemCount)
                    onItemClickListener?.invoke(color)
                }
            } else {
                selectedColor = color
                notifyItemRangeChanged(0, itemCount)
                onItemClickListener?.invoke(color)
            }
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setColors(colors: List<Int>) {
        this.colors = colors
        notifyDataSetChanged()
    }

    fun setTransparency(transparency: Int) {
        this.transparency = transparency
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedColor(color: Int?) {
        this.selectedColor = color
        notifyDataSetChanged()
    }

    fun getSelectedColor(): Int? {
        return selectedColor
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
        notifyDataSetChanged()
    }

    fun setOnLockedItemClickListener(listener: (LockableItem.Color, unlock: () -> Unit) -> Unit) {
        this.onLockedItemClickListener = listener
    }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        this.onItemClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById(R.id.colorView)
        val selectedCheck: ImageView = itemView.findViewById(R.id.selectedCheck)
        val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)
        val ivColorLockBadge: ImageView = itemView.findViewById(R.id.ivColorLockBadge)
    }
}