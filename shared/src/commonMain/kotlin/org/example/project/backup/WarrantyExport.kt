package org.example.project.backup

import org.example.project.model.CoveragePolicy
import org.example.project.model.Warranty

private fun String.jsonEscaped(): String = buildString {
    for (char in this@jsonEscaped) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (char.code < 0x20) append("\\u").append(char.code.toString(16).padStart(4, '0')) else append(char)
        }
    }
}

private fun jsonString(value: String?): String = if (value == null) "null" else "\"${value.jsonEscaped()}\""
private fun jsonNumber(value: Number?): String = value?.toString() ?: "null"

private fun CoveragePolicy.toJson(): String = """
    {
      "id": ${jsonString(id)},
      "kind": ${jsonString(kind.name)},
      "title": ${jsonString(title)},
      "provider": ${jsonString(provider)},
      "durationDays": ${jsonNumber(durationDays)},
      "endEpochDay": ${jsonNumber(endEpochDay)},
      "endDateLabel": ${jsonString(endDateLabel)},
      "notes": ${jsonString(notes)},
      "source": ${jsonString(source.name)}
    }
""".trimIndent()

private fun Warranty.toJson(): String = """
    {
      "id": ${jsonString(id)},
      "productName": ${jsonString(productName)},
      "store": ${jsonString(store)},
      "category": ${jsonString(category)},
      "purchaseDateLabel": ${jsonString(purchaseDateLabel)},
      "warrantyStatusLabel": ${jsonString(warrantyStatusLabel)},
      "urgencyDays": ${jsonNumber(urgencyDays)},
      "price": ${jsonNumber(price)},
      "shape": ${jsonString(shape.name)},
      "warrantyEndDateLabel": ${jsonString(warrantyEndDateLabel)},
      "photoPath": ${jsonString(photoPath)},
      "policies": [${policies.joinToString(",") { it.toJson() }}]
    }
""".trimIndent()

/** Hand-rolled rather than a serialization library — the shape is simple enough to encode
 * safely by hand, and this is the only place in the app that needs JSON at all. */
fun exportWarrantiesToJson(warranties: List<Warranty>): String =
    "[${warranties.joinToString(",") { it.toJson() }}]"
