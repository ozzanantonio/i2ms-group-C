package com.urbanfix.app

import com.urbanfix.app.data.Category
import com.urbanfix.app.data.InMemoryTicketRepository
import com.urbanfix.app.data.MapProjection
import com.urbanfix.app.data.NewReport
import com.urbanfix.app.viewmodel.DraftError
import com.urbanfix.app.viewmodel.MAX_DESCRIPTION_LENGTH
import com.urbanfix.app.viewmodel.searchTickets
import com.urbanfix.app.viewmodel.validateReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Acceptance-criteria checks that run on the JVM (no emulator needed). */
class UrbanFixLogicTest {

    // ── Validation (user story: submit a report with photo + location) ──
    @Test fun `report without photo and location is rejected`() {
        val errors = validateReport(null, "", "")
        assertEquals(setOf(DraftError.PHOTO_MISSING, DraftError.LOCATION_MISSING), errors)
    }

    @Test fun `valid report has no errors and description is optional`() {
        assertTrue(validateReport("content://photo/1", "ul. Piotrkowska 42", "").isEmpty())
    }

    @Test fun `description longer than limit is rejected`() {
        val errors = validateReport("content://photo/1", "Plac Wolności", "a".repeat(MAX_DESCRIPTION_LENGTH + 1))
        assertEquals(setOf(DraftError.DESCRIPTION_TOO_LONG), errors)
    }

    // ── Repository (user story: see my report in the public tracker) ──
    @Test fun `new report appears first in the list with a new id`() {
        val repo = InMemoryTicketRepository(clock = { 1_000L })
        val before = repo.tickets.value.size
        val t = repo.add(NewReport(Category.WASTE, " ul. Zielona 3 ", "Bin", "content://x", 0.3f, 0.4f))
        assertEquals(before + 1, repo.tickets.value.size)
        assertEquals(t, repo.tickets.value.first())
        assertEquals("#UF-1043", t.id)
        assertEquals("ul. Zielona 3", t.location) // trimmed
        assertFalse(t.isConfirmed)
    }

    // ── Confirm (user story: confirm an issue reported by someone else) ──
    @Test fun `confirm marks only the selected ticket`() {
        val repo = InMemoryTicketRepository()
        repo.confirm("#UF-1019")
        assertTrue(repo.find("#UF-1019")!!.isConfirmed)
        assertFalse(repo.find("#UF-1042")!!.isConfirmed)
    }

    // ── Search ──
    @Test fun `search matches street, category label and returns empty for unknown`() {
        val tickets = InMemoryTicketRepository().tickets.value
        val label: (Category) -> String = { it.name.lowercase() }
        assertEquals(listOf("#UF-0988"), searchTickets(tickets, "piotrkowska", label).map { it.id })
        assertEquals(listOf("#UF-1019"), searchTickets(tickets, "lights", label).map { it.id })
        assertTrue(searchTickets(tickets, "zzz", label).isEmpty())
        assertTrue(searchTickets(tickets, "  ", label).isEmpty())
    }

    // ── Map projection (GPS ↔ map position) ──
    @Test fun `projection round trip and clamping`() {
        val (x, y) = MapProjection.toMap(51.770, 19.460)
        val (lat, lng) = MapProjection.toLatLng(x, y)
        assertEquals(51.770, lat, 1e-4)
        assertEquals(19.460, lng, 1e-4)
        // Outside Łódź → clamped to the map edge
        assertEquals(0f to 0f, MapProjection.toMap(60.0, 0.0))
        assertFalse(MapProjection.isInsideMap(37.42, -122.08))
    }
}
