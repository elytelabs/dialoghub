package com.elytelabs.dialoghub.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R

import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem

class FontStyleAdapter(private val context: Context)
    : RecyclerView.Adapter<FontStyleAdapter.ViewHolder>() {

    private var fonts: List<Int> = emptyList()
    private var previewText: String? = null
    private var selectedFontResId: Int? = null
    private var selectedPosition: Int = -1
    private var onFontClickListener: ((Int) -> Unit)? = null
    private var lockProvider: ItemLockProvider? = null
    private var onLockedItemClickListener: ((LockableItem.Font, unlock: () -> Unit) -> Unit)? = null
    private val typefaceCache = LruCache<Int, Typeface>(20)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.font_item_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fontRes = fonts.getOrNull(position) ?: return

        var typeface = typefaceCache.get(fontRes)
        if (typeface == null) {
            try {
                typeface = ResourcesCompat.getFont(context, fontRes)
                if (typeface != null) {
                    typefaceCache.put(fontRes, typeface)
                }
            } catch (_: Exception) {
                typeface = Typeface.DEFAULT
            }
        }

        holder.textView.typeface = typeface ?: Typeface.DEFAULT

        val displayText = if (!previewText.isNullOrEmpty()) {
            val text = previewText!!.trim()
            if (text.length > 14) {
                val words = text.split(Regex("\\s+"))
                if (words.size >= 2) {
                    "${words[0]} ${words[1]}"
                } else {
                    text.take(12)
                }
            } else {
                text
            }
        } else {
            context.getString(R.string.dh_sample_text)
        }

        holder.textView.text = displayText

        val isLocked = lockProvider?.isFontLocked(fontRes) == true
        holder.ivFontLockBadge.visibility = if (isLocked) View.VISIBLE else View.GONE

        // Selection highlight (strictly single-card selection with default fallback)
        val isSelected = !isLocked && (position == selectedPosition || (selectedPosition == -1 && (position == 0 || selectedFontResId == fontRes)))
        holder.selectedOverlay.visibility = if (isSelected) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            if (isLocked) {
                onLockedItemClickListener?.invoke(LockableItem.Font(fontRes)) {
                    selectedPosition = holder.bindingAdapterPosition
                    selectedFontResId = fontRes
                    notifyItemRangeChanged(0, itemCount)
                    onFontClickListener?.invoke(fontRes)
                }
            } else {
                selectedPosition = holder.bindingAdapterPosition
                selectedFontResId = fontRes
                notifyItemRangeChanged(0, itemCount)
                onFontClickListener?.invoke(fontRes)
            }
        }
    }

    override fun getItemCount(): Int {
        return fonts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFonts(font: List<Int>) {
        this.fonts = font
        val target = selectedFontResId ?: fonts.firstOrNull()
        this.selectedPosition = if (target != null) fonts.indexOf(target) else -1
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setPreviewText(text: String?) {
        this.previewText = text
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedFont(fontResId: Int?) {
        this.selectedFontResId = fontResId
        val target = fontResId ?: fonts.firstOrNull()
        this.selectedPosition = if (target != null) fonts.indexOf(target) else -1
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
        notifyDataSetChanged()
    }

    fun setOnLockedItemClickListener(listener: (LockableItem.Font, unlock: () -> Unit) -> Unit) {
        this.onLockedItemClickListener = listener
    }

    fun setOnFontClickListener(listener: (Int) -> Unit) {
        this.onFontClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
        val selectedOverlay: View = itemView.findViewById(R.id.selectedOverlay)
        val ivFontLockBadge: ImageView = itemView.findViewById(R.id.ivFontLockBadge)
    }
}