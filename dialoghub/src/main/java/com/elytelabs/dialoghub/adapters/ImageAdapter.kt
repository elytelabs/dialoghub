package com.elytelabs.dialoghub.adapters

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
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

    // Memory cache for decoded thumbnails (max 30 items)
    private val thumbnailCache = LruCache<Int, Bitmap>(30)

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
            val imageResource = backgrounds.getOrNull(position - 1) ?: return

            // Load downscaled thumbnail from cache or decode efficiently with inSampleSize
            val thumbnail = getOrCreateThumbnail(holder.itemView.resources, imageResource)
            if (thumbnail != null) {
                holder.imageView.setImageBitmap(thumbnail)
            } else {
                try {
                    holder.imageView.setImageResource(imageResource)
                } catch (e: OutOfMemoryError) {
                    holder.imageView.setImageDrawable(null)
                }
            }

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
        notifyDataSetChanged()
    }

    fun setOnImageClickListener(listener: (Int) -> Unit) {
        this.onImageClickListener = listener
    }

    fun setOnColorPickerClickListener(listener: () -> Unit) {
        this.onColorPickerListener = listener
    }

    private fun getOrCreateThumbnail(resources: Resources, resId: Int): Bitmap? {
        thumbnailCache.get(resId)?.let { return it }

        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeResource(resources, resId, options)

            val reqWidth = 200
            val reqHeight = 200
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565 // Low memory footprint

            val bitmap = BitmapFactory.decodeResource(resources, resId, options)
            if (bitmap != null) {
                thumbnailCache.put(resId, bitmap)
            }
            bitmap
        } catch (e: Exception) {
            null
        } catch (oom: OutOfMemoryError) {
            thumbnailCache.evictAll()
            null
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val height: Int = options.outHeight
        val width: Int = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
