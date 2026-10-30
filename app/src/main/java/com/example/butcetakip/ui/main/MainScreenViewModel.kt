package com.example.butcetakip.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.butcetakip.data.Category
import com.example.butcetakip.data.Currency
import com.example.butcetakip.data.Transaction
import com.example.butcetakip.data.TransactionRepository
import com.example.butcetakip.data.TransactionType
import com.example.butcetakip.data.DataRepository
import com.example.butcetakip.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class DashboardDateFilter(val label: String) {
    DAILY("Günlük"),
    WEEKLY("Haftalık"),
    MONTHLY("Aylık"),
    ALL("Tümü")
}

data class FinanceUiState(
    val transactions: List<Transaction> = emptyList(),
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val delta: Double = 0.0, // Kazanç/Kayıp değişimi
    val categoryBreakdown: Map<Category, Double> = emptyMap(),
    val selectedCurrency: Currency = Currency.TL,
    val dashboardFilter: DashboardDateFilter = DashboardDateFilter.MONTHLY,
    val appTheme: AppTheme = AppTheme.DARK,
    val startDate: Long? = null,
    val endDate: Long? = null,
    // All balances for display
    val tlBalance: Double = 0.0,
    val usdBalance: Double = 0.0,
    val eurBalance: Double = 0.0,
    val goldBalance: Double = 0.0,
    val customCategories: List<com.example.butcetakip.data.CustomCategoryDef> = emptyList(),
    val incomeBreakdown: Map<Category, Double> = emptyMap(),
    val statsQuickFilter: String = "MONTH", // WEEK, MONTH, YEAR, CUSTOM, ALL
    val allTransactions: List<Transaction> = emptyList() // NEW FIELD FOR ALL TRANSACTIONS
)

class FinanceViewModel(private val repository: TransactionRepository = DataRepository) : ViewModel() {

    val selectedCurrency = MutableStateFlow(Currency.TL)
    val dashboardFilter = MutableStateFlow(DashboardDateFilter.MONTHLY)
    val statsQuickFilter = MutableStateFlow("MONTH")
    val appTheme = MutableStateFlow(AppTheme.DARK)
    val startDate = MutableStateFlow<Long?>(null)
    val endDate = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<FinanceUiState> = combine(
        repository.transactions,
        selectedCurrency,
        dashboardFilter,
        appTheme,
        startDate,
        endDate,
        repository.customCategories,
        statsQuickFilter
    ) { params ->
        val list = params[0] as List<Transaction>
        val currency = params[1] as Currency
        val filter = params[2] as DashboardDateFilter
        val theme = params[3] as AppTheme
        val start = params[4] as Long?
        val end = params[5] as Long?
        val customCats = params[6] as List<com.example.butcetakip.data.CustomCategoryDef>
        val currentStatsFilter = params[7] as String
        
        
        // Cüzdan genel toplamları (Tarih bağımsız)
        val tlBalance = list.filter { it.currency == Currency.TL }.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        val usdBalance = list.filter { it.currency == Currency.USD }.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        val eurBalance = list.filter { it.currency == Currency.EUR }.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
        val goldBalance = list.filter { it.currency == Currency.GOLD }.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }

        // Seçilen kura ve İstatistik sayfasına göre filtreleme
        val statsList = list.filter { tx ->
            val matchCurrency = tx.currency == currency
            val matchDate = if (start != null && end != null) tx.date in start..end else true
            matchCurrency && matchDate
        }

        // Ana Sayfa İçin Filtre (Dashboard)
        val nowCal = java.util.Calendar.getInstance()
        val filterStartCal = java.util.Calendar.getInstance().apply {
            when(filter) {
                DashboardDateFilter.DAILY -> add(java.util.Calendar.DAY_OF_YEAR, -1)
                DashboardDateFilter.WEEKLY -> add(java.util.Calendar.DAY_OF_YEAR, -7)
                DashboardDateFilter.MONTHLY -> add(java.util.Calendar.MONTH, -1)
                DashboardDateFilter.ALL -> timeInMillis = 0L
            }
        }
        val dashboardList = list.filter { tx ->
            tx.currency == currency && tx.date >= filterStartCal.timeInMillis
        }

        val income = dashboardList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = dashboardList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val delta = income - expense // Profit/Loss

        val breakdown = dashboardList
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        val incBreakdown = dashboardList
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }

        FinanceUiState(
            transactions = statsList, // Statistics ekranı start-end tarihlerini kullanır
            totalBalance = list.filter { it.currency == currency }.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount },
            totalIncome = income,
            totalExpense = expense,
            delta = delta,
            categoryBreakdown = breakdown,
            selectedCurrency = currency,
            dashboardFilter = filter,
            appTheme = theme,
            startDate = start,
            endDate = end,
            tlBalance = tlBalance,
            usdBalance = usdBalance,
            eurBalance = eurBalance,
            goldBalance = goldBalance,
            customCategories = customCats,
            incomeBreakdown = incBreakdown,
            statsQuickFilter = currentStatsFilter,
            allTransactions = list.filter { it.currency == currency }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinanceUiState())

    fun addTransaction(transaction: Transaction) {
        repository.addTransaction(transaction)
    }

    fun addCustomCategory(cat: com.example.butcetakip.data.CustomCategoryDef) {
        repository.addCustomCategory(cat)
    }

    fun deleteTransaction(id: String) {
        repository.deleteTransaction(id)
    }

    fun resetData() {
        if (repository is DataRepository) {
            repository.clearAllTransactions()
        }
    }

    fun setCurrency(currency: Currency) {
        selectedCurrency.value = currency
    }

    fun setDashboardFilter(filter: DashboardDateFilter) {
        dashboardFilter.value = filter
    }

    fun setStatsQuickFilter(filter: String) {
        statsQuickFilter.value = filter
    }

    fun setTheme(theme: AppTheme) {
        appTheme.value = theme
    }
    
    fun setDateRange(start: Long?, end: Long?) {
        startDate.value = start
        endDate.value = end
    }
}
