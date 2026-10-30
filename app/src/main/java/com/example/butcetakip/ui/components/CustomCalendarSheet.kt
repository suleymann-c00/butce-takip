package com.example.butcetakip.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.butcetakip.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCalendarSheet(
    onDismiss: () -> Unit,
    onDateRangeSelected: (Long, Long) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tarih Aralığı Seç",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Month Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    val newCal = currentMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, -1)
                    currentMonth = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Önceki Ay", tint = MaterialTheme.colorScheme.onSurface)
                }

                val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("tr", "TR"))
                Text(
                    text = monthFormat.format(currentMonth.time).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { 
                    val newCal = currentMonth.clone() as Calendar
                    newCal.add(Calendar.MONTH, 1)
                    currentMonth = newCal
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Sonraki Ay", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of week
            val daysOfWeek = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid
            val daysInMonth = getDaysInMonth(currentMonth)
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(260.dp),
                userScrollEnabled = false
            ) {
                items(daysInMonth) { dayCal ->
                    if (dayCal == null) {
                        Box(modifier = Modifier.aspectRatio(1f))
                    } else {
                        val time = dayCal.timeInMillis
                        
                        val isStart = startDate != null && isSameDay(time, startDate!!)
                        val isEnd = endDate != null && isSameDay(time, endDate!!)
                        val isBetween = startDate != null && endDate != null && time > startDate!! && time < endDate!!
                        
                        val bgColor = when {
                            isStart || isEnd -> MaterialTheme.colorScheme.primary
                            isBetween -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else -> Color.Transparent
                        }
                        
                        val textColor = when {
                            isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.onBackground
                        }

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .clickable {
                                    if (startDate == null) {
                                        startDate = time
                                    } else if (endDate == null) {
                                        if (time < startDate!!) {
                                            endDate = startDate
                                            startDate = time
                                        } else {
                                            endDate = time
                                        }
                                    } else {
                                        startDate = time
                                        endDate = null
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayCal.get(Calendar.DAY_OF_MONTH).toString(),
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = if (isStart || isEnd) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (startDate != null) {
                        val finalStart = startDate!!
                        val finalEnd = endDate ?: finalStart
                        
                        // Set start to beginning of day
                        val sCal = Calendar.getInstance().apply { timeInMillis = finalStart }
                        sCal.set(Calendar.HOUR_OF_DAY, 0)
                        sCal.set(Calendar.MINUTE, 0)
                        
                        // Set end to end of day
                        val eCal = Calendar.getInstance().apply { timeInMillis = finalEnd }
                        eCal.set(Calendar.HOUR_OF_DAY, 23)
                        eCal.set(Calendar.MINUTE, 59)
                        
                        onDateRangeSelected(sCal.timeInMillis, eCal.timeInMillis)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = startDate != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Uygula", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun getDaysInMonth(month: Calendar): List<Calendar?> {
    val result = mutableListOf<Calendar?>()
    val cal = month.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    
    // Day of week (1 = Sunday, 7 = Saturday). Convert to Monday = 1
    var firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    if (firstDayOfWeek == 0) firstDayOfWeek = 7 // Sunday -> 7
    
    // Add empty slots for first row
    for (i in 1 until firstDayOfWeek) {
        result.add(null)
    }
    
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDays) {
        val dayCal = cal.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, i)
        result.add(dayCal)
    }
    
    return result
}

private fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
