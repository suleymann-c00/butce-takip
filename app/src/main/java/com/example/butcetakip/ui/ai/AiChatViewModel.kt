package com.example.butcetakip.ui.ai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.butcetakip.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" veya "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "ai",
            text = "Merhaba! Ben sizin finansal asistanınızım. Size nasıl yardımcı olabilirim?\n\nÖrneğin: \"Bugün markete 50 TL harcadım\" veya \"Bu ay ne kadar harcadım?\""
        )
    ),
    val isLoading: Boolean = false,
    val hasApiKey: Boolean = false,
    val error: String? = null
)

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private val context get() = getApplication<Application>().applicationContext

    init {
        checkApiKey()
    }

    fun checkApiKey() {
        _uiState.value = _uiState.value.copy(
            hasApiKey = AiAssistantService.hasApiKey(context)
        )
    }

    fun sendMessage(
        userMessage: String,
        transactions: List<Transaction>,
        onAddTransaction: (Transaction) -> Unit,
        onDeleteTransaction: (String) -> Unit
    ) {
        if (userMessage.isBlank() || _uiState.value.isLoading) return

        val userMsg = ChatMessage(role = "user", text = userMessage)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMsg,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val transactionContext = AiAssistantService.buildTransactionContext(transactions)
                val response = AiAssistantService.sendMessage(
                    context = context,
                    userMessage = userMessage,
                    transactionContext = transactionContext
                )

                // AI'ın döndürdüğü eylemleri uygula
                for (action in response.actions) {
                    when (action) {
                        is AiAssistantService.AiAction.Add -> {
                            val category = try {
                                Category.valueOf(action.category)
                            } catch (e: Exception) {
                                Category.OTHER_EXPENSE
                            }

                            val currency = when (action.currency.uppercase()) {
                                "TRY", "TL" -> Currency.TL
                                "USD" -> Currency.USD
                                "EUR" -> Currency.EUR
                                "GOLD" -> Currency.GOLD
                                else -> Currency.TL
                            }

                            val type = when (action.type.uppercase()) {
                                "INCOME" -> TransactionType.INCOME
                                else -> TransactionType.EXPENSE
                            }

                            val date = if (!action.date.isNullOrBlank()) {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                                    sdf.parse(action.date)?.time ?: System.currentTimeMillis()
                                } catch (e: Exception) {
                                    try {
                                        val sdf2 = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        sdf2.parse(action.date)?.time ?: System.currentTimeMillis()
                                    } catch (e2: Exception) {
                                        System.currentTimeMillis()
                                    }
                                }
                            } else {
                                System.currentTimeMillis()
                            }

                            val tx = Transaction(
                                id = UUID.randomUUID().toString(),
                                title = action.title,
                                amount = action.amount,
                                category = category,
                                currency = currency,
                                date = date,
                                type = type
                            )
                            onAddTransaction(tx)
                        }
                        is AiAssistantService.AiAction.Delete -> {
                            onDeleteTransaction(action.transactionId)
                        }
                    }
                }

                val aiMsg = ChatMessage(role = "ai", text = response.text)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMsg,
                    isLoading = false
                )

            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    role = "ai",
                    text = "Bir hata oluştu: ${e.message ?: "Bilinmeyen hata"}"
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMsg,
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
