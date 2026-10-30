package com.example.butcetakip.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.UUID
import kotlin.random.Random
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// --- Data Models ---

enum class TransactionType { INCOME, EXPENSE }

enum class Category(val label: String) {
    SALARY("Maaş"),
    FREELANCE("Serbest Çalışma"),
    INVESTMENT_INCOME("Yatırım Getirisi"),
    SCHOLARSHIP("Burs"),
    POCKET_MONEY("Harçlık"),
    OTHER_INCOME("Diğer Gelir"),
    
    FOOD("Yiyecek & İçecek"),
    TRANSPORT("Ulaşım"),
    RENT("Kira"),
    SHOPPING("Alışveriş"),
    BILLS("Faturalar"),
    SUBSCRIPTION("Abonelik"),
    INVESTMENT_EXPENSE("Yatırım"),
    HEALTH("Sağlık"),
    ENTERTAINMENT("Eğlence"),
    EDUCATION("Eğitim"),
    OTHER_EXPENSE("Diğer Gider"),
    CUSTOM("Özel Kategori")
}

data class CustomCategoryDef(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val iconName: String,
    val type: TransactionType
)

enum class Currency(val code: String, val symbol: String) {
    TL("TRY", "₺"),
    USD("USD", "$"),
    EUR("EUR", "€"),
    GOLD("GOLD", "gr")
}

fun formatCurrencyFull(amount: Double, currency: Currency): String {
    val formatted = String.format(java.util.Locale.US, "%,.2f", amount)
    return if (currency == Currency.GOLD) {
        "$formatted ${currency.symbol}"
    } else {
        "${currency.symbol}$formatted"
    }
}

fun Category.transactionType(): TransactionType =
    if (this in listOf(Category.SALARY, Category.FREELANCE, Category.INVESTMENT_INCOME, Category.SCHOLARSHIP, Category.POCKET_MONEY, Category.OTHER_INCOME))
        TransactionType.INCOME else TransactionType.EXPENSE

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: Category,
    val customCategoryName: String? = null,
    val customCategoryIcon: String? = null,
    val currency: Currency = Currency.TL,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isRecurring: Boolean = false,
    val type: TransactionType = category.transactionType()
)

// --- Repository ---

interface TransactionRepository {
    val transactions: Flow<List<Transaction>>
    val customCategories: Flow<List<CustomCategoryDef>>
    fun addTransaction(transaction: Transaction)
    fun deleteTransaction(id: String)
    fun addCustomCategory(cat: CustomCategoryDef)
    fun processRecurringTransactions()
}

object DataRepository : TransactionRepository {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    override val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _customCategories = MutableStateFlow<List<CustomCategoryDef>>(emptyList())
    override val customCategories: StateFlow<List<CustomCategoryDef>> = _customCategories.asStateFlow()

    private var prefs: SharedPreferences? = null
    private const val PREFS_NAME = "butce_takip_data"
    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_CUSTOM_CATEGORIES = "custom_categories"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadFromDisk()
        processRecurringTransactions()
    }

    // --- JSON Serialization ---

    private fun transactionToJson(tx: Transaction): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("title", tx.title)
            put("amount", tx.amount)
            put("category", tx.category.name)
            put("customCategoryName", tx.customCategoryName ?: JSONObject.NULL)
            put("customCategoryIcon", tx.customCategoryIcon ?: JSONObject.NULL)
            put("currency", tx.currency.name)
            put("date", tx.date)
            put("note", tx.note)
            put("isRecurring", tx.isRecurring)
            put("type", tx.type.name)
        }
    }

    private fun jsonToTransaction(json: JSONObject): Transaction? {
        return try {
            Transaction(
                id = json.getString("id"),
                title = json.getString("title"),
                amount = json.getDouble("amount"),
                category = Category.valueOf(json.getString("category")),
                customCategoryName = if (json.isNull("customCategoryName")) null else json.getString("customCategoryName"),
                customCategoryIcon = if (json.isNull("customCategoryIcon")) null else json.getString("customCategoryIcon"),
                currency = Currency.valueOf(json.getString("currency")),
                date = json.getLong("date"),
                note = json.optString("note", ""),
                isRecurring = json.optBoolean("isRecurring", false),
                type = TransactionType.valueOf(json.getString("type"))
            )
        } catch (e: Exception) {
            null // Bozuk veriyi atla
        }
    }

    private fun customCategoryToJson(cat: CustomCategoryDef): JSONObject {
        return JSONObject().apply {
            put("id", cat.id)
            put("name", cat.name)
            put("iconName", cat.iconName)
            put("type", cat.type.name)
        }
    }

    private fun jsonToCustomCategory(json: JSONObject): CustomCategoryDef? {
        return try {
            CustomCategoryDef(
                id = json.getString("id"),
                name = json.getString("name"),
                iconName = json.getString("iconName"),
                type = TransactionType.valueOf(json.getString("type"))
            )
        } catch (e: Exception) {
            null
        }
    }

    // --- Disk Operations ---

    private fun loadFromDisk() {
        val sp = prefs ?: return

        // İşlemleri yükle
        val txJson = sp.getString(KEY_TRANSACTIONS, null)
        if (txJson != null) {
            try {
                val arr = JSONArray(txJson)
                val list = mutableListOf<Transaction>()
                for (i in 0 until arr.length()) {
                    jsonToTransaction(arr.getJSONObject(i))?.let { list.add(it) }
                }
                _transactions.value = list
            } catch (_: Exception) {
                _transactions.value = emptyList()
            }
        }

        // Özel kategorileri yükle
        val catJson = sp.getString(KEY_CUSTOM_CATEGORIES, null)
        if (catJson != null) {
            try {
                val arr = JSONArray(catJson)
                val list = mutableListOf<CustomCategoryDef>()
                for (i in 0 until arr.length()) {
                    jsonToCustomCategory(arr.getJSONObject(i))?.let { list.add(it) }
                }
                _customCategories.value = list
            } catch (_: Exception) {
                _customCategories.value = emptyList()
            }
        }
    }

    private fun saveToDisk() {
        val sp = prefs ?: return

        // İşlemleri kaydet
        val txArr = JSONArray()
        _transactions.value.forEach { txArr.put(transactionToJson(it)) }

        // Özel kategorileri kaydet
        val catArr = JSONArray()
        _customCategories.value.forEach { catArr.put(customCategoryToJson(it)) }

        val txStr = txArr.toString()
        val catStr = catArr.toString()

        sp.edit()
            .putString(KEY_TRANSACTIONS, txStr)
            .putString(KEY_CUSTOM_CATEGORIES, catStr)
            .apply()
            
        // Buluta da kaydet (eğer oturum açıksa)
        saveToFirestore(txStr, catStr)
    }

    // --- Public API ---

    override fun addTransaction(transaction: Transaction) {
        val currentList = _transactions.value.toMutableList()
        currentList.add(0, transaction)
        _transactions.value = currentList
        saveToDisk()
    }

    override fun addCustomCategory(cat: CustomCategoryDef) {
        val currentList = _customCategories.value.toMutableList()
        currentList.add(cat)
        _customCategories.value = currentList
        saveToDisk()
    }

    override fun processRecurringTransactions() {
        val currentTxs = _transactions.value.toMutableList()
        val recurringGroups = currentTxs.filter { it.isRecurring }.groupBy { "${it.title}_${it.category.name}_${it.currency.name}" }
        val now = System.currentTimeMillis()
        var hasNew = false

        recurringGroups.forEach { (_, groupTxs) ->
            var latestTx = groupTxs.maxByOrNull { it.date } ?: return@forEach
            
            while (true) {
                val cal = java.util.Calendar.getInstance()
                cal.timeInMillis = latestTx.date
                cal.add(java.util.Calendar.MONTH, 1)
                
                if (cal.timeInMillis <= now) {
                    val newTx = latestTx.copy(
                        id = java.util.UUID.randomUUID().toString(),
                        date = cal.timeInMillis
                    )
                    currentTxs.add(newTx)
                    latestTx = newTx
                    hasNew = true
                } else {
                    break
                }
            }
        }
        
        if (hasNew) {
            _transactions.value = currentTxs.sortedByDescending { it.date }
            saveToDisk()
        }
    }

    override fun deleteTransaction(id: String) {
        _transactions.value = _transactions.value.filter { it.id != id }
        saveToDisk()
    }
    
    fun clearAllTransactions() {
        _transactions.value = emptyList()
        _customCategories.value = emptyList()
        saveToDisk()
    }

    // --- Cloud Sync (Firestore) ---

    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun syncFromFirestore() {
        val uid = getUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        listenerRegistration?.remove()
        listenerRegistration = db.collection("users").document(uid).addSnapshotListener { document, e ->
            if (e != null) {
                return@addSnapshotListener
            }

            // Yerel olarak henüz sunucuya gitmemiş değişiklikleri (kendi yaptığımız) tekrar parse etmemek için
            if (document != null && document.exists() && !document.metadata.hasPendingWrites()) {
                try {
                    val data = document.data
                    
                    val txList = data?.get("transactions") as? String
                    if (txList != null) {
                        val arr = JSONArray(txList)
                        val list = mutableListOf<Transaction>()
                        for (i in 0 until arr.length()) {
                            jsonToTransaction(arr.getJSONObject(i))?.let { list.add(it) }
                        }
                        _transactions.value = list
                    }

                    val catList = data?.get("custom_categories") as? String
                    if (catList != null) {
                        val arr = JSONArray(catList)
                        val list = mutableListOf<CustomCategoryDef>()
                        for (i in 0 until arr.length()) {
                            jsonToCustomCategory(arr.getJSONObject(i))?.let { list.add(it) }
                        }
                        _customCategories.value = list
                    }
                    
                    // Yerel diski de güncelle
                    saveToDiskLocallyOnly()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }
    
    fun stopSync() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    private fun saveToDiskLocallyOnly() {
        val sp = prefs ?: return
        val txArr = JSONArray()
        _transactions.value.forEach { txArr.put(transactionToJson(it)) }
        val catArr = JSONArray()
        _customCategories.value.forEach { catArr.put(customCategoryToJson(it)) }

        sp.edit()
            .putString(KEY_TRANSACTIONS, txArr.toString())
            .putString(KEY_CUSTOM_CATEGORIES, catArr.toString())
            .apply()
    }

    private fun saveToFirestore(txJson: String, catJson: String) {
        val uid = getUserId() ?: return
        val db = FirebaseFirestore.getInstance()
        
        val data = hashMapOf(
            "transactions" to txJson,
            "custom_categories" to catJson,
            "last_updated" to System.currentTimeMillis()
        )
        
        // Firestore'a arka planda yaz (UI'ı bloklama)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("users").document(uid).set(data).await()
            } catch (e: Exception) {
                // Çevrimdışıysak Firebase SDK'sı zaten otomatik cache'e alıp internet gelince yollayacak.
            }
        }
    }
}
