package com.elytelabs.dialoghub.dialogs

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
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
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.models.StudioTab
import com.elytelabs.dialoghub.models.TextHighlightConfig
import com.elytelabs.dialoghub.models.TextStrokeConfig
import com.elytelabs.dialoghub.models.TextTypographyConfig
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.dialoghub.utils.DialogThemeHelper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup

/**
 * All-In-One Unified Text Studio Dialog for comprehensive typography styling:
 * Fonts, Colors, Formatting (Size & Alignment), Text Stroke/Outline,
 * Visual Effects (Styles & Shadow), and Background Ribbon/Highlight.
 *
 * Supports feature customization via [enabledTabs], active effect badges, and optional [showPreviewPane].
 */
class TextStudioDialog(private val context: Context) {

    private var currentConfig = TextTypographyConfig()
    private var samplePreviewText: String? = null
    private var customFonts: List<Int>? = null
    private var enabledTabs: Set<StudioTab> = StudioTab.DEFAULT
    private var showPreviewPane: Boolean = false
    private var presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
    private var studioListener: TextStudioListener? = null
    private var livePreviewListener: ((TextTypographyConfig) -> Unit)? = null
    private var dismissListener: (() -> Unit)? = null

    fun interface TextStudioListener {
        fun onTypographyApplied(config: TextTypographyConfig)
    }

    fun setConfig(config: TextTypographyConfig) = apply { this.currentConfig = config }
    fun setPreviewText(text: String?) = apply { this.samplePreviewText = text }
    fun setFonts(fonts: List<Int>) = apply { this.customFonts = fonts }
    fun setTabs(vararg tabs: StudioTab) = apply { this.enabledTabs = tabs.toSet() }
    fun setTabs(tabs: Set<StudioTab>) = apply { this.enabledTabs = tabs }
    fun setShowPreviewPane(show: Boolean) = apply { this.showPreviewPane = show }
    fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
    fun setStudioListener(listener: TextStudioListener) = apply { this.studioListener = listener }
    fun setStudioListener(listener: (TextTypographyConfig) -> Unit) = apply {
        this.studioListener = TextStudioListener { listener(it) }
    }
    fun setOnLivePreviewListener(listener: (TextTypographyConfig) -> Unit) = apply {
        this.livePreviewListener = listener
    }
    fun setOnDismissListener(listener: () -> Unit) = apply { this.dismissListener = listener }

    fun show(
        initialConfig: TextTypographyConfig = this.currentConfig,
        previewText: String? = null,
        fonts: List<Int>? = null,
        enabledTabs: Set<StudioTab> = this.enabledTabs,
        showPreviewPane: Boolean = this.showPreviewPane,
        presentationStyle: PresentationStyle = this.presentationStyle,
        onLivePreview: ((config: TextTypographyConfig) -> Unit)? = null,
        onTypographyApplied: (config: TextTypographyConfig) -> Unit
    ) {
        this.currentConfig = initialConfig
        if (previewText != null) this.samplePreviewText = previewText
        if (fonts != null) this.customFonts = fonts
        this.enabledTabs = enabledTabs
        this.showPreviewPane = showPreviewPane
        this.presentationStyle = presentationStyle
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
            dismissListener?.invoke()
        }

        val dragHandle = dialogView.findViewById<View>(R.id.dragHandle)
        dragHandle?.visibility = View.VISIBLE

        val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)
        val cardPreview = dialogView.findViewById<CardView>(R.id.cardStudioPreview)
        val btnCanvasToggle = dialogView.findViewById<ImageView>(R.id.btnStudioCanvasToggle)
        val tvPreview = dialogView.findViewById<TextView>(R.id.tvStudioPreview)
        val btnApply = dialogView.findViewById<Button>(R.id.btnApplyStudio)
        val hsvStudioTabs = dialogView.findViewById<android.widget.HorizontalScrollView>(R.id.hsvStudioTabs)

        // In-dialog preview pane visibility (default false for on-screen live sync)
        cardPreview?.visibility = if (showPreviewPane) View.VISIBLE else View.GONE

        // Canvas Contrast Mode Toggle (0: Dark Slate, 1: Light Slate, 2: Warm Parchment)
        var canvasMode = 0
        val canvasColors = listOf("#0F172A".toColorInt(), "#F8FAFC".toColorInt(), "#FEF3C7".toColorInt())

        fun updateCanvasMode() {
            cardPreview?.setCardBackgroundColor(canvasColors[canvasMode])
            if (canvasMode == 0) {
                btnCanvasToggle?.backgroundTintList = ColorStateList.valueOf("#33FFFFFF".toColorInt())
                btnCanvasToggle?.setColorFilter(Color.WHITE)
            } else {
                btnCanvasToggle?.backgroundTintList = ColorStateList.valueOf("#33000000".toColorInt())
                btnCanvasToggle?.setColorFilter("#1F2937".toColorInt())
            }
        }
        btnCanvasToggle?.setOnClickListener {
            canvasMode = (canvasMode + 1) % canvasColors.size
            updateCanvasMode()
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

        fun switchTab(activeTabBtn: TextView, activeContentView: View) {
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
            item.container.setOnClickListener { switchTab(item.tabBtn, item.contentView) }
            item.tabBtn.setOnClickListener { switchTab(item.tabBtn, item.contentView) }
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
        }

        if (!samplePreviewText.isNullOrEmpty() && tvPreview != null) {
            tvPreview.text = samplePreviewText
        }
        renderPreview()

        // 1. Setup Fonts RecyclerView (Default first tab)
        rvFonts.layoutManager = GridLayoutManager(themedContext, 2)
        val fontAdapter = FontStyleAdapter(themedContext)
        rvFonts.adapter = fontAdapter
        customFonts?.let { fontAdapter.setFonts(it) }
        fontAdapter.setPreviewText(samplePreviewText)
        fontAdapter.setSelectedFont(currentConfig.fontResId)
        fontAdapter.setOnFontClickListener { fontResId ->
            currentConfig = currentConfig.copy(fontResId = fontResId)
            renderPreview()
        }

        // 2. Setup Colors RecyclerView with Category Chips
        val rvColors = dialogView.findViewById<RecyclerView>(R.id.rvStudioColors)
        rvColors.layoutManager = GridLayoutManager(themedContext, 5)
        val colorAdapter = ColorAdapter()
        rvColors.adapter = colorAdapter
        colorAdapter.setColors(ColorPalettes.ALL_CURATED)
        colorAdapter.setSelectedColor(currentConfig.textColor)
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

        when (currentConfig.alignment) {
            TextFormatDialog.TextAlignment.LEFT -> tgAlignment?.check(R.id.btnStudioAlignLeft)
            TextFormatDialog.TextAlignment.CENTER -> tgAlignment?.check(R.id.btnStudioAlignCenter)
            TextFormatDialog.TextAlignment.RIGHT -> tgAlignment?.check(R.id.btnStudioAlignRight)
        }

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
        val strokeColorAdapter = ColorSwatchAdapter(includeNoneOption = false)
        rvStrokeColors.adapter = strokeColorAdapter
        strokeColorAdapter.setColors(ColorPalettes.ALL_CURATED)
        strokeColorAdapter.setSelectedColor(currentConfig.strokeConfig.strokeColor)
        strokeColorAdapter.setOnSwatchClickListener { color, _ ->
            currentConfig = currentConfig.copy(
                strokeConfig = currentConfig.strokeConfig.copy(
                    strokeColor = color,
                    isEnabled = currentConfig.strokeConfig.strokeWidthDp > 0
                )
            )
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
        val shadowColorAdapter = ColorSwatchAdapter(includeNoneOption = false)
        rvShadowColors.adapter = shadowColorAdapter
        shadowColorAdapter.setColors(ColorPalettes.ALL_CURATED)
        shadowColorAdapter.setSelectedColor(currentConfig.effectConfig.shadowColor)
        shadowColorAdapter.setOnSwatchClickListener { color, _ ->
            val eff = currentConfig.effectConfig
            currentConfig = currentConfig.copy(effectConfig = eff.copy(shadowColor = color))
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
        val ribbonColorAdapter = ColorSwatchAdapter(includeNoneOption = true)
        rvRibbonColors.adapter = ribbonColorAdapter
        ribbonColorAdapter.setColors(ColorPalettes.ALL_CURATED)
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

        btnClose.setOnClickListener { bottomSheet.dismiss() }

        btnApply.setOnClickListener {
            studioListener?.onTypographyApplied(currentConfig)
            bottomSheet.dismiss()
        }

        bottomSheet.show()
    }

    class Builder(private val context: Context) {
        private var config = TextTypographyConfig()
        private var previewText: String? = null
        private var fonts: List<Int>? = null
        private var enabledTabs: Set<StudioTab> = StudioTab.DEFAULT
        private var showPreviewPane: Boolean = false
        private var presentationStyle = PresentationStyle.BOTTOM_SHEET
        private var listener: TextStudioListener? = null
        private var livePreviewListener: ((TextTypographyConfig) -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        fun setConfig(config: TextTypographyConfig) = apply { this.config = config }
        fun setPreviewText(text: String?) = apply { this.previewText = text }
        fun setFonts(fonts: List<Int>) = apply { this.fonts = fonts }
        fun setTabs(vararg tabs: StudioTab) = apply { this.enabledTabs = tabs.toSet() }
        fun setTabs(tabs: Set<StudioTab>) = apply { this.enabledTabs = tabs }
        fun setShowPreviewPane(show: Boolean) = apply { this.showPreviewPane = show }
        fun setPresentationStyle(style: PresentationStyle) = apply { this.presentationStyle = style }
        fun setOnLivePreviewListener(listener: (TextTypographyConfig) -> Unit) = apply {
            this.livePreviewListener = listener
        }
        fun setOnTypographyApplied(listener: (TextTypographyConfig) -> Unit) = apply {
            this.listener = TextStudioListener { listener(it) }
        }
        fun setOnDismiss(listener: () -> Unit) = apply { this.dismissListener = listener }

        fun build(): TextStudioDialog {
            val dialog = TextStudioDialog(context)
            dialog.setConfig(config)
            dialog.setPreviewText(previewText)
            fonts?.let { dialog.setFonts(it) }
            dialog.setTabs(enabledTabs)
            dialog.setShowPreviewPane(showPreviewPane)
            dialog.setPresentationStyle(presentationStyle)
            listener?.let { dialog.setStudioListener(it) }
            livePreviewListener?.let { dialog.setOnLivePreviewListener(it) }
            dismissListener?.let { dialog.setOnDismissListener(it) }
            return dialog
        }

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
