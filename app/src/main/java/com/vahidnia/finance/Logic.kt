package com.vahidnia.finance

/** موجودی کارت = موجودی اولیه + درآمدها − هزینه‌ها. هر تراکنش دقیقاً یک بار حساب می‌شود. */
fun cardBalance(card: BankCard, txs: List<Tx>): Long {
    var sum = card.initial
    for (t in txs) {
        if (t.cardId == card.id) sum += if (t.income) t.amount else -t.amount
    }
    return sum
}

fun Place.progress(): Int = if (items.isEmpty()) 0 else items.sumOf { it.progress } / items.size
fun Place.totalHours(): Int = items.sumOf { it.hours }
fun Place.totalDays(): Int = items.sumOf { it.days }

/** موجودی همه کارت‌ها در یک پیمایش روی تراکنش‌ها (هر تراکنش یک بار) */
fun allBalances(cards: List<BankCard>, txs: List<Tx>): Map<String, Long> {
    val m = HashMap<String, Long>(cards.size * 2)
    for (c in cards) m[c.id] = c.initial
    for (t in txs) {
        val cur = m[t.cardId] ?: continue
        m[t.cardId] = cur + if (t.income) t.amount else -t.amount
    }
    return m
}
