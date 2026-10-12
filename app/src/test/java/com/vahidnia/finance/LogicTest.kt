package com.vahidnia.finance

import org.junit.Assert.assertEquals
import org.junit.Test

class LogicTest {
    private val card = BankCard("c1", "بانک ملی", "1234", 15_000_000L)

    @Test
    fun balanceAfterExpense() {
        val txs = listOf(Tx("t1", "c1", false, 10_000_000L, "خرید", "", 0L))
        assertEquals(5_000_000L, cardBalance(card, txs))
    }

    @Test
    fun otherCardsDoNotAffectBalance() {
        val txs = listOf(
            Tx("t1", "c1", true, 2_000_000L, "حقوق", "", 0L),
            Tx("t2", "c2", false, 9_000_000L, "خرید", "", 0L)
        )
        assertEquals(17_000_000L, cardBalance(card, txs))
    }

    @Test
    fun jalaliConversion() {
        val d = g2j(2026, 10, 12)
        assertEquals(JDate(1405, 7, 20), d)
        assertEquals(JDate(1405, 1, 1), g2j(2026, 3, 21))
    }

    @Test
    fun moneyFormat() {
        assertEquals("۲۵,۸۷۰,۰۰۰", 25_870_000L.money())
        assertEquals(1_500_000L, parseAmount("۱,۵۰۰,۰۰۰"))
    }
}
