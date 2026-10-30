package com.example.butcetakip.ui.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.data.Category
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.data.TransactionType
import com.example.butcetakip.data.transactionType
import com.example.butcetakip.theme.ExpenseRed
import com.example.butcetakip.theme.IncomeGreen
import com.example.butcetakip.ui.dashboard.TransactionItem
import com.example.butcetakip.ui.main.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDate(date: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
    return sdf.format(Date(date))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var filterType by remember { mutableStateOf<TransactionType?>(null) }
    var filterCategory by remember { mutableStateOf<Category?>(null) }

    val filtered = state.transactions
        .let { list -> if (filterType != null) list.filter { it.type == filterType } else list }
        .let { list -> if (filterCategory != null) list.filter { it.category == filterCategory } else list }

    // Group by date
    val grouped = filtered.groupBy { formatDate(it.date) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Tüm İşlemler",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
        }
        
        Text(
            text = "💡 Bir işlemi silmek için sola kaydırın",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )
        Text(
            text = "${filtered.size} işlem",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, bottom = 8.dp)
        )

        // Filter chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            item {
                FilterChip(
                    selected = filterType == null,
                    onClick = { filterType = null; filterCategory = null },
                    label = { Text("Tümü") }
                )
            }
            item {
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = { filterType = TransactionType.INCOME; filterCategory = null },
                    label = { Text("💰 Gelir") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                        selectedLabelColor = IncomeGreen
                    )
                )
            }
            item {
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = { filterType = TransactionType.EXPENSE; filterCategory = null },
                    label = { Text("💸 Gider") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                        selectedLabelColor = ExpenseRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🔍", fontSize = 48.sp)
                    Text("İşlem bulunamadı", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                grouped.forEach { (date, txs) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    items(items = txs, key = { it.id }) { tx ->
                        SwipeToDismissTransactionItem(
                            transaction = tx,
                            modifier = Modifier.animateItem(),
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeToDismissTransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    ExpenseRed.copy(alpha = 0.8f) else Color.Transparent,
                label = "swipe_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
    ) {
        TransactionItem(transaction = transaction, currency = transaction.currency)
    }
}
