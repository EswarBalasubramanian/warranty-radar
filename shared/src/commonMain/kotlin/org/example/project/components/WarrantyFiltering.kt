package org.example.project.components

import org.example.project.model.Warranty

const val ALL_CATEGORIES = "All items"

fun categoriesFor(warranties: List<Warranty>): List<String> =
    listOf(ALL_CATEGORIES) + warranties.map { it.category }.distinct().sorted()

fun filterWarranties(warranties: List<Warranty>, query: String, category: String): List<Warranty> =
    warranties.filter { warranty ->
        (category == ALL_CATEGORIES || warranty.category == category) &&
            (query.isBlank() ||
                warranty.productName.contains(query, ignoreCase = true) ||
                warranty.store.contains(query, ignoreCase = true) ||
                warranty.policies.any { policy ->
                    policy.title.contains(query, ignoreCase = true) ||
                        policy.provider?.contains(query, ignoreCase = true) == true ||
                        policy.notes?.contains(query, ignoreCase = true) == true
                })
    }
