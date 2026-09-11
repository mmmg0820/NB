package com.hoscat.mtj.dev

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mtj.records.*

internal fun admittedRecords(existing: List<Envelope>, incoming: List<Envelope>): List<Envelope> {
    val plan = MigrationPlanner().plan(existing, incoming)
    require(plan.ready) { "Record batch rejected" }
    return plan.decisions.filter { it.action == Action.INSERT }.map { incoming[it.index] }
}

/** Dedicated MTJ database. Never opens or imports another app's storage. */
internal class RecordStore(context: Context) {
    private val access = Mutex()
    private val codec = RecordsJsonCodec()
    private val helper = object : SQLiteOpenHelper(context.applicationContext, "mtj-records-v1.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE records (identity TEXT PRIMARY KEY NOT NULL, envelope BLOB NOT NULL)")
        }
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            error("Migration required; refusing destructive upgrade")
        }
    }
    private fun read(db: SQLiteDatabase): List<Envelope> =
        db.rawQuery("SELECT envelope FROM records ORDER BY identity", null).use { cursor ->
            buildList { while (cursor.moveToNext()) add(codec.decode(cursor.getBlob(0))) }
        }.also { require(MigrationPlanner().plan(it, emptyList()).ready) { "Stored records invalid" } }

    suspend fun list(): List<Envelope> = withContext(Dispatchers.IO) { access.withLock { read(helper.readableDatabase) } }

    suspend fun insert(batch: List<Envelope>) = withContext(Dispatchers.IO) {
        access.withLock {
        // Freeze caller-owned maps before entering the transaction.
        val frozen = batch.map { codec.decode(codec.encode(it)) }
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            admittedRecords(read(db), frozen).forEach { record ->
                db.insertOrThrow("records", null, ContentValues().apply {
                    put("identity", record.origin.commonId)
                    put("envelope", codec.encode(record))
                })
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        }
    }

    suspend fun delete(origin: Origin) = withContext(Dispatchers.IO) {
        access.withLock {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val existing = read(db)
            require(existing.none { origin in it.profileRefs }) { "Referenced profile cannot be deleted" }
            db.delete("records", "identity = ?", arrayOf(origin.commonId))
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        }
    }
}
