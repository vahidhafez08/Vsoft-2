package com.vsoft.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import com.vsoft.app.data.db.AccountDao
import com.vsoft.app.data.db.TransactionDao
import com.vsoft.app.data.db.TransferDao
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.model.TransferEntity
import com.vsoft.app.util.DAY_MS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val transferDao: TransferDao,
    private val db: com.vsoft.app.data.db.AppDatabase,
    @ApplicationContext private val context: Context
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()

    fun observeTransfers(): Flow<List<TransferEntity>> = transferDao.observeAll()

    fun observeAccountBalances(): Flow<Map<Long, Long>> =
        combine(observeAccounts(), observeTransactions(), observeTransfers()) { accounts, transactions, transfers ->
            computeBalances(accounts, transactions, transfers)
        }

    suspend fun addTransaction(title: String, amount: Long, isIncome: Boolean, category: String, accountId: Long? = null) {
        transactionDao.upsert(
            TransactionEntity(
                title = title,
                amount = amount,
                isIncome = isIncome,
                category = category,
                dateMillis = System.currentTimeMillis(),
                accountId = accountId
            )
        )
        rememberLastAccount(accountId)
    }

    /** Insert or update (also used to restore a deleted item with its old id). */
    suspend fun saveTransaction(tx: TransactionEntity) {
        transactionDao.upsert(tx)
        rememberLastAccount(tx.accountId)
    }

    private fun rememberLastAccount(accountId: Long?) {
        context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE).edit().putLong("last_account_id", accountId ?: -1L).apply()
    }

    suspend fun deleteTransaction(tx: TransactionEntity) {
        transactionDao.delete(tx)
    }

    suspend fun saveAccount(account: AccountEntity) {
        accountDao.upsert(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = deleteAccountDetachingTransactions(account)

    suspend fun deleteAccountDetachingTransactions(account: AccountEntity) {
        db.withTransaction {
            transactionDao.detachAccount(account.id)
            transferDao.deleteForAccount(account.id)
            accountDao.delete(account)
        }
    }

    suspend fun addTransfer(from: AccountEntity, to: AccountEntity, amount: Long, dateMillis: Long, note: String = "") {
        require(from.id != to.id) { "حساب مبدأ و مقصد باید متفاوت باشن" }
        require(amount > 0L) { "مبلغ رو وارد کن" }
        db.withTransaction {
            val currentAccounts = accountDao.getAllOnce()
            require(currentAccounts.any { it.id == from.id } && currentAccounts.any { it.id == to.id }) {
                "حساب انتخاب‌شده در دسترس نیست"
            }
            val balances = computeBalances(currentAccounts, transactionDao.getAllOnce(), transferDao.getAllOnce())
            try {
                Math.subtractExact(balances[from.id] ?: from.balance, amount)
                Math.addExact(balances[to.id] ?: to.balance, amount)
            } catch (overflow: ArithmeticException) {
                throw IllegalArgumentException("مبلغ انتقال خیلی بزرگه")
            }
            transferDao.insert(
                TransferEntity(fromAccountId = from.id, toAccountId = to.id, amount = amount, dateMillis = dateMillis, note = note.trim())
            )
        }
    }

    suspend fun deleteTransfer(transfer: TransferEntity) {
        transferDao.delete(transfer)
    }

    /** Inserts demo data once (first launch only). */
    suspend fun seedDemoDataIfNeeded(prefs: SharedPreferences) {
        if (prefs.getBoolean(KEY_SEEDED, false)) return

        accountDao.upsert(AccountEntity(name = "بانک ملت", last4 = "5784", balance = 8_570_000, colorKey = "blue"))
        accountDao.upsert(AccountEntity(name = "بانک سامان", last4 = "7219", balance = 3_200_000, colorKey = "dark"))
        accountDao.upsert(AccountEntity(name = "کارت هدیه", last4 = "1477", balance = 1_500_000, colorKey = "green"))
        accountDao.upsert(AccountEntity(name = "نقدی", last4 = "0000", balance = 1_060_000, colorKey = "purple"))

        val now = System.currentTimeMillis()
        fun tx(title: String, amount: Long, income: Boolean, category: String, daysAgo: Int) =
            TransactionEntity(
                title = title,
                amount = amount,
                isIncome = income,
                category = category,
                dateMillis = now - daysAgo * DAY_MS
            )

        val demo = listOf(
            tx("دریافت حقوق", 20_000_000, true, "حقوق", 2),
            tx("فروش محصول", 5_500_000, true, "فروش", 9),
            tx("خرید از فروشگاه", 1_250_000, false, "خوراک و آشامیدنی", 0),
            tx("سوپرمارکت", 950_000, false, "خوراک و آشامیدنی", 2),
            tx("بنزین", 1_000_000, false, "حمل و نقل", 1),
            tx("اسنپ", 350_000, false, "حمل و نقل", 5),
            tx("اجاره منزل", 12_000_000, false, "مسکن", 3),
            tx("سینما و رستوران", 1_800_000, false, "تفریح و سرگرمی", 4),
            tx("خرید لباس", 2_100_000, false, "پوشاک", 6),
            tx("قبض برق", 580_000, false, "سایر", 7),
            tx("خرید اینترنت", 680_000, false, "سایر", 8),
            tx("دریافت حقوق", 18_000_000, true, "حقوق", 32),
            tx("اجاره منزل", 12_000_000, false, "مسکن", 33),
            tx("خرید از فروشگاه", 1_400_000, false, "خوراک و آشامیدنی", 35),
            tx("بنزین", 900_000, false, "حمل و نقل", 40),
            tx("سینما و رستوران", 2_200_000, false, "تفریح و سرگرمی", 45)
        )
        demo.forEach { transactionDao.upsert(it) }

        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private companion object {
        const val KEY_SEEDED = "demo_seeded"
    }
}


fun computeBalances(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    transfers: List<TransferEntity> = emptyList()
): Map<Long, Long> {
    val totals = accounts.associate { it.id to it.balance }.toMutableMap()
    transactions.forEach { tx ->
        val id = tx.accountId
        if (id != null && totals.containsKey(id)) {
            val delta = if (tx.isIncome) tx.amount else -tx.amount
            totals[id] = (totals[id] ?: 0L) + delta
        }
    }
    transfers.forEach { transfer ->
        if (totals.containsKey(transfer.fromAccountId)) {
            totals[transfer.fromAccountId] = (totals[transfer.fromAccountId] ?: 0L) - transfer.amount
        }
        if (totals.containsKey(transfer.toAccountId)) {
            totals[transfer.toAccountId] = (totals[transfer.toAccountId] ?: 0L) + transfer.amount
        }
    }
    return totals
}
