package com.elytelabs.dialoghub.adapters

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var backgrounds: List<Int> = emptyList()
    private var enableGalleryPick: Boolean = false
    private var selectedBackgroundResId: Int? = null

    private var onImageClickListener: ((Int) -> Unit)? = null
    private var onColorPickerListener: (() -> Unit)? = null
    private var onGalleryClickListener: (() -> Unit)? = null

    // Memory cache for decoded thumbnails (max 30 items)
    private val thumbnailCache = LruCache<Int, Bitmap>(30)

    companion object {
        private const val VIEW_TYPE_GALLERY = 0
        private const val VIEW_TYPE_COLOR_WHEEL = 1
        private const val VIEW_TYPE_IMAGE = 2
    }

    override fun getItemViewType(position: Int): Int {
        if (enableGalleryPick) {
            if (position == 0) return VIEW_TYPE_GALLERY
            if (position == 1) return VIEW_TYPE_COLOR_WHEEL
            return VIEW_TYPE_IMAGE
        } else {
            if (position == 0) return VIEW_TYPE_COLOR_WHEEL
            return VIEW_TYPE_IMAGE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val viewType = getItemViewType(position)

        when (viewType) {
            VIEW_TYPE_GALLERY -> {
                holder.imageView.visibility = View.GONE
                holder.actionTileLayout.visibility = View.VISIBLE
                holder.actionTileLayout.setBackgroundResource(R.drawable.bg_tile_gallery)
                holder.actionTileIcon.setImageResource(R.drawable.ic_tile_gallery)
                holder.tileLabel.setText(R.string.pick_from_gallery)
                holder.selectedOverlay.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    onGalleryClickListener?.invoke()
                }
            }
            VIEW_TYPE_COLOR_WHEEL -> {
                holder.imageView.visibility = View.GONE
                holder.actionTileLayout.visibility = View.VISIBLE
                holder.actionTileLayout.setBackgroundResource(R.drawable.bg_tile_color)
                holder.actionTileIcon.setImageResource(R.drawable.ic_tile_palette)
                holder.tileLabel.setText(R.string.color_wheel_title)
                holder.selectedOverlay.visibility = View.GONE
                holder.itemView.setOnClickListener {
                    onColorPickerListener?.invoke()
                }
            }
            else -> {
                holder.actionTileLayout.visibility = View.GONE
                holder.imageView.visibility = View.VISIBLE

                val offset = if (enableGalleryPick) 2 else 1
                val imageResource = backgrounds.getOrNull(position - offset) ?: return

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

                // Selection highlight
                val isSelected = (selectedBackgroundResId != null && selectedBackgroundResId == imageResource)
                holder.selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

                holder.itemView.setOnClickListener {
                    selectedBackgroundResId = imageResource
                    notifyItemRangeChanged(0, itemCount)
                    onImageClickListener?.invoke(imageResource)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        val extra = if (enableGalleryPick) 2 else 1
        return backgrounds.size + extra
    }

    fun setBackgrounds(backgrounds: List<Int>) {
        this.backgrounds = backgrounds
        notifyDataSetChanged()
    }

    fun setEnableGalleryPick(enable: Boolean) {
        this.enableGalleryPick = enable
        notifyDataSetChanged()
    }

    fun setSelectedBackground(resId: Int?) {
        this.selectedBackgroundResId = resId
        notifyDataSetChanged()
    }

    fun setOnImageClickListener(listener: (Int) -> Unit) {
        this.onImageClickListener = listener
    }

    fun setOnColorPickerClickListener(listener: () -> Unit) {
        this.onColorPickerListener = listener
    }

    fun setOnGalleryClickListener(listener: () -> Unit) {
        this.onGalleryClickListener = listener
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
        val actionTileLayout: LinearLayout = itemView.findViewById(R.id.actionTileLayout)
        val actionTileIcon: ImageView = itemView.findViewById(R.id.actionTileIcon)
        val tileLabel: TextView = itemView.findViewById(R.id.tileLabel)
        val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)
    }
}
