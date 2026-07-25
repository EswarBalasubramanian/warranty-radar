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
import org.example.project.model.PolicyKind
import org.example.project.model.PolicySource
import org.example.project.model.ProductShape
import org.example.project.model.Warranty
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
    if (queries.countAll().executeAsOne() == 0L) {
        seedWarranties().forEach { queries.insertWarranty(it) }
    }
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
            queries.transaction {
                queries.deletePoliciesForWarranty(id)
                queries.deleteWarrantyById(id)
            }
        }
    }
}

private class InMemoryWarrantyRepository : WarrantyRepository {
    private val state = MutableStateFlow(seedWarranties())

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
            warrantyEndDateLabel = warranty.warrantyEndDateLabel
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
    policies = policies
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

private fun seedPolicy(
    id: String,
    kind: PolicyKind,
    title: String,
    provider: String? = null,
    durationDays: Int? = null,
    endsInDays: Int? = null
): CoveragePolicy {
    val end = endsInDays?.let { todayEpochDay() + it }
    return CoveragePolicy(
        id = id,
        kind = kind,
        title = title,
        provider = provider,
        durationDays = durationDays,
        endEpochDay = end,
        endDateLabel = end?.let { formatEpochDayLabel(it) },
        source = PolicySource.Manual
    )
}

private fun seedWarranties(): List<Warranty> = listOf(
    Warranty(
        "seed-1", "Apple Watch", "John Lewis", "Tech", "3 Jan 2026", "Protected", null, 399.0, ProductShape.Watch,
        policies = listOf(
            seedPolicy("seed-1-p1", PolicyKind.Warranty, "1 year Apple warranty", provider = "Apple", durationDays = 365, endsInDays = 140)
        )
    ),
    Warranty("seed-2", "Desk lamp", "Habitat", "Home", "18 Feb 2026", "Protected", null, 45.0, ProductShape.Lamp),
    Warranty(
        "seed-3", "MacBook Air", "Apple", "Tech", "2 Jun 2025", "2 years left", null, 1299.0, ProductShape.Laptop,
        policies = listOf(
            seedPolicy("seed-3-p1", PolicyKind.Protection, "AppleCare+ cover", provider = "Apple", endsInDays = 700)
        )
    ),
    Warranty("seed-4", "Coffee machine", "Currys", "Home", "9 Nov 2025", "Protected", null, 219.0, ProductShape.Coffee),
    Warranty(
        "seed-5", "Sony WH-1000XM5", "Currys", "Tech", "20 Apr 2026", "Return window ends", 3, 349.0, ProductShape.Other,
        policies = listOf(
            seedPolicy("seed-5-p1", PolicyKind.Return, "30-day returns", provider = "Currys", durationDays = 30, endsInDays = 3),
            seedPolicy("seed-5-p2", PolicyKind.Warranty, "2 year manufacturer guarantee", provider = "Manufacturer", durationDays = 730, endsInDays = 680)
        )
    ),
    Warranty(
        "seed-6", "Ninja Air Fryer MAX", "Argos", "Home", "2 Jul 2026", "Return window ends", 16, 129.0, ProductShape.Other,
        policies = listOf(
            seedPolicy("seed-6-p1", PolicyKind.Replacement, "Replacement window", provider = "Argos", endsInDays = 16),
            seedPolicy("seed-6-p2", PolicyKind.Warranty, "1 year guarantee", durationDays = 365, endsInDays = 349)
        )
    )
)
