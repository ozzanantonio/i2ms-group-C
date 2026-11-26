package com.urbanfix.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

interface TicketRepository {
    val tickets: StateFlow<List<Ticket>>
    suspend fun add(report: NewReport): Ticket
    suspend fun confirm(ticketId: String, timestamp: Long = System.currentTimeMillis())
    fun find(ticketId: String): Ticket?
    suspend fun syncPendingWithApi(): ApiResult<String>
    suspend fun resetDemoData() {}
}

/**
 * SQLite-backed repository (Week 6) with API synchronization & offline fallback (Week 7).
 */
class PersistentTicketRepository(
    context: Context,
    private val clock: () -> Long = System::currentTimeMillis
) : TicketRepository {

    private val dbHelper = UrbanFixDbHelper(context)
    private val apiClient = UrbanFixApiClient(context)

    private val _tickets = MutableStateFlow<List<Ticket>>(emptyList())
    override val tickets: StateFlow<List<Ticket>> = _tickets.asStateFlow()

    private var nextId = 1045

    init {
        val existing = dbHelper.getAllTickets()
        _tickets.value = existing
        val maxId = existing.mapNotNull { it.id.removePrefix("#UF-").toIntOrNull() }.maxOrNull()
        if (maxId != null) {
            nextId = maxId + 1
        }
    }

    override suspend fun add(report: NewReport): Ticket = withContext(Dispatchers.IO) {
        val now = clock()
        val ticket = Ticket(
            id = "#UF-${nextId++}",
            category = report.category,
            createdAt = now,
            location = report.location.trim(),
            description = report.description.trim(),
            photoUri = report.photoUri,
            mapX = report.mapX,
            mapY = report.mapY,
            isConfirmed = false,
            isSyncedWithApi = false,
            isTestData = false,
            isOwnReport = true
        )

        // 1. Save in local SQLite database immediately (Week 6 - Offline-first)
        dbHelper.insertTicket(ticket)
        _tickets.update { listOf(ticket) + it.filterNot { old -> old.id == ticket.id } }

        // 2. Attempt API Sync (Week 7)
        var finalTicket = ticket
        val syncRes = apiClient.syncTicket(ticket)
        if (syncRes is ApiResult.Success) {
            dbHelper.markSynced(ticket.id)
            finalTicket = ticket.copy(isSyncedWithApi = true)
            _tickets.update { list ->
                list.map { if (it.id == ticket.id) finalTicket else it }
            }
        }
        finalTicket
    }

    override suspend fun confirm(ticketId: String, timestamp: Long) = withContext(Dispatchers.IO) {
        val existing = find(ticketId) ?: return@withContext
        if (existing.isOwnReport) return@withContext // Prevent self-confirmation

        dbHelper.updateConfirmation(ticketId, timestamp)

        _tickets.update { list ->
            list.map {
                if (it.id == ticketId) it.copy(isConfirmed = true, createdAt = timestamp, isSyncedWithApi = false)
                else it
            }
        }

        val updated = find(ticketId)
        if (updated != null) {
            val syncRes = apiClient.syncTicket(updated)
            if (syncRes is ApiResult.Success) {
                dbHelper.markSynced(ticketId)
                _tickets.update { list ->
                    list.map { if (it.id == ticketId) it.copy(isSyncedWithApi = true) else it }
                }
            }
        }
    }

    override fun find(ticketId: String): Ticket? = _tickets.value.firstOrNull { it.id == ticketId }

    override suspend fun syncPendingWithApi(): ApiResult<String> = withContext(Dispatchers.IO) {
        val pending = _tickets.value.filterNot { it.isSyncedWithApi }
        if (pending.isEmpty()) {
            return@withContext ApiResult.Success("All data is up to date locally & remotely")
        }
        var syncedCount = 0
        for (t in pending) {
            val res = apiClient.syncTicket(t)
            if (res is ApiResult.Success) {
                dbHelper.markSynced(t.id)
                syncedCount++
            }
        }
        _tickets.value = dbHelper.getAllTickets()
        if (syncedCount > 0) {
            ApiResult.Success("Synced $syncedCount report(s) with city server")
        } else {
            ApiResult.Error("Offline: data remains saved locally and will sync when reconnected", isOffline = true)
        }
    }

    override suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        dbHelper.resetToDemoData()
        val fresh = dbHelper.getAllTickets()
        _tickets.value = fresh
        val maxId = fresh.mapNotNull { it.id.removePrefix("#UF-").toIntOrNull() }.maxOrNull()
        nextId = (maxId ?: 1044) + 1
    }
}