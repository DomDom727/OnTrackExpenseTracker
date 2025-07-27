package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.util.Log

class ExpenseDatabase(context: Context) {
    private lateinit var databaseHandler : DatabaseHandler

    init {
        this.databaseHandler = DatabaseHandler(context)
    }

    fun addExpense(expense: Expense): Int {
        val db = databaseHandler.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(DatabaseHandler.NAME, expense.name)
        contentValues.put(DatabaseHandler.AMOUNT, expense.amount)
        contentValues.put(DatabaseHandler.CATEGORY, expense.category)
        contentValues.put(DatabaseHandler.DATE_TIME, expense.dateTime)

        val _id = db.insert(DatabaseHandler.TRANSACTION_TABLE, null, contentValues)

        db.close()
        return _id.toInt()
    }

    fun updateExpense(expense: Expense) {
        val db = databaseHandler.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(DatabaseHandler.NAME, expense.name)
        contentValues.put(DatabaseHandler.AMOUNT, expense.amount)
        contentValues.put(DatabaseHandler.CATEGORY, expense.category)

        db.update(
            DatabaseHandler.TRANSACTION_TABLE,
            contentValues,
            "${DatabaseHandler.ID} = ?",
            arrayOf(expense.id.toString())
        )
        db.close()
    }

    fun deleteExpense(expense: Expense) {
        val db = databaseHandler.writableDatabase

        db.delete(
            DatabaseHandler.TRANSACTION_TABLE,
            "${DatabaseHandler.ID} = ?",
            arrayOf(expense.id.toString())
        )
        db.close()
    }

    fun getExpenses(): ArrayList<Expense> {
        val result = ArrayList<Expense>()
        val db = databaseHandler.readableDatabase
        var cursor: Cursor? = null

        try{
            cursor = db.query(
                DatabaseHandler.TRANSACTION_TABLE,
                null,
                null,
                null,
                null,
                null,
                null
            )
        } catch (e: SQLiteException) {
            db.execSQL("CREATE TABLE IF NOT EXISTS ${DatabaseHandler.TRANSACTION_TABLE}")
            return ArrayList()
        }

        var id: Int
        var name: String
        var amount: Double
        var category: String
        var dateTime: String

        if (cursor!!.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.ID))
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.NAME))
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.AMOUNT))
                category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.CATEGORY))
                dateTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.DATE_TIME))

                val expense = Expense(id, name, amount, category, dateTime)
                result.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return result
    }

    fun getExpenseFromId(id: Int): Expense? {
        val db = databaseHandler.readableDatabase
        var cursor: Cursor? = null
        var expense: Expense? = null

        try{
            val projection = arrayOf(
                DatabaseHandler.ID,
                DatabaseHandler.NAME,
                DatabaseHandler.AMOUNT,
                DatabaseHandler.CATEGORY,
                DatabaseHandler.DATE_TIME,
                DatabaseHandler.PHOTO_PATH
            )
            val selection = "${DatabaseHandler.ID} = ?"
            val selectionArgs = arrayOf(id.toString())

            cursor = db.query(
                DatabaseHandler.TRANSACTION_TABLE,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val expenseId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHandler.ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.NAME))
                val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHandler.AMOUNT))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.CATEGORY))
                val dateTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHandler.DATE_TIME))

                expense = Expense(expenseId, name, amount, category, dateTime)
            }
        } catch (e: SQLiteException) {
            Log.e("DatabaseHandler", "Error getting expense by ID: ${e.message}")
        } finally {
            cursor?.close()
            db.close()
        }
        return expense
    }

}