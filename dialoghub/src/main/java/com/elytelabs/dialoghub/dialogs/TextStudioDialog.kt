package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elytelabs.dialoghub.R
import com.elytelabs.dialoghub.adapters.ColorAdapter
import com.elytelabs.dialoghub.adapters.ColorSwatchAdapter
import com.elytelabs.dialoghub.adapters.FontStyleAdapter
import com.elytelabs.dialoghub.models.StudioTab
import com.elytelabs.dialoghub.models.TextTypographyConfig
import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
import com.elytelabs.dialoghub.monetization.ItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * All-In-One Unified Text Studio Dialog for comprehensive typography styling:
 * Fonts, Curated Colors, Formatting (Size & Alignment),
 * Text Stroke/Outline, Visual Effects (Styles & Shadow), and Background Ribbon/Highlight.
 *
 * Features real-time live preview with custom wallpaper background canvas, monetization item locking, 1-Tap Reset,
 * and zero redundant bottom buttons.
 */
class TextStudioDialog(private val context: Context) {

    private var initialConfig = TextTypographyConfig()
    private var currentConfig = TextTypographyConfig()
    private var samplePreviewText: String? = null
    private var customFonts: List<Int>? = null
    private var enabledTabs: Set<StudioTab> = StudioTab.DEFAULT
    private var showPreviewPane: Boolean = true
    private var backgroundDrawable: Drawable? = null
    private var backgroundDrawableRes: Int? = null
    private var backgroundColorInt: Int? = null
    private var studioListener: TextStudioListener? = null
    private var livePreviewListener: ((TextTypographyConfig) -> Unit)? = null
    private var lockProvider: ItemLockProvider? = null
    private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null

    fun interface TextStudioListener {
        fun onTypographyApplied(config: TextTypographyConfig)
    }

    /**
     * Sets the initial typography configuration for the studio editor.
     */
    fun setConfig(config: TextTypographyConfig) = apply {
        this.initialConfig = config
        this.currentConfig = config
    }

    /**
     * Sets custom preview sample text for the live preview box and font cards.
     */
    fun setPreviewText(text: String?) = apply { this.samplePreviewText = text }

    /**
     * Sets the font resources to display in the Fonts tab.
     */
    fun setFonts(fonts: List<Int>) = apply { this.customFonts = fonts }

    /**
     * Sets which studio tabs to enable.
     */
    fun setTabs(vararg tabs: StudioTab) = apply { this.enabledTabs = tabs.toSet() }

    /**
     * Sets which studio tabs to enable.
     */
    fun setTabs(tabs: Set<StudioTab>) = apply { this.enabledTabs = tabs }

    /**
     * Controls whether the top live preview pane is displayed inside the bottom sheet.
     */
    fun setShowPreviewPane(show: Boolean) = apply { this.showPreviewPane = show }

    /**
     * Sets a custom wallpaper background drawable for the preview canvas.
     */
    fun setBackgroundDrawable(drawable: Drawable?) = apply { this.backgroundDrawable = drawable }

    /**
     * Sets a custom wallpaper background drawable resource ID for the preview canvas.
     */
    fun setBackgroundRes(resId: Int?) = apply { this.backgroundDrawableRes = resId }

    /**
     * Sets a solid background color for the preview canvas.
     */
    fun setBackgroundColor(color: Int?) = apply { this.backgroundColorInt = color }

    /**
     * Sets monetization item lock provider.
     */
    fun setLockProvider(provider: ItemLockProvider?) = apply { this.lockProvider = provider }

    /**
     * Sets click interceptor callback for locked items, fonts, wallpapers, or studio tool tabs.
     */
    fun setOnLockedItemClickListener(listener: (LockableItem, unlock: () -> Unit) -> Unit) = apply {
        this.lockedItemClickListener = listener
    }

    /**
     * Sets callback invoked when typography changes are applied.
     */
    fun setStudioListener(listener: TextStudioListener) = apply { this.studioListener = listener }

    /**
     * Sets callback invoked when typography changes are applied.
     */
    fun setStudioListener(listener: (TextTypographyConfig) -> Unit) = apply {
        this.studioListener = TextStudioListener { listener(it) }
    }

    /**
     * Sets live preview callback invoked in real-time as the user tweaks sliders or selections.
     */
    fun setOnLivePreviewListener(listener: (TextTypographyConfig) -> Unit) = apply {
        this.livePreviewListener = listener
    }

    /**
     * Sets dismissal listener for the dialog.
     */
    fun setOnDismissListener(listener: () -> Unit) = apply { this.dismissListener = listener }

    /**
     * Convenience method to show the unified text studio dialog using Kotlin lambda callbacks.
     */
    fun show(
        initialConfig: TextTypographyConfig = this.currentConfig,
        previewText: String? = null,
        fonts: List<Int>? = null,
        enabledTabs: Set<StudioTab> = this.enabledTabs,
        showPreviewPane: Boolean = this.showPreviewPane,
        backgroundDrawable: Drawable? = this.backgroundDrawable,
        backgroundDrawableRes: Int? = this.backgroundDrawableRes,
        backgroundColorInt: Int? = this.backgroundColorInt,
        onLivePreview: ((config: TextTypographyConfig) -> Unit)? = null,
        onTypographyApplied: (config: TextTypographyConfig) -> Unit
    ) {
        this.initialConfig = initialConfig
        this.currentConfig = initialConfig
        if (previewText != null) this.samplePreviewText = previewText
        if (fonts != null) this.customFonts = fonts
        this.enabledTabs = enabledTabs
        this.showPreviewPane = showPreviewPane
        this.backgroundDrawable = backgroundDrawable
        this.backgroundDrawableRes = backgroundDrawableRes
        this.backgroundColorInt = backgroundColorInt
        this.livePreviewListener = onLivePreview
        this.studioListener = TextStudioListener { onTypographyApplied(it) }
        showTextStudioDialog()
    }

    fun showTextStudioDialog() {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return
        }

        val themedContext = DialogThemeHelper.getThemedContext(context)
        val dialogView = LayoutInflater.from(themedContext).inflate(R.layout.dialog_text_studio, null)

        val bottomSheet = BottomSheetDialog(themedContext)
        bottomSheet.setContentView(dialogView)
        dialogView.setBackgroundResource(R.drawable.bg_bottom_sheet)
        bottomSheet.behavior.apply {
            isFitToContents = true
            skipCollapsed = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet.setOnDismissListener {
            studioListener?.onTypographyApplied(currentConfig)
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = View.VISIBLE

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        val btnReset = dialogView.findViewById<TextView>(R.id.btnStudioReset)
        val cardPreview = dialogView.findViewById<CardView>(R.id.cardStudioPreview)
        val ivPreviewBg = dialogView.findViewById<ImageView>(R.id.ivStudioPreviewBg)
        val btnCanvasToggle = dialogView.findViewById<ImageView>(R.id.btnStudioCanvasToggle)
        val tvPreview = dialogView.findViewById<TextView>(R.id.tvStudioPreview)
        val hsvStudioTabs = dialogView.findViewById<android.widget.HorizontalScrollView>(R.id.hsvStudioTabs)

        // In-dialog preview pane visibility (default true for high-fidelity on-screen preview)
        cardPreview?.visibility = if (showPreviewPane) View.VISIBLE else View.GONE

        // Apply Host Background if provided
        var hasCustomBg = false
        if (backgroundDrawable != null) {
            ivPreviewBg?.setImageDrawable(backgroundDrawable)
            ivPreviewBg?.visibility = View.VISIBLE
            btnCanvasToggle?.visibility = View.GONE
            hasCustomBg = true
        } else if (backgroundDrawableRes != null) {
            ivPreviewBg?.setImageResource(backgroundDrawableRes!!)
            ivPreviewBg?.visibility = View.VISIBLE
            btnCanvasToggle?.visibility = View.GONE
            hasCustomBg = true
        } else if (backgroundColorInt != null) {
            cardPreview?.setCardBackgroundColor(backgroundColorInt!!)
            ivPreviewBg?.visibility = View.GONE
            btnCanvasToggle?.visibility = View.GONE
            hasCustomBg = true
        }

        // Canvas Contrast Mode Toggle (0: Dark Slate, 1: Light Slate, 2: Warm Parchment)
        var canvasMode = 0
        val canvasColors = listOf("#0F172A".toColorInt(), "#F8FAFC".toColorInt(), "#FEF3C7".toColorInt())

        fun updateCanvasMode() {
            if (!hasCustomBg) {
                cardPreview?.setCardBackgroundColor(canvasColors[canvasMode])
                if (canvasMode == 0) {
                    btnCanvasToggle?.backgroundTintList = ColorStateList.valueOf("#33FFFFFF".toColorInt())
                    btnCanvasToggle?.setColorFilter(Color.WHITE)
                } else {
                    btnCanvasToggle?.backgroundTintList = ColorStateList.valueOf("#33000000".toColorInt())
                    btnCanvasToggle?.setColorFilter("#1F2937".toColorInt())
                }
            }
        }
        if (!hasCustomBg) {
            btnCanvasToggle?.setOnClickListener {
                canvasMode = (canvasMode + 1) % canvasColors.size
                updateCanvasMode()
            }
        }

        // Tab Container Views
        val containerFont = dialogView.findViewById<View>(R.id.containerTabFont)
        val containerColor = dialogView.findViewById<View>(R.id.containerTabColor)
        val containerFormat = dialogView.findViewById<View>(R.id.containerTabFormat)
        val containerStroke = dialogView.findViewById<View>(R.id.containerTabStroke)
        val containerEffects = dialogView.findViewById<View>(R.id.containerTabEffects)
        val containerRibbon = dialogView.findViewById<View>(R.id.containerTabRibbon)

        // Tab Text Views
        val tabFont = dialogView.findViewById<TextView>(R.id.tabFont)
        val tabColor = dialogView.findViewById<TextView>(R.id.tabColor)
        val tabFormat = dialogView.findViewById<TextView>(R.id.tabFormat)
        val tabStroke = dialogView.findViewById<TextView>(R.id.tabStroke)
        val tabEffects = dialogView.findViewById<TextView>(R.id.tabEffects)
        val tabRibbon = dialogView.findViewById<TextView>(R.id.tabRibbon)

        // Badge Dots
        val dotFont = dialogView.findViewById<View>(R.id.dotFontBadge)
        val dotColor = dialogView.findViewById<View>(R.id.dotColorBadge)
        val dotFormat = dialogView.findViewById<View>(R.id.dotFormatBadge)
        val dotStroke = dialogView.findViewById<View>(R.id.dotStrokeBadge)
        val dotEffects = dialogView.findViewById<View>(R.id.dotEffectsBadge)
        val dotRibbon = dialogView.findViewById<View>(R.id.dotRibbonBadge)

        // Content Views
        val rvFonts = dialogView.findViewById<RecyclerView>(R.id.rvStudioFonts)
        val viewColor = dialogView.findViewById<View>(R.id.viewStudioColor)
        val viewFormat = dialogView.findViewById<View>(R.id.viewStudioFormat)
        val viewStroke = dialogView.findViewById<View>(R.id.viewStudioStroke)
        val viewEffects = dialogView.findViewById<View>(R.id.viewStudioEffects)
        val viewRibbon = dialogView.findViewById<View>(R.id.viewStudioRibbon)

        // Update active dot badges
        fun updateActiveTabBadges() {
            dotFont?.visibility = if (currentConfig.fontResId != null) View.VISIBLE else View.GONE
            dotColor?.visibility = if (currentConfig.textColor != Color.WHITE) View.VISIBLE else View.GONE
            dotFormat?.visibility = if (currentConfig.textSizeSp != 20f || currentConfig.alignment != TextFormatDialog.TextAlignment.CENTER) View.VISIBLE else View.GONE
            dotStroke?.visibility = if (currentConfig.strokeConfig.isEnabled && currentConfig.strokeConfig.strokeWidthDp > 0) View.VISIBLE else View.GONE
            dotEffects?.visibility = if (
                currentConfig.effectConfig.shadowRadius > 0 ||
                currentConfig.effectConfig.isBold ||
                currentConfig.effectConfig.isItalic ||
                currentConfig.effectConfig.isUnderline ||
                currentConfig.effectConfig.isAllCaps
            ) View.VISIBLE else View.GONE
            dotRibbon?.visibility = if (currentConfig.highlightConfig.isEnabled && Color.alpha(currentConfig.highlightConfig.backgroundColor) > 0) View.VISIBLE else View.GONE
        }

        // Initialize Adapters
        val fontAdapter = FontStyleAdapter(themedContext)
        val colorAdapter = ColorAdapter()
        val strokeColorAdapter = ColorSwatchAdapter(includeNoneOption = false)
        val shadowColorAdapter = ColorSwatchAdapter(includeNoneOption = false)
        val ribbonColorAdapter = ColorSwatchAdapter(includeNoneOption = true)

        fun refreshAllAdapters() {
            fontAdapter.notifyItemRangeChanged(0, fontAdapter.itemCount)
            colorAdapter.notifyItemRangeChanged(0, colorAdapter.itemCount)
            strokeColorAdapter.notifyItemRangeChanged(0, strokeColorAdapter.itemCount)
            shadowColorAdapter.notifyItemRangeChanged(0, shadowColorAdapter.itemCount)
            ribbonColorAdapter.notifyItemRangeChanged(0, ribbonColorAdapter.itemCount)
        }

        // Tab structure: (StudioTab, ContainerView, TabTextView, ContentView)
        data class TabItem(val tab: StudioTab, val container: View, val tabBtn: TextView, val contentView: View)

        val tabItems = listOf(
            TabItem(StudioTab.FONT, containerFont, tabFont, rvFonts),
            TabItem(StudioTab.COLOR, containerColor, tabColor, viewColor),
            TabItem(StudioTab.FORMAT, containerFormat, tabFormat, viewFormat),
            TabItem(StudioTab.STROKE, containerStroke, tabStroke, viewStroke),
            TabItem(StudioTab.EFFECTS, containerEffects, tabEffects, viewEffects),
            TabItem(StudioTab.RIBBON, containerRibbon, tabRibbon, viewRibbon)
        )

        // Filter and show only enabled tabs
        tabItems.forEach { item ->
            item.container.visibility = if (enabledTabs.contains(item.tab)) View.VISIBLE else View.GONE
        }

        val activeEnabledItems = tabItems.filter { enabledTabs.contains(it.tab) }

        fun switchTab(activeTabBtn: TextView, activeContentView: View, targetTab: StudioTab? = null) {
            if (targetTab != null && lockProvider?.isTabLocked(targetTab) == true) {
                lockedItemClickListener?.invoke(LockableItem.StudioFeatureTab(targetTab)) {
                    refreshAllAdapters()
                    switchTab(activeTabBtn, activeContentView, null)
                }
                return
            }

            activeEnabledItems.forEach { item ->
                val isSelected = (item.tabBtn == activeTabBtn)
                if (isSelected) {
                    item.tabBtn.setBackgroundResource(R.drawable.bg_tab_selected)
                    item.tabBtn.setTextColor(Color.WHITE)
                    item.tabBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(item.tab.iconResId, 0, 0, 0)
                    item.tabBtn.compoundDrawableTintList = ColorStateList.valueOf(Color.WHITE)
                    item.tabBtn.setTypeface(null, Typeface.BOLD)
                } else {
                    item.tabBtn.setBackgroundResource(R.drawable.bg_tab_unselected)
                    item.tabBtn.setTextColor("#4B5563".toColorInt())
                    item.tabBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(item.tab.iconResId, 0, 0, 0)
                    item.tabBtn.compoundDrawableTintList = ColorStateList.valueOf("#4B5563".toColorInt())
                    item.tabBtn.setTypeface(null, Typeface.NORMAL)
                }
                item.contentView.visibility = if (item.contentView == activeContentView) View.VISIBLE else View.GONE
            }

            hsvStudioTabs?.post {
                val scrollX = activeTabBtn.left - ((hsvStudioTabs.width - activeTabBtn.width) / 2)
                hsvStudioTabs.smoothScrollTo(scrollX.coerceAtLeast(0), 0)
            }
        }

        activeEnabledItems.forEach { item ->
            item.container.setOnClickListener { switchTab(item.tabBtn, item.contentView, item.tab) }
            item.tabBtn.setOnClickListener { switchTab(item.tabBtn, item.contentView, item.tab) }
        }

        // Initially select first enabled tab
        if (activeEnabledItems.isNotEmpty()) {
            val first = activeEnabledItems.first()
            switchTab(first.tabBtn, first.contentView)
        }

        // Live preview renderer
        fun renderPreview() {
            if (showPreviewPane && tvPreview != null) {
                val textToDisplay = samplePreviewText ?: "Sample Quote / نمونہ کلام"
                val effect = currentConfig.effectConfig

                val finalText = if (effect.isAllCaps) textToDisplay.uppercase() else textToDisplay
                if (effect.isUnderline) {
                    val spannable = SpannableString(finalText)
                    spannable.setSpan(UnderlineSpan(), 0, spannable.length, 0)
                    tvPreview.text = spannable
                } else {
                    tvPreview.text = finalText
                }
                currentConfig.applyTo(tvPreview)
            }

            updateActiveTabBadges()
            livePreviewListener?.invoke(currentConfig)
            studioListener?.onTypographyApplied(currentConfig)
        }

        if (!samplePreviewText.isNullOrEmpty() && tvPreview != null) {
            tvPreview.text = samplePreviewText
        }
        renderPreview()

        // 1. Setup Fonts RecyclerView (Default first tab)
        rvFonts.layoutManager = GridLayoutManager(themedContext, 2)
        rvFonts.adapter = fontAdapter
        customFonts?.let { fontAdapter.setFonts(it) }
        fontAdapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            fontAdapter.setOnLockedItemClickListener { lockedFont, unlock ->
                listener(lockedFont) {
                    refreshAllAdapters()
                    unlock()
                }
            }
        }
        fontAdapter.setPreviewText(samplePreviewText)
        fontAdapter.setSelectedFont(currentConfig.fontResId)
        fontAdapter.setOnFontClickListener { fontResId ->
            currentConfig = currentConfig.copy(fontResId = fontResId)
            renderPreview()
        }

        // 2. Setup Color Tab (Curated Color Palettes)
        val rvColors = dialogView.findViewById<RecyclerView>(R.id.rvStudioColors)
        rvColors.layoutManager = GridLayoutManager(themedContext, 5)
        rvColors.adapter = colorAdapter
        colorAdapter.setColors(ColorPalettes.ALL_CURATED)
        colorAdapter.setSelectedColor(currentConfig.textColor)
        colorAdapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            colorAdapter.setOnLockedItemClickListener { lockedColor, unlock ->
                listener(lockedColor) {
                    refreshAllAdapters()
                    unlock()
                }
            }
        }
        colorAdapter.setOnItemClickListener { color ->
            currentConfig = currentConfig.copy(textColor = color)
            renderPreview()
        }

        val chipStudioAll = dialogView.findViewById<TextView>(R.id.chipStudioColorAll)
        val chipStudioBold = dialogView.findViewById<TextView>(R.id.chipStudioColorBold)
        val chipStudioNeon = dialogView.findViewById<TextView>(R.id.chipStudioColorNeon)
        val chipStudioCalm = dialogView.findViewById<TextView>(R.id.chipStudioColorCalm)
        val chipStudioPastel = dialogView.findViewById<TextView>(R.id.chipStudioColorPastel)
        val chipStudioDark = dialogView.findViewById<TextView>(R.id.chipStudioColorDark)
        val chipStudioVintage = dialogView.findViewById<TextView>(R.id.chipStudioColorVintage)

        val colorChips = listOfNotNull(chipStudioAll, chipStudioBold, chipStudioNeon, chipStudioCalm, chipStudioPastel, chipStudioDark, chipStudioVintage)

        fun selectColorChip(selectedChip: TextView, colors: List<Int>) {
            colorChips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = ColorStateList.valueOf("#1F2937".toColorInt())
                    chip.setTextColor(Color.WHITE)
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.backgroundTintList = ColorStateList.valueOf("#F3F4F6".toColorInt())
                    chip.setTextColor("#4B5563".toColorInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            colorAdapter.setColors(colors)
        }

        chipStudioAll?.setOnClickListener { selectColorChip(chipStudioAll, ColorPalettes.ALL_CURATED) }
        chipStudioBold?.setOnClickListener { selectColorChip(chipStudioBold, ColorPalettes.MOTIVATIONAL_BOLD) }
        chipStudioNeon?.setOnClickListener { selectColorChip(chipStudioNeon, ColorPalettes.AESTHETIC_NEON) }
        chipStudioCalm?.setOnClickListener { selectColorChip(chipStudioCalm, ColorPalettes.NATURE_SUFI_CALM) }
        chipStudioPastel?.setOnClickListener { selectColorChip(chipStudioPastel, ColorPalettes.PASTEL_SOFT) }
        chipStudioDark?.setOnClickListener { selectColorChip(chipStudioDark, ColorPalettes.MELANCHOLY_DARK) }
        chipStudioVintage?.setOnClickListener { selectColorChip(chipStudioVintage, ColorPalettes.VINTAGE_EARTHY) }

        // 3. Setup Format View (Size & Alignment)
        val sbSize = dialogView.findViewById<SeekBar>(R.id.sbStudioTextSize)
        val tvSizeVal = dialogView.findViewById<TextView>(R.id.tvStudioTextSizeVal)
        val tgAlignment = dialogView.findViewById<MaterialButtonToggleGroup>(R.id.tgStudioAlignment)
        val btnLeft = dialogView.findViewById<Button>(R.id.btnStudioAlignLeft)
        val btnCenter = dialogView.findViewById<Button>(R.id.btnStudioAlignCenter)
        val btnRight = dialogView.findViewById<Button>(R.id.btnStudioAlignRight)

        sbSize.progress = (currentConfig.textSizeSp - 12f).coerceAtLeast(0f).toInt()
        tvSizeVal.text = "${currentConfig.textSizeSp.toInt()}sp"

        sbSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = 12f + progress
                currentConfig = currentConfig.copy(textSizeSp = size)
                tvSizeVal.text = "${size.toInt()}sp"
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        fun updateAlignmentToggle(alignment: TextFormatDialog.TextAlignment) {
            when (alignment) {
                TextFormatDialog.TextAlignment.LEFT -> tgAlignment?.check(R.id.btnStudioAlignLeft)
                TextFormatDialog.TextAlignment.CENTER -> tgAlignment?.check(R.id.btnStudioAlignCenter)
                TextFormatDialog.TextAlignment.RIGHT -> tgAlignment?.check(R.id.btnStudioAlignRight)
            }
        }
        updateAlignmentToggle(currentConfig.alignment)

        btnLeft?.setOnClickListener {
            currentConfig = currentConfig.copy(alignment = TextFormatDialog.TextAlignment.LEFT)
            renderPreview()
        }
        btnCenter?.setOnClickListener {
            currentConfig = currentConfig.copy(alignment = TextFormatDialog.TextAlignment.CENTER)
            renderPreview()
        }
        btnRight?.setOnClickListener {
            currentConfig = currentConfig.copy(alignment = TextFormatDialog.TextAlignment.RIGHT)
            renderPreview()
        }

        // Letter Spacing & Line Spacing
        val sbLetterSpacing = dialogView.findViewById<SeekBar>(R.id.sbStudioLetterSpacing)
        val tvLetterSpacingVal = dialogView.findViewById<TextView>(R.id.tvStudioLetterSpacingVal)
        val sbLineSpacing = dialogView.findViewById<SeekBar>(R.id.sbStudioLineSpacing)
        val tvLineSpacingVal = dialogView.findViewById<TextView>(R.id.tvStudioLineSpacingVal)

        sbLetterSpacing?.progress = (currentConfig.effectConfig.letterSpacing * 100).toInt().coerceIn(0, 30)
        tvLetterSpacingVal?.text = String.format("%.2f", currentConfig.effectConfig.letterSpacing)
        sbLetterSpacing?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val ls = progress / 100f
                currentConfig = currentConfig.copy(
                    effectConfig = currentConfig.effectConfig.copy(letterSpacing = ls)
                )
                tvLetterSpacingVal?.text = String.format("%.2f", ls)
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        sbLineSpacing?.progress = ((currentConfig.effectConfig.lineSpacingMultiplier - 0.8f) * 10).toInt().coerceIn(0, 20)
        tvLineSpacingVal?.text = String.format("%.1fx", currentConfig.effectConfig.lineSpacingMultiplier)
        sbLineSpacing?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val mult = 0.8f + (progress / 10f)
                currentConfig = currentConfig.copy(
                    effectConfig = currentConfig.effectConfig.copy(lineSpacingMultiplier = mult)
                )
                tvLineSpacingVal?.text = String.format("%.1fx", mult)
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // 4. Setup Stroke View
        val sbStroke = dialogView.findViewById<SeekBar>(R.id.sbStudioStroke)
        val tvStrokeVal = dialogView.findViewById<TextView>(R.id.tvStudioStrokeVal)
        val rvStrokeColors = dialogView.findViewById<RecyclerView>(R.id.rvStudioStrokeColors)

        sbStroke.progress = currentConfig.strokeConfig.strokeWidthDp.toInt()
        tvStrokeVal.text = "${currentConfig.strokeConfig.strokeWidthDp.toInt()}dp"
        sbStroke.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(
                    strokeConfig = currentConfig.strokeConfig.copy(
                        strokeWidthDp = progress.toFloat(),
                        isEnabled = progress > 0
                    )
                )
                tvStrokeVal.text = "${progress}dp"
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        rvStrokeColors.layoutManager = LinearLayoutManager(themedContext, LinearLayoutManager.HORIZONTAL, false)
        rvStrokeColors.adapter = strokeColorAdapter
        strokeColorAdapter.setColors(ColorPalettes.ALL_CURATED)
        strokeColorAdapter.setSelectedColor(currentConfig.strokeConfig.strokeColor)
        strokeColorAdapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            strokeColorAdapter.setOnLockedItemClickListener { lockedColor, unlock ->
                listener(lockedColor) {
                    refreshAllAdapters()
                    unlock()
                }
            }
        }
        strokeColorAdapter.setOnSwatchClickListener { color, _ ->
            val strokeWidth = if (currentConfig.strokeConfig.strokeWidthDp <= 0f) 4f else currentConfig.strokeConfig.strokeWidthDp
            currentConfig = currentConfig.copy(
                strokeConfig = currentConfig.strokeConfig.copy(
                    strokeColor = color,
                    strokeWidthDp = strokeWidth,
                    isEnabled = true
                )
            )
            sbStroke.progress = strokeWidth.toInt()
            tvStrokeVal.text = "${strokeWidth.toInt()}dp"
            renderPreview()
        }

        val chipStrokeAll = dialogView.findViewById<TextView>(R.id.chipStudioStrokeAll)
        val chipStrokeBold = dialogView.findViewById<TextView>(R.id.chipStudioStrokeBold)
        val chipStrokeNeon = dialogView.findViewById<TextView>(R.id.chipStudioStrokeNeon)
        val chipStrokeCalm = dialogView.findViewById<TextView>(R.id.chipStudioStrokeCalm)
        val chipStrokePastel = dialogView.findViewById<TextView>(R.id.chipStudioStrokePastel)
        val chipStrokeDark = dialogView.findViewById<TextView>(R.id.chipStudioStrokeDark)
        val chipStrokeVintage = dialogView.findViewById<TextView>(R.id.chipStudioStrokeVintage)

        val strokeChips = listOfNotNull(chipStrokeAll, chipStrokeBold, chipStrokeNeon, chipStrokeCalm, chipStrokePastel, chipStrokeDark, chipStrokeVintage)

        fun selectStrokeChip(selectedChip: TextView, colors: List<Int>) {
            strokeChips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = ColorStateList.valueOf("#1F2937".toColorInt())
                    chip.setTextColor(Color.WHITE)
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.backgroundTintList = ColorStateList.valueOf("#F3F4F6".toColorInt())
                    chip.setTextColor("#4B5563".toColorInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            strokeColorAdapter.setColors(colors)
        }

        chipStrokeAll?.setOnClickListener { selectStrokeChip(chipStrokeAll, ColorPalettes.ALL_CURATED) }
        chipStrokeBold?.setOnClickListener { selectStrokeChip(chipStrokeBold, ColorPalettes.MOTIVATIONAL_BOLD) }
        chipStrokeNeon?.setOnClickListener { selectStrokeChip(chipStrokeNeon, ColorPalettes.AESTHETIC_NEON) }
        chipStrokeCalm?.setOnClickListener { selectStrokeChip(chipStrokeCalm, ColorPalettes.NATURE_SUFI_CALM) }
        chipStrokePastel?.setOnClickListener { selectStrokeChip(chipStrokePastel, ColorPalettes.PASTEL_SOFT) }
        chipStrokeDark?.setOnClickListener { selectStrokeChip(chipStrokeDark, ColorPalettes.MELANCHOLY_DARK) }
        chipStrokeVintage?.setOnClickListener { selectStrokeChip(chipStrokeVintage, ColorPalettes.VINTAGE_EARTHY) }

        // 5. Setup Effects View
        val btnBold = dialogView.findViewById<Button>(R.id.btnStudioBold)
        val btnItalic = dialogView.findViewById<Button>(R.id.btnStudioItalic)
        val btnUnderline = dialogView.findViewById<Button>(R.id.btnStudioUnderline)
        val btnCaps = dialogView.findViewById<Button>(R.id.btnStudioCaps)
        val sbShadow = dialogView.findViewById<SeekBar>(R.id.sbStudioShadow)
        val tvShadowVal = dialogView.findViewById<TextView>(R.id.tvStudioShadowValue)
        val rvShadowColors = dialogView.findViewById<RecyclerView>(R.id.rvStudioShadowColors)

        btnBold.setOnClickListener {
            val eff = currentConfig.effectConfig
            currentConfig = currentConfig.copy(effectConfig = eff.copy(isBold = !eff.isBold))
            renderPreview()
        }
        btnItalic.setOnClickListener {
            val eff = currentConfig.effectConfig
            currentConfig = currentConfig.copy(effectConfig = eff.copy(isItalic = !eff.isItalic))
            renderPreview()
        }
        btnUnderline.setOnClickListener {
            val eff = currentConfig.effectConfig
            currentConfig = currentConfig.copy(effectConfig = eff.copy(isUnderline = !eff.isUnderline))
            renderPreview()
        }
        btnCaps.setOnClickListener {
            val eff = currentConfig.effectConfig
            currentConfig = currentConfig.copy(effectConfig = eff.copy(isAllCaps = !eff.isAllCaps))
            renderPreview()
        }

        sbShadow.progress = currentConfig.effectConfig.shadowRadius.toInt()
        tvShadowVal.text = "${currentConfig.effectConfig.shadowRadius.toInt()}dp"
        sbShadow.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val eff = currentConfig.effectConfig
                currentConfig = currentConfig.copy(effectConfig = eff.copy(shadowRadius = progress.toFloat()))
                tvShadowVal.text = "${progress}dp"
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        rvShadowColors.layoutManager = LinearLayoutManager(themedContext, LinearLayoutManager.HORIZONTAL, false)
        rvShadowColors.adapter = shadowColorAdapter
        shadowColorAdapter.setColors(ColorPalettes.ALL_CURATED)
        shadowColorAdapter.setSelectedColor(currentConfig.effectConfig.shadowColor)
        shadowColorAdapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            shadowColorAdapter.setOnLockedItemClickListener { lockedColor, unlock ->
                listener(lockedColor) {
                    refreshAllAdapters()
                    unlock()
                }
            }
        }
        shadowColorAdapter.setOnSwatchClickListener { color, _ ->
            val eff = currentConfig.effectConfig
            val radius = if (eff.shadowRadius <= 0f) 6f else eff.shadowRadius
            currentConfig = currentConfig.copy(
                effectConfig = eff.copy(
                    shadowColor = color,
                    shadowRadius = radius
                )
            )
            sbShadow.progress = radius.toInt()
            tvShadowVal.text = "${radius.toInt()}dp"
            renderPreview()
        }

        // 6. Setup Ribbon View
        val sbRadius = dialogView.findViewById<SeekBar>(R.id.sbStudioRadius)
        val tvRadiusVal = dialogView.findViewById<TextView>(R.id.tvStudioRadiusVal)
        val rvRibbonColors = dialogView.findViewById<RecyclerView>(R.id.rvStudioRibbonColors)

        sbRadius.progress = currentConfig.highlightConfig.cornerRadiusDp.toInt()
        tvRadiusVal.text = "${currentConfig.highlightConfig.cornerRadiusDp.toInt()}dp"
        sbRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                currentConfig = currentConfig.copy(
                    highlightConfig = currentConfig.highlightConfig.copy(cornerRadiusDp = progress.toFloat())
                )
                tvRadiusVal.text = "${progress}dp"
                renderPreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        rvRibbonColors.layoutManager = LinearLayoutManager(themedContext, LinearLayoutManager.HORIZONTAL, false)
        rvRibbonColors.adapter = ribbonColorAdapter
        ribbonColorAdapter.setColors(ColorPalettes.ALL_CURATED)
        ribbonColorAdapter.setLockProvider(lockProvider)
        lockedItemClickListener?.let { listener ->
            ribbonColorAdapter.setOnLockedItemClickListener { lockedColor, unlock ->
                listener(lockedColor) {
                    refreshAllAdapters()
                    unlock()
                }
            }
        }
        ribbonColorAdapter.setSelectedColor(
            if (currentConfig.highlightConfig.isEnabled) currentConfig.highlightConfig.backgroundColor else null,
            isNone = !currentConfig.highlightConfig.isEnabled
        )
        ribbonColorAdapter.setOnSwatchClickListener { color, isNone ->
            currentConfig = currentConfig.copy(
                highlightConfig = currentConfig.highlightConfig.copy(
                    backgroundColor = if (isNone) Color.TRANSPARENT else color,
                    isEnabled = !isNone
                )
            )
            renderPreview()
        }

        val chipRibbonAll = dialogView.findViewById<TextView>(R.id.chipStudioRibbonAll)
        val chipRibbonBold = dialogView.findViewById<TextView>(R.id.chipStudioRibbonBold)
        val chipRibbonNeon = dialogView.findViewById<TextView>(R.id.chipStudioRibbonNeon)
        val chipRibbonCalm = dialogView.findViewById<TextView>(R.id.chipStudioRibbonCalm)
        val chipRibbonPastel = dialogView.findViewById<TextView>(R.id.chipStudioRibbonPastel)
        val chipRibbonDark = dialogView.findViewById<TextView>(R.id.chipStudioRibbonDark)
        val chipRibbonVintage = dialogView.findViewById<TextView>(R.id.chipStudioRibbonVintage)

        val ribbonChips = listOfNotNull(chipRibbonAll, chipRibbonBold, chipRibbonNeon, chipRibbonCalm, chipRibbonPastel, chipRibbonDark, chipRibbonVintage)

        fun selectRibbonChip(selectedChip: TextView, colors: List<Int>) {
            ribbonChips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.backgroundTintList = ColorStateList.valueOf("#1F2937".toColorInt())
                    chip.setTextColor(Color.WHITE)
                    chip.setTypeface(null, Typeface.BOLD)
                } else {
                    chip.backgroundTintList = ColorStateList.valueOf("#F3F4F6".toColorInt())
                    chip.setTextColor("#4B5563".toColorInt())
                    chip.setTypeface(null, Typeface.NORMAL)
                }
            }
            ribbonColorAdapter.setColors(colors)
        }

        chipRibbonAll?.setOnClickListener { selectRibbonChip(chipRibbonAll, ColorPalettes.ALL_CURATED) }
        chipRibbonBold?.setOnClickListener { selectRibbonChip(chipRibbonBold, ColorPalettes.MOTIVATIONAL_BOLD) }
        chipRibbonNeon?.setOnClickListener { selectRibbonChip(chipRibbonNeon, ColorPalettes.AESTHETIC_NEON) }
        chipRibbonCalm?.setOnClickListener { selectRibbonChip(chipRibbonCalm, ColorPalettes.NATURE_SUFI_CALM) }
        chipRibbonPastel?.setOnClickListener { selectRibbonChip(chipRibbonPastel, ColorPalettes.PASTEL_SOFT) }
        chipRibbonDark?.setOnClickListener { selectRibbonChip(chipRibbonDark, ColorPalettes.MELANCHOLY_DARK) }
        chipRibbonVintage?.setOnClickListener { selectRibbonChip(chipRibbonVintage, ColorPalettes.VINTAGE_EARTHY) }

        // 1-Tap Reset Handler (Restores initialConfig with which the studio opened)
        btnReset?.setOnClickListener {
            currentConfig = initialConfig
            fontAdapter.setSelectedFont(currentConfig.fontResId)
            colorAdapter.setSelectedColor(currentConfig.textColor)
            strokeColorAdapter.setSelectedColor(currentConfig.strokeConfig.strokeColor)
            shadowColorAdapter.setSelectedColor(currentConfig.effectConfig.shadowColor)
            ribbonColorAdapter.setSelectedColor(
                if (currentConfig.highlightConfig.isEnabled) currentConfig.highlightConfig.backgroundColor else null,
                isNone = !currentConfig.highlightConfig.isEnabled
            )

            sbSize.progress = (currentConfig.textSizeSp - 12f).coerceAtLeast(0f).toInt()
            tvSizeVal.text = "${currentConfig.textSizeSp.toInt()}sp"
            updateAlignmentToggle(currentConfig.alignment)

            sbLetterSpacing?.progress = (currentConfig.effectConfig.letterSpacing * 100).toInt().coerceIn(0, 30)
            tvLetterSpacingVal?.text = String.format("%.2f", currentConfig.effectConfig.letterSpacing)
            sbLineSpacing?.progress = ((currentConfig.effectConfig.lineSpacingMultiplier - 0.8f) * 10).toInt().coerceIn(0, 20)
            tvLineSpacingVal?.text = String.format("%.1fx", currentConfig.effectConfig.lineSpacingMultiplier)

            sbStroke.progress = currentConfig.strokeConfig.strokeWidthDp.toInt()
            tvStrokeVal.text = "${currentConfig.strokeConfig.strokeWidthDp.toInt()}dp"

            sbShadow.progress = currentConfig.effectConfig.shadowRadius.toInt()
            tvShadowVal.text = "${currentConfig.effectConfig.shadowRadius.toInt()}dp"

            sbRadius.progress = currentConfig.highlightConfig.cornerRadiusDp.toInt()
            tvRadiusVal.text = "${currentConfig.highlightConfig.cornerRadiusDp.toInt()}dp"

            renderPreview()
        }

        btnClose.setOnClickListener {
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    /**
     * Fluent Builder for constructing and displaying a [TextStudioDialog].
     */
    class Builder(private val context: Context) {
        private var config = TextTypographyConfig()
        private var previewText: String? = null
        private var fonts: List<Int>? = null
        private var enabledTabs: Set<StudioTab> = StudioTab.DEFAULT
        private var showPreviewPane: Boolean = true
        private var backgroundDrawable: Drawable? = null
        private var backgroundDrawableRes: Int? = null
        private var backgroundColorInt: Int? = null
        private var lockProvider: ItemLockProvider? = null
        private var lockedItemClickListener: ((LockableItem, unlock: () -> Unit) -> Unit)? = null
        private var listener: TextStudioListener? = null
        private var livePreviewListener: ((TextTypographyConfig) -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        /** Sets the initial typography configuration. */
        fun setConfig(config: TextTypographyConfig) = apply { this.config = config }

        /** Sets custom preview sample text. */
        fun setPreviewText(text: String?) = apply { this.previewText = text }

        /** Sets the list of font resource IDs. */
        fun setFonts(fonts: List<Int>) = apply { this.fonts = fonts }

        /** Sets the font resource IDs. */
        fun setFonts(vararg fonts: Int) = apply { this.fonts = fonts.toList() }

        /** Sets enabled tabs. */
        fun setTabs(vararg tabs: StudioTab) = apply { this.enabledTabs = tabs.toSet() }

        /** Sets enabled tabs. */
        fun setTabs(tabs: Set<StudioTab>) = apply { this.enabledTabs = tabs }

        /** Controls visibility of the top in-dialog preview pane. */
        fun setShowPreviewPane(show: Boolean) = apply { this.showPreviewPane = show }

        /** Sets a background wallpaper drawable for the preview canvas. */
        fun setBackgroundDrawable(drawable: Drawable?) = apply { this.backgroundDrawable = drawable }

        /** Sets a background wallpaper drawable resource ID for the preview canvas. */
        fun setBackgroundRes(resId: Int?) = apply { this.backgroundDrawableRes = resId }

        /** Sets a background color for the preview canvas. */
        fun setBackgroundColor(color: Int?) = apply { this.backgroundColorInt = color }

        /** Sets monetization item lock provider. */
        fun setLockProvider(provider: ItemLockProvider) = apply { this.lockProvider = provider }

        /** Locks specified font resource IDs behind monetization. */
        fun setLockedFonts(vararg fontResIds: Int) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockFonts(*fontResIds)
        }

        /** Locks specified font resource IDs behind monetization. */
        fun setLockedFonts(fontResIds: Collection<Int>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockFonts(fontResIds)
        }

        /** Locks specified color values behind monetization. */
        fun setLockedColors(vararg colors: Int) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockColors(*colors)
        }

        /** Locks specified color values behind monetization. */
        fun setLockedColors(colors: Collection<Int>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockColors(colors)
        }

        /** Locks specified studio feature tabs behind monetization. */
        fun setLockedTabs(vararg tabs: StudioTab) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockTabs(*tabs)
        }

        /** Locks specified studio feature tabs behind monetization. */
        fun setLockedTabs(tabs: Collection<StudioTab>) = apply {
            val provider = (this.lockProvider as? DefaultItemLockProvider) ?: DefaultItemLockProvider().also { this.lockProvider = it }
            provider.lockTabs(tabs)
        }

        /** Intercepts clicks on locked items/tabs to trigger ads or IAP prompts. */
        fun setOnLockedItemClicked(listener: (LockableItem, unlock: () -> Unit) -> Unit) = apply {
            this.lockedItemClickListener = listener
        }

        /** Receives real-time typography updates as user tweaks controls. */
        fun setOnLivePreviewListener(listener: (TextTypographyConfig) -> Unit) = apply {
            this.livePreviewListener = listener
        }

        /** Receives the final typography configuration upon dialog completion. */
        fun setOnTypographyApplied(listener: (TextTypographyConfig) -> Unit) = apply {
            this.listener = TextStudioListener { listener(it) }
        }

        /** Sets dialog dismissal listener. */
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        /** Builds the [TextStudioDialog] instance. */
        fun build(): TextStudioDialog {
            val dialog = TextStudioDialog(context)
            dialog.setConfig(config)
            dialog.setPreviewText(previewText)
            fonts?.let { dialog.setFonts(it) }
            dialog.setTabs(enabledTabs)
            dialog.setShowPreviewPane(showPreviewPane)
            dialog.setBackgroundDrawable(backgroundDrawable)
            dialog.setBackgroundRes(backgroundDrawableRes)
            dialog.setBackgroundColor(backgroundColorInt)
            dialog.setLockProvider(lockProvider)
            lockedItemClickListener?.let { dialog.setOnLockedItemClickListener(it) }
            listener?.let { dialog.setStudioListener(it) }
            livePreviewListener?.let { dialog.setOnLivePreviewListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

        /** Builds and displays the [TextStudioDialog]. */
        fun show(onApplied: ((TextTypographyConfig) -> Unit)? = null): TextStudioDialog {
            val dialog = build()
            if (onApplied != null) {
                dialog.setStudioListener { onApplied(it) }
            }
            dialog.showTextStudioDialog()
            return dialog
        }
    }
}
