package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ColorAdapter
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.toolbox.ColorGenerator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem

/**
 * Dialog for selecting colors from a palette with live transparency slider across all swatches,
 * real-time preview, and custom hex code input/copying.
 * Supports monetization locking (IAP/Rewarded Ads), fluent Builder, and Kotlin DSL.
 */
class ColorPickerDialog(private val context: Context) {

    private var customColors: List<Int>? = null
    private var selectedColor: Int? = null
    private var currentAlpha: Int = 255
    private var colorPickerListener: ColorPickerListener? = null
    private var lockProvider: ItemLockProvider? = null
    private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null

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
     * Sets dismissal listener.
     */
    fun setOnDismissListener(listener: () -> Unit) {
        this.dismissListener = listener
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
     * Sets monetization item lock provider.
     */
    fun setLockProvider(provider: ItemLockProvider?) {
        this.lockProvider = provider
    }

    /**
     * Sets click listener for locked color items.
     */
    fun setOnLockedItemClickListener(listener: (LockableItem, unlock: () -> Unit) -> Unit) {
        this.lockedItemClickListener = listener
    }

    /**
     * Sets the initial alpha transparency (30 to 255).
     */
    fun setInitialTransparency(transparency: Int) {
        this.currentAlpha = transparency.coerceIn(MIN_ALPHA, MAX_ALPHA)
    }

    /**
     * Convenience method to show the color picker dialog using a Kotlin lambda callback.
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

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_color_picker, null)

        val bottomSheet = BottomSheetDialog(themedContext)
        bottomSheet.setContentView(dialogView)
        dialogView.setBackgroundResource(R.drawable.bg_bottom_sheet)
        bottomSheet.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet.setOnDismissListener {
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = View.VISIBLE

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.colorRecyclerView)
        val transparencySeekBar = dialogView.findViewById<SeekBar>(R.id.transparencySeekBar)
        val viewSelectedColorPreview = dialogView.findViewById<View>(R.id.viewSelectedColorPreview)
        val tvHexCode = dialogView.findViewById<TextView>(R.id.tvHexCode)
        val tvOpacityValue = dialogView.findViewById<TextView>(R.id.tvOpacityValue)
        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

        recyclerView.layoutManager = GridLayoutManager(themedContext, 5)
        val adapter = ColorAdapter()
        recyclerView.adapter = adapter

        btnClose?.setOnClickListener {
            bottomSheet.dismiss()
        }

        adapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            adapter.setOnLockedItemClickListener { lockedColor, unlock ->
                listener(lockedColor, unlock)
            }
        }

        val palette = customColors ?: ColorGenerator.getColorList()
        var pickedColor = selectedColor ?: palette.firstOrNull() ?: Color.WHITE

        val chipAll = dialogView.findViewById<TextView>(R.id.chipPaletteAll)
        val chipBold = dialogView.findViewById<TextView>(R.id.chipPaletteBold)
        val chipNeon = dialogView.findViewById<TextView>(R.id.chipPaletteNeon)
        val chipCalm = dialogView.findViewById<TextView>(R.id.chipPaletteCalm)
        val chipPastel = dialogView.findViewById<TextView>(R.id.chipPalettePastel)
        val chipDark = dialogView.findViewById<TextView>(R.id.chipPaletteDark)
        val chipVintage = dialogView.findViewById<TextView>(R.id.chipPaletteVintage)

        val chips = listOfNotNull(chipAll, chipBold, chipNeon, chipCalm, chipPastel, chipDark, chipVintage)

        fun selectChip(selectedChip: TextView, colors: List<Int>) {
            chips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = android.content.res.ColorStateList.valueOf("#E5E7EB".toColorInt())
                    chip.setTextColor("#1F2937".toColorInt())
                    chip.setTypeface(null, android.graphics.Typeface.BOLD)
                } else {
                    chip.backgroundTintList = android.content.res.ColorStateList.valueOf("#F3F4F6".toColorInt())
                    chip.setTextColor("#4B5563".toColorInt())
                    chip.setTypeface(null, android.graphics.Typeface.NORMAL)
                }
            }
            adapter.setColors(colors)
        }

        chipAll?.setOnClickListener { selectChip(chipAll, customColors ?: ColorGenerator.getColorList()) }
        chipBold?.setOnClickListener { selectChip(chipBold, ColorPalettes.MOTIVATIONAL_BOLD) }
        chipNeon?.setOnClickListener { selectChip(chipNeon, ColorPalettes.AESTHETIC_NEON) }
        chipCalm?.setOnClickListener { selectChip(chipCalm, ColorPalettes.NATURE_SUFI_CALM) }
        chipPastel?.setOnClickListener { selectChip(chipPastel, ColorPalettes.PASTEL_SOFT) }
        chipDark?.setOnClickListener { selectChip(chipDark, ColorPalettes.MELANCHOLY_DARK) }
        chipVintage?.setOnClickListener { selectChip(chipVintage, ColorPalettes.VINTAGE_EARTHY) }

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
        fun openHexInput() {
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

        tvHexCode.setOnClickListener {
            val isHexLocked = lockProvider?.isHexInputLocked() == true
            if (isHexLocked) {
                lockedItemClickListener?.invoke(LockableItem.CustomHexInput) {
                    openHexInput()
                }
            } else {
                openHexInput()
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

        bottomSheet.show()
    }

    private fun showHexInputDialog(initialColor: Int, onHexApplied: (Int) -> Unit) {
        val themedContext = DialogThemeHelper.getThemedContext(context)
        val hexView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_hex_input, null)
        val hexDialog = Dialog(themedContext)
        hexDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
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
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                Toast.makeText(context, "Hex copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        btnApply.setOnClickListener {
            val rawInput = etHex.text.toString().trim()
            val formatted = if (!rawInput.startsWith("#")) "#$rawInput" else rawInput
            try {
                val parsedColor = formatted.toColorInt()
                onHexApplied(parsedColor)
                hexDialog.dismiss()
            } catch (e: Exception) {
                Toast.makeText(context, "Invalid Hex Color. Use #RRGGBB or #AARRGGBB", Toast.LENGTH_SHORT).show()
            }
        }

        hexDialog.show()
    }

    /**
     * Fluent Builder for [ColorPickerDialog].
     */
    class Builder(private val context: Context) {
        private var customColors: List<Int>? = null
        private var selectedColor: Int? = null
        private var initialTransparency: Int = 255
        private var lockProvider: ItemLockProvider? = null
        private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
        private var listener: ColorPickerListener? = null
        private var dismissListener: (() -> Unit)? = null

        fun setCustomColors(colors: List<Int>) = apply { this.customColors = colors }
        fun setCustomColors(vararg colors: Int) = apply { this.customColors = colors.toList() }
        fun setSelectedColor(color: Int?) = apply { this.selectedColor = color }
        fun setInitialTransparency(alpha: Int) = apply { this.initialTransparency = alpha }
        fun setLockProvider(provider: ItemLockProvider) = apply { this.lockProvider = provider }
        fun setLockedColors(vararg colors: Int) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockColors(*colors)
        }
        fun setLockedColors(colors: Collection<Int>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockColors(colors)
        }
        fun setLockHexInput(lock: Boolean = true) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockHexInput(lock)
        }
        fun setOnLockedItemClicked(listener: (LockableItem, unlock: () -> Unit) -> Unit) = apply {
            this.lockedItemClickListener = listener
        }
        fun setOnColorSelected(listener: (Int) -> Unit) = apply { this.listener = ColorPickerListener { listener(it) } }
        fun setOnColorSelected(listener: ColorPickerListener) = apply { this.listener = listener }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): ColorPickerDialog {
            val dialog = ColorPickerDialog(context)
            customColors?.let { dialog.setCustomColors(it) }
            selectedColor?.let { dialog.setSelectedColor(it) }
            dialog.setInitialTransparency(initialTransparency)
            dialog.setLockProvider(lockProvider)
            lockedItemClickListener?.let { dialog.setOnLockedItemClickListener(it) }
            listener?.let { dialog.setColorSelectedListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        fun show(onColorSelected: ((Int) -> Unit)? = null): ColorPickerDialog {
            val dialog = build()
            if (onColorSelected != null) {
                dialog.setColorSelectedListener { onColorSelected(it) }
            }
            dialog.showColorPickerDialog()
            return dialog
        }
    }
}
