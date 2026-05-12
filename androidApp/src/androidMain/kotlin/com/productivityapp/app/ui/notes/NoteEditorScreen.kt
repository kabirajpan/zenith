package com.productivityapp.app.ui.notes

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.media.MediaRecorder
import android.media.MediaPlayer
import java.io.File

import com.productivityapp.model.NoteItem
import com.productivityapp.model.NoteBlock
import com.productivityapp.app.ui.notes.components.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState

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
    var showStyleModal by remember { mutableStateOf(false) }

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
    ) { isGranted -> }

    val amplitudes = remember { mutableStateListOf<Float>() }
    var recordingDuration by remember { mutableStateOf(0L) }

    LaunchedEffect(mediaRecorder) {
        if (mediaRecorder != null) {
            amplitudes.clear()
            recordingDuration = 0L
            val startTime = System.currentTimeMillis()
            while (mediaRecorder != null) {
                try {
                    val amp = mediaRecorder?.maxAmplitude ?: 0
                    amplitudes.add((amp.toFloat() / 32767f).coerceIn(0f, 1f))
                    if (amplitudes.size > 100) amplitudes.removeAt(0)
                    recordingDuration = System.currentTimeMillis() - startTime
                } catch (e: Exception) {}
                kotlinx.coroutines.delay(50)
            }
        }
    }

    fun stopAndSaveRecording(index: Int) {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            val minutes = (recordingDuration / 1000) / 60
            val seconds = (recordingDuration / 1000) % 60
            val durationStr = String.format("%d:%02d", minutes, seconds)
            blocks[index] = NoteBlock.Audio(durationStr, isRecording = false, filePath = currentRecordingPath)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startRecording() {
        if (mediaRecorder != null) {
            val recordingIndex = blocks.indexOfFirst { it is NoteBlock.Audio && it.isRecording }
            if (recordingIndex != -1) stopAndSaveRecording(recordingIndex)
        }

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
            kotlinx.coroutines.delay(32)
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

    fun updateStyle(index: Int, weight: Int? = null, size: Int? = null, align: Int? = null, color: Long? = null) {
        val block = blocks[index]
        when (block) {
            is NoteBlock.Text -> {
                blocks[index] = block.copy(
                    fontWeight = weight ?: block.fontWeight,
                    fontSize = size ?: block.fontSize,
                    textAlign = align ?: block.textAlign,
                    color = color ?: block.color
                )
            }
            is NoteBlock.Checklist -> {
                blocks[index] = block.copy(
                    fontWeight = weight ?: block.fontWeight,
                    fontSize = size ?: block.fontSize,
                    textAlign = align ?: block.textAlign,
                    color = color ?: block.color
                )
            }
            else -> {}
        }
    }

    val saveAndBack = {
        if (mediaRecorder != null) {
            val recordingIndex = blocks.indexOfFirst { it is NoteBlock.Audio && it.isRecording }
            if (recordingIndex != -1) stopAndSaveRecording(recordingIndex)
        }

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

    androidx.activity.compose.BackHandler { saveAndBack() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
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
            
            // Content
            Column(modifier = Modifier.weight(1f).padding(horizontal = 20.dp)) {
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
                
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(blocks) { index, block ->
                        Column {
                            when (block) {
                                is NoteBlock.Text -> NoteTextBlock(
                                    block = block,
                                    isFocused = focusedIndex == index,
                                    showPlaceholder = index == 0 && blocks.size == 1,
                                    onFocus = { focusedIndex = index },
                                    onContentChange = { blocks[index] = block.copy(content = it) },
                                    onEnter = {
                                        blocks.add(index + 1, NoteBlock.Text(""))
                                        focusedIndex = index + 1
                                    },
                                    onBackspace = {
                                        if (index > 0 && block.content.isEmpty()) {
                                            blocks.removeAt(index)
                                            focusedIndex = index - 1
                                        }
                                    }
                                )
                                is NoteBlock.Checklist -> NoteChecklistItem(
                                    block = block,
                                    isFocused = focusedIndex == index,
                                    onFocus = { focusedIndex = index },
                                    onContentChange = { blocks[index] = block.copy(content = it) },
                                    onCheckedChange = { blocks[index] = block.copy(isChecked = it) },
                                    onEnter = {
                                        blocks.add(index + 1, NoteBlock.Checklist("", false))
                                        focusedIndex = index + 1
                                    },
                                    onBackspace = {
                                        if (block.content.isEmpty()) {
                                            blocks[index] = NoteBlock.Text("")
                                            focusedIndex = index
                                        } else if (index > 0) {
                                            blocks.removeAt(index)
                                            focusedIndex = index - 1
                                        }
                                    }
                                )
                                is NoteBlock.Link -> NoteLinkBlock(
                                    url = block.url,
                                    isFocused = focusedIndex == index,
                                    onFocus = { focusedIndex = index },
                                    onUrlChange = { blocks[index] = block.copy(url = it) },
                                    onBackspace = {
                                        if (block.url.isEmpty()) {
                                            blocks[index] = NoteBlock.Text("")
                                            focusedIndex = index
                                        } else if (index > 0) {
                                            blocks.removeAt(index)
                                            focusedIndex = index - 1
                                        }
                                    }
                                )
                                is NoteBlock.Table -> NoteTableBlock(
                                    data = block.data,
                                    onDataChange = { blocks[index] = NoteBlock.Table(it) },
                                    onBackspace = {
                                        if (index > 0) {
                                            blocks.removeAt(index)
                                            focusedIndex = index - 1
                                        }
                                    }
                                )
                                is NoteBlock.Image -> NoteImageBlock(block.uri, onDelete = { blocks.removeAt(index) })
                                is NoteBlock.Audio -> NoteAudioBlock(
                                    duration = if (block.isRecording) {
                                        val mins = (recordingDuration / 1000) / 60
                                        val secs = (recordingDuration / 1000) % 60
                                        String.format("%d:%02d", mins, secs)
                                    } else block.duration,
                                    isRecording = block.isRecording,
                                    progress = playbackProgress[index] ?: 0f,
                                    amplitudes = amplitudes,
                                    onPlay = { block.filePath?.let { playAudio(index, it) } },
                                    onStop = { stopAndSaveRecording(index) },
                                    onDelete = { blocks.removeAt(index) }
                                )
                            }
                            Spacer(modifier = Modifier.fillMaxWidth().height(4.dp).clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null
                            ) {
                                blocks.add(index + 1, NoteBlock.Text(""))
                                focusedIndex = index + 1
                            })
                        }
                    }
                    item { Spacer(modifier = Modifier.height(150.dp)) }
                }
            }
        }

        // Floating Control Panel
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Style Panel
            androidx.compose.animation.AnimatedVisibility(
                visible = showStyleModal,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Surface(
                    color = Color(0xFF1E293B).copy(alpha = 0.98f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val currentBlock = blocks.getOrNull(focusedIndex)
                        if (currentBlock is NoteBlock.Text || currentBlock is NoteBlock.Checklist) {
                            val currentSize = if (currentBlock is NoteBlock.Text) currentBlock.fontSize else (currentBlock as? NoteBlock.Checklist)?.fontSize ?: 17
                            val currentWeight = if (currentBlock is NoteBlock.Text) currentBlock.fontWeight else (currentBlock as? NoteBlock.Checklist)?.fontWeight ?: 400
                            val currentAlign = if (currentBlock is NoteBlock.Text) currentBlock.textAlign else (currentBlock as? NoteBlock.Checklist)?.textAlign ?: 0
                            val currentColor = if (currentBlock is NoteBlock.Text) currentBlock.color else (currentBlock as? NoteBlock.Checklist)?.color ?: 0xFFFFFFFF

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp)).padding(2.dp)) {
                                    IconButton(onClick = { updateStyle(focusedIndex, size = (currentSize - 1).coerceAtLeast(10)) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                    Text(currentSize.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { updateStyle(focusedIndex, size = (currentSize + 1).coerceAtMost(72)) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    StyleOption("Reg", isSelected = currentWeight == 400) { updateStyle(focusedIndex, weight = 400) }
                                    StyleOption("Bold", isSelected = currentWeight == 700) { updateStyle(focusedIndex, weight = 700) }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            val colors = listOf(0xFFFFFFFF, 0xFF818CF8, 0xFFF87171, 0xFF4ADE80, 0xFFFB7185, 0xFFFBBF24, 0xFFA78BFA)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                colors.forEach { colorVal ->
                                    Box(modifier = Modifier.size(24.dp).background(Color(colorVal), CircleShape).border(if (currentColor == colorVal) 2.dp else 0.dp, Color.White, CircleShape).clickable { updateStyle(focusedIndex, color = colorVal) })
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PresetOption("H1", isSelected = currentSize == 32, modifier = Modifier.weight(1f)) { updateStyle(focusedIndex, size = 32, weight = 700) }
                                PresetOption("H2", isSelected = currentSize == 26, modifier = Modifier.weight(1f)) { updateStyle(focusedIndex, size = 26, weight = 700) }
                                PresetOption("H3", isSelected = currentSize == 22, modifier = Modifier.weight(1f)) { updateStyle(focusedIndex, size = 22, weight = 600) }
                                PresetOption("P", isSelected = currentSize == 17, modifier = Modifier.weight(1f)) { updateStyle(focusedIndex, size = 17, weight = 400) }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StyleOption("Thin", isSelected = currentWeight == 300) { updateStyle(focusedIndex, weight = 300) }
                                    StyleOption("Light", isSelected = currentWeight == 200) { updateStyle(focusedIndex, weight = 200) }
                                }
                                Row(modifier = Modifier.background(Color.White.copy(alpha = 0.05f), CircleShape).padding(2.dp)) {
                                    IconButton(onClick = { updateStyle(focusedIndex, align = 0) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.FormatAlignLeft, contentDescription = null, tint = if (currentAlign == 0) Color(0xFF818CF8) else Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { updateStyle(focusedIndex, align = 1) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.FormatAlignCenter, contentDescription = null, tint = if (currentAlign == 1) Color(0xFF818CF8) else Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(onClick = { updateStyle(focusedIndex, align = 2) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.FormatAlignRight, contentDescription = null, tint = if (currentAlign == 2) Color(0xFF818CF8) else Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        } else {
                            Text("Select a text block to style", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }

            // Toolbar
            Surface(color = Color(0xFF1E293B).copy(alpha = 0.95f), shape = RoundedCornerShape(24.dp), elevation = 8.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showStyleModal = !showStyleModal }, modifier = Modifier.size(36.dp).focusProperties { canFocus = false }) {
                        Icon(Icons.Default.TextFields, contentDescription = "Style", tint = if (showStyleModal) Color(0xFF818CF8) else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                    }
                    ToolButton(Icons.Default.CheckBox) { 
                        if (focusedIndex < blocks.size && blocks[focusedIndex] is NoteBlock.Text) blocks[focusedIndex] = NoteBlock.Checklist((blocks[focusedIndex] as NoteBlock.Text).content, false)
                        else { blocks.add(focusedIndex + 1, NoteBlock.Checklist("", false)); focusedIndex++ }
                    }
                    ToolButton(Icons.Default.GridOn) { blocks.add(focusedIndex + 1, NoteBlock.Table(listOf(listOf("", ""), listOf("", "")))); focusedIndex++ }
                    ToolButton(Icons.Default.Link) { blocks.add(focusedIndex + 1, NoteBlock.Link("")); focusedIndex++ }
                    ToolButton(Icons.Default.Image) { imagePicker.launch("image/*") }
                    ToolButton(Icons.Default.Mic) { startRecording(); blocks.add(focusedIndex + 1, NoteBlock.Audio("0:00", isRecording = true)); focusedIndex++ }
                }
            }
        }
    }
}

@Composable
fun StyleOption(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) Color(0xFF818CF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(8.dp), border = if (isSelected) BorderStroke(1.dp, Color(0xFF818CF8)) else null, modifier = Modifier.clickable { onClick() }) {
        Text(label, color = if (isSelected) Color(0xFF818CF8) else Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp))
    }
}

@Composable
fun PresetOption(label: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(color = if (isSelected) Color(0xFF818CF8) else Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(12.dp), modifier = modifier.height(44.dp).clickable { onClick() }) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f), fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
