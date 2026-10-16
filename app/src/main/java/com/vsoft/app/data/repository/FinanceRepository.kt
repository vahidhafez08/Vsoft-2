package com.vsoft.app.data.repository

import android.content.SharedPreferences
import com.vsoft.app.data.db.AccountDao
import com.vsoft.app.data.db.TransactionDao
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.DAY_MS
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> = transactionDao.observeAll()
    fun observeAccounts(): Flow<List<AccountEntity>> = accountDao.observeAll()

    suspend fun addTransaction(title: String, amount: Long, isIncome: Boolean, category: String) {
        transactionDao.upsert(
            TransactionEntity(
                title = title,
                amount = amount,
                isIncome = isIncome,
                category = category,
                dateMillis = System.currentTimeMillis()
            )
        )
    }

    /** Insert or update (also used to restore a deleted item with its old id). */
    suspend fun saveTransaction(tx: TransactionEntity) {
        transactionDao.upsert(tx)
    }

    suspend fun deleteTransaction(tx: TransactionEntity) {
        transactionDao.delete(tx)
    }

    suspend fun saveAccount(account: AccountEntity) {
        accountDao.upsert(account)
    }

    suspend fun deleteAccount(account: AccountEntity) {
        accountDao.delete(account)
    }

    suspend fun transfer(from: AccountEntity, to: AccountEntity, amount: Long) {
        accountDao.upsert(from.copy(balance = from.balance - amount))
        accountDao.upsert(to.copy(balance = to.balance + amount))
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
