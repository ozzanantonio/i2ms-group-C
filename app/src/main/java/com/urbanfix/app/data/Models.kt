package com.urbanfix.app.data

import org.json.JSONObject

/**
 * Domain categories for urban reports.
 */
enum class Category(val emoji: String) {
    ROADS("🚧"), WASTE("🗑️"), LIGHTS("💡"), OTHER("⚠️")
}

/**
 * A persistent ticket model.
 * Survives application restarts via SQLite local storage (Week 6).
 */
data class Ticket(
    val id: String,
    val category: Category,
    val createdAt: Long, // Updated when confirmed to refresh the 30-day cleanup policy
    val location: String,
    val description: String,
    val photoUri: String?, // Optional
    val mapX: Float,
    val mapY: Float,
    val isConfirmed: Boolean = false,
    val isSyncedWithApi: Boolean = true, // Track sync status (Week 7)
    val isTestData: Boolean = false, // Clearly label fictional/demo data (Week 9)
    val isOwnReport: Boolean = false // Distinguish user's own reports (no self-confirm)
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("category", category.name)
        put("createdAt", createdAt)
        put("location", location)
        put("description", description)
        put("photoUri", photoUri ?: JSONObject.NULL)
        put("mapX", mapX.toDouble())
        put("mapY", mapY.toDouble())
        put("isConfirmed", isConfirmed)
        put("isSyncedWithApi", isSyncedWithApi)
        put("isTestData", isTestData)
        put("isOwnReport", isOwnReport)
    }

    companion object {
        fun fromJson(json: JSONObject): Ticket {
            val photoVal = if (json.isNull("photoUri")) null else json.optString("photoUri")
            return Ticket(
                id = json.getString("id"),
                category = Category.valueOf(json.optString("category", Category.ROADS.name)),
                createdAt = json.optLong("createdAt", System.currentTimeMillis()),
                location = json.optString("location", ""),
                description = json.optString("description", ""),
                photoUri = if (photoVal.isNullOrBlank()) null else photoVal,
                mapX = json.optDouble("mapX", 0.5).toFloat(),
                mapY = json.optDouble("mapY", 0.5).toFloat(),
                isConfirmed = json.optBoolean("isConfirmed", false),
                isSyncedWithApi = json.optBoolean("isSyncedWithApi", true),
                isTestData = json.optBoolean("isTestData", false),
                isOwnReport = json.optBoolean("isOwnReport", false)
            )
        }
    }
}

/**
 * Submitted by user before persistence generates ID and timestamp.
 */
data class NewReport(
    val category: Category,
    val location: String,
    val description: String,
    val photoUri: String?, // Optional
    val mapX: Float,
    val mapY: Float
)