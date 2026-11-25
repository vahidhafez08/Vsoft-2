package com.vsoft.app.data.repository

import com.vsoft.app.data.db.InvestmentDao
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.data.model.InvestmentTypeEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepository @Inject constructor(private val dao: InvestmentDao) {
    fun observeTypes() = dao.observeTypes()
    fun observeInvestments() = dao.observeInvestments()
    suspend fun saveType(type: InvestmentTypeEntity): Long = if (type.id == 0L) dao.insertType(type) else type.id.also { dao.updateType(type) }
    suspend fun deleteType(type: InvestmentTypeEntity): Boolean {
        if (type.isDefault || dao.countByType(type.id) > 0) return false
        dao.deleteType(type); return true
    }
    suspend fun save(item: InvestmentEntity): Long = if (item.id == 0L) dao.insert(item) else item.id.also { dao.update(item) }
    suspend fun delete(item: InvestmentEntity) = dao.delete(item)
    suspend fun updatePrice(id: Long, price: Double) { require(price > 0.0 && price.isFinite()); dao.updatePrice(id, price, System.currentTimeMillis()) }
    suspend fun updateTypePrice(typeId: Long, price: Double) { require(price > 0.0 && price.isFinite()); dao.updatePriceForType(typeId, price, System.currentTimeMillis()) }
    suspend fun sell(item: InvestmentEntity, price: Double) { require(price > 0.0 && price.isFinite()); dao.update(item.copy(status="SOLD", sellPrice=price)) }
}