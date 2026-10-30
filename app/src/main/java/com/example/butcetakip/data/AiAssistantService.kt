package com.example.butcetakip.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Gemini API ile iletişim kuran yapay zeka asistanı servisi.
 * Web'deki useAiAssistant.js'in Kotlin/Android karşılığı.
 */
object AiAssistantService {

    private const val PREFS_NAME = "butce_takip_ai"
    private const val KEY_API_KEY = "gemini_api_key"

    private val MODELS = listOf(
        "gemini-3.6-flash",
        "gemini-3.7-flash",
        "gemini-3.5-flash-lite"
    )

    private fun getSystemPrompt(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()
        val today = dateFormat.format(Date())

        return """Sen yardımsever bir Türk finansal danışmanısın. Bugünün tarihi: $today (YYYY-MM-DD formatında)
Kullanıcı işlem EKLEMEK veya SİLMEK isterse, SADECE markdown içinde JSON çıktısı VER (başka kelime etme). 
Aynı anda birden fazla işlem yapmak için "actions" dizisi (array) kullan:
```json
{
  "actions": [
    {"action": "add", "type": "EXPENSE", "amount": 50, "category": "FOOD", "title": "Market", "currency": "TRY", "date": "2026-09-04T12:00:00.000Z"},
    {"action": "delete", "transactionId": "id-buraya"}
  ]
}
```
- Tarihler bağlamda YYYY-MM-DD olarak gelir (Örn: 2026-09-06 = 6 Eylül 2026). Kullanıcı "6 Eylülü sil" derse, bağlamda 2026-09-06 tarihli işlemleri bulup silmelisin.
- Ekleme işleminde kullanıcı tarih belirtmezse bugünü yaz.
- Kategori anahtarları (JSON oluştururken) KESİNLİKLE ŞUNLAR OLMALIDIR: FOOD (Gıda), TRANSPORT (Ulaşım), RENT (Kira), SHOPPING (Alışveriş), BILLS (Fatura), SUBSCRIPTION (Abonelik), HEALTH (Sağlık), ENTERTAINMENT (Eğlence), EDUCATION (Eğitim), OTHER_EXPENSE (Diğer Gider), SALARY (Maaş), FREELANCE, INVESTMENT_INCOME, POCKET_MONEY.
- Kullanıcı bir işlemi GÜNCELLEMEK/DEĞİŞTİRMEK isterse, ona doğrudan güncelleme yeteneğin olmadığını, ancak eski işlemi silip yenisini aynı anda (actions dizisi ile) ekleyebileceğini söyle ve işlemi bu şekilde gerçekleştir.
- Para Birimi (currency) KESİNLİKLE ŞUNLARDAN BİRİ OLMALIDIR: TRY, USD, EUR, GOLD. (Eğer kullanıcı "2 gram altın" veya "2 gr altın" derse amount: 2, currency: "GOLD" olmalıdır. Dolar için USD, Euro için EUR kullan).
- ÖNEMLİ: Kullanıcıya doğal dilde yanıt verirken (özet, rapor veya sohbet sırasında) ASLA İngilizce kategori anahtarlarını (FOOD, HEALTH, POCKET_MONEY vb.) kullanma. Her zaman saf Türkçe karşılıklarını (Gıda, Sağlık, Harçlık vb.) kullan.

Eğer kullanıcı sadece bir soru soruyorsa JSON üretme, normal dilde sohbet et."""
    }

    fun saveApiKey(context: Context, apiKey: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_API_KEY, apiKey.trim())
            .apply()
    }

    fun getApiKey(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_API_KEY, null)
    }

    fun removeApiKey(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_API_KEY)
            .apply()
    }

    fun hasApiKey(context: Context): Boolean {
        return !getApiKey(context).isNullOrBlank()
    }

    /**
     * İşlem listesini AI'ın anlayabileceği bağlam stringine dönüştürür.
     * Yerel saat diliminde YYYY-MM-DD formatında tarih kullanır.
     */
    fun buildTransactionContext(transactions: List<Transaction>): String {
        if (transactions.isEmpty()) return "Henüz işlem yok."

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()

        val oneYearAgo = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000
        return transactions
            .filter { it.date >= oneYearAgo }
            .sortedByDescending { it.date }
            .take(500)
            .joinToString("\n") { tx ->
                val dateStr = dateFormat.format(Date(tx.date))
                val sign = if (tx.type == TransactionType.INCOME) "+" else "-"
                val currencyCode = when (tx.currency) {
                    Currency.TL -> "TRY"
                    Currency.USD -> "USD"
                    Currency.EUR -> "EUR"
                    Currency.GOLD -> "GOLD"
                }
                "ID:${tx.id} | Tarih:$dateStr | $sign${tx.amount} $currencyCode | Kategori:${tx.category.name} (${tx.title})"
            }
    }

    /**
     * AI asistanına mesaj gönderir ve yanıtı döndürür.
     * Yanıtta JSON varsa ayrıştırır ve action listesini döndürür.
     */
    data class AiResponse(
        val text: String,
        val actions: List<AiAction> = emptyList()
    )

    sealed class AiAction {
        data class Add(
            val type: String,
            val amount: Double,
            val category: String,
            val title: String,
            val currency: String,
            val date: String?
        ) : AiAction()

        data class Delete(val transactionId: String) : AiAction()
    }

    suspend fun sendMessage(
        context: Context,
        userMessage: String,
        transactionContext: String
    ): AiResponse = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
            ?: throw Exception("API Anahtarı bulunamadı. Lütfen ayarlardan ekleyin.")

        val systemPrompt = getSystemPrompt() +
                "\n\nKullanıcının Son 500 İşlemi (Bağlam Bilgisi):\n$transactionContext"

        val requestBody = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userMessage) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
            })
        }

        var lastException: Exception? = null

        for (model in MODELS) {
            try {
                val url = URL(
                    "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 60000

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode

                if (responseCode == 503 || responseCode == 429) {
                    connection.disconnect()
                    continue // Sonraki modele geç
                }

                if (responseCode != 200) {
                    val errorStream = connection.errorStream ?: connection.inputStream
                    val errorText = BufferedReader(InputStreamReader(errorStream, "UTF-8")).readText()
                    connection.disconnect()
                    throw Exception("HTTP $responseCode: $errorText")
                }

                val responseText = BufferedReader(
                    InputStreamReader(connection.inputStream, "UTF-8")
                ).readText()
                connection.disconnect()

                val responseJson = JSONObject(responseText)
                val aiText = responseJson
                    .optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text", "") ?: ""

                // JSON ayrıştırma (actions dizisi)
                val jsonRegex = Regex("```json\\s+([\\s\\S]*?)\\s+```")
                val jsonMatch = jsonRegex.find(aiText)

                if (jsonMatch != null) {
                    try {
                        val jsonStr = jsonMatch.groupValues[1]
                        val parsedData = JSONObject(jsonStr)
                        val actionsArray = parsedData.optJSONArray("actions")
                            ?: JSONArray().apply { put(parsedData) }

                        val actions = mutableListOf<AiAction>()
                        for (i in 0 until actionsArray.length()) {
                            val actionObj = actionsArray.getJSONObject(i)
                            when (actionObj.optString("action")) {
                                "add" -> actions.add(
                                    AiAction.Add(
                                        type = actionObj.optString("type", "EXPENSE"),
                                        amount = actionObj.optDouble("amount", 0.0),
                                        category = actionObj.optString("category", "OTHER_EXPENSE"),
                                        title = actionObj.optString("title", ""),
                                        currency = actionObj.optString("currency", "TRY"),
                                        date = actionObj.optString("date", null)
                                    )
                                )
                                "delete" -> {
                                    val txId = actionObj.optString("transactionId", "")
                                    if (txId.isNotBlank()) {
                                        actions.add(AiAction.Delete(txId))
                                    }
                                }
                            }
                        }

                        if (actions.isNotEmpty()) {
                            val addCount = actions.count { it is AiAction.Add }
                            val deleteCount = actions.count { it is AiAction.Delete }
                            val msgs = mutableListOf<String>()
                            if (addCount > 0) msgs.add("$addCount adet işlem başarıyla eklendi! 🎉")
                            if (deleteCount > 0) msgs.add("$deleteCount adet işlem başarıyla silindi! 🗑️")
                            return@withContext AiResponse(
                                text = msgs.joinToString("\n"),
                                actions = actions
                            )
                        } else {
                            return@withContext AiResponse(
                                text = "İstediğiniz kritere uygun bir işlem bulamadım veya herhangi bir değişiklik yapılamadı. Tarihi veya işlem adını kontrol edip tekrar deneyebilirsiniz. 🕵️‍♂️"
                            )
                        }
                    } catch (e: Exception) {
                        // JSON parse başarısız, düz metni döndür
                    }
                }

                // JSON yoksa düz metin yanıt
                return@withContext AiResponse(text = aiText)

            } catch (e: Exception) {
                if (e.message?.contains("503") == true || e.message?.contains("429") == true) {
                    lastException = e
                    continue
                }
                throw e
            }
        }

        throw lastException ?: Exception("Tüm modeller şu an yoğun. Lütfen tekrar deneyin.")
    }
}
