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
