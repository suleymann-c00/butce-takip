package com.example.butcetakip.ui.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.theme.FintechGreen
import com.example.butcetakip.theme.FintechRed

/**
 * Markdown metnini temizleyip düz, okunabilir hale getirir.
 * Yıldızlar, hash'ler ve gereksiz sembolleri kaldırır.
 */
private fun cleanMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        for ((index, rawLine) in lines.withIndex()) {
            var line = rawLine

            // ### başlıkları
            if (line.trimStart().startsWith("### ")) {
                line = line.trimStart().removePrefix("### ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = FintechGreen)) {
                    append(cleanInlineMarkdown(line))
                }
            }
            // ## başlıkları
            else if (line.trimStart().startsWith("## ")) {
                line = line.trimStart().removePrefix("## ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 17.sp, color = FintechGreen)) {
                    append(cleanInlineMarkdown(line))
                }
            }
            // # başlıkları
            else if (line.trimStart().startsWith("# ")) {
                line = line.trimStart().removePrefix("# ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp, color = FintechGreen)) {
                    append(cleanInlineMarkdown(line))
                }
            }
            // Ayırıcı çizgi
            else if (line.trim() == "---" || line.trim() == "***") {
                withStyle(SpanStyle(color = Color.White.copy(alpha = 0.2f))) {
                    append("━━━━━━━━━━━━━━━━━━━━")
                }
            }
            // Madde işaretli liste
            else if (line.trimStart().matches(Regex("^[*\\-]\\s+.*"))) {
                val content = line.trimStart().removePrefix("* ").removePrefix("- ")
                append("  • ")
                append(cleanInlineMarkdownAnnotated(content))
            }
            // Numaralı liste
            else if (line.trimStart().matches(Regex("^\\d+\\.\\s+.*"))) {
                val content = line.trimStart().replace(Regex("^\\d+\\.\\s+"), "")
                val number = line.trimStart().replace(Regex("^(\\d+)\\..*"), "$1")
                withStyle(SpanStyle(color = FintechGreen, fontWeight = FontWeight.Bold)) {
                    append("  $number. ")
                }
                append(cleanInlineMarkdownAnnotated(content))
            }
            // Normal satır
            else {
                append(cleanInlineMarkdownAnnotated(line))
            }

            if (index < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

/**
 * Satır içi markdown işaretlerini (kalın, eğik) temizler ve düz string döndürür.
 */
private fun cleanInlineMarkdown(text: String): String {
    return text
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("\\*(.*?)\\*"), "$1")
        .replace(Regex("`(.*?)`"), "$1")
}

/**
 * Satır içi markdown işaretlerini AnnotatedString olarak render eder.
 */
private fun cleanInlineMarkdownAnnotated(text: String): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            // Kalın metin (**...**)
            val boldMatch = Regex("\\*\\*(.*?)\\*\\*").find(remaining)
            // Eğik metin (*...*)
            val italicMatch = Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)").find(remaining)

            val firstMatch = listOfNotNull(boldMatch, italicMatch).minByOrNull { it.range.first }

            if (firstMatch != null && firstMatch.range.first < remaining.length) {
                // Eşleşme öncesi düz metni ekle
                if (firstMatch.range.first > 0) {
                    append(remaining.substring(0, firstMatch.range.first))
                }

                val content = firstMatch.groupValues[1]
                when (firstMatch) {
                    boldMatch -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(content)
                        }
                    }
                    italicMatch -> {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = Color.White.copy(alpha = 0.8f))) {
                            append(content)
                        }
                    }
                }
                remaining = remaining.substring(firstMatch.range.last + 1)
            } else {
                append(remaining)
                break
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiChatViewModel,
    transactions: List<Transaction>,
    onAddTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (String) -> Unit,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Yeni mesaj geldiğinde otomatik kaydır
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    // API anahtarı kontrolü
    LaunchedEffect(Unit) {
        viewModel.checkApiKey()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(FintechGreen, FintechGreen.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Yapay Zeka Asistanı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (state.isLoading) "Düşünüyor..." else "Çevrimiçi",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (state.isLoading) Color(0xFFFFA726) else FintechGreen
                    )
                }
            }
        }

        // Mesaj Listesi
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // API Key yoksa uyarı
            if (!state.hasApiKey) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FintechRed.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ API Anahtarı Gerekli",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = FintechRed
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Yapay zeka asistanını kullanabilmek için Ayarlar sayfasından Gemini API anahtarınızı eklemelisiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onNavigateToSettings,
                                colors = ButtonDefaults.buttonColors(containerColor = FintechGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ayarlara Git", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Mesajlar
            items(state.messages, key = { it.id }) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 })
                ) {
                    MessageBubble(message = message)
                }
            }

            // Yükleniyor göstergesi
            if (state.isLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(FintechGreen.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = FintechGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = FintechGreen
                        )
                        Text(
                            "Düşünüyor...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        // Mesaj Giriş Alanı
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "Bir mesaj yazın...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    enabled = state.hasApiKey && !state.isLoading,
                    singleLine = false,
                    maxLines = 3,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FintechGreen,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank() && !state.isLoading && state.hasApiKey) {
                                val msg = inputText.trim()
                                inputText = ""
                                focusManager.clearFocus()
                                viewModel.sendMessage(msg, transactions, onAddTransaction, onDeleteTransaction)
                            }
                        }
                    )
                )

                // Gönder Butonu
                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !state.isLoading && state.hasApiKey) {
                            val msg = inputText.trim()
                            inputText = ""
                            focusManager.clearFocus()
                            viewModel.sendMessage(msg, transactions, onAddTransaction, onDeleteTransaction)
                        }
                    },
                    enabled = inputText.isNotBlank() && !state.isLoading && state.hasApiKey,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = FintechGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = FintechGreen.copy(alpha = 0.3f),
                        disabledContentColor = Color.Black.copy(alpha = 0.3f)
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Gönder", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(FintechGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = FintechGreen,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = if (isUser) 18.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp
            ),
            color = if (isUser) FintechGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            tonalElevation = if (isUser) 0.dp else 1.dp
        ) {
            if (isUser) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = cleanMarkdown(message.text),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}
