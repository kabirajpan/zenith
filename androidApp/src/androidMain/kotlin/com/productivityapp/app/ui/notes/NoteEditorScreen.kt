package com.productivityapp.app.ui.notes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import android.content.Context
import android.media.MediaRecorder
import android.media.MediaPlayer
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.util.*
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun NoteEditorScreen(initialNote: NoteItem? = null, onBack: () -> Unit) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    val blocks = remember { 
        mutableStateListOf<NoteBlock>().apply {
            if (initialNote != null) addAll(initialNote.blocks)
            else add(NoteBlock.Text(""))
        }
    }
    var focusedIndex by remember { mutableStateOf(0) }
    val focusRequesters = remember { mutableStateMapOf<Int, FocusRequester>() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            blocks.add(focusedIndex + 1, NoteBlock.Image(it.toString()))
            focusedIndex++
        }
    }

    val context = LocalContext.current
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentRecordingPath by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            mediaRecorder?.release()
            mediaPlayer?.release()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, you might want to auto-start if the user just clicked
        }
    }

    val startRecording = {
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                val file = File(context.cacheDir, "rec_${System.currentTimeMillis()}.m4a")
                currentRecordingPath = file.absolutePath
                val recorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(file.absolutePath)
                    prepare()
                    start()
                }
                mediaRecorder = recorder
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    val stopAndSaveRecording = { index: Int ->
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            blocks[index] = NoteBlock.Audio("0:45", isRecording = false, filePath = currentRecordingPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    var playingIndex by remember { mutableStateOf<Int?>(null) }
    val playbackProgress = remember { mutableStateMapOf<Int, Float>() }

    LaunchedEffect(playingIndex) {
        while (playingIndex != null) {
            mediaPlayer?.let { mp ->
                try {
                    if (mp.isPlaying) {
                        playbackProgress[playingIndex!!] = mp.currentPosition.toFloat() / mp.duration.toFloat()
                    }
                } catch (e: Exception) {}
            }
            kotlinx.coroutines.delay(100)
        }
    }

    val playAudio = { index: Int, path: String ->
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener { 
                    playingIndex = null
                    playbackProgress[index] = 0f
                }
                start()
                playingIndex = index
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val saveAndBack = {
        val contentSummary = blocks.joinToString("\n") { block ->
            when (block) {
                is NoteBlock.Text -> block.content
                is NoteBlock.Checklist -> if (block.isChecked) "[x] ${block.content}" else "[ ] ${block.content}"
                is NoteBlock.Table -> "Table"
                is NoteBlock.Link -> block.url
                is NoteBlock.Image -> "[Image]"
                is NoteBlock.Audio -> "[Audio]"
            }
        }
        NotesRepository.saveOrUpdateNote(initialNote?.id, title, contentSummary, blocks.toList())
        onBack()
    }

    // Handle System Back
    androidx.activity.compose.BackHandler {
        saveAndBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .imePadding()
        ) {
            // Header Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = saveAndBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                
                Text("Notes", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                
                IconButton(onClick = saveAndBack) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
            
            // Title
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) Text("Title", color = Color.Gray, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    innerTextField()
                }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Blocks
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                blocks.add(0, NoteBlock.Text(""))
                                focusedIndex = 0
                            }
                    )
                }

                items(blocks.size) { index ->
                    val requester = focusRequesters.getOrPut(index) { FocusRequester() }
                    val block = blocks[index]
                    
                    Column {
                        when (block) {
                            is NoteBlock.Text -> NoteTextBlock(
                                content = block.content,
                                focusRequester = requester,
                                showPlaceholder = index == 0 && blocks.size == 1,
                                onFocus = { focusedIndex = index },
                                onContentChange = { blocks[index] = NoteBlock.Text(it) },
                                onEnter = {
                                    blocks.add(index + 1, NoteBlock.Text(""))
                                    focusedIndex = index + 1
                                },
                                onBackspace = {
                                    if (index > 0) {
                                        val prevBlock = blocks[index - 1]
                                        if (prevBlock !is NoteBlock.Text && prevBlock !is NoteBlock.Checklist) {
                                            // If above is a Table/Image/etc, delete IT instead of current line
                                            blocks.removeAt(index - 1)
                                            focusedIndex = index - 1
                                        } else if (block.content.isEmpty()) {
                                            // If above is text and current is empty, delete CURRENT line
                                            blocks.removeAt(index)
                                            focusedIndex = index - 1
                                        }
                                    }
                                }
                            )
                            is NoteBlock.Checklist -> NoteChecklistItem(
                                content = block.content,
                                isChecked = block.isChecked,
                                focusRequester = requester,
                                onFocus = { focusedIndex = index },
                                onContentChange = { blocks[index] = NoteBlock.Checklist(it, block.isChecked) },
                                onCheckedChange = { blocks[index] = NoteBlock.Checklist(block.content, it) },
                                onEnter = {
                                    blocks.add(index + 1, NoteBlock.Checklist("", false))
                                    focusedIndex = index + 1
                                },
                                onBackspace = {
                                    if (index > 0) {
                                        val prevBlock = blocks[index - 1]
                                        if (prevBlock !is NoteBlock.Text && prevBlock !is NoteBlock.Checklist) {
                                            // If above is a Table/Image/etc, delete IT instead of current line
                                            blocks.removeAt(index - 1)
                                            focusedIndex = index - 1
                                        } else if (block.content.isEmpty()) {
                                            // If current is empty, convert to text or delete
                                            if (block.content.isEmpty()) {
                                                blocks[index] = NoteBlock.Text("")
                                            } else {
                                                blocks.removeAt(index)
                                                focusedIndex = index - 1
                                            }
                                        }
                                    } else if (block.content.isEmpty()) {
                                        blocks[index] = NoteBlock.Text("")
                                    }
                                }
                            )
                            is NoteBlock.Table -> {
                                NoteTableBlock(
                                    data = block.data,
                                    onDataChange = { blocks[index] = NoteBlock.Table(it) }
                                )
                            }
                            is NoteBlock.Link -> {
                                NoteLinkBlock(block.url) { blocks[index] = NoteBlock.Link(it) }
                            }
                            is NoteBlock.Image -> {
                                NoteImageBlock(block.uri, onDelete = { blocks.removeAt(index) })
                            }
                            is NoteBlock.Audio -> {
                                NoteAudioBlock(
                                    duration = block.duration,
                                    isRecording = block.isRecording,
                                    progress = playbackProgress[index] ?: 0f,
                                    onPlay = { block.filePath?.let { playAudio(index, it) } },
                                    onStop = { stopAndSaveRecording(index) },
                                    onDelete = { blocks.removeAt(index) }
                                )
                            }
                        }
                        
                        // Clickable gap below each item
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null
                                ) {
                                    blocks.add(index + 1, NoteBlock.Text(""))
                                    focusedIndex = index + 1
                                }
                        )
                    }
                }
                
                // Final bottom spacer for extra breathing room at the end
                item {
                    Spacer(modifier = Modifier.height(150.dp))
                }
            }
        }

        // Persistent Tools Bar (Apple Style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .padding(horizontal = 20.dp)
        ) {
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(24.dp),
                elevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ToolButton(Icons.Default.CheckBox) { 
                            if (focusedIndex < blocks.size && blocks[focusedIndex] is NoteBlock.Text) {
                                blocks[focusedIndex] = NoteBlock.Checklist((blocks[focusedIndex] as NoteBlock.Text).content, false)
                            } else {
                                blocks.add(focusedIndex + 1, NoteBlock.Checklist("", false))
                                focusedIndex++
                            }
                        }
                        ToolButton(Icons.Default.Link) { 
                            blocks.add(focusedIndex + 1, NoteBlock.Link("")) 
                            focusedIndex++
                        }
                        ToolButton(Icons.Default.Image) { imagePicker.launch("image/*") }
                        ToolButton(Icons.Default.Mic) { 
                            startRecording()
                            blocks.add(focusedIndex + 1, NoteBlock.Audio("0:00", isRecording = true))
                            focusedIndex++
                        }
                        ToolButton(Icons.Default.TableChart) { 
                            blocks.add(focusedIndex + 1, NoteBlock.Table(listOf(listOf("", ""), listOf("", ""))))
                            focusedIndex++
                        }
                    }
                    
                    Text(
                        "Done",
                        color = Color(0xFF818CF8),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp).clickable { 
                            saveAndBack() 
                        }
                    )
                }
            }
        }
    }
    
    // Auto-focus effect - wrapped in try-catch to prevent IllegalStateException
    // Added blocks.size as a key so it re-triggers when blocks are removed/added
    LaunchedEffect(focusedIndex, blocks.size) {
        try {
            focusRequesters[focusedIndex]?.requestFocus()
        } catch (e: Exception) {
            // Focus requester might not be attached to any node yet or doesn't support focus
        }
    }
}

sealed class NoteBlock {
    data class Text(val content: String) : NoteBlock()
    data class Checklist(val content: String, val isChecked: Boolean) : NoteBlock()
    data class Table(val data: List<List<String>>) : NoteBlock()
    data class Link(val url: String) : NoteBlock()
    data class Image(val uri: String) : NoteBlock()
    data class Audio(val duration: String, val isRecording: Boolean = false, val filePath: String? = null) : NoteBlock()
}

@Composable
fun ToolButton(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
    }
}

@Composable
fun NoteTextBlock(
    content: String, 
    focusRequester: FocusRequester,
    showPlaceholder: Boolean,
    onFocus: () -> Unit,
    onContentChange: (String) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
) {
    BasicTextField(
        value = content,
        onValueChange = {
            if (it.endsWith("\n")) onEnter()
            else onContentChange(it.replace("\n", ""))
        },
        textStyle = TextStyle(color = Color.White.copy(alpha = 0.9f), fontSize = 17.sp, lineHeight = 26.sp),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .onFocusChanged { if (it.isFocused) onFocus() }
            .onKeyEvent { 
                if (it.key == Key.Backspace && content.isEmpty()) { onBackspace(); true } else false
            },
        decorationBox = { innerTextField ->
            if (showPlaceholder && content.isEmpty()) Text("Start typing...", color = Color.Gray, fontSize = 17.sp)
            innerTextField()
        }
    )
}

@Composable
fun NoteChecklistItem(
    content: String, 
    isChecked: Boolean, 
    focusRequester: FocusRequester,
    onFocus: () -> Unit,
    onContentChange: (String) -> Unit, 
    onCheckedChange: (Boolean) -> Unit,
    onEnter: () -> Unit,
    onBackspace: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Custom Square Checkbox
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isChecked) Color(0xFF818CF8) else Color.Transparent)
                .border(2.dp, if (isChecked) Color(0xFF818CF8) else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .clickable { onCheckedChange(!isChecked) },
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        BasicTextField(
            value = content,
            onValueChange = {
                if (it.endsWith("\n")) onEnter()
                else onContentChange(it.replace("\n", ""))
            },
            textStyle = TextStyle(
                color = if (isChecked) Color.Gray else Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else null
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onFocus() }
                .onKeyEvent { 
                    if (it.key == Key.Backspace && content.isEmpty()) { onBackspace(); true } else false
                }
        )
    }
}

@Composable
fun NoteTableBlock(data: List<List<String>>, onDataChange: (List<List<String>>) -> Unit) {
    var expandedRowIndex by remember { mutableStateOf(-1) }
    var expandedColIndex by remember { mutableStateOf(-1) }
    var isTableFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isTableFocused = it.hasFocus }
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        // Column Headers (3 Dots for Columns) - Only visible when focused
        androidx.compose.animation.AnimatedVisibility(visible = isTableFocused) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (data.isNotEmpty()) {
                    data[0].forEachIndexed { colIndex, _ ->
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = { expandedColIndex = colIndex }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
                            }
                            
                            if (expandedColIndex == colIndex) {
                                androidx.compose.ui.window.Popup(
                                    onDismissRequest = { expandedColIndex = -1 },
                                    alignment = Alignment.TopCenter
                                ) {
                                    Surface(
                                        color = Color(0xFF1E293B),
                                        shape = RoundedCornerShape(8.dp),
                                        elevation = 8.dp,
                                        modifier = Modifier.width(130.dp)
                                    ) {
                                        ColMenuContent(
                                            colIndex = colIndex,
                                            data = data,
                                            onDataChange = onDataChange,
                                            onDismiss = { expandedColIndex = -1 }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(24.dp)) // Offset for row menu
                }
            }
        }

        data.forEachIndexed { rowIndex, row ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                row.forEachIndexed { colIndex, cell ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(0.5.dp, Color.White.copy(alpha = 0.1f))
                            .padding(8.dp)
                    ) {
                        BasicTextField(
                            value = cell,
                            onValueChange = { newVal ->
                                val newData = data.mapIndexed { r, rList ->
                                    if (r == rowIndex) rList.mapIndexed { c, cVal -> if (c == colIndex) newVal else cVal }
                                    else rList
                                }
                                onDataChange(newData)
                            },
                            textStyle = TextStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp),
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Row Menu Trigger - Only visible when focused
                if (isTableFocused) {
                    Box {
                        IconButton(onClick = { expandedRowIndex = rowIndex }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreHoriz, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        }
                        if (expandedRowIndex == rowIndex) {
                            androidx.compose.ui.window.Popup(
                                onDismissRequest = { expandedRowIndex = -1 },
                                alignment = Alignment.TopEnd
                            ) {
                                Surface(
                                    color = Color(0xFF1E293B),
                                    shape = RoundedCornerShape(8.dp),
                                    elevation = 8.dp,
                                    modifier = Modifier.width(130.dp)
                                ) {
                                    RowMenuContent(
                                        rowIndex = rowIndex,
                                        data = data,
                                        onDataChange = onDataChange,
                                        onDismiss = { expandedRowIndex = -1 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Table wide actions - Only visible when focused
        androidx.compose.animation.AnimatedVisibility(visible = isTableFocused) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "+ Add Row", 
                    color = Color(0xFF818CF8).copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val newRow = List(if (data.isEmpty()) 2 else data[0].size) { "" }
                        onDataChange(data + listOf(newRow))
                    }
                )
                Text(
                    "+ Add Column", 
                    color = Color(0xFF818CF8).copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onDataChange(data.map { it + "" })
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun ColMenuContent(
    colIndex: Int, 
    data: List<List<String>>, 
    onDataChange: (List<List<String>>) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.padding(4.dp).verticalScroll(rememberScrollState())) {
        Text(
            "Add Col Right", 
            color = Color.White, 
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().clickable {
                val newData = data.map { row -> 
                    row.toMutableList().apply { add(colIndex + 1, "") }
                }
                onDataChange(newData)
                onDismiss()
            }.padding(8.dp)
        )
        Text(
            "Delete Column", 
            color = Color.Red.copy(alpha = 0.8f), 
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().clickable {
                if (data.isNotEmpty() && data[0].size > 1) {
                    val newData = data.map { row -> 
                        row.toMutableList().apply { removeAt(colIndex) }
                    }
                    onDataChange(newData)
                }
                onDismiss()
            }.padding(8.dp)
        )
    }
}

@Composable
fun RowMenuContent(
    rowIndex: Int, 
    data: List<List<String>>, 
    onDataChange: (List<List<String>>) -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = Modifier.padding(4.dp).verticalScroll(rememberScrollState())) {
        Text(
            "Add Row Below", 
            color = Color.White, 
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().clickable {
                val newRow = List(data[0].size) { "" }
                onDataChange(data.toMutableList().apply { add(rowIndex + 1, newRow) })
                onDismiss()
            }.padding(8.dp)
        )
        Text(
            "Delete Row", 
            color = Color.Red.copy(alpha = 0.8f), 
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().clickable {
                if (data.size > 1) {
                    onDataChange(data.toMutableList().apply { removeAt(rowIndex) })
                }
                onDismiss()
            }.padding(8.dp)
        )
    }
}

@Composable
fun NoteLinkBlock(url: String, onUrlChange: (String) -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val focusRequester = remember { FocusRequester() }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isEditing) {
            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = url,
                onValueChange = onUrlChange,
                textStyle = TextStyle(color = Color(0xFF60A5FA), fontSize = 16.sp, textDecoration = TextDecoration.Underline),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester).onFocusChanged { if (!it.isFocused) isEditing = false },
                decorationBox = { innerTextField ->
                    if (url.isEmpty()) Text("Paste link here...", color = Color.Gray.copy(alpha = 0.5f), fontSize = 16.sp)
                    innerTextField()
                }
            )
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        } else {
            Text(
                text = if (url.isEmpty()) "Add link..." else url,
                color = if (url.isEmpty()) Color.Gray.copy(alpha = 0.5f) else Color(0xFF60A5FA),
                fontSize = 16.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { if (url.isNotEmpty()) try { uriHandler.openUri(if (!url.startsWith("http")) "https://$url" else url) } catch (e: Exception) {} },
                            onDoubleTap = { isEditing = true }
                        )
                    }
            )
        }
    }
}

@Composable
fun NoteImageBlock(uri: String, onDelete: () -> Unit) {
    var isFullScreen by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    if (uri.isEmpty()) {
        Box(
            modifier = Modifier.size(120.dp).background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
        }
    } else {
        Box(modifier = Modifier.padding(vertical = 4.dp)) {
            AsyncImage(
                model = uri,
                contentDescription = "Note image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .clickable { isFullScreen = true },
                contentScale = ContentScale.Crop
            )
            
            // Overflow menu for Image
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp).background(Color.Black.copy(alpha = 0.3f), CircleShape)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1E293B)).padding(horizontal = 8.dp)
                ) {
                    Text(
                        "Delete Image", 
                        color = Color.Red.copy(alpha = 0.8f), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { 
                            showMenu = false
                            onDelete() 
                        }.padding(8.dp)
                    )
                }
            }
        }
        
        if (isFullScreen) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { isFullScreen = false }) {
                Box(modifier = Modifier.fillMaxSize().clickable { isFullScreen = false }, contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = uri,
                        contentDescription = "Full image",
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun NoteAudioBlock(duration: String, isRecording: Boolean, progress: Float, onPlay: () -> Unit, onStop: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
            .border(1.dp, if (isRecording) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isRecording) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.Red, CircleShape)
                    .clickable { onStop() },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(12.dp).background(Color.White, RoundedCornerShape(2.dp)))
            }
        } else {
            Surface(
                color = Color(0xFF818CF8),
                shape = CircleShape,
                modifier = Modifier.size(36.dp).clickable { onPlay() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            if (isRecording) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).graphicsLayer(alpha = alpha).background(Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recording...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                    Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFF818CF8), CircleShape))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Voice Memo • $duration", color = Color.Gray, fontSize = 11.sp)
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        if (!isRecording) {
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color(0xFF1E293B)).padding(horizontal = 8.dp)
                ) {
                    Text(
                        "Delete Audio", 
                        color = Color.Red.copy(alpha = 0.8f), 
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { 
                            showMenu = false
                            onDelete() 
                        }.padding(8.dp)
                    )
                }
            }
        }
    }
}
