package com.elytelabs.dialoghub.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.toolbox.ColorGenerator

import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem

/**
 * High-performance horizontal/grid color swatch adapter with distinct active selection rings,
 * lock badge indicators for monetization, and optional 'None' (transparent) option support.
 */
class ColorSwatchAdapter(
    private val includeNoneOption: Boolean = false
) : RecyclerView.Adapter<ColorSwatchAdapter.ViewHolder>() {

    private var colors: List<Int> = emptyList()
    private var selectedColor: Int? = null
    private var isNoneSelected: Boolean = false
    private var onSwatchClickListener: ((color: Int, isNone: Boolean) -> Unit)? = null
    private var lockProvider: ItemLockProvider? = null
    private var onLockedItemClickListener: ((LockableItem.Color, unlock: () -> Unit) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_swatch, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isNoneItem = includeNoneOption && position == 0

        if (isNoneItem) {
            // None / Transparent tile
            holder.cardColorSwatch.setCardBackgroundColor(Color.parseColor("#F1F5F9"))
            holder.viewSwatchColor.setBackgroundColor(Color.TRANSPARENT)
            holder.ivNoneIcon.visibility = View.VISIBLE
            holder.ivSelectedCheck.visibility = View.GONE
            holder.ivSwatchLockBadge.visibility = View.GONE

            if (isNoneSelected) {
                holder.viewActiveRing.visibility = View.VISIBLE
            } else {
                holder.viewActiveRing.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                isNoneSelected = true
                selectedColor = null
                notifyItemRangeChanged(0, itemCount)
                onSwatchClickListener?.invoke(Color.TRANSPARENT, true)
            }
        } else {
            val colorIndex = if (includeNoneOption) position - 1 else position
            val color = colors[colorIndex]

            holder.ivNoneIcon.visibility = View.GONE
            holder.viewSwatchColor.setBackgroundColor(color)
            holder.cardColorSwatch.setCardBackgroundColor(color)

            val isLocked = lockProvider?.isColorLocked(color) == true
            holder.ivSwatchLockBadge.visibility = if (isLocked) View.VISIBLE else View.GONE

            val isSelected = !isLocked && !isNoneSelected && selectedColor != null && isColorMatching(selectedColor!!, color)

            if (isSelected) {
                holder.viewActiveRing.visibility = View.VISIBLE
                holder.ivSelectedCheck.visibility = View.VISIBLE
                val checkColor = if (ColorGenerator.isDarkColor(color)) Color.WHITE else Color.parseColor("#1E293B")
                holder.ivSelectedCheck.setColorFilter(checkColor)
            } else {
                holder.viewActiveRing.visibility = View.GONE
                holder.ivSelectedCheck.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                if (isLocked) {
                    onLockedItemClickListener?.invoke(LockableItem.Color(color)) {
                        isNoneSelected = false
                        selectedColor = color
                        notifyItemRangeChanged(0, itemCount)
                        onSwatchClickListener?.invoke(color, false)
                    }
                } else {
                    isNoneSelected = false
                    selectedColor = color
                    notifyItemRangeChanged(0, itemCount)
                    onSwatchClickListener?.invoke(color, false)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (includeNoneOption) colors.size + 1 else colors.size
    }

    fun setColors(colors: List<Int>) {
        this.colors = colors
        notifyDataSetChanged()
    }

    fun setSelectedColor(color: Int?, isNone: Boolean = false) {
        this.selectedColor = color
        this.isNoneSelected = isNone
        notifyDataSetChanged()
    }

    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
        notifyDataSetChanged()
    }

    fun setOnLockedItemClickListener(listener: (LockableItem.Color, unlock: () -> Unit) -> Unit) {
        this.onLockedItemClickListener = listener
    }

    fun setOnSwatchClickListener(listener: (color: Int, isNone: Boolean) -> Unit) {
        this.onSwatchClickListener = listener
    }

    private fun isColorMatching(c1: Int, c2: Int): Boolean {
        return Color.red(c1) == Color.red(c2) &&
                Color.green(c1) == Color.green(c2) &&
                Color.blue(c1) == Color.blue(c2)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewActiveRing: View = itemView.findViewById(R.id.viewActiveRing)
        val cardColorSwatch: CardView = itemView.findViewById(R.id.cardColorSwatch)
        val viewSwatchColor: View = itemView.findViewById(R.id.viewSwatchColor)
        val ivNoneIcon: ImageView = itemView.findViewById(R.id.ivNoneIcon)
        val ivSelectedCheck: ImageView = itemView.findViewById(R.id.ivSelectedCheck)
        val ivSwatchLockBadge: ImageView = itemView.findViewById(R.id.ivSwatchLockBadge)
    }
}
