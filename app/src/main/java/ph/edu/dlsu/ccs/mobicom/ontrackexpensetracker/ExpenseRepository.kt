package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class ExpenseRepository {
    private val db = Firebase.firestore
    private val currentUser = FirebaseAuth.getInstance().currentUser

    companion object  {
        private const val TAG = "ExpenseRepository"
        private const val USERS_COLLECTION = "users"
        private const val EXPENSES_COLLECTION = "expenses"
    }

    private fun getUserExpensesCollectionRef(): Query? {
        return currentUser?.uid?.let {
            db.collection(USERS_COLLECTION).document(it).collection(EXPENSES_COLLECTION).orderBy("dateTime", Query.Direction.DESCENDING)
        }
    }

    suspend fun getExpenses(): List<Expense> {
        val userExpensesRef = getUserExpensesCollectionRef()
        if (userExpensesRef == null) {
            Log.e(TAG, "User not authenticated or expenses collection not found")
            return emptyList()
        }
        return try {
            val snapshot = userExpensesRef.get().await()
            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject<Expense>()
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document to Expense object", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expenses", e)
            emptyList()
        }
    }

    suspend fun addExpense(expense: Expense): String? {
        val userExpensesRef = currentUser?.uid?.let {
            db.collection(USERS_COLLECTION).document(it).collection(EXPENSES_COLLECTION)
        }
        if (userExpensesRef == null) {
            Log.e(TAG, "User not authenticated or expenses collection not found")
            return null
        }

        // check the expense has same userId
        expense.userId = currentUser?.uid

        return try {
            val documentRef = userExpensesRef.add(expense).await()
            documentRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding expense", e)
            null
        }
    }

    suspend fun updateExpense(expense: Expense): Boolean {
        if (expense.id == null || expense.userId == null) {
            Log.e(TAG, "Invalid expense object: expense.id is '${expense.id}' and expense.userId is '${expense.userId}'")
            return false
        }
        val docRef = db.collection(USERS_COLLECTION)
            .document(expense.userId!!)
            .collection(EXPENSES_COLLECTION)
            .document(expense.id!!)
        return try {
            docRef.set(expense).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating expense", e)
            false
        }
    }

    suspend fun deleteExpense(expenseId: String, userId: String): Boolean {
        if (userId.isEmpty()) {
            Log.e(TAG, "Empty user ID, Cannot Delete")
            return false
        }
        val docRef = db.collection(USERS_COLLECTION)
            .document(userId)
            .collection(EXPENSES_COLLECTION)
            .document(expenseId)
        return try {
            docRef.delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting expense", e)
            false
        }
    }
}