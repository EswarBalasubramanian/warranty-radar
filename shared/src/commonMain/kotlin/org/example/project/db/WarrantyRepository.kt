package org.example.project.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.example.project.model.CoveragePolicy
import org.example.project.model.Currency
import org.example.project.model.ProductShape
import org.example.project.model.Warranty
import org.example.project.model.currencyFrom
import org.example.project.model.formatEpochDayLabel
import org.example.project.model.policyKindFrom
import org.example.project.model.policySourceFrom
import org.example.project.model.todayEpochDay
import org.example.project.model.withLiveStatus

interface WarrantyRepository {
    fun observeAll(): Flow<List<Warranty>>
    suspend fun insert(warranty: Warranty)
    suspend fun update(warranty: Warranty)
    suspend fun delete(id: String)
}

fun createWarrantyRepository(factory: DatabaseDriverFactory): WarrantyRepository {
    val driver = factory.createDriver() ?: return InMemoryWarrantyRepository()
    val queries = WarrantyDatabase(driver).warrantyQueries
    return SqlDelightWarrantyRepository(queries)
}

private class SqlDelightWarrantyRepository(private val queries: WarrantyQueries) : WarrantyRepository {
    override fun observeAll(): Flow<List<Warranty>> =
        combine(
            queries.selectAll().asFlow().mapToList(Dispatchers.Default),
            queries.selectAllPolicies().asFlow().mapToList(Dispatchers.Default)
        ) { warrantyRows, policyRows ->
            val policiesByWarranty = policyRows.groupBy { it.warrantyId }
            val today = todayEpochDay()
            warrantyRows.map { row ->
                row.toWarranty(policiesByWarranty[row.id].orEmpty().map { it.toPolicy() })
                    .withLiveStatus(today)
            }
        }

    override suspend fun insert(warranty: Warranty) {
        withContext(Dispatchers.Default) { queries.insertWarranty(warranty) }
    }

    override suspend fun update(warranty: Warranty) {
        withContext(Dispatchers.Default) {
            queries.transaction {
                queries.deletePoliciesForWarranty(warranty.id)
                queries.deleteWarrantyById(warranty.id)
            }
            queries.insertWarranty(warranty)
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.Default) {
            val photoPath = queries.selectPhotoPathById(id).executeAsOneOrNull()?.photoPath
            queries.transaction {
                queries.deletePoliciesForWarranty(id)
                queries.deleteWarrantyById(id)
            }
            photoPath?.let { deleteStoredFile(it) }
        }
    }
}

private class InMemoryWarrantyRepository : WarrantyRepository {
    private val state = MutableStateFlow(emptyList<Warranty>())

    override fun observeAll(): Flow<List<Warranty>> =
        state.map { warranties ->
            val today = todayEpochDay()
            warranties.map { it.withLiveStatus(today) }
        }

    override suspend fun insert(warranty: Warranty) {
        state.value = state.value + warranty
    }

    override suspend fun update(warranty: Warranty) {
        state.value = state.value.map { if (it.id == warranty.id) warranty else it }
    }

    override suspend fun delete(id: String) {
        state.value.firstOrNull { it.id == id }?.photoPath?.let { deleteStoredFile(it) }
        state.value = state.value.filterNot { it.id == id }
    }
}

private fun WarrantyQueries.insertWarranty(warranty: Warranty) {
    transaction {
        insertWarranty(
            id = warranty.id,
            productName = warranty.productName,
            store = warranty.store,
            category = warranty.category,
            purchaseDateLabel = warranty.purchaseDateLabel,
            warrantyStatusLabel = warranty.warrantyStatusLabel,
            urgencyDays = warranty.urgencyDays?.toLong(),
            price = warranty.price,
            shape = warranty.shape.name,
            warrantyEndDateLabel = warranty.warrantyEndDateLabel,
            photoPath = warranty.photoPath,
            currency = warranty.currency.name
        )
        warranty.policies.forEach { policy ->
            insertPolicy(
                id = policy.id,
                warrantyId = warranty.id,
                kind = policy.kind.name,
                title = policy.title,
                provider = policy.provider,
                durationDays = policy.durationDays?.toLong(),
                endEpochDay = policy.endEpochDay?.toLong(),
                endDateLabel = policy.endDateLabel,
                notes = policy.notes,
                source = policy.source.name
            )
        }
    }
}

private fun WarrantyEntity.toWarranty(policies: List<CoveragePolicy>) = Warranty(
    id = id,
    productName = productName,
    store = store,
    category = category,
    purchaseDateLabel = purchaseDateLabel,
    warrantyStatusLabel = warrantyStatusLabel,
    urgencyDays = urgencyDays?.toInt(),
    price = price,
    shape = ProductShape.valueOf(shape),
    warrantyEndDateLabel = warrantyEndDateLabel,
    policies = policies,
    photoPath = photoPath,
    currency = currencyFrom(currency)
)

private fun PolicyEntity.toPolicy() = CoveragePolicy(
    id = id,
    kind = policyKindFrom(kind),
    title = title,
    provider = provider,
    durationDays = durationDays?.toInt(),
    endEpochDay = endEpochDay?.toInt(),
    endDateLabel = endDateLabel,
    notes = notes,
    source = policySourceFrom(source)
)

