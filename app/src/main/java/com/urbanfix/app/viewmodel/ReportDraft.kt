package com.urbanfix.app.viewmodel

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.urbanfix.app.data.Category
import com.urbanfix.app.data.NewReport

const val MAX_DESCRIPTION_LENGTH = 300

enum class DraftError { LOCATION_MISSING, DESCRIPTION_TOO_LONG }

/**
 * Validation rules:
 * - Photo is NOT mandatory anymore! (Week 6/7 request)
 * - Location is mandatory.
 * - Description cannot exceed MAX_DESCRIPTION_LENGTH.
 */
fun validateReport(location: String, description: String): Set<DraftError> =
    buildSet {
        if (location.isBlank()) add(DraftError.LOCATION_MISSING)
        if (description.length > MAX_DESCRIPTION_LENGTH) add(DraftError.DESCRIPTION_TOO_LONG)
    }

@Stable
class ReportDraft {
    var category by mutableStateOf(Category.ROADS); private set
    var photoUri by mutableStateOf<String?>(null); private set
    var location by mutableStateOf(""); private set
    var description by mutableStateOf(""); private set

    var mapX by mutableStateOf<Float?>(null); private set
    var mapY by mutableStateOf<Float?>(null); private set

    var errors by mutableStateOf(emptySet<DraftError>()); private set

    fun updateCategory(c: Category) { category = c }

    fun updatePhoto(uri: String?) {
        photoUri = uri
    }

    fun updateLocationText(text: String) {
        location = text
        mapX = null; mapY = null
        if (text.isNotBlank()) errors = errors - DraftError.LOCATION_MISSING
    }

    fun setLocationWithPosition(text: String, x: Float, y: Float) {
        location = text
        mapX = x; mapY = y
        errors = errors - DraftError.LOCATION_MISSING
    }

    fun updateDescription(text: String) {
        description = text.take(MAX_DESCRIPTION_LENGTH + 50)
        errors = if (description.length > MAX_DESCRIPTION_LENGTH) {
            errors + DraftError.DESCRIPTION_TOO_LONG
        } else {
            errors - DraftError.DESCRIPTION_TOO_LONG
        }
    }

    fun validate(): Set<DraftError> {
        errors = validateReport(location, description)
        return errors
    }

    fun toNewReport(): NewReport = NewReport(
        category = category,
        location = location,
        description = description,
        photoUri = photoUri,
        mapX = mapX ?: 0.5f,
        mapY = mapY ?: 0.5f
    )

    fun reset() {
        category = Category.ROADS; photoUri = null; location = ""; description = ""
        mapX = null; mapY = null; errors = emptySet()
    }
}