package com.elytelabs.dialoghub

import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.elytelabs.dialoghub.demo.R
import com.elytelabs.dialoghub.dialogs.ImageSelectorDialog
import com.elytelabs.dialoghub.dialogs.TextStudioDialog
import com.elytelabs.dialoghub.models.StudioTab
import com.elytelabs.dialoghub.models.TextAlignment
import com.elytelabs.dialoghub.models.TextTypographyConfig
import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
import com.elytelabs.dialoghub.monetization.LockableItem
import com.elytelabs.dialoghub.monetization.UsageQuotaManager
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: RelativeLayout
    private lateinit var previewCard: CardView
    private lateinit var ivMainPreviewBg: ImageView
    private lateinit var textView: TextView
    private lateinit var tvQuotaStatus: TextView
    private lateinit var tvProBadge: TextView
    private lateinit var btnSimulateRewardedAd: Button
    private lateinit var btnTogglePro: Button

    private lateinit var quotaManager: UsageQuotaManager
    private val lockProvider = DefaultItemLockProvider()

    private var currentBackgroundRes: Int? = null
    private var currentColor: Int? = null

    private var currentTypography = TextTypographyConfig(
        textColor = Color.WHITE,
        textSizeSp = 18f,
        alignment = TextAlignment.CENTER
    )

    private fun updateActiveBackground(resId: Int? = null, color: Int? = null, drawable: Drawable? = null) {
        currentBackgroundRes = resId
        currentColor = color

        if (drawable != null) {
            ivMainPreviewBg.setImageDrawable(drawable)
            ivMainPreviewBg.visibility = View.VISIBLE
            rootLayout.background = drawable
        } else if (resId != null) {
            ivMainPreviewBg.setImageResource(resId)
            ivMainPreviewBg.visibility = View.VISIBLE
            rootLayout.setBackgroundResource(resId)
        } else if (color != null) {
            ivMainPreviewBg.visibility = View.GONE
            previewCard.setCardBackgroundColor(color)
            rootLayout.background = null
            rootLayout.setBackgroundColor(color)
        }
    }

    // System gallery picker launcher
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    if (bitmap != null) {
                        updateActiveBackground(drawable = bitmap.toDrawable(resources))
                        Toast.makeText(this, "Gallery photo applied as background!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateQuotaUI() {
        if (quotaManager.isProUser()) {
            tvQuotaStatus.text = "VIP PRO Status: Unlimited Free Access!"
            tvProBadge.text = " PRO VIP"
            tvProBadge.setBackgroundColor("#10B981".toColorInt())
        } else if (lockProvider.isPassActive()) {
            val remaining = lockProvider.getRemainingPassFormatted()
            tvQuotaStatus.text = "12-Hour VIP Pass Active: $remaining left"
            tvProBadge.text = "⏱ 12H PASS"
            tvProBadge.setBackgroundColor("#F59E0B".toColorInt())
        } else {
            val remaining = quotaManager.getRemainingEdits()
            tvQuotaStatus.text = "Daily Free Quota: $remaining edits left"
            tvProBadge.text = "FREE TIER"
            tvProBadge.setBackgroundColor("#3B82F6".toColorInt())
        }
    }

    private fun setupLockProvider() {
        lockProvider.attachTimedPass(this)
        if (quotaManager.isProUser()) {
            lockProvider.unlockAll()
        } else {
            // Lock VIP fonts
            lockProvider.lockFonts(R.font.righteous, R.font.sofadi_one)
            // Lock VIP backgrounds
            lockProvider.lockBackgrounds(R.drawable.bg5)
        }
    }

    private fun handleLockedItem(item: LockableItem, onUnlocked: () -> Unit) {
        val itemName = when (item) {
            is LockableItem.Font -> "VIP Font"
            is LockableItem.Background -> "VIP Wallpaper"
            is LockableItem.Color -> "VIP Color"
            is LockableItem.StudioFeatureTab -> "Premium Studio Tool (${item.tab.name})"
            is LockableItem.GalleryPicker -> "Gallery Import"
        }

        AlertDialog.Builder(this)
            .setTitle("🔒 Unlock $itemName")
            .setMessage("Watch a short video ad to unlock ALL VIP features, fonts, and tools for 12 HOURS!")
            .setPositiveButton("📺 Watch Video (12h Pass)") { _, _ ->
                lockProvider.grantTimedPass(hours = 12)
                quotaManager.addBonusEdits(5)
                updateQuotaUI()
                onUnlocked()
                Toast.makeText(this, "🎉 12-Hour VIP Pass Activated! All features unlocked.", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("👑 Go Pro") { _, _ ->
                quotaManager.setProUser(true)
                setupLockProvider()
                updateQuotaUI()
                onUnlocked()
                Toast.makeText(this, "🎉 VIP PRO Activated! All items unlocked permanently.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun checkQuotaBeforeEdit(onProceed: () -> Unit) {
        if (quotaManager.isProUser() || lockProvider.isPassActive() || quotaManager.consumeEdit()) {
            updateQuotaUI()
            onProceed()
        } else {
            AlertDialog.Builder(this)
                .setTitle("⚠️ Daily Limit Reached")
                .setMessage("You've reached your free daily quota of 3 edits. Watch a short video to activate a 12-Hour VIP Pass!")
                .setPositiveButton("📺 Watch Video (12h Pass)") { _, _ ->
                    lockProvider.grantTimedPass(hours = 12)
                    quotaManager.addBonusEdits(5)
                    updateQuotaUI()
                    onProceed()
                }
                .setNeutralButton("👑 Go PRO") { _, _ ->
                    quotaManager.setProUser(true)
                    setupLockProvider()
                    updateQuotaUI()
                    onProceed()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        quotaManager = UsageQuotaManager(this)

        rootLayout = findViewById(R.id.rootLayout)
        previewCard = findViewById(R.id.previewCard)
        ivMainPreviewBg = findViewById(R.id.ivMainPreviewBg)
        textView = findViewById(R.id.textView)
        tvQuotaStatus = findViewById(R.id.tvQuotaStatus)
        tvProBadge = findViewById(R.id.tvProBadge)
        btnSimulateRewardedAd = findViewById(R.id.btnSimulateRewardedAd)
        btnTogglePro = findViewById(R.id.btnTogglePro)

        ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLockProvider()
        updateQuotaUI()

        btnSimulateRewardedAd.setOnClickListener {
            lockProvider.grantTimedPass(hours = 12)
            val updated = quotaManager.addBonusEdits(5)
            updateQuotaUI()
            Toast.makeText(this, "📺 Ad completed! 12-Hour VIP Pass Active & +5 Edits added!", Toast.LENGTH_SHORT).show()
        }

        btnTogglePro.setOnClickListener {
            val newProStatus = !quotaManager.isProUser()
            quotaManager.setProUser(newProStatus)
            setupLockProvider()
            updateQuotaUI()
            val msg = if (newProStatus) "👑 VIP PRO Enabled! Unlimited edits & unlocked items." else "Free tier active."
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        // Initialize active background and typography
        updateActiveBackground(resId = R.drawable.bg25)
        currentTypography.applyTo(textView)

        val backgrounds = listOf(
            R.drawable.bg11,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25,
            R.drawable.bg5,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25,
            R.drawable.bg22,
            R.drawable.bg23,
            R.drawable.bg25
        )

        val fonts = listOf(
            R.font.righteous,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one,
            R.font.salsa,
            R.font.schoolbell,
            R.font.sofadi_one
        )

        val btnImageSelector: Button = findViewById(R.id.btnImageSelector)
        val btnFontSelector: Button = findViewById(R.id.btnFontSelector)
        val btnColorSelector: Button = findViewById(R.id.btnColorSelector)
        val btnFormatSelector: Button = findViewById(R.id.btnFormatSelector)
        val btnEffectsSelector: Button = findViewById(R.id.btnEffectsSelector)
        val btnTextStudio: Button = findViewById(R.id.btnTextStudio)

        fun syncTypography(config: TextTypographyConfig) {
            currentTypography = config
            config.applyTo(textView)
        }

        // ⭐ ALL-IN-ONE TEXT STUDIO
        btnTextStudio.setOnClickListener {
            checkQuotaBeforeEdit {
                TextStudioDialog.Builder(this)
                    .setConfig(currentTypography)
                    .setPreviewText(textView.text.toString())
                    .setFonts(fonts)
                    .setBackgroundDrawable(rootLayout.background)
                    .setBackgroundRes(currentBackgroundRes)
                    .setBackgroundColor(currentColor)
                    .setLockProvider(lockProvider)
                    .setOnLockedItemClicked { item, onUnlocked ->
                        handleLockedItem(item, onUnlocked)
                    }
                    .setOnLivePreviewListener { liveConfig ->
                        syncTypography(liveConfig)
                    }
                    .setOnTypographyApplied { applied ->
                        syncTypography(applied)
                    }
                    .show()
            }
        }

        // 1. Background Image / Gallery / Color Selector
        btnImageSelector.setOnClickListener {
            checkQuotaBeforeEdit {
                ImageSelectorDialog.Builder(this)
                    .setBackgrounds(backgrounds)
                    .setSelectedBackground(currentBackgroundRes)
                    .setEnableGalleryPick(true) { galleryLauncher.launch("image/*") }
                    .setLockProvider(lockProvider)
                    .setOnLockedItemClicked { item, onUnlocked ->
                        handleLockedItem(item, onUnlocked)
                    }
                    .setOnImageSelected { resId ->
                        updateActiveBackground(resId = resId)
                    }
                    .setOnColorSelected { color ->
                        updateActiveBackground(color = color)
                    }
                    .show()
            }
        }

        // 2. Font Tab
        btnFontSelector.setOnClickListener {
            checkQuotaBeforeEdit {
                TextStudioDialog.Builder(this)
                    .setConfig(currentTypography)
                    .setFonts(fonts)
                    .setTabs(StudioTab.FONT)
                    .setShowPreviewPane(false)
                    .setLockProvider(lockProvider)
                    .setOnLockedItemClicked { item, onUnlocked ->
                        handleLockedItem(item, onUnlocked)
                    }
                    .setOnLivePreviewListener { liveConfig ->
                        syncTypography(liveConfig)
                    }
                    .setOnTypographyApplied { applied ->
                        syncTypography(applied)
                    }
                    .show()
            }
        }

        // 3. Color Tab
        btnColorSelector.setOnClickListener {
            checkQuotaBeforeEdit {
                TextStudioDialog.Builder(this)
                    .setConfig(currentTypography)
                    .setTabs(StudioTab.COLOR)
                    .setShowPreviewPane(false)
                    .setLockProvider(lockProvider)
                    .setOnLockedItemClicked { item, onUnlocked ->
                        handleLockedItem(item, onUnlocked)
                    }
                    .setOnLivePreviewListener { liveConfig ->
                        syncTypography(liveConfig)
                    }
                    .setOnTypographyApplied { applied ->
                        syncTypography(applied)
                    }
                    .show()
            }
        }

        // 4. Format Tab
        btnFormatSelector.setOnClickListener {
            checkQuotaBeforeEdit {
                TextStudioDialog.Builder(this)
                    .setConfig(currentTypography)
                    .setTabs(StudioTab.FORMAT)
                    .setShowPreviewPane(false)
                    .setOnLivePreviewListener { liveConfig ->
                        syncTypography(liveConfig)
                    }
                    .setOnTypographyApplied { applied ->
                        syncTypography(applied)
                    }
                    .show()
            }
        }

        // 5. Effects Tab
        btnEffectsSelector.setOnClickListener {
            checkQuotaBeforeEdit {
                TextStudioDialog.Builder(this)
                    .setConfig(currentTypography)
                    .setTabs(StudioTab.EFFECTS)
                    .setShowPreviewPane(false)
                    .setOnLivePreviewListener { liveConfig ->
                        syncTypography(liveConfig)
                    }
                    .setOnTypographyApplied { applied ->
                        syncTypography(applied)
                    }
                    .show()
            }
        }
    }
}