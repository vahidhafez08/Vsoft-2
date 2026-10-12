package com.vahidnia.finance

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppVm(app: Application) : AndroidViewModel(app) {
    private val ctx: Context = app
    private val file = File(app.filesDir, "vsoft_data.json")
    private val prefs = app.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE)

    var cards by mutableStateOf(listOf<BankCard>())
        private set
    var txs by mutableStateOf(listOf<Tx>())
        private set
    var places by mutableStateOf(listOf<Place>())
        private set

    var theme by mutableIntStateOf(prefs.getInt("theme", 0))
        private set
    var font by mutableIntStateOf(prefs.getInt("font", 0))
        private set
    var glass by mutableStateOf(prefs.getBoolean("glass", true))
        private set
    var autoBackup by mutableStateOf(prefs.getBoolean("auto", true))
        private set
    var keepCount by mutableIntStateOf(prefs.getInt("keep", 5))
        private set
    var backups by mutableStateOf(listOf<File>())
        private set

    private fun id() = UUID.randomUUID().toString()

    // ---------- تنظیمات ----------
    fun updateTheme(v: Int) { theme = v; prefs.edit().putInt("theme", v).apply() }
    fun updateFont(v: Int) { font = v; prefs.edit().putInt("font", v).apply() }
    fun updateGlass(v: Boolean) { glass = v; prefs.edit().putBoolean("glass", v).apply() }
    fun updateAuto(v: Boolean) { autoBackup = v; prefs.edit().putBoolean("auto", v).apply() }
    fun updateKeep(v: Int) {
        keepCount = v
        prefs.edit().putInt("keep", v).apply()
        prune()
        refreshBackups()
    }

    // ---------- کارت‌ها ----------
    fun addCard(bank: String, last4: String, initial: Long) {
        cards = cards + BankCard(id(), bank, last4, initial)
        save()
    }

    fun deleteCard(cardId: String) {
        cards = cards.filter { it.id != cardId }
        txs = txs.filter { it.cardId != cardId }
        save()
    }

    // ---------- تراکنش‌ها ----------
    fun addTx(income: Boolean, cardId: String, amount: Long, category: String, note: String) {
        txs = txs + Tx(id(), cardId, income, amount, category, note, System.currentTimeMillis())
        save()
    }

    fun deleteTx(txId: String) {
        txs = txs.filter { it.id != txId }
        save()
    }

    // ---------- کار و پروژه ----------
    fun addPlace(name: String) {
        places = places + Place(id(), name, emptyList())
        save()
    }

    fun deletePlace(placeId: String) {
        places = places.filter { it.id != placeId }
        save()
    }

    fun saveItem(placeId: String, item: WorkItem) {
        places = places.map { p ->
            if (p.id != placeId) p
            else {
                val exists = p.items.any { it.id == item.id }
                p.copy(items = if (exists) p.items.map { if (it.id == item.id) item else it } else p.items + item)
            }
        }
        save()
    }

    fun newItemId(): String = id()

    fun deleteItem(placeId: String, itemId: String) {
        places = places.map { p -> if (p.id == placeId) p.copy(items = p.items.filter { it.id != itemId }) else p }
        save()
    }

    fun loadSample() {
        val day = 86400000L
        val now = System.currentTimeMillis()
        val a = BankCard(id(), "بانک ملی", "1234", 8_000_000L)
        val b = BankCard(id(), "بلو بانک", "5678", 3_000_000L)
        val c = BankCard(id(), "بانک مسکن", "9012", 2_000_000L)
        cards = cards + listOf(a, b, c)
        txs = txs + listOf(
            Tx(id(), a.id, true, 18_000_000L, "حقوق", "واریز حقوق", now - 3 * day),
            Tx(id(), a.id, false, 480_000L, "خرید", "خرید از فروشگاه", now - 3600000L),
            Tx(id(), b.id, false, 250_000L, "قبوض", "قبض برق", now - day),
            Tx(id(), b.id, true, 1_000_000L, "واریز", "کارت به کارت", now - day - 7200000L),
            Tx(id(), c.id, false, 760_000L, "اینترنت", "خرید اینترنت", now - 5 * day),
            Tx(id(), a.id, false, 2_400_000L, "مصالح", "سیمان و آهن", now - 8 * day)
        )
        places = places + Place(
            id(), "ساختمان شهرک ۱", listOf(
                WorkItem(id(), "فونداسیون", 100, 0, 12, 96, 0, ""),
                WorkItem(id(), "اسکلت طبقه اول", 70, 1, 9, 72, 1, "علی"),
                WorkItem(id(), "سفت‌کاری", 30, 2, 5, 40, 2, "رضا")
            )
        )
        save()
    }

    // ---------- ذخیره و بازیابی ----------
    private fun toJson(): JSONObject {
        val o = JSONObject()
        o.put("version", 1)
        val ca = JSONArray()
        for (c in cards) {
            ca.put(JSONObject().put("id", c.id).put("bank", c.bank).put("last4", c.last4).put("initial", c.initial))
        }
        o.put("cards", ca)
        val ta = JSONArray()
        for (t in txs) {
            ta.put(
                JSONObject().put("id", t.id).put("cardId", t.cardId).put("income", t.income)
                    .put("amount", t.amount).put("category", t.category).put("note", t.note).put("time", t.time)
            )
        }
        o.put("txs", ta)
        val pa = JSONArray()
        for (p in places) {
            val ia = JSONArray()
            for (w in p.items) {
                ia.put(
                    JSONObject().put("id", w.id).put("title", w.title).put("progress", w.progress)
                        .put("floor", w.floor).put("days", w.days).put("hours", w.hours)
                        .put("worker", w.worker).put("workerName", w.workerName)
                )
            }
            pa.put(JSONObject().put("id", p.id).put("name", p.name).put("items", ia))
        }
        o.put("places", pa)
        return o
    }

    private fun applyJson(o: JSONObject) {
        val c = mutableListOf<BankCard>()
        val ca = o.optJSONArray("cards") ?: JSONArray()
        for (i in 0 until ca.length()) {
            val x = ca.getJSONObject(i)
            c.add(BankCard(x.getString("id"), x.getString("bank"), x.optString("last4", ""), x.getLong("initial")))
        }
        val t = mutableListOf<Tx>()
        val ta = o.optJSONArray("txs") ?: JSONArray()
        for (i in 0 until ta.length()) {
            val x = ta.getJSONObject(i)
            t.add(
                Tx(
                    x.getString("id"), x.getString("cardId"), x.getBoolean("income"), x.getLong("amount"),
                    x.optString("category", "سایر"), x.optString("note", ""), x.getLong("time")
                )
            )
        }
        val p = mutableListOf<Place>()
        val pa = o.optJSONArray("places") ?: JSONArray()
        for (i in 0 until pa.length()) {
            val x = pa.getJSONObject(i)
            val items = mutableListOf<WorkItem>()
            val ia = x.optJSONArray("items") ?: JSONArray()
            for (j in 0 until ia.length()) {
                val w = ia.getJSONObject(j)
                items.add(
                    WorkItem(
                        w.getString("id"), w.getString("title"), w.getInt("progress"), w.optInt("floor", 0),
                        w.optInt("days", 0), w.optInt("hours", 0), w.optInt("worker", 0), w.optString("workerName", "")
                    )
                )
            }
            p.add(Place(x.getString("id"), x.getString("name"), items))
        }
        cards = c
        txs = t
        places = p
    }

    private fun save() {
        try {
            val tmp = File(file.parentFile, "vsoft_data.tmp")
            tmp.writeText(toJson().toString())
            if (!tmp.renameTo(file)) {
                file.writeText(tmp.readText())
                tmp.delete()
            }
        } catch (e: Exception) {
            // ذخیره ناموفق؛ داده در حافظه می‌ماند
        }
    }

    private fun backupDir(): File {
        val base = ctx.getExternalFilesDir(null) ?: ctx.filesDir
        val d = File(base, "backups")
        d.mkdirs()
        return d
    }

    private fun listBackups(): List<File> {
        val all = backupDir().listFiles() ?: return emptyList()
        return all.filter { it.name.startsWith("vsoft_") && it.name.endsWith(".json") }
            .sortedByDescending { it.name }
    }

    private fun prune() {
        listBackups().drop(keepCount).forEach { it.delete() }
    }

    fun refreshBackups() {
        backups = listBackups()
    }

    fun backupNow(): Boolean {
        return try {
            val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            File(backupDir(), "vsoft_$stamp.json").writeText(toJson().toString())
            prune()
            refreshBackups()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restore(f: File): Boolean {
        return try {
            val o = JSONObject(f.readText())
            backupNow() // نسخه ایمنی از وضعیت فعلی
            applyJson(o)
            save()
            true
        } catch (e: Exception) {
            false
        }
    }

    init {
        try {
            if (file.exists()) applyJson(JSONObject(file.readText()))
        } catch (e: Exception) {
            // فایل خراب؛ با داده خالی شروع می‌کنیم
        }
        refreshBackups()
        if (autoBackup && (cards.isNotEmpty() || txs.isNotEmpty() || places.isNotEmpty())) {
            val last = backups.firstOrNull()
            if (last == null || System.currentTimeMillis() - last.lastModified() > 86400000L) backupNow()
        }
    }
}
