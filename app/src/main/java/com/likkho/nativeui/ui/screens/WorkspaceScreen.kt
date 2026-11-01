package com.likkho.nativeui.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.likkho.nativeui.util.CanvasBackgroundManager
import java.io.File
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import com.likkho.nativeui.model.FontItem
import com.likkho.nativeui.model.FontSpan
import com.likkho.nativeui.model.FormatSpan
import com.likkho.nativeui.model.HeadingSpan
import com.likkho.nativeui.util.CustomFontManager
import com.likkho.nativeui.util.FormatSpanManager
import com.likkho.nativeui.util.HeadingSpanManager
import com.likkho.nativeui.util.TextEditDiff
import com.likkho.nativeui.util.ListAndFormatUtils
import com.likkho.nativeui.model.CanvasLayer
import com.likkho.nativeui.model.FloatingCanvasImage
import com.likkho.nativeui.ui.canvas.CanvasBackgroundEditDialog
import com.likkho.nativeui.ui.canvas.FloatingCanvasImageView
import com.likkho.nativeui.ui.canvas.FloatingImagesLayerBar
import com.likkho.nativeui.util.CanvasImageManager
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.likkho.nativeui.AppViewModel
import com.likkho.nativeui.model.Avatar
import com.likkho.nativeui.model.DiaryLog
import com.likkho.nativeui.ui.components.HairlineDivider
import com.likkho.nativeui.ui.components.SquareIconButton
import com.likkho.nativeui.ui.components.WikiHeader
import com.likkho.nativeui.ui.theme.Wiki
import com.likkho.nativeui.util.Fmt
import com.likkho.nativeui.util.toast
import kotlinx.coroutines.delay

/** One button of the bottom formatting toolbar (visual only for now). */
private data class Tool(val label: String, val icon: ImageVector? = null, val text: String? = null)

private val TOOL_GROUPS: List<List<Tool>> = listOf(
    listOf(Tool("Undo", Icons.Outlined.Undo), Tool("Redo", Icons.Outlined.Redo)),
    listOf(
        Tool("Bold", Icons.Outlined.FormatBold),
        Tool("Italic", Icons.Outlined.FormatItalic),
        Tool("Underline", Icons.Outlined.FormatUnderlined),
        Tool("Strikethrough", Icons.Outlined.StrikethroughS),
        Tool("Subscript", Icons.Outlined.Subscript),
        Tool("Superscript", Icons.Outlined.Superscript),
    ),
    listOf(Tool("Heading 1", text = "H1"), Tool("Heading 2", text = "H2"), Tool("Heading 3", text = "H3")),
    listOf(
        Tool("Bullet List", Icons.Outlined.FormatListBulleted),
        Tool("Numbered List", Icons.Outlined.FormatListNumbered),
        Tool("To-Do Checkbox", Icons.Outlined.CheckBox),
        Tool("Quotation", Icons.Outlined.FormatQuote),
        Tool("Code Snippet", Icons.Outlined.Code),
        Tool("Section Divider", Icons.Outlined.HorizontalRule),
    ),
    listOf(
        Tool("Link", Icons.Outlined.Link),
        Tool("Highlight", Icons.Outlined.BorderColor),
        Tool("Text Color", Icons.Outlined.FormatColorText),
        Tool("Text Size", Icons.Outlined.FormatSize),
        Tool("Fonts", text = "A"),
    ),
    listOf(
        Tool("Align Left", Icons.Outlined.FormatAlignLeft),
        Tool("Align Center", Icons.Outlined.FormatAlignCenter),
        Tool("Align Right", Icons.Outlined.FormatAlignRight),
        Tool("Justify", Icons.Outlined.FormatAlignJustify),
        Tool("Indent", Icons.Outlined.FormatIndentIncrease),
        Tool("Outdent", Icons.Outlined.FormatIndentDecrease),
    ),
    listOf(
        Tool("Media Picker", Icons.Outlined.Folder),
        Tool("Background Image", Icons.Outlined.Wallpaper),
        Tool("Record Voice", Icons.Outlined.Mic),
        Tool("Wikipedia", Icons.Outlined.Public),
        Tool("Word Analysis", Icons.Outlined.Spellcheck),
        Tool("LaTeX", Icons.Outlined.Functions),
        Tool("Spoiler", Icons.Outlined.VisibilityOff),
        Tool("Date/Time Stamp", Icons.Outlined.Schedule),
        Tool("Import Document", Icons.Outlined.FileUpload),
    ),
)

@Composable
fun WorkspaceScreen(vm: AppViewModel, logId: String?) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val ctx = LocalContext.current

    val existing = remember(logId) { logId?.let { id -> vm.logs.firstOrNull { it.id == id } } }
    var currentId by remember { mutableStateOf(logId) }

    var headingValue by remember(logId) {
        val initialText = existing?.heading ?: ""
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = initialText,
                selection = androidx.compose.ui.text.TextRange(initialText.length),
            )
        )
    }

    var bodyValue by remember(logId) {
        val initialText = existing?.body ?: ""
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = initialText,
                selection = androidx.compose.ui.text.TextRange(initialText.length),
            )
        )
    }

    var fontSpans by remember(logId) { mutableStateOf(existing?.fontSpans ?: emptyList()) }
    var activeFontId by remember(logId) { mutableStateOf<String?>(null) }
    var showFontBar by remember { mutableStateOf(false) }

    var bgImageUri by remember(logId) { mutableStateOf(existing?.bgImageUri) }
    var bgOpacity by remember(logId) { mutableFloatStateOf(existing?.bgOpacity ?: 0.35f) }
    var bgFitMode by remember(logId) { mutableStateOf(existing?.bgFitMode ?: "cover") }
    var showBgPanel by remember { mutableStateOf(false) }
    var showBgEditDialog by remember { mutableStateOf(false) }

    var boldSpans by remember(logId) { mutableStateOf(existing?.boldSpans ?: emptyList()) }
    var italicSpans by remember(logId) { mutableStateOf(existing?.italicSpans ?: emptyList()) }
    var underlineSpans by remember(logId) { mutableStateOf(existing?.underlineSpans ?: emptyList()) }
    var strikethroughSpans by remember(logId) { mutableStateOf(existing?.strikethroughSpans ?: emptyList()) }
    var subscriptSpans by remember(logId) { mutableStateOf(existing?.subscriptSpans ?: emptyList()) }
    var superscriptSpans by remember(logId) { mutableStateOf(existing?.superscriptSpans ?: emptyList()) }
    var codeSpans by remember(logId) { mutableStateOf(existing?.codeSpans ?: emptyList()) }
    var quoteSpans by remember(logId) { mutableStateOf(existing?.quoteSpans ?: emptyList()) }
    var headingSpans by remember(logId) { mutableStateOf(existing?.headingSpans ?: emptyList()) }
    var activeHeadingLevel by remember(logId) { mutableStateOf<Int?>(null) }

    var floatingImages by remember(logId) { mutableStateOf(existing?.floatingImages ?: emptyList()) }
    var selectedImageId by remember { mutableStateOf<String?>(null) }

    var isBoldActive by remember(logId) { mutableStateOf(false) }
    var isItalicActive by remember(logId) { mutableStateOf(false) }
    var isUnderlineActive by remember(logId) { mutableStateOf(false) }
    var isStrikethroughActive by remember(logId) { mutableStateOf(false) }
    var isSubscriptActive by remember(logId) { mutableStateOf(false) }
    var isSuperscriptActive by remember(logId) { mutableStateOf(false) }
    var isCodeActive by remember(logId) { mutableStateOf(false) }
    var isQuoteActive by remember(logId) { mutableStateOf(false) }
    var isBulletListActive by remember(logId) { mutableStateOf(false) }
    var isNumberedListActive by remember(logId) { mutableStateOf(false) }
    var isTodoListActive by remember(logId) { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        CustomFontManager.init(ctx)
    }

    val builtInFonts = remember {
        listOf(
            FontItem("builtin_default", "Default", isBuiltin = true),
            FontItem("builtin_serif", "Cormorant", isBuiltin = true),
            FontItem("builtin_short_stack", "Short Stack", isBuiltin = true),
            FontItem("builtin_sans", "Jakarta Sans", isBuiltin = true),
            FontItem("builtin_mono", "IBM Plex Mono", isBuiltin = true),
        )
    }
    val allFonts = builtInFonts + CustomFontManager.customFonts

    val undoRedo = remember(logId) {
        com.likkho.nativeui.util.CanvasUndoRedoManager(
            initialBody = bodyValue,
            initialHeading = headingValue,
            initialFontSpans = fontSpans,
            initialActiveFontId = activeFontId,
            initialBoldSpans = boldSpans,
            initialItalicSpans = italicSpans,
            initialUnderlineSpans = underlineSpans,
            initialStrikethroughSpans = strikethroughSpans,
            initialSubscriptSpans = subscriptSpans,
            initialSuperscriptSpans = superscriptSpans,
            initialCodeSpans = codeSpans,
            initialQuoteSpans = quoteSpans,
            initialHeadingSpans = headingSpans,
            initialIsBoldActive = isBoldActive,
            initialIsItalicActive = isItalicActive,
            initialIsUnderlineActive = isUnderlineActive,
            initialIsStrikethroughActive = isStrikethroughActive,
            initialIsSubscriptActive = isSubscriptActive,
            initialIsSuperscriptActive = isSuperscriptActive,
            initialIsCodeActive = isCodeActive,
            initialIsQuoteActive = isQuoteActive,
            initialActiveHeadingLevel = activeHeadingLevel,
            initialBgImageUri = bgImageUri,
            initialBgOpacity = bgOpacity,
            initialBgFitMode = bgFitMode,
            initialFloatingImages = floatingImages,
        )
    }

    fun recordHistory(
        newBody: androidx.compose.ui.text.input.TextFieldValue = bodyValue,
        newHeading: androidx.compose.ui.text.input.TextFieldValue = headingValue,
        newFontSpans: List<FontSpan> = fontSpans,
        newActiveFontId: String? = activeFontId,
        newBoldSpans: List<FormatSpan> = boldSpans,
        newItalicSpans: List<FormatSpan> = italicSpans,
        newUnderlineSpans: List<FormatSpan> = underlineSpans,
        newStrikethroughSpans: List<FormatSpan> = strikethroughSpans,
        newSubscriptSpans: List<FormatSpan> = subscriptSpans,
        newSuperscriptSpans: List<FormatSpan> = superscriptSpans,
        newCodeSpans: List<FormatSpan> = codeSpans,
        newQuoteSpans: List<FormatSpan> = quoteSpans,
        newHeadingSpans: List<HeadingSpan> = headingSpans,
        newIsBoldActive: Boolean = isBoldActive,
        newIsItalicActive: Boolean = isItalicActive,
        newIsUnderlineActive: Boolean = isUnderlineActive,
        newIsStrikethroughActive: Boolean = isStrikethroughActive,
        newIsSubscriptActive: Boolean = isSubscriptActive,
        newIsSuperscriptActive: Boolean = isSuperscriptActive,
        newIsCodeActive: Boolean = isCodeActive,
        newIsQuoteActive: Boolean = isQuoteActive,
        newActiveHeadingLevel: Int? = activeHeadingLevel,
        newBgImageUri: String? = bgImageUri,
        newBgOpacity: Float = bgOpacity,
        newBgFitMode: String = bgFitMode,
        newFloatingImages: List<FloatingCanvasImage> = floatingImages,
    ) {
        undoRedo.record(
            body = newBody,
            heading = newHeading,
            fontSpans = newFontSpans,
            activeFontId = newActiveFontId,
            boldSpans = newBoldSpans,
            italicSpans = newItalicSpans,
            underlineSpans = newUnderlineSpans,
            strikethroughSpans = newStrikethroughSpans,
            subscriptSpans = newSubscriptSpans,
            superscriptSpans = newSuperscriptSpans,
            codeSpans = newCodeSpans,
            quoteSpans = newQuoteSpans,
            headingSpans = newHeadingSpans,
            isBoldActive = newIsBoldActive,
            isItalicActive = newIsItalicActive,
            isUnderlineActive = newIsUnderlineActive,
            isStrikethroughActive = newIsStrikethroughActive,
            isSubscriptActive = newIsSubscriptActive,
            isSuperscriptActive = newIsSuperscriptActive,
            isCodeActive = newIsCodeActive,
            isQuoteActive = newIsQuoteActive,
            activeHeadingLevel = newActiveHeadingLevel,
            bgImageUri = newBgImageUri,
            bgOpacity = newBgOpacity,
            bgFitMode = newBgFitMode,
            floatingImages = newFloatingImages,
        )
    }

    val bgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = CanvasBackgroundManager.saveBackgroundFromUri(ctx, uri)
            if (saved != null) {
                bgImageUri = saved
                recordHistory(newBgImageUri = saved)
                ctx.toast("Canvas background set")
            } else {
                ctx.toast("Could not load image")
            }
        }
    }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = CanvasImageManager.saveImageFromUri(ctx, uri)
            if (saved != null) {
                val newImg = FloatingCanvasImage(
                    id = "img_${System.currentTimeMillis()}",
                    uri = saved,
                    x = 60f,
                    y = 100f,
                    scale = 1.0f,
                    rotation = 0f,
                    layer = CanvasLayer.FOREGROUND,
                )
                val updated = floatingImages + newImg
                floatingImages = updated
                selectedImageId = newImg.id
                recordHistory(newFloatingImages = updated)
                ctx.toast("Image added to canvas (Foreground)")
            } else {
                ctx.toast("Could not load image")
            }
        }
    }

    fun toggleHeading(level: Int) {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = HeadingSpanManager.toggleHeadingRange(
                spans = headingSpans,
                start = min,
                end = max,
                targetLevel = level,
            )
            val covered = HeadingSpanManager.isRangeCoveredByLevel(updated, min, max, level)
            headingSpans = updated
            activeHeadingLevel = if (covered) level else null
            recordHistory(newHeadingSpans = updated, newActiveHeadingLevel = activeHeadingLevel)
            ctx.toast(if (covered) "Heading $level applied" else "Heading $level removed")
        } else {
            val nextState = if (activeHeadingLevel == level) null else level
            activeHeadingLevel = nextState
            recordHistory(newActiveHeadingLevel = nextState)
            ctx.toast(if (nextState != null) "Heading $level enabled (Tap again to disable)" else "Heading $level disabled")
        }
    }

    fun applyTextReplacement(
        newValue: androidx.compose.ui.text.input.TextFieldValue,
        oldLen: Int,
        newLen: Int,
        editIndex: Int,
        toastMsg: String? = null,
    ) {
        val edited = TextEditDiff.applyToSpans(
            oldText = bodyValue.text,
            newText = newValue.text,
            fontSpans = fontSpans,
            boldSpans = boldSpans,
            italicSpans = italicSpans,
            underlineSpans = underlineSpans,
            strikethroughSpans = strikethroughSpans,
            subscriptSpans = subscriptSpans,
            superscriptSpans = superscriptSpans,
            codeSpans = codeSpans,
            quoteSpans = quoteSpans,
            headingSpans = headingSpans,
            activeFontId = null,
            boldOn = false,
            italicOn = false,
            underlineOn = false,
            strikethroughOn = false,
            subscriptOn = false,
            superscriptOn = false,
            codeOn = false,
            quoteOn = false,
            headingLevel = null,
        )
        val updatedFontSpans = edited.fontSpans
        val updatedBold = edited.boldSpans
        val updatedItalic = edited.italicSpans
        val updatedUnderline = edited.underlineSpans
        val updatedStrikethrough = edited.strikethroughSpans
        val updatedSubscript = edited.subscriptSpans
        val updatedSuperscript = edited.superscriptSpans
        val updatedCode = edited.codeSpans
        val updatedQuote = edited.quoteSpans
        val updatedHeadings = edited.headingSpans

        bodyValue = newValue
        fontSpans = updatedFontSpans
        boldSpans = updatedBold
        italicSpans = updatedItalic
        underlineSpans = updatedUnderline
        strikethroughSpans = updatedStrikethrough
        subscriptSpans = updatedSubscript
        superscriptSpans = updatedSuperscript
        codeSpans = updatedCode
        quoteSpans = updatedQuote
        headingSpans = updatedHeadings

        recordHistory(
            newBody = newValue,
            newFontSpans = updatedFontSpans,
            newBoldSpans = updatedBold,
            newItalicSpans = updatedItalic,
            newUnderlineSpans = updatedUnderline,
            newStrikethroughSpans = updatedStrikethrough,
            newSubscriptSpans = updatedSubscript,
            newSuperscriptSpans = updatedSuperscript,
            newCodeSpans = updatedCode,
            newQuoteSpans = updatedQuote,
            newHeadingSpans = updatedHeadings,
        )
        if (toastMsg != null) ctx.toast(toastMsg)
    }

    fun insertSectionDivider() {
        val text = bodyValue.text
        val sel = bodyValue.selection
        val min = sel.min
        val max = sel.max

        val prefix = if (min > 0 && text[min - 1] != '\n') "\n" else ""
        val dividerStr = "────────────────────────────────────────"
        val suffix = if (max < text.length && text[max] == '\n') "\n" else "\n"
        val insertText = "$prefix$dividerStr$suffix"

        val newText = text.substring(0, min) + insertText + text.substring(max)
        val newCursor = min + insertText.length
        val newValue = androidx.compose.ui.text.input.TextFieldValue(
            text = newText,
            selection = androidx.compose.ui.text.TextRange(newCursor),
        )

        applyTextReplacement(
            newValue = newValue,
            oldLen = text.length,
            newLen = newText.length,
            editIndex = newCursor,
            toastMsg = "Section divider inserted",
        )
    }

    fun toggleBold() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(boldSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            boldSpans = updated
            isBoldActive = covered
            recordHistory(newBoldSpans = updated, newIsBoldActive = covered)
            ctx.toast(if (covered) "Bold applied" else "Bold removed")
        } else {
            val nextState = !isBoldActive
            isBoldActive = nextState
            recordHistory(newIsBoldActive = nextState)
            ctx.toast(if (nextState) "Bold enabled" else "Bold disabled")
        }
    }

    fun toggleItalic() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(italicSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            italicSpans = updated
            isItalicActive = covered
            recordHistory(newItalicSpans = updated, newIsItalicActive = covered)
            ctx.toast(if (covered) "Italic applied" else "Italic removed")
        } else {
            val nextState = !isItalicActive
            isItalicActive = nextState
            recordHistory(newIsItalicActive = nextState)
            ctx.toast(if (nextState) "Italic enabled" else "Italic disabled")
        }
    }

    fun toggleUnderline() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(underlineSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            underlineSpans = updated
            isUnderlineActive = covered
            recordHistory(newUnderlineSpans = updated, newIsUnderlineActive = covered)
            ctx.toast(if (covered) "Underline applied" else "Underline removed")
        } else {
            val nextState = !isUnderlineActive
            isUnderlineActive = nextState
            recordHistory(newIsUnderlineActive = nextState)
            ctx.toast(if (nextState) "Underline enabled" else "Underline disabled")
        }
    }

    fun toggleStrikethrough() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(strikethroughSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            strikethroughSpans = updated
            isStrikethroughActive = covered
            recordHistory(newStrikethroughSpans = updated, newIsStrikethroughActive = covered)
            ctx.toast(if (covered) "Strikethrough applied" else "Strikethrough removed")
        } else {
            val nextState = !isStrikethroughActive
            isStrikethroughActive = nextState
            recordHistory(newIsStrikethroughActive = nextState)
            ctx.toast(if (nextState) "Strikethrough enabled" else "Strikethrough disabled")
        }
    }

    fun toggleSubscript() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(subscriptSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            val updatedSuper = if (covered) FormatSpanManager.removeRange(superscriptSpans, min, max) else superscriptSpans
            subscriptSpans = updated
            superscriptSpans = updatedSuper
            isSubscriptActive = covered
            if (covered) isSuperscriptActive = false
            recordHistory(
                newSubscriptSpans = updated,
                newSuperscriptSpans = updatedSuper,
                newIsSubscriptActive = covered,
                newIsSuperscriptActive = isSuperscriptActive,
            )
            ctx.toast(if (covered) "Subscript applied" else "Subscript removed")
        } else {
            val nextState = !isSubscriptActive
            isSubscriptActive = nextState
            if (nextState) isSuperscriptActive = false
            recordHistory(newIsSubscriptActive = nextState, newIsSuperscriptActive = isSuperscriptActive)
            ctx.toast(if (nextState) "Subscript enabled" else "Subscript disabled")
        }
    }

    fun toggleSuperscript() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(superscriptSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            val updatedSub = if (covered) FormatSpanManager.removeRange(subscriptSpans, min, max) else subscriptSpans
            superscriptSpans = updated
            subscriptSpans = updatedSub
            isSuperscriptActive = covered
            if (covered) isSubscriptActive = false
            recordHistory(
                newSuperscriptSpans = updated,
                newSubscriptSpans = updatedSub,
                newIsSuperscriptActive = covered,
                newIsSubscriptActive = isSubscriptActive,
            )
            ctx.toast(if (covered) "Superscript applied" else "Superscript removed")
        } else {
            val nextState = !isSuperscriptActive
            isSuperscriptActive = nextState
            if (nextState) isSubscriptActive = false
            recordHistory(newIsSuperscriptActive = nextState, newIsSubscriptActive = isSubscriptActive)
            ctx.toast(if (nextState) "Superscript enabled" else "Superscript disabled")
        }
    }

    fun toggleCode() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(codeSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            codeSpans = updated
            isCodeActive = covered
            recordHistory(newCodeSpans = updated, newIsCodeActive = covered)
            ctx.toast(if (covered) "Code format applied" else "Code format removed")
        } else {
            val nextState = !isCodeActive
            isCodeActive = nextState
            recordHistory(newIsCodeActive = nextState)
            ctx.toast(if (nextState) "Code format enabled" else "Code format disabled")
        }
    }

    fun toggleNumberedList() {
        val sel = bodyValue.selection
        val text = bodyValue.text
        if (sel.start != sel.end) {
            val res = ListAndFormatUtils.toggleNumberedList(bodyValue)
            isNumberedListActive = !isNumberedListActive
            applyTextReplacement(
                newValue = res,
                oldLen = text.length,
                newLen = res.text.length,
                editIndex = res.selection.end,
                toastMsg = if (isNumberedListActive) "Numbered list applied" else "Numbered list removed",
            )
            return
        }

        val lineStart = if (sel.min == 0) 0 else text.lastIndexOf('\n', sel.min - 1).let { if (it == -1) 0 else it + 1 }
        val nextNewline = text.indexOf('\n', sel.min).let { if (it == -1) text.length else it }
        val line = if (lineStart < text.length) text.substring(lineStart, nextNewline) else ""
        val numRegex = Regex("""^\s*(\d+)\.\s+""")

        if (isNumberedListActive || numRegex.containsMatchIn(line)) {
            isNumberedListActive = false
            if (numRegex.containsMatchIn(line)) {
                val clean = numRegex.replace(line, "")
                val delta = line.length - clean.length
                val newText = text.substring(0, lineStart) + clean + text.substring(nextNewline)
                val newCursor = (sel.start - delta).coerceIn(lineStart, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "Numbered list disabled",
                )
            } else {
                ctx.toast("Numbered list disabled")
            }
        } else {
            isNumberedListActive = true
            isBulletListActive = false
            isTodoListActive = false

            if (text.isEmpty()) {
                val newText = "1. "
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(3)),
                    oldLen = 0,
                    newLen = 3,
                    editIndex = 3,
                    toastMsg = "Numbered list enabled",
                )
            } else {
                val clean = ListAndFormatUtils.stripAnyListPrefix(line)
                val newText = text.substring(0, lineStart) + "1. $clean" + text.substring(nextNewline)
                val delta = (3 + clean.length) - line.length
                val newCursor = (sel.start + delta).coerceIn(lineStart + 3, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "Numbered list enabled",
                )
            }
        }
    }

    fun toggleBulletList() {
        val sel = bodyValue.selection
        val text = bodyValue.text
        if (sel.start != sel.end) {
            val res = ListAndFormatUtils.toggleBulletList(bodyValue)
            isBulletListActive = !isBulletListActive
            applyTextReplacement(
                newValue = res,
                oldLen = text.length,
                newLen = res.text.length,
                editIndex = res.selection.end,
                toastMsg = if (isBulletListActive) "Bullet list applied" else "Bullet list removed",
            )
            return
        }

        val lineStart = if (sel.min == 0) 0 else text.lastIndexOf('\n', sel.min - 1).let { if (it == -1) 0 else it + 1 }
        val nextNewline = text.indexOf('\n', sel.min).let { if (it == -1) text.length else it }
        val line = if (lineStart < text.length) text.substring(lineStart, nextNewline) else ""

        if (isBulletListActive || line.startsWith("• ")) {
            isBulletListActive = false
            if (line.startsWith("• ")) {
                val clean = line.removePrefix("• ")
                val newText = text.substring(0, lineStart) + clean + text.substring(nextNewline)
                val newCursor = (sel.start - 2).coerceIn(lineStart, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "Bullet list disabled",
                )
            } else {
                ctx.toast("Bullet list disabled")
            }
        } else {
            isBulletListActive = true
            isNumberedListActive = false
            isTodoListActive = false

            if (text.isEmpty()) {
                val newText = "• "
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(2)),
                    oldLen = 0,
                    newLen = 2,
                    editIndex = 2,
                    toastMsg = "Bullet list enabled",
                )
            } else {
                val clean = ListAndFormatUtils.stripAnyListPrefix(line)
                val newText = text.substring(0, lineStart) + "• $clean" + text.substring(nextNewline)
                val delta = (2 + clean.length) - line.length
                val newCursor = (sel.start + delta).coerceIn(lineStart + 2, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "Bullet list enabled",
                )
            }
        }
    }

    fun toggleTodoCheckbox() {
        val sel = bodyValue.selection
        val text = bodyValue.text
        if (sel.start != sel.end) {
            val res = ListAndFormatUtils.toggleTodoCheckbox(bodyValue)
            isTodoListActive = !isTodoListActive
            applyTextReplacement(
                newValue = res,
                oldLen = text.length,
                newLen = res.text.length,
                editIndex = res.selection.end,
                toastMsg = if (isTodoListActive) "To-Do checkboxes applied" else "To-Do checkboxes removed",
            )
            return
        }

        val lineStart = if (sel.min == 0) 0 else text.lastIndexOf('\n', sel.min - 1).let { if (it == -1) 0 else it + 1 }
        val nextNewline = text.indexOf('\n', sel.min).let { if (it == -1) text.length else it }
        val line = if (lineStart < text.length) text.substring(lineStart, nextNewline) else ""
        val todoRegex = Regex("""^\s*([☐☑]|\[[ xX]\])\s+""")

        if (isTodoListActive || todoRegex.containsMatchIn(line)) {
            isTodoListActive = false
            if (todoRegex.containsMatchIn(line)) {
                val clean = todoRegex.replace(line, "")
                val delta = line.length - clean.length
                val newText = text.substring(0, lineStart) + clean + text.substring(nextNewline)
                val newCursor = (sel.start - delta).coerceIn(lineStart, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "To-Do checkbox disabled",
                )
            } else {
                ctx.toast("To-Do checkbox disabled")
            }
        } else {
            isTodoListActive = true
            isBulletListActive = false
            isNumberedListActive = false

            if (text.isEmpty()) {
                val newText = "☐ "
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(2)),
                    oldLen = 0,
                    newLen = 2,
                    editIndex = 2,
                    toastMsg = "To-Do checkbox enabled",
                )
            } else {
                val clean = ListAndFormatUtils.stripAnyListPrefix(line)
                val newText = text.substring(0, lineStart) + "☐ $clean" + text.substring(nextNewline)
                val delta = (2 + clean.length) - line.length
                val newCursor = (sel.start + delta).coerceIn(lineStart + 2, newText.length)
                applyTextReplacement(
                    newValue = androidx.compose.ui.text.input.TextFieldValue(newText, androidx.compose.ui.text.TextRange(newCursor)),
                    oldLen = text.length,
                    newLen = newText.length,
                    editIndex = newCursor,
                    toastMsg = "To-Do checkbox enabled",
                )
            }
        }
    }

    fun toggleQuotation() {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = FormatSpanManager.toggleRange(quoteSpans, min, max)
            val covered = FormatSpanManager.isRangeFullyCovered(updated, min, max)
            quoteSpans = updated
            isQuoteActive = covered
            recordHistory(newQuoteSpans = updated, newIsQuoteActive = covered)
            ctx.toast(if (covered) "Quotation applied" else "Quotation removed")
        } else {
            val nextState = !isQuoteActive
            isQuoteActive = nextState
            recordHistory(newIsQuoteActive = nextState)
            ctx.toast(if (nextState) "Quotation enabled" else "Quotation disabled")
        }
    }

    fun applyOrToggleFont(targetId: String) {
        val sel = bodyValue.selection
        if (sel.start != sel.end) {
            val min = sel.min
            val max = sel.max
            val updated = CustomFontManager.applyFontToRange(fontSpans, min, max, targetId)
            fontSpans = updated
            recordHistory(newFontSpans = updated)
            val fontName = allFonts.firstOrNull { it.id == targetId }?.name ?: "Font"
            if (targetId == "builtin_default") {
                ctx.toast("Reverted selection to Default Font")
            } else {
                ctx.toast("Applied $fontName to selection")
            }
        } else {
            if (targetId == "builtin_default" || activeFontId == targetId) {
                activeFontId = null
                recordHistory(newActiveFontId = null)
                ctx.toast("Default Font enabled")
            } else {
                activeFontId = targetId
                recordHistory(newActiveFontId = targetId)
                val fontName = allFonts.firstOrNull { it.id == targetId }?.name ?: "Font"
                ctx.toast("$fontName enabled (Tap again to disable)")
            }
        }
    }

    val fontPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val saved = CustomFontManager.saveFontFromUri(ctx, uri)
            if (saved != null) {
                ctx.toast("Added font: ${saved.name}")
                applyOrToggleFont(saved.id)
            } else {
                ctx.toast("Could not read font file. Please select a valid .ttf or .otf file.")
            }
        }
    }

    var reminderAt by remember(logId) { mutableStateOf(existing?.reminderAt) }
    var reading by remember(logId) { mutableStateOf(existing != null) }
    var showReminder by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var savedFlash by remember { mutableStateOf(false) }

    LaunchedEffect(savedFlash) {
        if (savedFlash) {
            delay(1500)
            savedFlash = false
        }
    }

    fun performUndo() {
        undoRedo.undo()?.let { snap ->
            bodyValue = snap.body
            headingValue = snap.heading
            fontSpans = snap.fontSpans
            activeFontId = snap.activeFontId
            boldSpans = snap.boldSpans
            italicSpans = snap.italicSpans
            underlineSpans = snap.underlineSpans
            strikethroughSpans = snap.strikethroughSpans
            subscriptSpans = snap.subscriptSpans
            superscriptSpans = snap.superscriptSpans
            codeSpans = snap.codeSpans
            quoteSpans = snap.quoteSpans
            headingSpans = snap.headingSpans
            isBoldActive = snap.isBoldActive
            isItalicActive = snap.isItalicActive
            isUnderlineActive = snap.isUnderlineActive
            isStrikethroughActive = snap.isStrikethroughActive
            isSubscriptActive = snap.isSubscriptActive
            isSuperscriptActive = snap.isSuperscriptActive
            isCodeActive = snap.isCodeActive
            isQuoteActive = snap.isQuoteActive
            activeHeadingLevel = snap.activeHeadingLevel
            bgImageUri = snap.bgImageUri
            bgOpacity = snap.bgOpacity
            bgFitMode = snap.bgFitMode
            floatingImages = snap.floatingImages
        }
    }

    fun performRedo() {
        undoRedo.redo()?.let { snap ->
            bodyValue = snap.body
            headingValue = snap.heading
            fontSpans = snap.fontSpans
            activeFontId = snap.activeFontId
            boldSpans = snap.boldSpans
            italicSpans = snap.italicSpans
            underlineSpans = snap.underlineSpans
            strikethroughSpans = snap.strikethroughSpans
            subscriptSpans = snap.subscriptSpans
            superscriptSpans = snap.superscriptSpans
            codeSpans = snap.codeSpans
            quoteSpans = snap.quoteSpans
            headingSpans = snap.headingSpans
            isBoldActive = snap.isBoldActive
            isItalicActive = snap.isItalicActive
            isUnderlineActive = snap.isUnderlineActive
            isStrikethroughActive = snap.isStrikethroughActive
            isSubscriptActive = snap.isSubscriptActive
            isSuperscriptActive = snap.isSuperscriptActive
            isCodeActive = snap.isCodeActive
            isQuoteActive = snap.isQuoteActive
            activeHeadingLevel = snap.activeHeadingLevel
            bgImageUri = snap.bgImageUri
            bgOpacity = snap.bgOpacity
            bgFitMode = snap.bgFitMode
            floatingImages = snap.floatingImages
        }
    }

    fun save() {
        val h = headingValue.text.trim()
        val bodyText = bodyValue.text
        if (h.isEmpty() && bodyText.isBlank() && currentId == null) return
        val now = System.currentTimeMillis()
        val old = currentId?.let { id -> vm.logs.firstOrNull { it.id == id } }
        val title = h.ifEmpty { "Untitled" }
        if (old != null && old.heading == title && old.body == bodyText && old.reminderAt == reminderAt &&
            old.fontSpans == fontSpans && old.boldSpans == boldSpans && old.italicSpans == italicSpans &&
            old.underlineSpans == underlineSpans && old.strikethroughSpans == strikethroughSpans &&
            old.subscriptSpans == subscriptSpans && old.superscriptSpans == superscriptSpans &&
            old.codeSpans == codeSpans && old.quoteSpans == quoteSpans &&
            old.headingSpans == headingSpans &&
            old.bgImageUri == bgImageUri && old.bgOpacity == bgOpacity && old.bgFitMode == bgFitMode &&
            old.floatingImages == floatingImages
        ) return
        val base = old ?: DiaryLog(
            id = "log_$now",
            heading = title,
            body = bodyText,
            createdAt = now,
            updatedAt = now,
            avatar = Avatar.monogram(title),
            fontSpans = fontSpans,
            boldSpans = boldSpans,
            italicSpans = italicSpans,
            underlineSpans = underlineSpans,
            strikethroughSpans = strikethroughSpans,
            subscriptSpans = subscriptSpans,
            superscriptSpans = superscriptSpans,
            codeSpans = codeSpans,
            quoteSpans = quoteSpans,
            headingSpans = headingSpans,
            bgImageUri = bgImageUri,
            bgOpacity = bgOpacity,
            bgFitMode = bgFitMode,
            floatingImages = floatingImages,
        )
        val log = base.copy(
            heading = title,
            body = bodyText,
            updatedAt = now,
            reminderAt = reminderAt,
            fontSpans = fontSpans,
            boldSpans = boldSpans,
            italicSpans = italicSpans,
            underlineSpans = underlineSpans,
            strikethroughSpans = strikethroughSpans,
            subscriptSpans = subscriptSpans,
            superscriptSpans = superscriptSpans,
            codeSpans = codeSpans,
            quoteSpans = quoteSpans,
            headingSpans = headingSpans,
            bgImageUri = bgImageUri,
            bgOpacity = bgOpacity,
            bgFitMode = bgFitMode,
            floatingImages = floatingImages,
        )
        vm.upsert(log)
        currentId = log.id
    }

    fun exit() {
        save()
        vm.back()
    }

    BackHandler { exit() }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.isCtrlPressed) {
                    when {
                        event.key == Key.B -> {
                            toggleBold()
                            true
                        }
                        event.key == Key.I -> {
                            toggleItalic()
                            true
                        }
                        event.key == Key.U -> {
                            toggleUnderline()
                            true
                        }
                        event.key == Key.Z && event.isShiftPressed -> {
                            if (undoRedo.canRedo) {
                                performRedo()
                                true
                            } else false
                        }
                        event.key == Key.Z -> {
                            if (undoRedo.canUndo) {
                                performUndo()
                                true
                            } else false
                        }
                        event.key == Key.One -> {
                            toggleHeading(1)
                            true
                        }
                        event.key == Key.Two -> {
                            toggleHeading(2)
                            true
                        }
                        event.key == Key.Three -> {
                            toggleHeading(3)
                            true
                        }
                        event.key == Key.Y -> {
                            if (undoRedo.canRedo) {
                                performRedo()
                                true
                            } else false
                        }
                        else -> false
                    }
                } else false
            },
    ) {
        // ---------------------------------------------- header
        WikiHeader(height = 58.dp, horizontal = 12.dp) {
            SquareIconButton(
                icon = Icons.Outlined.ArrowBack,
                contentDescription = "Back to Homepage",
                onClick = { exit() },
                bordered = false,
            )
            Spacer(Modifier.width(4.dp))

            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                val avatar = existing?.avatar ?: Avatar.monogram(headingValue.text.ifBlank { "L" })
                Box(Modifier.clickable { ctx.toast("PFP Studio — to be wired later") }) {
                    DiaryAvatar(avatar, 40.dp)
                }
                Spacer(Modifier.width(10.dp))
                if (existing?.lockPin != null) {
                    Icon(Icons.Outlined.Lock, "Diary locked", tint = c.danger, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                BasicTextField(
                    value = headingValue,
                    onValueChange = {
                        headingValue = it
                        recordHistory(newHeading = it)
                    },
                    readOnly = reading,
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = f.serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = c.text,
                    ),
                    cursorBrush = SolidColor(c.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (headingValue.text.isEmpty()) {
                                Text(
                                    "Diary Heading...",
                                    color = c.muted.copy(alpha = 0.6f),
                                    maxLines = 1,
                                    style = TextStyle(
                                        fontFamily = f.serif,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                )
                            }
                            inner()
                        }
                    },
                )
            }

            Spacer(Modifier.width(6.dp))
            if (currentId != null) {
                SquareIconButton(
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete entry",
                    onClick = { showDelete = true },
                    size = 36.dp,
                    bordered = false,
                    tint = c.muted,
                    iconSize = 18.dp,
                )
            }
            val active = reminderAt != null && reminderAt!! > System.currentTimeMillis()
            SquareIconButton(
                icon = if (active) Icons.Outlined.NotificationsActive else Icons.Outlined.Notifications,
                contentDescription = "Set reminder",
                onClick = { showReminder = true },
                tint = if (active) c.primary else c.text,
                background = if (active) c.primary.copy(alpha = 0.10f) else c.bg,
                iconSize = 18.dp,
            )
            Spacer(Modifier.width(6.dp))
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .background(c.primary)
                    .clickable {
                        if (reading) {
                            reading = false
                            ctx.toast("Edit Mode enabled")
                        } else {
                            save()
                            savedFlash = true
                        }
                    }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val icon = when {
                    reading -> Icons.Outlined.Edit
                    savedFlash -> Icons.Outlined.Check
                    else -> Icons.Outlined.Save
                }
                val label = when {
                    reading -> "Edit"
                    savedFlash -> "Saved"
                    else -> "Save"
                }
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(
                    label,
                    color = Color.White,
                    style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                )
            }
        }

        // ---------------------------------------------- reminder sub-bar
        val r = reminderAt
        if (r != null && r > System.currentTimeMillis()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.NotificationsActive, null, tint = c.primary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    Fmt.dateTime(r),
                    modifier = Modifier.weight(1f),
                    style = TextStyle(fontFamily = f.mono, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                )
                Text(
                    "Edit",
                    color = c.primary,
                    modifier = Modifier.clickable { showReminder = true },
                    style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                )
            }
            HairlineDivider()
        }

        // ---------------------------------------------- canvas
        val proseStyle = TextStyle(
            fontFamily = f.prose,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.All,
            lineHeight = 28.sp,
            color = c.text,
        )

        val isBoldCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(boldSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isBoldActive

        val isItalicCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(italicSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isItalicActive

        val isUnderlineCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(underlineSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isUnderlineActive

        val isStrikethroughCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(strikethroughSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isStrikethroughActive

        val isH1Current = if (bodyValue.selection.start != bodyValue.selection.end) {
            HeadingSpanManager.isRangeCoveredByLevel(headingSpans, bodyValue.selection.min, bodyValue.selection.max, 1)
        } else activeHeadingLevel == 1

        val isH2Current = if (bodyValue.selection.start != bodyValue.selection.end) {
            HeadingSpanManager.isRangeCoveredByLevel(headingSpans, bodyValue.selection.min, bodyValue.selection.max, 2)
        } else activeHeadingLevel == 2

        val isH3Current = if (bodyValue.selection.start != bodyValue.selection.end) {
            HeadingSpanManager.isRangeCoveredByLevel(headingSpans, bodyValue.selection.min, bodyValue.selection.max, 3)
        } else activeHeadingLevel == 3

        val isSubscriptCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(subscriptSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isSubscriptActive

        val isSuperscriptCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(superscriptSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isSuperscriptActive

        val isCodeCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(codeSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isCodeActive

        val isQuoteCurrent = if (bodyValue.selection.start != bodyValue.selection.end) {
            FormatSpanManager.isRangeFullyCovered(quoteSpans, bodyValue.selection.min, bodyValue.selection.max)
        } else isQuoteActive

        Box(Modifier.weight(1f).fillMaxWidth()) {
            // Layer 0: Canvas Background Viewport
            if (!bgImageUri.isNullOrEmpty()) {
                val contentScale = when (bgFitMode) {
                    "contain", "fit" -> ContentScale.Fit
                    "fill", "stretch" -> ContentScale.FillBounds
                    else -> ContentScale.Crop
                }
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data(File(bgImageUri!!).takeIf { it.exists() } ?: bgImageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Canvas Background",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = bgOpacity },
                    contentScale = contentScale,
                )
            }

            // Layer 1: Background Floating Images (rendered behind text)
            floatingImages.filter { it.layer == CanvasLayer.BACKGROUND }.forEach { img ->
                FloatingCanvasImageView(
                    image = img,
                    isSelected = (selectedImageId == img.id),
                    isReadingMode = reading,
                    primaryColor = c.primary,
                    surfaceColor = c.surface,
                    textColor = c.text,
                    onSelect = { selectedImageId = img.id },
                    onDeselect = { selectedImageId = null },
                    onLiveTransform = { x, y, scale, rot ->
                        floatingImages = floatingImages.map {
                            if (it.id == img.id) it.copy(x = x, y = y, scale = scale, rotation = rot) else it
                        }
                    },
                    onCommitTransform = { updated ->
                        val list = floatingImages.map { if (it.id == updated.id) updated else it }
                        floatingImages = list
                        recordHistory(newFloatingImages = list)
                    },
                    onToggleLayer = {
                        val updated = img.copy(layer = CanvasLayer.FOREGROUND)
                        val list = floatingImages.map { if (it.id == img.id) updated else it }
                        floatingImages = list
                        recordHistory(newFloatingImages = list)
                        ctx.toast("Moved to Foreground (in front of text)")
                    },
                    onDelete = {
                        val list = floatingImages.filterNot { it.id == img.id }
                        floatingImages = list
                        if (selectedImageId == img.id) selectedImageId = null
                        recordHistory(newFloatingImages = list)
                        ctx.toast("Image removed")
                    },
                )
            }

            // Layer 2: Canvas Content (Reading Mode vs Edit Mode)
            if (reading) {
                val annotatedBody = remember(
                    bodyValue.text,
                    fontSpans,
                    boldSpans,
                    italicSpans,
                    underlineSpans,
                    strikethroughSpans,
                    subscriptSpans,
                    superscriptSpans,
                    codeSpans,
                    quoteSpans,
                    headingSpans,
                    f,
                    c.text,
                    c.isDark,
                ) {
                    buildAnnotatedProse(
                        text = bodyValue.text,
                        boldSpans = boldSpans,
                        italicSpans = italicSpans,
                        underlineSpans = underlineSpans,
                        strikethroughSpans = strikethroughSpans,
                        subscriptSpans = subscriptSpans,
                        superscriptSpans = superscriptSpans,
                        codeSpans = codeSpans,
                        quoteSpans = quoteSpans,
                        headingSpans = headingSpans,
                        fontSpans = fontSpans,
                        wikiFonts = f,
                        textColor = c.text,
                        isDark = c.isDark,
                    )
                }
                SelectionContainer {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        // Subtle reading mode header pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .background(c.surface.copy(alpha = 0.88f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .border(1.dp, c.border.copy(alpha = 0.45f), shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Icon(Icons.Outlined.MenuBook, null, tint = c.muted, modifier = Modifier.size(13.dp))
                                Text(
                                    "Reading Mode",
                                    style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, color = c.muted),
                                )
                            }
                            Text(
                                "Tap to Edit",
                                style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                                modifier = Modifier
                                    .clickable { reading = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }

                        val fullText = bodyValue.text
                        if (fullText.isEmpty()) {
                            Text(
                                "(Empty diary entry)",
                                style = proseStyle.copy(fontStyle = FontStyle.Italic, color = c.muted.copy(alpha = 0.6f)),
                            )
                        } else {
                            val dividerRegex = Regex("""^\s*[─\-_]{3,}\s*$""")
                            val todoRegex = Regex("""^\s*([☐☑]|\[[ xX]\])\s*(.*)$""")
                            val quoteRegex = Regex("""^\s*>\s*(.*)$""")

                            var lineStart = 0
                            val lines = fullText.split('\n')
                            lines.forEachIndexed { _, line ->
                                val lineEnd = lineStart + line.length
                                val isQuotedSpan = line.isNotBlank() && quoteSpans.any { it.start <= lineStart && it.end >= lineEnd }

                                when {
                                    dividerRegex.matches(line) -> {
                                        // Full width thin line for section divider
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp)
                                                .height(1.dp)
                                                .background(c.border.copy(alpha = 0.65f))
                                        )
                                    }
                                    todoRegex.matches(line) -> {
                                        val match = todoRegex.find(line)!!
                                        val symbol = match.groupValues[1]
                                        val isChecked = symbol == "☑" || symbol.equals("[x]", ignoreCase = true) || symbol.equals("[X]", ignoreCase = true)
                                        val todoContent = match.groupValues[2]
                                        val lineOffset = lineStart
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    val updatedText = ListAndFormatUtils.toggleCheckboxOnLine(bodyValue.text, lineOffset)
                                                    bodyValue = bodyValue.copy(text = updatedText)
                                                    save()
                                                }
                                                .padding(vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // Checkbox Box (not brackets)
                                            Box(
                                                modifier = Modifier
                                                    .size(19.dp)
                                                    .background(
                                                        color = if (isChecked) c.primary else Color.Transparent,
                                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                                                    )
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = if (isChecked) c.primary else c.muted.copy(alpha = 0.65f),
                                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(3.dp),
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Check,
                                                        contentDescription = "Completed",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp),
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = todoContent,
                                                style = proseStyle.copy(
                                                    textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                                    color = if (isChecked) c.muted else c.text,
                                                ),
                                            )
                                        }
                                    }
                                    quoteRegex.matches(line) || isQuotedSpan -> {
                                        val cleanQuote = if (quoteRegex.matches(line)) quoteRegex.find(line)!!.groupValues[1] else line
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .background(c.surface, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                                                .border(1.dp, c.border.copy(alpha = 0.25f), shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            // 3px solid border on left
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(24.dp)
                                                    .background(c.border, shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
                                            )
                                            // padding thodi si (0.15rem 0.5rem -> 3.dp vertical, 8.dp horizontal)
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = cleanQuote,
                                                    style = proseStyle.copy(
                                                        fontStyle = FontStyle.Italic,
                                                        color = c.muted,
                                                    ),
                                                )
                                            }
                                        }
                                    }
                                    else -> {
                                        val lineAnnotated = annotatedBody.subSequence(lineStart, lineEnd)
                                        Text(text = lineAnnotated, style = proseStyle)
                                    }
                                }
                                lineStart = lineEnd + 1
                            }
                        }
                    }
                }
            } else {
                var textLayoutResult by remember { mutableStateOf<androidx.compose.ui.text.TextLayoutResult?>(null) }
                BasicTextField(
                    value = bodyValue,
                    onValueChange = { newValue ->
                        if (bodyValue != newValue) {
                            val oldLen = bodyValue.text.length
                            val newLen = newValue.text.length
                            val editIndex = newValue.selection.end
                            val delta = newLen - oldLen
                            val insertedStart = if (delta > 0) editIndex - delta else 0
                            val isEnter = delta > 0 && newValue.text.getOrNull(insertedStart) == '\n'

                            val enterHandledValue = if (isEnter) {
                                ListAndFormatUtils.handleEnterKey(bodyValue.text, newValue.text, editIndex)
                            } else null

                            val finalValue = enterHandledValue ?: newValue
                            val effectiveNewLen = finalValue.text.length
                            val effectiveEditIndex = finalValue.selection.end

                            val edited = TextEditDiff.applyToSpans(
                                oldText = bodyValue.text,
                                newText = finalValue.text,
                                fontSpans = fontSpans,
                                boldSpans = boldSpans,
                                italicSpans = italicSpans,
                                underlineSpans = underlineSpans,
                                strikethroughSpans = strikethroughSpans,
                                subscriptSpans = subscriptSpans,
                                superscriptSpans = superscriptSpans,
                                codeSpans = codeSpans,
                                quoteSpans = quoteSpans,
                                headingSpans = headingSpans,
                                activeFontId = activeFontId,
                                boldOn = isBoldActive,
                                italicOn = isItalicActive,
                                underlineOn = isUnderlineActive,
                                strikethroughOn = isStrikethroughActive,
                                subscriptOn = isSubscriptActive,
                                superscriptOn = isSuperscriptActive,
                                codeOn = isCodeActive,
                                quoteOn = isQuoteActive,
                                headingLevel = activeHeadingLevel,
                            )
                            val updatedFontSpans = edited.fontSpans
                            val updatedBold = edited.boldSpans
                            val updatedItalic = edited.italicSpans
                            val updatedUnderline = edited.underlineSpans
                            val updatedStrikethrough = edited.strikethroughSpans
                            val updatedSubscript = edited.subscriptSpans
                            val updatedSuperscript = edited.superscriptSpans
                            val updatedCode = edited.codeSpans
                            val updatedQuote = edited.quoteSpans
                            val updatedHeadings = edited.headingSpans

                            if (isEnter) {
                                activeHeadingLevel = null
                            }

                            // Sync list states and span states with current cursor / line
                            val cur = effectiveEditIndex
                            val lineStart = if (cur <= 0) 0 else finalValue.text.lastIndexOf('\n', cur - 1) + 1
                            val nextNewline = finalValue.text.indexOf('\n', cur).let { if (it == -1) finalValue.text.length else it }
                            val currentLine = if (lineStart < finalValue.text.length) finalValue.text.substring(lineStart, nextNewline) else ""

                            if (finalValue.text.isNotEmpty()) {
                                isBulletListActive = currentLine.startsWith("• ")
                                isNumberedListActive = Regex("""^\s*\d+\.\s+""").containsMatchIn(currentLine)
                                isTodoListActive = Regex("""^\s*([☐☑]|\[[ xX]\])\s+""").containsMatchIn(currentLine)
                            }

                            // If selection collapsed (just cursor movement), sync active states with cursor position
                            if (oldLen == effectiveNewLen && finalValue.selection.start == finalValue.selection.end) {
                                isBoldActive = FormatSpanManager.isCursorCovered(updatedBold, cur)
                                isItalicActive = FormatSpanManager.isCursorCovered(updatedItalic, cur)
                                isUnderlineActive = FormatSpanManager.isCursorCovered(updatedUnderline, cur)
                                isStrikethroughActive = FormatSpanManager.isCursorCovered(updatedStrikethrough, cur)
                                isSubscriptActive = FormatSpanManager.isCursorCovered(updatedSubscript, cur)
                                isSuperscriptActive = FormatSpanManager.isCursorCovered(updatedSuperscript, cur)
                                isCodeActive = FormatSpanManager.isCursorCovered(updatedCode, cur)
                                isQuoteActive = FormatSpanManager.isCursorCovered(updatedQuote, cur)
                                activeHeadingLevel = HeadingSpanManager.getHeadingAtCursor(updatedHeadings, cur)
                            }

                            fontSpans = updatedFontSpans
                            boldSpans = updatedBold
                            italicSpans = updatedItalic
                            underlineSpans = updatedUnderline
                            strikethroughSpans = updatedStrikethrough
                            subscriptSpans = updatedSubscript
                            superscriptSpans = updatedSuperscript
                            codeSpans = updatedCode
                            quoteSpans = updatedQuote
                            headingSpans = updatedHeadings

                            bodyValue = finalValue
                            recordHistory(
                                newBody = finalValue,
                                newFontSpans = updatedFontSpans,
                                newBoldSpans = updatedBold,
                                newItalicSpans = updatedItalic,
                                newUnderlineSpans = updatedUnderline,
                                newStrikethroughSpans = updatedStrikethrough,
                                newSubscriptSpans = updatedSubscript,
                                newSuperscriptSpans = updatedSuperscript,
                                newCodeSpans = updatedCode,
                                newQuoteSpans = updatedQuote,
                                newHeadingSpans = updatedHeadings,
                                newIsBoldActive = isBoldActive,
                                newIsItalicActive = isItalicActive,
                                newIsUnderlineActive = isUnderlineActive,
                                newIsStrikethroughActive = isStrikethroughActive,
                                newIsSubscriptActive = isSubscriptActive,
                                newIsSuperscriptActive = isSuperscriptActive,
                                newIsCodeActive = isCodeActive,
                                newIsQuoteActive = isQuoteActive,
                                newActiveHeadingLevel = activeHeadingLevel,
                            )
                        }
                    },
                    textStyle = proseStyle,
                    visualTransformation = remember(
                        fontSpans,
                        boldSpans,
                        italicSpans,
                        underlineSpans,
                        strikethroughSpans,
                        subscriptSpans,
                        superscriptSpans,
                        codeSpans,
                        quoteSpans,
                        headingSpans,
                        f,
                        c.text,
                        c.isDark,
                    ) {
                        VisualTransformation { text ->
                            val transformed = buildAnnotatedProse(
                                text = text.text,
                                boldSpans = boldSpans,
                                italicSpans = italicSpans,
                                underlineSpans = underlineSpans,
                                strikethroughSpans = strikethroughSpans,
                                subscriptSpans = subscriptSpans,
                                superscriptSpans = superscriptSpans,
                                codeSpans = codeSpans,
                                quoteSpans = quoteSpans,
                                headingSpans = headingSpans,
                                fontSpans = fontSpans,
                                wikiFonts = f,
                                textColor = c.text,
                                isDark = c.isDark,
                            )
                            TransformedText(transformed, OffsetMapping.Identity)
                        }
                    },
                    cursorBrush = SolidColor(c.primary),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    onTextLayout = { textLayoutResult = it },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .pointerInput(bodyValue.text) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial)
                                    if (event.type == androidx.compose.ui.input.pointer.PointerEventType.Press) {
                                        val change = event.changes.firstOrNull()
                                        if (change != null) {
                                            val pos = change.position
                                            val tlr = textLayoutResult
                                            if (tlr != null) {
                                                val charIndex = tlr.getOffsetForPosition(pos)
                                                val text = bodyValue.text
                                                if (charIndex in 0 until text.length) {
                                                    val lineStart = if (charIndex == 0) 0 else text.lastIndexOf('\n', charIndex - 1).let { if (it == -1) 0 else it + 1 }
                                                    if (charIndex <= lineStart + 1) {
                                                        val char = text.getOrNull(lineStart)
                                                        if (char == '☐' || char == '☑') {
                                                            val updated = ListAndFormatUtils.toggleCheckboxOnLine(text, lineStart)
                                                            bodyValue = bodyValue.copy(text = updated)
                                                            recordHistory(newBody = bodyValue)
                                                            change.consume()
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxSize()) {
                            if (bodyValue.text.isEmpty()) {
                                Text(
                                    "Start writing...",
                                    color = c.muted.copy(alpha = 0.65f),
                                    style = proseStyle.copy(fontStyle = FontStyle.Italic),
                                )
                            }
                            inner()
                        }
                    },
                )
            }

            // Layer 3: Foreground Floating Images (rendered in front of text)
            floatingImages.filter { it.layer == CanvasLayer.FOREGROUND }.forEach { img ->
                FloatingCanvasImageView(
                    image = img,
                    isSelected = (selectedImageId == img.id),
                    isReadingMode = reading,
                    primaryColor = c.primary,
                    surfaceColor = c.surface,
                    textColor = c.text,
                    onSelect = { selectedImageId = img.id },
                    onDeselect = { selectedImageId = null },
                    onLiveTransform = { x, y, scale, rot ->
                        floatingImages = floatingImages.map {
                            if (it.id == img.id) it.copy(x = x, y = y, scale = scale, rotation = rot) else it
                        }
                    },
                    onCommitTransform = { updated ->
                        val list = floatingImages.map { if (it.id == updated.id) updated else it }
                        floatingImages = list
                        recordHistory(newFloatingImages = list)
                    },
                    onToggleLayer = {
                        val updated = img.copy(layer = CanvasLayer.BACKGROUND)
                        val list = floatingImages.map { if (it.id == img.id) updated else it }
                        floatingImages = list
                        recordHistory(newFloatingImages = list)
                        ctx.toast("Moved to Background (behind text)")
                    },
                    onDelete = {
                        val list = floatingImages.filterNot { it.id == img.id }
                        floatingImages = list
                        if (selectedImageId == img.id) selectedImageId = null
                        recordHistory(newFloatingImages = list)
                        ctx.toast("Image removed")
                    },
                )
            }

            // Layer 4: Quick selection & Layers strip (shown in Edit Mode when canvas has images)
            if (!reading && floatingImages.isNotEmpty()) {
                FloatingImagesLayerBar(
                    images = floatingImages,
                    selectedImageId = selectedImageId,
                    primaryColor = c.primary,
                    surfaceColor = c.surface,
                    onSelect = { id -> selectedImageId = if (selectedImageId == id) null else id },
                    onAddMore = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 12.dp),
                )
            }
        }

        // ---------------------------------------------- toolbar & attached font bar
        if (!reading) {
            // Attached Font Bar directly above the toolbar
            if (showFontBar) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border.copy(alpha = 0.4f)))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(c.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = if (activeFontId != null) {
                                val fontName = allFonts.firstOrNull { it.id == activeFontId }?.name ?: "Custom"
                                "Active Font: $fontName (Tap again to disable)"
                            } else {
                                "Fonts (Select text or tap to enable for typing)"
                            },
                            style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.Normal, color = c.muted),
                            modifier = Modifier.weight(1f),
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Upload .ttf / .otf button
                            Row(
                                modifier = Modifier
                                    .background(c.bg, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                    .border(1.dp, c.border.copy(alpha = 0.5f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                    .clickable {
                                        fontPickerLauncher.launch(arrayOf("font/ttf", "font/otf", "application/x-font-ttf", "application/x-font-opentype", "*/*"))
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(Icons.Outlined.Upload, contentDescription = "Upload .ttf font", tint = c.primary, modifier = Modifier.size(14.dp))
                                Text(
                                    "Upload .ttf",
                                    style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.Normal, color = c.text),
                                )
                            }
                            // Close button
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Close font bar",
                                tint = c.muted,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { showFontBar = false },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        allFonts.forEach { fontItem ->
                            val isSelectedOnSelection = if (bodyValue.selection.start != bodyValue.selection.end) {
                                val selMin = bodyValue.selection.min
                                val selMax = bodyValue.selection.max
                                fontSpans.any { it.fontId == fontItem.id && it.start <= selMin && it.end >= selMax }
                            } else false

                            val isActive = (fontItem.id == activeFontId) || isSelectedOnSelection || (fontItem.id == "builtin_default" && activeFontId == null && !isSelectedOnSelection)

                            val fontStyleFamily = CustomFontManager.resolveFontFamily(fontItem.id, f) ?: f.sans

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isActive) c.primary else c.bg,
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                    )
                                    .border(
                                        1.dp,
                                        if (isActive) c.primary else c.border.copy(alpha = 0.5f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                                    )
                                    .clickable {
                                        applyOrToggleFont(fontItem.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = fontItem.name,
                                    style = TextStyle(
                                        fontFamily = fontStyleFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isActive) Color.White else c.text,
                                    ),
                                )
                            }
                        }
                    }
                }
            }

            // Attached Background Image Bottombar directly above the toolbar
            if (showBgPanel) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.border.copy(alpha = 0.4f)))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(c.surface)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Left: Canvas Background label
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(Icons.Outlined.Wallpaper, contentDescription = null, tint = c.primary, modifier = Modifier.size(16.dp))
                            Text(
                                "Canvas Background",
                                style = TextStyle(fontFamily = f.sans, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.text),
                            )
                        }

                        // Right: Controls
                        if (bgImageUri == null) {
                            // No image chosen: only "Choose Image" & "X" (close)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(c.primary.copy(alpha = 0.12f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .border(1.dp, c.primary.copy(alpha = 0.45f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .clickable {
                                            bgPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = "Choose image", tint = c.primary, modifier = Modifier.size(14.dp))
                                    Text(
                                        "Choose Image",
                                        style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = c.primary),
                                    )
                                }

                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Close",
                                    tint = c.muted,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .clickable { showBgPanel = false },
                                )
                            }
                        }
                    }
                    if (bgImageUri != null) {
                        Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                // Change Image button
                                Row(
                                    modifier = Modifier
                                        .background(c.primary.copy(alpha = 0.10f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .border(1.dp, c.primary.copy(alpha = 0.35f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .clickable {
                                            bgPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(Icons.Outlined.PhotoSizeSelectActual, contentDescription = "Change Image", tint = c.primary, modifier = Modifier.size(13.dp))
                                    Text(
                                        "Change Image",
                                        style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.primary),
                                    )
                                }

                                // Edit button
                                Row(
                                    modifier = Modifier
                                        .background(c.primary.copy(alpha = 0.10f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .border(1.dp, c.primary.copy(alpha = 0.35f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .clickable { showBgEditDialog = true }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(Icons.Outlined.Tune, contentDescription = "Edit", tint = c.primary, modifier = Modifier.size(13.dp))
                                    Text(
                                        "Edit",
                                        style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = c.primary),
                                    )
                                }

                                // Remove button
                                Row(
                                    modifier = Modifier
                                        .background(c.danger.copy(alpha = 0.08f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .border(1.dp, c.danger.copy(alpha = 0.25f), shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .clickable {
                                            bgImageUri = null
                                            recordHistory(newBgImageUri = null)
                                            ctx.toast("Background removed")
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Remove", tint = c.danger, modifier = Modifier.size(13.dp))
                                    Text(
                                        "Remove",
                                        style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, color = c.danger),
                                    )
                                }

                                // Done button
                                Row(
                                    modifier = Modifier
                                        .background(c.primary, shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                                        .clickable { showBgPanel = false }
                                        .padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                ) {
                                    Icon(Icons.Outlined.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(13.dp))
                                    Text(
                                        "Done",
                                        style = TextStyle(fontFamily = f.sans, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White),
                                    )
                                }
                            }
                    }
                }
            }

            Box(Modifier.fillMaxWidth().height(1.dp).background(c.border.copy(alpha = 0.4f)))
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(c.surface)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TOOL_GROUPS.forEachIndexed { gi, group ->
                    if (gi > 0) {
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .width(1.dp)
                                .height(20.dp)
                                .background(c.border.copy(alpha = 0.30f)),
                        )
                    }
                    group.forEach { tool ->
                        when (tool.label) {
                            "Undo" -> {
                                ToolButton(
                                    tool = tool,
                                    enabled = undoRedo.canUndo,
                                    onClick = { performUndo() },
                                )
                            }
                            "Redo" -> {
                                ToolButton(
                                    tool = tool,
                                    enabled = undoRedo.canRedo,
                                    onClick = { performRedo() },
                                )
                            }
                            "Bold" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isBoldCurrent,
                                    enabled = true,
                                    onClick = { toggleBold() },
                                )
                            }
                            "Italic" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isItalicCurrent,
                                    enabled = true,
                                    onClick = { toggleItalic() },
                                )
                            }
                            "Underline" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isUnderlineCurrent,
                                    enabled = true,
                                    onClick = { toggleUnderline() },
                                )
                            }
                            "Strikethrough" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isStrikethroughCurrent,
                                    enabled = true,
                                    onClick = { toggleStrikethrough() },
                                )
                            }
                            "Subscript" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isSubscriptCurrent,
                                    enabled = true,
                                    onClick = { toggleSubscript() },
                                )
                            }
                            "Superscript" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isSuperscriptCurrent,
                                    enabled = true,
                                    onClick = { toggleSuperscript() },
                                )
                            }
                            "Heading 1" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isH1Current,
                                    enabled = true,
                                    onClick = { toggleHeading(1) },
                                )
                            }
                            "Heading 2" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isH2Current,
                                    enabled = true,
                                    onClick = { toggleHeading(2) },
                                )
                            }
                            "Heading 3" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isH3Current,
                                    enabled = true,
                                    onClick = { toggleHeading(3) },
                                )
                            }
                            "Bullet List" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isBulletListActive,
                                    enabled = true,
                                    onClick = { toggleBulletList() },
                                )
                            }
                            "Numbered List" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isNumberedListActive,
                                    enabled = true,
                                    onClick = { toggleNumberedList() },
                                )
                            }
                            "To-Do Checkbox" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isTodoListActive,
                                    enabled = true,
                                    onClick = { toggleTodoCheckbox() },
                                )
                            }
                            "Quotation" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isQuoteCurrent,
                                    enabled = true,
                                    onClick = { toggleQuotation() },
                                )
                            }
                            "Code Snippet" -> {
                                ToolButton(
                                    tool = tool,
                                    active = isCodeCurrent,
                                    enabled = true,
                                    onClick = { toggleCode() },
                                )
                            }
                            "Section Divider" -> {
                                ToolButton(
                                    tool = tool,
                                    enabled = true,
                                    onClick = { insertSectionDivider() },
                                )
                            }
                            "Fonts" -> {
                                ToolButton(
                                    tool = tool,
                                    active = showFontBar || activeFontId != null,
                                    enabled = true,
                                    onClick = {
                                        showFontBar = !showFontBar
                                        if (showFontBar) showBgPanel = false
                                    },
                                )
                            }
                            "Background Image" -> {
                                ToolButton(
                                    tool = tool,
                                    active = showBgPanel || bgImageUri != null,
                                    enabled = true,
                                    onClick = {
                                        showBgPanel = !showBgPanel
                                        if (showBgPanel) showFontBar = false
                                    },
                                )
                            }
                            "Media Picker" -> {
                                ToolButton(
                                    tool = tool,
                                    active = floatingImages.isNotEmpty(),
                                    enabled = true,
                                    onClick = {
                                        mediaPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                )
                            }
                            else -> {
                                ToolButton(tool = tool) { ctx.toast("${tool.label} — to be wired later") }
                            }
                        }
                        Spacer(Modifier.width(2.dp))
                    }
                }
            }
        }
    }

    if (showReminder) {
        ReminderDialog(
            current = reminderAt,
            darkTheme = c.isDark,
            onDismiss = { showReminder = false },
            onSave = { reminderAt = it },
        )
    }
    if (showDelete) {
        val target = currentId?.let { id -> vm.logs.firstOrNull { it.id == id } }
        if (target != null) {
            DeleteEntryDialog(
                log = target,
                onDismiss = { showDelete = false },
                onConfirm = {
                    showDelete = false
                    vm.delete(target.id)
                    vm.back()
                },
            )
        } else {
            showDelete = false
        }
    }

    if (showBgEditDialog && bgImageUri != null) {
        CanvasBackgroundEditDialog(
            bgImageUri = bgImageUri!!,
            currentOpacity = bgOpacity,
            currentFitMode = bgFitMode,
            primaryColor = c.primary,
            surfaceColor = c.surface,
            textColor = c.text,
            mutedColor = c.muted,
            borderColor = c.border,
            onApply = { newOpacity, newFitMode ->
                bgOpacity = newOpacity
                bgFitMode = newFitMode
                recordHistory(newBgOpacity = newOpacity, newBgFitMode = newFitMode)
                ctx.toast("Background updated")
            },
            onDismiss = { showBgEditDialog = false },
        )
    }
}

private fun buildAnnotatedProse(
    text: String,
    boldSpans: List<FormatSpan>,
    italicSpans: List<FormatSpan>,
    underlineSpans: List<FormatSpan>,
    strikethroughSpans: List<FormatSpan>,
    subscriptSpans: List<FormatSpan> = emptyList(),
    superscriptSpans: List<FormatSpan> = emptyList(),
    codeSpans: List<FormatSpan> = emptyList(),
    quoteSpans: List<FormatSpan> = emptyList(),
    headingSpans: List<HeadingSpan>,
    fontSpans: List<FontSpan>,
    wikiFonts: com.likkho.nativeui.ui.theme.WikiFonts,
    textColor: Color = Color.Black,
    isDark: Boolean = false,
): AnnotatedString {
    val builder = AnnotatedString.Builder(text)
    val len = text.length

    // 1. Headings (H1, H2, H3) apply base font size and bold weights
    for (h in headingSpans) {
        val st = h.start.coerceIn(0, len)
        val en = h.end.coerceIn(st, len)
        if (en > st) {
            when (h.level) {
                1 -> builder.addStyle(
                    SpanStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Style,
                        shadow = fauxBold(textColor, 1.1f),
                        letterSpacing = (-0.3).sp,
                    ),
                    st,
                    en,
                )
                2 -> builder.addStyle(
                    SpanStyle(
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Style,
                        shadow = fauxBold(textColor, 1.1f),
                        letterSpacing = (-0.2).sp,
                    ),
                    st,
                    en,
                )
                3 -> builder.addStyle(
                    SpanStyle(
                        fontSize = 18.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Style,
                        shadow = fauxBold(textColor, 0.8f),
                    ),
                    st,
                    en,
                )
            }
        }
    }

    for (s in boldSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            builder.addStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.Style,
                    shadow = fauxBold(textColor, 1.1f),
                ),
                st,
                en,
            )
        }
    }
    for (s in italicSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) builder.addStyle(SpanStyle(fontStyle = FontStyle.Italic), st, en)
    }

    // Subscript formatting
    for (s in subscriptSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            builder.addStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Subscript,
                    fontSize = 11.sp,
                ),
                st,
                en,
            )
        }
    }

    // Superscript formatting
    for (s in superscriptSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            builder.addStyle(
                SpanStyle(
                    baselineShift = BaselineShift.Superscript,
                    fontSize = 11.sp,
                ),
                st,
                en,
            )
        }
    }

    // Code formatting (IBM Plex Mono, 0.88em (~15.sp), surface background chip)
    val codeBg = if (isDark) Color(0xFF242629) else Color(0xFFF1F3F5)
    for (s in codeSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            builder.addStyle(
                SpanStyle(
                    fontFamily = wikiFonts.mono,
                    background = codeBg,
                    color = textColor,
                    fontSize = 15.sp,
                    letterSpacing = (-0.2).sp,
                ),
                st,
                en,
            )
        }
    }

    // Quote lines or spans (italic, muted color, surface background)
    val quoteBg = if (isDark) Color(0xFF242629) else Color(0xFFF1F3F5)
    val quoteRegex = Regex("""(?m)^\s*>\s+.*$""")
    for (m in quoteRegex.findAll(text)) {
        builder.addStyle(
            SpanStyle(
                fontStyle = FontStyle.Italic,
                color = textColor.copy(alpha = 0.70f),
                background = quoteBg,
            ),
            m.range.first,
            m.range.last + 1,
        )
    }
    for (s in quoteSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            builder.addStyle(
                SpanStyle(
                    fontStyle = FontStyle.Italic,
                    color = textColor.copy(alpha = 0.70f),
                    background = quoteBg,
                ),
                st,
                en,
            )
        }
    }

    // Checked To-Do items strike-through in editor (supports ☑ as well as [x]/[X])
    val checkedTodoRegex = Regex("""(?m)^\s*([☑]|\[[xX]\])\s+(.*)$""")
    for (m in checkedTodoRegex.findAll(text)) {
        builder.addStyle(
            SpanStyle(
                color = textColor.copy(alpha = 0.50f),
                textDecoration = TextDecoration.LineThrough,
            ),
            m.range.first,
            m.range.last + 1,
        )
    }
    val uncheckedTodoRegex = Regex("""(?m)^\s*([☐]|\[ \])\s+""")
    for (m in uncheckedTodoRegex.findAll(text)) {
        builder.addStyle(
            SpanStyle(
                color = textColor.copy(alpha = 0.70f),
                fontWeight = FontWeight.Bold,
            ),
            m.range.first,
            m.range.last + 1,
        )
    }

    // Combine underline and strikethrough accurately per range
    if (underlineSpans.isNotEmpty() || strikethroughSpans.isNotEmpty()) {
        val uSet = BooleanArray(len)
        val sSet = BooleanArray(len)
        for (s in underlineSpans) {
            val st = s.start.coerceIn(0, len)
            val en = s.end.coerceIn(st, len)
            for (i in st until en) uSet[i] = true
        }
        for (s in strikethroughSpans) {
            val st = s.start.coerceIn(0, len)
            val en = s.end.coerceIn(st, len)
            for (i in st until en) sSet[i] = true
        }

        var i = 0
        while (i < len) {
            val u = uSet[i]
            val st = sSet[i]
            if (!u && !st) {
                i++
                continue
            }
            val startChunk = i
            while (i < len && uSet[i] == u && sSet[i] == st) {
                i++
            }
            val dec = when {
                u && st -> TextDecoration.combine(
                    listOf(
                        TextDecoration.Underline,
                        TextDecoration.LineThrough,
                    )
                )
                u -> TextDecoration.Underline
                else -> TextDecoration.LineThrough
            }
            builder.addStyle(SpanStyle(textDecoration = dec), startChunk, i)
        }
    }

    for (s in fontSpans) {
        val st = s.start.coerceIn(0, len)
        val en = s.end.coerceIn(st, len)
        if (en > st) {
            CustomFontManager.resolveFontFamily(s.fontId, wikiFonts)?.let { family ->
                val hasRealBold = s.fontId == "builtin_serif" || s.fontId == "builtin_sans"
                builder.addStyle(
                    if (hasRealBold) {
                        SpanStyle(
                            fontFamily = family,
                            shadow = androidx.compose.ui.graphics.Shadow.None,
                            fontSynthesis = androidx.compose.ui.text.font.FontSynthesis.All,
                        )
                    } else {
                        SpanStyle(fontFamily = family)
                    },
                    st,
                    en,
                )
            }
        }
    }

    // Elegant styling for section dividers (──────────)
    val dividerRegex = Regex("[─\\-_]{3,}")
    for (match in dividerRegex.findAll(text)) {
        builder.addStyle(
            SpanStyle(
                color = textColor.copy(alpha = 0.35f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Light,
            ),
            match.range.first,
            match.range.last + 1,
        )
    }

    return builder.toAnnotatedString()
}

/**
 * Bold that always shows. Gilda Display / Short Stack / Plex Mono have no real bold
 * face, so FontWeight.Bold alone changes nothing: a thin same-colour double-strike
 * makes the text visibly thicker.
 */
private fun fauxBold(color: Color, strike: Float): androidx.compose.ui.graphics.Shadow =
    androidx.compose.ui.graphics.Shadow(
        color = color,
        offset = androidx.compose.ui.geometry.Offset(strike, 0f),
        blurRadius = 0.2f,
    )

@Composable
private fun ToolButton(
    tool: Tool,
    enabled: Boolean = true,
    active: Boolean = false,
    onClick: () -> Unit,
) {
    val c = Wiki.colors
    val f = Wiki.fonts
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                if (active) c.primary.copy(alpha = 0.14f) else Color.Transparent,
                shape = shape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val tint = if (active) c.primary else if (enabled) c.text.copy(alpha = 0.82f) else c.muted.copy(alpha = 0.35f)
        if (tool.icon != null) {
            Icon(tool.icon, tool.label, tint = tint, modifier = Modifier.size(19.dp))
        } else if (tool.label == "Fonts" || tool.text == "A") {
            // Elegant, stylish italic serif 'A' for fonts
            Text(
                text = "A",
                style = TextStyle(
                    fontFamily = f.serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Normal,
                    color = tint,
                ),
            )
        } else {
            // Clear, recognizable bold typography for H1, H2, H3
            Text(
                text = tool.text.orEmpty(),
                style = TextStyle(
                    fontFamily = f.sans,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                ),
            )
        }
    }
}
