package org.example.project.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.example.project.model.ProductShape
import org.example.project.model.Warranty

interface WarrantyRepository {
    fun observeAll(): Flow<List<Warranty>>
    suspend fun insert(warranty: Warranty)
}

fun createWarrantyRepository(factory: DatabaseDriverFactory): WarrantyRepository {
    val driver = factory.createDriver() ?: return InMemoryWarrantyRepository()
    val queries = WarrantyDatabase(driver).warrantyQueries
    if (queries.countAll().executeAsOne() == 0L) {
        seedWarranties().forEach { queries.insertWarranty(it) }
    }
    return SqlDelightWarrantyRepository(queries)
}

private class SqlDelightWarrantyRepository(private val queries: WarrantyQueries) : WarrantyRepository {
    override fun observeAll(): Flow<List<Warranty>> =
        queries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toWarranty() } }

    override suspend fun insert(warranty: Warranty) {
        withContext(Dispatchers.Default) { queries.insertWarranty(warranty) }
    }
}

private class InMemoryWarrantyRepository : WarrantyRepository {
    private val state = MutableStateFlow(seedWarranties())

    override fun observeAll(): Flow<List<Warranty>> = state

    override suspend fun insert(warranty: Warranty) {
        state.value = state.value + warranty
    }
}

private fun WarrantyQueries.insertWarranty(warranty: Warranty) {
    insertWarranty(
        id = warranty.id,
        productName = warranty.productName,
        store = warranty.store,
        category = warranty.category,
        purchaseDateLabel = warranty.purchaseDateLabel,
        warrantyStatusLabel = warranty.warrantyStatusLabel,
        urgencyDays = warranty.urgencyDays?.toLong(),
        price = warranty.price,
        shape = warranty.shape.name
    )
}

private fun WarrantyEntity.toWarranty() = Warranty(
    id = id,
    productName = productName,
    store = store,
    category = category,
    purchaseDateLabel = purchaseDateLabel,
    warrantyStatusLabel = warrantyStatusLabel,
    urgencyDays = urgencyDays?.toInt(),
    price = price,
    shape = ProductShape.valueOf(shape)
)

private fun seedWarranties(): List<Warranty> = listOf(
    Warranty("seed-1", "Apple Watch", "John Lewis", "Tech", "3 Jan 2026", "Protected", null, 399.0, ProductShape.Watch),
    Warranty("seed-2", "Desk lamp", "Habitat", "Home", "18 Feb 2026", "Protected", null, 45.0, ProductShape.Lamp),
    Warranty("seed-3", "MacBook Air", "Apple", "Tech", "2 Jun 2025", "2 years left", null, 1299.0, ProductShape.Laptop),
    Warranty("seed-4", "Coffee machine", "Currys", "Home", "9 Nov 2025", "Protected", null, 219.0, ProductShape.Coffee),
    Warranty("seed-5", "Sony WH-1000XM5", "Currys", "Tech", "20 Apr 2026", "Return window ends", 3, 349.0, ProductShape.Other),
    Warranty("seed-6", "Ninja Air Fryer MAX", "Argos", "Home", "2 Jul 2026", "Return window ends", 16, 129.0, ProductShape.Other)
)
