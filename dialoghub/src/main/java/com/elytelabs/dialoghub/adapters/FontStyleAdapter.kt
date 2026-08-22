package com.elytelabs.dialoghub.adapters

import android.content.Context
import android.graphics.Typeface
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R

class FontStyleAdapter(private val context: Context)
    : RecyclerView.Adapter<FontStyleAdapter.ViewHolder>() {

    private var fonts: List<Int> = emptyList()
    private var onFontClickListener: ((Int) -> Unit)? = null
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
            } catch (e: Exception) {
                typeface = Typeface.DEFAULT
            }
        }

        holder.textView.typeface = typeface ?: Typeface.DEFAULT
        holder.itemView.setOnClickListener {
            onFontClickListener?.invoke(fontRes)
        }
    }

    override fun getItemCount(): Int {
        return fonts.size
    }

    fun setFonts(font: List<Int>) {
        this.fonts = font
        notifyDataSetChanged()
    }

    fun setOnFontClickListener(listener: (Int) -> Unit) {
        this.onFontClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}