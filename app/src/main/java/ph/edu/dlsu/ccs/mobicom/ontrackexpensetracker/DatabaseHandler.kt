package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlin.text.format


class DatabaseHandler(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "ExpenseDatabase"
        const val TRANSACTION_TABLE = "transaction_table"

        const val ID = "id"
        const val NAME = "name"
        const val AMOUNT = "amount"
        const val CATEGORY = "category"
        const val DATE_TIME = "date_time"
        const val PHOTO_PATH = "photo_path"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTransactionTableQuery = "CREATE TABLE IF NOT EXISTS $TRANSACTION_TABLE (" +
                "$ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$NAME TEXT NOT NULL," +
                "$AMOUNT REAL NOT NULL," +   // REAL for Double,
                "$CATEGORY TEXT NOT NULL," +
                "$DATE_TIME TEXT NOT NULL," +
                "$PHOTO_PATH TEXT)"
        db?.execSQL(createTransactionTableQuery)

        val sampleExpenses = listOf(
            ExpenseItem(
                name = "Morning Coffee",
                amount = 3.50,
                category = "Food",
                dateTime = "2025-07-21 13:42:07"
            ),
            ExpenseItem(
                name = "Bus Ticket",
                amount = 1.75,
                category = "Transport",
                dateTime = "2025-07-23 08:17:55"
            ),
            ExpenseItem(
                name = "Groceries for the week",
                amount = 75.20,
                category = "Groceries",
                dateTime = "2025-07-25 19:03:12"
            ),
            ExpenseItem(
                name = "Movie Night",
                amount = 12.00,
                category = "Entertainment",
                dateTime = "2025-07-20 22:30:46"
            )
        )

        sampleExpenses.forEach { expense ->
            val values = ContentValues().apply {
                put(NAME, expense.name)
                put(AMOUNT, expense.amount)
                put(CATEGORY, expense.category)
                put(DATE_TIME, expense.dateTime)
            }
            db?.insert(TRANSACTION_TABLE, null, values)
        }
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TRANSACTION_TABLE")
        onCreate(db)
    }

    data class ExpenseItem(val name: String, val amount: Double, val category: String, val dateTime: String)
}