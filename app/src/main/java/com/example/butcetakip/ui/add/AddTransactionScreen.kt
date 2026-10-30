package com.example.butcetakip.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.butcetakip.data.Category
import com.example.butcetakip.data.Currency
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.data.TransactionType
import com.example.butcetakip.data.transactionType
import com.example.butcetakip.theme.ExpenseRed
import com.example.butcetakip.theme.IncomeGreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.butcetakip.ui.main.FinanceViewModel
import com.example.butcetakip.ui.utils.getCategoryIcon
import com.example.butcetakip.ui.utils.getCustomIconByName
import com.example.butcetakip.data.CustomCategoryDef
import androidx.compose.material.icons.filled.Add

sealed class CategoryItem {
    data class BuiltIn(val category: Category) : CategoryItem()
    data class Custom(val def: CustomCategoryDef) : CategoryItem()
    object AddNew : CategoryItem()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    
    // Tab State
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Filter categories based on selected tab
    val availableCategories = Category.entries.filter { it.transactionType() == selectedType && it != Category.CUSTOM }
    val customCategories = state.customCategories.filter { it.type == selectedType }
    
    val categoryItems = remember(selectedType, customCategories) {
        val items = mutableListOf<CategoryItem>()
        availableCategories.forEach { items.add(CategoryItem.BuiltIn(it)) }
        customCategories.forEach { items.add(CategoryItem.Custom(it)) }
        items.add(CategoryItem.AddNew)
        items
    }
    
    var selectedCategoryItem by remember { mutableStateOf<CategoryItem>(CategoryItem.BuiltIn(availableCategories.first())) }
    
    // Update selectedCategory when tab changes if current is invalid
    LaunchedEffect(selectedType) {
        selectedCategoryItem = CategoryItem.BuiltIn(availableCategories.first())
    }

    var selectedCurrency by remember { mutableStateOf(Currency.TL) }
    var isRecurring by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni İşlem", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            amountError = "Lütfen geçerli bir rakam giriniz"
                            return@Button
                        }
                        val category = when(val item = selectedCategoryItem) {
                            is CategoryItem.BuiltIn -> item.category
                            is CategoryItem.Custom -> Category.CUSTOM
                            CategoryItem.AddNew -> availableCategories.first() // Fallback
                        }
                        
                        val customName = (selectedCategoryItem as? CategoryItem.Custom)?.def?.name
                        val customIcon = (selectedCategoryItem as? CategoryItem.Custom)?.def?.iconName
                        
                        if (title.isBlank()) {
                            title = when(val item = selectedCategoryItem) {
                                is CategoryItem.BuiltIn -> item.category.label
                                is CategoryItem.Custom -> item.def.name
                                CategoryItem.AddNew -> category.label
                            }
                        }
                        viewModel.addTransaction(
                            Transaction(
                                title = title,
                                amount = amountValue,
                                category = category,
                                customCategoryName = customName,
                                customCategoryIcon = customIcon,
                                currency = selectedCurrency,
                                isRecurring = isRecurring
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedType == TransactionType.INCOME) IncomeGreen else ExpenseRed
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kaydet", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Modern Segmented Control for Income/Expense
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedType == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                        .clickable { selectedType = TransactionType.INCOME },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gelir", 
                        fontWeight = FontWeight.Bold, 
                        color = if (selectedType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(22.dp))
                        .background(if (selectedType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                        .clickable { selectedType = TransactionType.EXPENSE },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Gider", 
                        fontWeight = FontWeight.Bold, 
                        color = if (selectedType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Başlık
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("İşlem Başlığı (İsteğe Bağlı)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Para Birimi ve Tutar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Currency Selector
                Column(modifier = Modifier.weight(1f)) {
                    Text("Para Birimi", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Currency.entries.forEach { curr ->
                            val isSelected = selectedCurrency == curr
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedCurrency = curr },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = curr.symbol,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Tutar
                Column(modifier = Modifier.weight(2f)) {
                    Text("Tutar", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            amount = it
                            amountError = null
                        },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        isError = amountError != null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            if (amountError != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = amountError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            // Düzenli / Düzensiz Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Düzenli İşlem", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "Bu işlem her ay takvime eşit dağıtılır (Örn: Maaş, Kira)", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Kategori
            Text("Kategori", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryItems.forEach { item ->
                    val isSelected = selectedCategoryItem == item
                    val label = when(item) {
                        is CategoryItem.BuiltIn -> item.category.label
                        is CategoryItem.Custom -> item.def.name
                        CategoryItem.AddNew -> "Yeni Kategori"
                    }
                    val icon = when(item) {
                        is CategoryItem.BuiltIn -> getCategoryIcon(item.category)
                        is CategoryItem.Custom -> getCustomIconByName(item.def.iconName)
                        CategoryItem.AddNew -> Icons.Default.Add
                    }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { 
                            if (item is CategoryItem.AddNew) {
                                showNewCategoryDialog = true
                            } else {
                                selectedCategoryItem = item 
                            }
                        },
                        leadingIcon = {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showNewCategoryDialog) {
        var newCatName by remember { mutableStateOf("") }
        val iconList = listOf("Star", "Favorite", "ShoppingCart", "Home", "Build", "CardTravel", "Pets", "Flight")
        var selectedIconName by remember { mutableStateOf(iconList.first()) }

        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            title = { Text("Yeni Kategori Ekle", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newCatName,
                        onValueChange = { newCatName = it },
                        label = { Text("Kategori İsmi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("İkon Seç", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        iconList.forEach { iconName ->
                            val isIconSelected = selectedIconName == iconName
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isIconSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedIconName = iconName },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCustomIconByName(iconName),
                                    contentDescription = null,
                                    tint = if (isIconSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCatName.isNotBlank()) {
                            val newCat = CustomCategoryDef(
                                name = newCatName,
                                iconName = selectedIconName,
                                type = selectedType
                            )
                            viewModel.addCustomCategory(newCat)
                            selectedCategoryItem = CategoryItem.Custom(newCat)
                            showNewCategoryDialog = false
                        }
                    }
                ) {
                    Text("Ekle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }
}
