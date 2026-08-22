package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ColorAdapter
import com.elytelabs.toolbox.ColorGenerator

/**
 * Dialog for selecting colors from a palette with live transparency slider across all swatches,
 * real-time preview, and custom hex code input/copying.
 */
class ColorPickerDialog(private val context: Context) {

    private var customColors: List<Int>? = null
    private var selectedColor: Int? = null
    private var currentAlpha: Int = 255
    private var colorPickerListener: ColorPickerListener? = null

    companion object {
        const val MIN_ALPHA = 30
        const val MAX_ALPHA = 255
    }

    /**
     * Traditional interface listener for Java/Kotlin interoperability.
     */
    fun interface ColorPickerListener {
        fun onColorSelected(color: Int)
    }

    /**
     * Sets the color picker listener using the traditional interface.
     */
    fun setColorSelectedListener(listener: ColorPickerListener) {
        this.colorPickerListener = listener
    }

    /**
     * Supplies a custom palette of colors to display in the grid.
     */
    fun setCustomColors(colors: List<Int>) {
        this.customColors = colors
    }

    /**
     * Sets the initially selected color for highlighting.
     */
    fun setSelectedColor(color: Int?) {
        this.selectedColor = color
    }

    /**
     * Convenience method to show the color picker dialog using a Kotlin lambda callback.
     *
     * @param customColors Optional list of custom color integers.
     * @param selectedColor Optional color integer currently active.
     * @param initialTransparency Initial alpha transparency (30 to 255, default: 255).
     * @param onColorSelected Lambda invoked when a color is changed/applied.
     */
    fun show(
        customColors: List<Int>? = null,
        selectedColor: Int? = null,
        initialTransparency: Int = 255,
        onColorSelected: (color: Int) -> Unit
    ) {
        if (customColors != null) {
            this.customColors = customColors
        }
        if (selectedColor != null) {
            this.selectedColor = selectedColor
        }
        this.currentAlpha = initialTransparency.coerceIn(MIN_ALPHA, MAX_ALPHA)
        this.colorPickerListener = ColorPickerListener { color -> onColorSelected(color) }
        showColorPickerDialog()
    }

    /**
     * Displays the color picker dialog.
     */
    fun showColorPickerDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null)
        val dialog = Dialog(context)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
        dialog.setContentView(dialogView)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.colorRecyclerView)
        val transparencySeekBar = dialogView.findViewById<SeekBar>(R.id.transparencySeekBar)
        val viewSelectedColorPreview = dialogView.findViewById<View>(R.id.viewSelectedColorPreview)
        val tvHexCode = dialogView.findViewById<TextView>(R.id.tvHexCode)
        val tvOpacityValue = dialogView.findViewById<TextView>(R.id.tvOpacityValue)
        val btnBack = dialogView.findViewById<ImageButton>(R.id.colorBackButton)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyColor)

        recyclerView.layoutManager = GridLayoutManager(context, 5)
        val adapter = ColorAdapter()
        recyclerView.adapter = adapter

        btnBack.setOnClickListener {
            dialog.dismiss()
        }

        val palette = customColors ?: ColorGenerator.getColorList()
        var pickedColor = selectedColor ?: palette.firstOrNull() ?: Color.WHITE

        fun getCurrentColorWithAlpha(): Int {
            return Color.argb(
                currentAlpha,
                Color.red(pickedColor),
                Color.green(pickedColor),
                Color.blue(pickedColor)
            )
        }

        fun updatePreview() {
            val colorWithAlpha = getCurrentColorWithAlpha()
            viewSelectedColorPreview.setBackgroundColor(colorWithAlpha)
            tvHexCode.text = String.format("#%08X", colorWithAlpha)
            val percent = (currentAlpha * 100) / 255
            tvOpacityValue.text = "$percent%"
        }

        // Configure seek bar (0 to 225 maps to MIN_ALPHA to MAX_ALPHA)
        val maxProgress = MAX_ALPHA - MIN_ALPHA
        transparencySeekBar.max = maxProgress
        transparencySeekBar.progress = currentAlpha - MIN_ALPHA
        adapter.setTransparency(currentAlpha)
        updatePreview()

        // Tap Hex code badge to open custom Hex dialog
        tvHexCode.setOnClickListener {
            showHexInputDialog(getCurrentColorWithAlpha()) { newColor ->
                val alpha = Color.alpha(newColor)
                if (alpha >= MIN_ALPHA) {
                    currentAlpha = alpha
                    transparencySeekBar.progress = currentAlpha - MIN_ALPHA
                    adapter.setTransparency(currentAlpha)
                }
                pickedColor = Color.rgb(Color.red(newColor), Color.green(newColor), Color.blue(newColor))
                adapter.setSelectedColor(pickedColor)
                updatePreview()
                val finalColor = getCurrentColorWithAlpha()
                colorPickerListener?.onColorSelected(finalColor)
            }
        }

        adapter.setOnItemClickListener { solidColor ->
            pickedColor = solidColor
            updatePreview()
            val colorWithAlpha = getCurrentColorWithAlpha()
            colorPickerListener?.onColorSelected(colorWithAlpha)
        }

        adapter.setColors(palette)
        adapter.setSelectedColor(selectedColor)

        transparencySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentAlpha = MIN_ALPHA + progress
                adapter.setTransparency(currentAlpha)
                adapter.notifyItemRangeChanged(0, adapter.itemCount)
                updatePreview()
                val colorWithAlpha = getCurrentColorWithAlpha()
                colorPickerListener?.onColorSelected(colorWithAlpha)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnApply.setOnClickListener {
            val colorWithAlpha = getCurrentColorWithAlpha()
            colorPickerListener?.onColorSelected(colorWithAlpha)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showHexInputDialog(initialColor: Int, onHexApplied: (Int) -> Unit) {
        val hexView = LayoutInflater.from(context).inflate(R.layout.dialog_hex_input, null)
        val hexDialog = Dialog(context)
        hexDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        hexDialog.window?.setWindowAnimations(R.style.DialogHubAnimation)
        hexDialog.setContentView(hexView)

        val etHex = hexView.findViewById<EditText>(R.id.etHexInput)
        val btnCopy = hexView.findViewById<Button>(R.id.btnCopyHex)
        val btnApply = hexView.findViewById<Button>(R.id.btnApplyHex)

        val currentHexStr = String.format("#%08X", initialColor)
        etHex.setText(currentHexStr)
        etHex.setSelection(etHex.text.length)

        btnCopy.setOnClickListener {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Hex Color", etHex.text.toString())
            clipboard?.setPrimaryClip(clip)
            Toast.makeText(context, "Hex copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        btnApply.setOnClickListener {
            val rawInput = etHex.text.toString().trim()
            val formatted = if (!rawInput.startsWith("#")) "#$rawInput" else rawInput
            try {
                val parsedColor = Color.parseColor(formatted)
                onHexApplied(parsedColor)
                hexDialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Invalid Hex Color. Use #RRGGBB or #AARRGGBB", Toast.LENGTH_SHORT).show()
            }
        }

        hexDialog.show()
    }
}