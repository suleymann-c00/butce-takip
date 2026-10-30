package com.example.butcetakip.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.data.Category
import com.example.butcetakip.data.Currency
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.data.TransactionType
import com.example.butcetakip.theme.ExpenseRed
import com.example.butcetakip.theme.FintechGreen
import com.example.butcetakip.theme.IncomeGreen
import com.example.butcetakip.ui.main.DashboardDateFilter
import com.example.butcetakip.ui.main.FinanceViewModel
import com.example.butcetakip.ui.utils.getCategoryIcon
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onAddClick: () -> Unit,
    onSeeAllClick: () -> Unit,
    onAiClick: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showExpenseChartSheet by remember { mutableStateOf(false) }
    var showIncomeChartSheet by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 56.dp), // Azaltılmış bottom padding
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Filters
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardDateFilter.entries.forEach { filter ->
                        item {
                            FilterChip(
                                selected = state.dashboardFilter == filter,
                                onClick = { viewModel.setDashboardFilter(filter) },
                                label = { Text(filter.label) },
                                shape = RoundedCornerShape(8.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // MIDAS STYLE BALANCE CARD
            item {
                val currentBalance = when (state.selectedCurrency) {
                    Currency.TL -> state.tlBalance
                    Currency.USD -> state.usdBalance
                    Currency.EUR -> state.eurBalance
                    Currency.GOLD -> state.goldBalance
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text("Toplam Varlıklar", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                            
                            // Diğer kurlar küçük sağ üstte (Daima 2 adet kalacak şekilde ayarlandı)
                            val bottomRightCurrency = if (state.selectedCurrency == Currency.GOLD) Currency.TL else Currency.GOLD
                            val topRightCurrencies = Currency.entries.filter { it != state.selectedCurrency && it != bottomRightCurrency }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                topRightCurrencies.forEach { curr ->
                                    val currBalance = when (curr) {
                                        Currency.TL -> state.tlBalance
                                        Currency.USD -> state.usdBalance
                                        Currency.EUR -> state.eurBalance
                                        Currency.GOLD -> state.goldBalance
                                    }
                                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.clickable { viewModel.setCurrency(curr) }) {
                                        Text(curr.code, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(com.example.butcetakip.data.formatCurrencyFull(currBalance, curr), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                                     Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            // Dev Seçili Kur ve Altında Delta
                            Column {
                                AnimatedCounter(
                                    count = currentBalance.toFloat(),
                                    currency = state.selectedCurrency,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                val deltaPrefix = if (state.delta > 0) "+" else if (state.delta < 0) "-" else ""
                                val deltaColor = if (state.delta > 0) IncomeGreen else if (state.delta < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "$deltaPrefix${com.example.butcetakip.data.formatCurrencyFull(abs(state.delta), state.selectedCurrency)}",
                                        color = deltaColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (state.delta >= 0) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = deltaColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("(${state.dashboardFilter.label})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            
                            // Sağ Alttaki Kur (Genellikle GOLD)
                            val bottomRightCurrency = if (state.selectedCurrency == Currency.GOLD) Currency.TL else Currency.GOLD
                            val bottomBalance = when (bottomRightCurrency) {
                                Currency.TL -> state.tlBalance
                                Currency.USD -> state.usdBalance
                                Currency.EUR -> state.eurBalance
                                Currency.GOLD -> state.goldBalance
                            }
                            Column(horizontalAlignment = Alignment.End, modifier = Modifier.clickable { viewModel.setCurrency(bottomRightCurrency) }) {
                                Text(bottomRightCurrency.code, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(com.example.butcetakip.data.formatCurrencyFull(bottomBalance, bottomRightCurrency), fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Seçilen Kur Özeti (Fintech Style)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SummaryCard(
                        title = "Gelir",
                        amount = state.totalIncome,
                        icon = Icons.Default.KeyboardArrowUp,
                        color = IncomeGreen,
                        currency = state.selectedCurrency,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Gider",
                        amount = state.totalExpense,
                        icon = Icons.Default.KeyboardArrowDown,
                        color = ExpenseRed,
                        currency = state.selectedCurrency,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Harcama Dağılımı
            if (state.categoryBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Harcama Dağılımı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    CategoryBreakdownCard(
                        breakdown = state.categoryBreakdown,
                        total = state.totalExpense,
                        currency = state.selectedCurrency,
                        onClick = { showExpenseChartSheet = true }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Gelir Dağılımı
            if (state.incomeBreakdown.isNotEmpty()) {
                item {
                    Text(
                        text = "Gelir Dağılımı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    CategoryBreakdownCard(
                        breakdown = state.incomeBreakdown,
                        total = state.totalIncome,
                        currency = state.selectedCurrency,
                        onClick = { showIncomeChartSheet = true }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Son İşlemler
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp).padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Son İşlemler",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onSeeAllClick) {
                        Text("Tümünü Gör", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            val recentTxs = state.transactions.sortedByDescending { it.date }.take(5)
            if (recentTxs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bu kurda henüz işlem yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(recentTxs, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx,
                        currency = state.selectedCurrency,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        // AI FAB
        FloatingActionButton(
            onClick = onAiClick,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 110.dp),
            containerColor = Color(0xFF1C1C22),
            contentColor = FintechGreen,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SmartToy, contentDescription = "AI Asistan")
                Text("AI", fontWeight = FontWeight.Bold, color = FintechGreen)
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 110.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
                Text("İşlem", fontWeight = FontWeight.Bold)
            }
        }
        
        // Expense Sheet
        if (showExpenseChartSheet) {
            ChartSheet(
                title = "Harcama Dağılımı",
                breakdown = state.categoryBreakdown,
                total = state.totalExpense,
                currency = state.selectedCurrency,
                onDismiss = { showExpenseChartSheet = false }
            )
        }
        
        // Income Sheet
        if (showIncomeChartSheet) {
            ChartSheet(
                title = "Gelir Dağılımı",
                breakdown = state.incomeBreakdown,
                total = state.totalIncome,
                currency = state.selectedCurrency,
                onDismiss = { showIncomeChartSheet = false }
            )
        }
    }
}

@Composable
fun AnimatedCounter(count: Float, currency: Currency, style: androidx.compose.ui.text.TextStyle) {
    var animatableState by remember { mutableStateOf(0f) }
    
    LaunchedEffect(count) {
        androidx.compose.animation.core.animate(
            initialValue = animatableState,
            targetValue = count,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        ) { value, _ ->
            animatableState = value
        }
    }
    
    Text(text = com.example.butcetakip.data.formatCurrencyFull(animatableState.toDouble(), currency), style = style)
}

@Composable
fun SummaryCard(title: String, amount: Double, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, currency: Currency, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
                Text(text = title, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedCounter(
                count = amount.toFloat(),
                currency = currency,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

@Composable
fun CategoryBreakdownCard(breakdown: Map<Category, Double>, total: Double, currency: Currency, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val topCategories = breakdown.entries.sortedByDescending { it.value }.take(4)
            topCategories.forEach { (category, amount) ->
                CategoryBar(category, amount, total, currency)
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (breakdown.size > 4) {
                Text(
                    text = "+ ${breakdown.size - 4} Kategori daha (Detaylar için dokunun)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryBar(category: Category, amount: Double, total: Double, currency: Currency) {
    val barColors = listOf(
        Color(0xFF4F9CF9), Color(0xFFFF6B8A), Color(0xFFFFB547),
        Color(0xFF9D79F2), Color(0xFF00C97A), Color(0xFFFF8C42)
    )
    val colorIndex = category.ordinal % barColors.size
    val progress = if (total > 0) (amount / total).toFloat() else 0f
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }
    val animatedProgress by animateFloatAsState(targetValue = if (startAnim) progress else 0f, animationSpec = tween(1000), label = "progress")

    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(getCategoryIcon(category), contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(category.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            
            val percentage = if (total > 0) (amount / total) * 100 else 0.0
            val formattedPercentage = String.format(Locale.US, "%.1f%%", percentage)
            Text(formattedPercentage, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 4.dp))
            Text(com.example.butcetakip.data.formatCurrencyFull(amount, currency), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = barColors[colorIndex],
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartSheet(title: String, breakdown: Map<Category, Double>, total: Double, currency: Currency, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            DonutChart(
                breakdown = breakdown,
                total = total,
                currency = currency,
                modifier = Modifier.size(200.dp).padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(breakdown.entries.sortedByDescending { it.value }) { (cat, amount) ->
                    CategoryBar(category = cat, amount = amount, total = total, currency = currency)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DonutChart(breakdown: Map<Category, Double>, total: Double, currency: Currency, modifier: Modifier = Modifier) {
    val barColors = listOf(
        Color(0xFF4F9CF9), Color(0xFFFF6B8A), Color(0xFFFFB547),
        Color(0xFF9D79F2), Color(0xFF00C97A), Color(0xFFFF8C42)
    )
    
    val proportions = breakdown.values.map { if (total > 0) (it / total).toFloat() else 0f }
    val sweepAngles = proportions.map { it * 360f }
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { startAnim = true }
    
    val animatedSweepAngles = sweepAngles.map { angle ->
        animateFloatAsState(targetValue = if (startAnim) angle else 0f, animationSpec = tween(1000)).value
    }
    
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            
            animatedSweepAngles.forEachIndexed { index, sweepAngle ->
                drawArc(
                    color = barColors[breakdown.keys.elementAt(index).ordinal % barColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 60f, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Toplam", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            AnimatedCounter(count = total.toFloat(), currency = currency, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currency: Currency, modifier: Modifier = Modifier, onDelete: (() -> Unit)? = null) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val prefix = if (isIncome) "+" else "-"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { /* Tıklanma animasyonu için */ },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                val icon = if (transaction.category == Category.CUSTOM && transaction.customCategoryIcon != null) {
                    com.example.butcetakip.ui.utils.getCustomIconByName(transaction.customCategoryIcon)
                } else {
                    getCategoryIcon(transaction.category)
                }
                Icon(
                    icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val catLabel = if (transaction.category == Category.CUSTOM && transaction.customCategoryName != null) {
                        transaction.customCategoryName
                    } else {
                        transaction.category.label
                    }
                    Text(catLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (transaction.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text("Düzenli", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Text(
                text = "$prefix${com.example.butcetakip.data.formatCurrencyFull(transaction.amount, currency)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

fun formatCurrencyRaw(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}
