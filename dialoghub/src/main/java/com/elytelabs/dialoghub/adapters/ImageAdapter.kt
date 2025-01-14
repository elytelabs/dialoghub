package com.elytelabs.dialoghub.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var backgrounds: List<Int> = emptyList()

    private var onImageClickListener: ((Int) -> Unit)? = null
    private var onColorPickerListener: (() -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) {
            holder.imageView.setImageResource(R.drawable.color_wheel)
            holder.itemView.setOnClickListener {
                onColorPickerListener?.invoke()
            }
        } else {
            val imageResource = backgrounds[position - 1]  // Adjusted position
            holder.imageView.setImageResource(imageResource)
            holder.itemView.setOnClickListener {
                onImageClickListener?.invoke(imageResource)
            }
        }
    }

    override fun getItemCount(): Int {
        // Add 1 to account for the color wheel item
        return backgrounds.size + 1
    }

    fun setBackgrounds(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
    }

    fun setOnImageClickListener(listener: (Int) -> Unit) {
        this.onImageClickListener = listener
    }

    fun setOnColorPickerClickListener(listener: () -> Unit) {
        this.onColorPickerListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
