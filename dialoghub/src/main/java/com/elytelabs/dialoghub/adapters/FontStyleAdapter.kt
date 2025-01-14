package com.elytelabs.dialoghub.adapters

import android.content.Context
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.font_item_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val typeface = fonts[position]
        holder.textView.typeface = ResourcesCompat.getFont(context, typeface)
        holder.itemView.setOnClickListener {
            onFontClickListener?.invoke(typeface)
        }
    }

    override fun getItemCount(): Int {
        return fonts.size
    }

    fun setFonts(font: List<Int>) {
        this.fonts = font
    }

    fun setOnFontClickListener(listener: (Int) -> Unit) {
        this.onFontClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)
    }
}