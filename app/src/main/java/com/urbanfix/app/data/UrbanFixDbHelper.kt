package com.urbanfix.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * SQLite Local Storage for Week 6 & Week 9 demo reset.
 * Guarantees that reports and confirmations survive app restarts & process death.
 */
class UrbanFixDbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "urbanfix.db"
        const val DATABASE_VERSION = 3

        const val TABLE_TICKETS = "tickets"
        const val COL_ID = "id"
        const val COL_CATEGORY = "category"
        const val COL_CREATED_AT = "created_at"
        const val COL_LOCATION = "location"
        const val COL_DESCRIPTION = "description"
        const val COL_PHOTO_URI = "photo_uri"
        const val COL_MAP_X = "map_x"
        const val COL_MAP_Y = "map_y"
        const val COL_IS_CONFIRMED = "is_confirmed"
        const val COL_IS_SYNCED = "is_synced"
        const val COL_IS_TEST_DATA = "is_test_data"
        const val COL_IS_OWN_REPORT = "is_own_report"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSql = """
            CREATE TABLE $TABLE_TICKETS (
                $COL_ID TEXT PRIMARY KEY,$COL_CATEGORY TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL,$COL_LOCATION TEXT NOT NULL,
                $COL_DESCRIPTION TEXT NOT NULL,$COL_PHOTO_URI TEXT,
                $COL_MAP_X REAL NOT NULL,$COL_MAP_Y REAL NOT NULL,
                $COL_IS_CONFIRMED INTEGER NOT NULL DEFAULT 0,$COL_IS_SYNCED INTEGER NOT NULL DEFAULT 1,
                $COL_IS_TEST_DATA INTEGER NOT NULL DEFAULT 0,$COL_IS_OWN_REPORT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createSql)
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TICKETS")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000
        val seed = listOf(
            Ticket(
                id = "#UF-1042",
                category = Category.ROADS,
                createdAt = now - 3 * day,
                location = "ul. Rembielińskiego",
                description = "Very deep pothole in the middle of the lane.",
                photoUri = null,
                mapX = 0.52f,
                mapY = 0.18f,
                isConfirmed = false,
                isSyncedWithApi = true,
                isTestData = true,
                isOwnReport = false
            ),
            Ticket(
                id = "#UF-1019",
                category = Category.LIGHTS,
                createdAt = now - 12 * day,
                location = "Plac Wolności",
                description = "Street light completely off for two nights.",
                photoUri = null,
                mapX = 0.26f,
                mapY = 0.42f,
                isConfirmed = false,
                isSyncedWithApi = true,
                isTestData = true,
                isOwnReport = false
            ),
            Ticket(
                id = "#UF-0988",
                category = Category.WASTE,
                createdAt = now - 18 * day,
                location = "ul. Piotrkowska, 15",
                description = "Overflowing bin, trash on the sidewalk.",
                photoUri = null,
                mapX = 0.78f,
                mapY = 0.22f,
                isConfirmed = false,
                isSyncedWithApi = true,
                isTestData = true,
                isOwnReport = false
            )
        )
        for (t in seed) {
            val cv = ContentValues().apply {
                put(COL_ID, t.id)
                put(COL_CATEGORY, t.category.name)
                put(COL_CREATED_AT, t.createdAt)
                put(COL_LOCATION, t.location)
                put(COL_DESCRIPTION, t.description)
                put(COL_PHOTO_URI, t.photoUri)
                put(COL_MAP_X, t.mapX)
                put(COL_MAP_Y, t.mapY)
                put(COL_IS_CONFIRMED, if (t.isConfirmed) 1 else 0)
                put(COL_IS_SYNCED, 1)
                put(COL_IS_TEST_DATA, 1)
                put(COL_IS_OWN_REPORT, 0)
            }
            db.insert(TABLE_TICKETS, null, cv)
        }
    }

    fun resetToDemoData() {
        val db = writableDatabase
        db.delete(TABLE_TICKETS, null, null)
        seedInitialData(db)
    }

    fun getAllTickets(): List<Ticket> {
        val list = mutableListOf<Ticket>()
        val db = readableDatabase
        val cursor = db.query(TABLE_TICKETS, null, null, null, null, null, "$COL_CREATED_AT DESC")
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COL_ID)
            val catIdx = it.getColumnIndexOrThrow(COL_CATEGORY)
            val timeIdx = it.getColumnIndexOrThrow(COL_CREATED_AT)
            val locIdx = it.getColumnIndexOrThrow(COL_LOCATION)
            val descIdx = it.getColumnIndexOrThrow(COL_DESCRIPTION)
            val photoIdx = it.getColumnIndexOrThrow(COL_PHOTO_URI)
            val xIdx = it.getColumnIndexOrThrow(COL_MAP_X)
            val yIdx = it.getColumnIndexOrThrow(COL_MAP_Y)
            val confIdx = it.getColumnIndexOrThrow(COL_IS_CONFIRMED)
            val syncIdx = it.getColumnIndexOrThrow(COL_IS_SYNCED)
            val testIdx = it.getColumnIndexOrThrow(COL_IS_TEST_DATA)
            val ownIdx = it.getColumnIndexOrThrow(COL_IS_OWN_REPORT)

            while (it.moveToNext()) {
                val catStr = it.getString(catIdx)
                val cat = try { Category.valueOf(catStr) } catch (_: Exception) { Category.OTHER }
                list.add(
                    Ticket(
                        id = it.getString(idIdx),
                        category = cat,
                        createdAt = it.getLong(timeIdx),
                        location = it.getString(locIdx),
                        description = it.getString(descIdx),
                        photoUri = if (it.isNull(photoIdx)) null else it.getString(photoIdx),
                        mapX = it.getFloat(xIdx),
                        mapY = it.getFloat(yIdx),
                        isConfirmed = it.getInt(confIdx) == 1,
                        isSyncedWithApi = it.getInt(syncIdx) == 1,
                        isTestData = it.getInt(testIdx) == 1,
                        isOwnReport = it.getInt(ownIdx) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertTicket(ticket: Ticket) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_ID, ticket.id)
            put(COL_CATEGORY, ticket.category.name)
            put(COL_CREATED_AT, ticket.createdAt)
            put(COL_LOCATION, ticket.location)
            put(COL_DESCRIPTION, ticket.description)
            put(COL_PHOTO_URI, ticket.photoUri)
            put(COL_MAP_X, ticket.mapX)
            put(COL_MAP_Y, ticket.mapY)
            put(COL_IS_CONFIRMED, if (ticket.isConfirmed) 1 else 0)
            put(COL_IS_SYNCED, if (ticket.isSyncedWithApi) 1 else 0)
            put(COL_IS_TEST_DATA, if (ticket.isTestData) 1 else 0)
            put(COL_IS_OWN_REPORT, if (ticket.isOwnReport) 1 else 0)
        }
        db.insertWithOnConflict(TABLE_TICKETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateConfirmation(ticketId: String, newTimestamp: Long) {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_IS_CONFIRMED, 1)
            put(COL_CREATED_AT, newTimestamp) // Refresh 30-day timer!
            put(COL_IS_SYNCED, 0) // Mark pending sync
        }
        db.update(TABLE_TICKETS, cv, "$COL_ID = ?", arrayOf(ticketId))
    }

    fun markSynced(ticketId: String) {
        val db = writableDatabase
        val cv = ContentValues().apply { put(COL_IS_SYNCED, 1) }
        db.update(TABLE_TICKETS, cv, "$COL_ID = ?", arrayOf(ticketId))
    }
}