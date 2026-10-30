package com.example.butcetakip.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.theme.AppTheme
import com.example.butcetakip.theme.ExpenseRed
import com.example.butcetakip.ui.main.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onProfileClick: () -> Unit,
    onSignOut: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Profil Kartı
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .clickable { onProfileClick() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                            Text(
                                firebaseUser?.displayName ?: firebaseUser?.email ?: "Bütçe Takip Kullanıcısı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                firebaseUser?.email ?: "Profili Görüntüle",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Tema Seçimi
            item {
                Text(
                    text = "Görünüm",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                SettingsItem(
                    title = "Uygulama Teması",
                    subtitle = "Sistem veya özel",
                    icon = Icons.Default.Brush,
                    onClick = { /* Theme dialog */ }
                ) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AppTheme.entries.forEach { themeOpt ->
                            FilterChip(
                                selected = state.appTheme == themeOpt,
                                onClick = { viewModel.setTheme(themeOpt) },
                                label = { Text(themeOpt.name) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Yapay Zeka Ayarları
            item {
                Text(
                    text = "Yapay Zeka (AI)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )

                var apiKeyInput by remember { mutableStateOf("") }
                val context = androidx.compose.ui.platform.LocalContext.current
                var hasKey by remember { mutableStateOf(com.example.butcetakip.data.AiAssistantService.hasApiKey(context)) }

                LaunchedEffect(Unit) {
                    apiKeyInput = com.example.butcetakip.data.AiAssistantService.getApiKey(context) ?: ""
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Gemini API Anahtarı", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (hasKey) "✅ Anahtar kayıtlı" else "⚠️ Anahtar girilmedi",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (hasKey) Color(0xFF00E676) else Color(0xFFFF9800)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("API Anahtarı") },
                            placeholder = { Text("AIzaSy...") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (apiKeyInput.isNotBlank()) {
                                        com.example.butcetakip.data.AiAssistantService.saveApiKey(context, apiKeyInput)
                                        hasKey = true
                                        android.widget.Toast.makeText(context, "API Anahtarı kaydedildi!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = apiKeyInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Kaydet", fontWeight = FontWeight.Bold)
                            }
                            if (hasKey) {
                                OutlinedButton(
                                    onClick = {
                                        com.example.butcetakip.data.AiAssistantService.removeApiKey(context)
                                        apiKeyInput = ""
                                        hasKey = false
                                        android.widget.Toast.makeText(context, "API Anahtarı silindi!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ExpenseRed)
                                ) {
                                    Text("Anahtarı Sil", color = ExpenseRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Veri Yönetimi
            item {
                Text(
                    text = "Veri Yönetimi",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                SettingsItem(
                    title = "Tüm Verileri Sıfırla",
                    subtitle = "İşlemleri ve istatistikleri sil",
                    icon = Icons.Default.Delete,
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Hesap
            item {
                Text(
                    text = "Hesap",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                
                SettingsItem(
                    title = "Çıkış Yap",
                    subtitle = "Oturumu kapat",
                    icon = Icons.Default.Logout,
                    isDestructive = true,
                    onClick = { onSignOut() }
                )
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Tüm Verileri Sil", fontWeight = FontWeight.Bold) },
            text = { Text("Tüm işlemleriniz kalıcı olarak silinecektir. Bu işlem geri alınamaz, emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetData()
                        showResetDialog = false
                    }
                ) {
                    Text("Evet, Sil", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    val contentColor = if (isDestructive) ExpenseRed else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top // Hizalamayı Top yaptık
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDestructive) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = contentColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (trailing != null) {
                    trailing()
                }
            }
        }
    }
}
