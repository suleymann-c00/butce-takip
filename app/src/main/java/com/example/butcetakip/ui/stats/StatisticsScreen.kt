package com.example.butcetakip.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.data.TransactionType
import com.example.butcetakip.theme.ExpenseRed
import com.example.butcetakip.theme.IncomeGreen
import com.example.butcetakip.data.Currency
import com.example.butcetakip.ui.dashboard.TransactionItem
import com.example.butcetakip.ui.dashboard.formatCurrencyRaw
import com.example.butcetakip.ui.main.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

enum class QuickDateFilter { WEEK, MONTH, YEAR, CUSTOM, ALL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: FinanceViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    val currentFilter = try { 
        QuickDateFilter.valueOf(state.statsQuickFilter) 
    } catch(e: Exception) { 
        QuickDateFilter.MONTH 
    }

    // Init default state
    LaunchedEffect(Unit) {
        if (state.startDate == null && state.endDate == null && currentFilter != QuickDateFilter.ALL) {
            val now = Calendar.getInstance().timeInMillis
            val start = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.timeInMillis
            viewModel.setDateRange(start, now)
        }
    }



    fun applyFilter(filter: QuickDateFilter) {
        viewModel.setStatsQuickFilter(filter.name)
        if (filter == QuickDateFilter.CUSTOM) {
            showDatePicker = true
            return
        }
        val now = Calendar.getInstance().timeInMillis
        val start = Calendar.getInstance().apply {
            when (filter) {
                QuickDateFilter.WEEK -> add(Calendar.DAY_OF_YEAR, -7)
                QuickDateFilter.MONTH -> add(Calendar.MONTH, -1)
                QuickDateFilter.YEAR -> add(Calendar.YEAR, -1)
                else -> {}
            }
        }.timeInMillis

        if (filter == QuickDateFilter.ALL) {
            viewModel.setDateRange(null, null)
        } else {
            viewModel.setDateRange(start, now)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "İstatistikler",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val orderedCurrencies = listOf(Currency.GOLD, Currency.USD, Currency.EUR, Currency.TL)
                orderedCurrencies.forEach { curr ->
                    val isSelected = state.selectedCurrency == curr
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.setCurrency(curr) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = curr.code,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = currentFilter == QuickDateFilter.ALL,
                    onClick = { applyFilter(QuickDateFilter.ALL) },
                    label = { Text("Tümü") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == QuickDateFilter.WEEK,
                    onClick = { applyFilter(QuickDateFilter.WEEK) },
                    label = { Text("1 Hafta") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == QuickDateFilter.MONTH,
                    onClick = { applyFilter(QuickDateFilter.MONTH) },
                    label = { Text("1 Ay") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == QuickDateFilter.YEAR,
                    onClick = { applyFilter(QuickDateFilter.YEAR) },
                    label = { Text("1 Yıl") },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            item {
                FilterChip(
                    selected = currentFilter == QuickDateFilter.CUSTOM,
                    onClick = { applyFilter(QuickDateFilter.CUSTOM) },
                    label = { Text("Özel") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        val sDate = state.startDate
        val eDate = state.endDate
        if (sDate != null && eDate != null && currentFilter == QuickDateFilter.CUSTOM) {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale("tr", "TR"))
            Text(
                text = "${sdf.format(Date(sDate))} - ${sdf.format(Date(eDate))}",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val income = state.transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = state.transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val delta = income - expense
        val deltaPrefix = if (delta > 0) "+" else if (delta < 0) "-" else ""
        val deltaColor = if (delta > 0) IncomeGreen else if (delta < 0) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            // Analiz Özeti
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Net Durum (Delta)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Text(
                            text = "$deltaPrefix${state.selectedCurrency.symbol}${formatCurrencyRaw(Math.abs(delta))}",
                            color = deltaColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Gelir", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.selectedCurrency.symbol}${formatCurrencyRaw(income)}", color = IncomeGreen, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Gider", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${state.selectedCurrency.symbol}${formatCurrencyRaw(expense)}", color = ExpenseRed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Gider Trendi
            item {
                Text("Gider Trendi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        AdvancedLineChart(
                            transactions = state.transactions.filter { it.type == TransactionType.EXPENSE },
                            currency = state.selectedCurrency,
                            lineColor = ExpenseRed,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            filterStartDate = sDate,
                            filterEndDate = eDate
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Gelir Trendi
            item {
                Text("Gelir Trendi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        AdvancedLineChart(
                            transactions = state.transactions.filter { it.type == TransactionType.INCOME },
                            currency = state.selectedCurrency,
                            lineColor = IncomeGreen,
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            filterStartDate = sDate,
                            filterEndDate = eDate
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Varlık Trendi
            item {
                Text("Varlık Trendi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(280.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        AdvancedLineChart(
                            transactions = state.allTransactions.filter { it.currency == state.selectedCurrency },
                            currency = state.selectedCurrency,
                            lineColor = Color(0xFF4F9CF9),
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            filterStartDate = sDate,
                            filterEndDate = eDate,
                            isCumulative = true
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text("İşlemler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(8.dp))
            }

            val txs = state.transactions.sortedByDescending { it.date }
            if (txs.isEmpty()) {
                item {
                    Text("Bu aralıkta işlem bulunamadı.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            } else {
                items(txs, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx, 
                        currency = state.selectedCurrency,
                        modifier = Modifier.animateItem()
                    )
                }
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }

    if (showDatePicker) {
        com.example.butcetakip.ui.components.CustomCalendarSheet(
            onDismiss = {
                showDatePicker = false
                if (state.startDate == null) viewModel.setStatsQuickFilter(QuickDateFilter.ALL.name)
            },
            onDateRangeSelected = { start, end ->
                viewModel.setDateRange(start, end)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun AdvancedLineChart(
    transactions: List<Transaction>,
    currency: Currency,
    lineColor: Color,
    modifier: Modifier = Modifier,
    filterStartDate: Long? = null,
    filterEndDate: Long? = null,
    isCumulative: Boolean = false
) {
    if (transactions.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("Grafik için yeterli veri yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    // Gruplama ve boş günleri 0 ile doldurma
    val minDateMs = filterStartDate ?: (transactions.minOfOrNull { it.date } ?: System.currentTimeMillis())
    val maxDateMs = filterEndDate ?: (transactions.maxOfOrNull { it.date } ?: System.currentTimeMillis())

    // Eğer min ve max arası 1 günden azsa, son 7 günü gösterelim ki grafik düz çizgi olsun
    val finalMinDateMs = if (maxDateMs - minDateMs < 24 * 60 * 60 * 1000L) {
        maxDateMs - 6 * 24 * 60 * 60 * 1000L
    } else minDateMs

    val startCal = Calendar.getInstance().apply { 
        timeInMillis = finalMinDateMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val endCal = Calendar.getInstance().apply {
        timeInMillis = maxDateMs
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val dailySums = mutableMapOf<Long, Float>()
    var current = startCal.timeInMillis
    while (current <= endCal.timeInMillis) {
        dailySums[current] = 0f
        current += 24 * 60 * 60 * 1000L
    }

    if (isCumulative) {
        var runningBalance = transactions.filter { it.date < startCal.timeInMillis }.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }.toFloat()
        
        val dailyDeltas = mutableMapOf<Long, Float>()
        transactions.filter { it.date >= startCal.timeInMillis }.forEach { tx ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = tx.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val t = cal.timeInMillis
            val amt = if (tx.type == TransactionType.INCOME) tx.amount.toFloat() else -tx.amount.toFloat()
            dailyDeltas[t] = (dailyDeltas[t] ?: 0f) + amt
        }

        var currentDay = startCal.timeInMillis
        while (currentDay <= endCal.timeInMillis) {
            runningBalance += (dailyDeltas[currentDay] ?: 0f)
            dailySums[currentDay] = runningBalance
            currentDay += 24 * 60 * 60 * 1000L
        }
    } else {
        transactions.filter { it.date >= startCal.timeInMillis }.forEach { tx ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = tx.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val t = cal.timeInMillis
            if (dailySums.containsKey(t)) {
                dailySums[t] = (dailySums[t] ?: 0f) + tx.amount.toFloat()
            }
        }
    }

    val dates = dailySums.keys.toList().sorted()
    val values = dates.map { dailySums[it] ?: 0f }

    val maxVal = values.maxOrNull()?.takeIf { it > 0 } ?: 1f
    
    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val textMeasurer = rememberTextMeasurer()
    
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var touchIndex by remember { mutableStateOf<Int?>(null) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            val drawWidth = size.width - 120f
                            val stepX = drawWidth / (values.size - 1)
                            if (stepX > 0) {
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, values.lastIndex)
                                touchIndex = index
                                touchPosition = offset
                            }
                            tryAwaitRelease()
                            touchIndex = null
                            touchPosition = null
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val drawWidth = size.width - 120f
                            val stepX = drawWidth / (values.size - 1)
                            if (stepX > 0) {
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, values.lastIndex)
                                touchIndex = index
                                touchPosition = offset
                            }
                        },
                        onDrag = { change, _ ->
                            val drawWidth = size.width - 120f
                            val stepX = drawWidth / (values.size - 1)
                            if (stepX > 0) {
                                val index = (change.position.x / stepX).roundToInt().coerceIn(0, values.lastIndex)
                                touchIndex = index
                                touchPosition = change.position
                            }
                        },
                        onDragEnd = {
                            touchIndex = null
                            touchPosition = null
                        },
                        onDragCancel = {
                            touchIndex = null
                            touchPosition = null
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val chartWidth = width - 120f // Sağda Y ekseni metinleri için 120px boşluk
            val stepX = chartWidth / (values.size - 1)
            
            // Grid Lines & Y Ekseni Rakamları
            val gridSteps = 4
            for (i in 0..gridSteps) {
                val y = height - (i * height / gridSteps)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 2f
                )
                // Y ekseni metni
                val valueAtGrid = (i * maxVal) / gridSteps
                drawText(
                    textMeasurer = textMeasurer,
                    text = formatCurrencyRaw(valueAtGrid.toDouble()),
                    style = TextStyle(color = textColor, fontSize = 10.sp),
                    topLeft = Offset(chartWidth + 16f, y - 16f)
                )
            }

            val path = Path()
            val points = values.mapIndexed { index, value ->
                val x = index * stepX
                val y = height - ((value / maxVal) * height)
                Offset(x, y)
            }

            points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    if (values.size > 60) {
                        path.lineTo(point.x, point.y)
                    } else {
                        val prevPoint = points[index - 1]
                        val controlX = (prevPoint.x + point.x) / 2
                        path.cubicTo(
                            controlX, prevPoint.y,
                            controlX, point.y,
                            point.x, point.y
                        )
                    }
                }
            }
            
            // Gradient Fill (Area Chart)
            val fillPath = androidx.compose.ui.graphics.Path()
            fillPath.addPath(path)
            fillPath.lineTo(points.last().x, height)
            fillPath.lineTo(points.first().x, height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.4f), Color.Transparent),
                    startY = 0f,
                    endY = height
                )
            )
            
            // Draw smooth line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw points (dots) like web for better visual
            if (values.size <= 60) {
                points.forEach { point ->
                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = point)
                    drawCircle(color = lineColor, radius = 2.dp.toPx(), center = point)
                }
            }

            // Touch Overlay
            touchIndex?.let { index ->
                if (index in points.indices) {
                    val point = points[index]
                    drawLine(
                        color = lineColor.copy(alpha = 0.5f),
                        start = Offset(point.x, 0f),
                        end = Offset(point.x, height),
                        strokeWidth = 2f
                    )
                    drawCircle(color = Color.White, radius = 6.dp.toPx(), center = point)
                    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = point)
                }
            }
        }

        // Tooltip
        touchIndex?.let { index ->
            if (index in values.indices) {
                val value = values[index]
                val date = dates[index]
                val sdf = SimpleDateFormat("dd MMM", Locale("tr", "TR"))
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-16).dp)
                        .background(MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = com.example.butcetakip.data.formatCurrencyFull(value.toDouble(), currency), color = MaterialTheme.colorScheme.surface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = sdf.format(Date(date)), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
