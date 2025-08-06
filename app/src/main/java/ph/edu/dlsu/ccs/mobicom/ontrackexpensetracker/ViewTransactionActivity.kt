package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AlertDialog

class ViewTransactionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_EXPENSE_NAME = "extra_expense_name"
        const val EXTRA_EXPENSE_AMOUNT = "extra_expense_amount"
        const val EXTRA_EXPENSE_CATEGORY = "extra_expense_category"
        const val EXTRA_EXPENSE_DATE_TIME = "extra_expense_date_time"
        const val EXTRA_EXPENSE_NOTES = "extra_expense_notes"
    }

    private lateinit var nameTextView: TextView
    private lateinit var amountTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var notesTextView: TextView
    private lateinit var editButton: ImageView
    private lateinit var deleteButton: ImageView
    private var currentExpenseId: String? = null

    private lateinit var expenseRepository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transaction)

        expenseRepository = ExpenseRepository()

        val intent = intent
        currentExpenseId = intent.getStringExtra(EXTRA_EXPENSE_ID)
        val name = intent.getStringExtra(EXTRA_EXPENSE_NAME)
        val amount = intent.getDoubleExtra(EXTRA_EXPENSE_AMOUNT, 0.0)
        val date = intent.getStringExtra(EXTRA_EXPENSE_DATE_TIME)
        val category = intent.getStringExtra(EXTRA_EXPENSE_CATEGORY)
        val notes = intent.getStringExtra(EXTRA_EXPENSE_NOTES)

        findViewById<TextView>(R.id.expense_name_tv).setText(name)
        findViewById<TextView>(R.id.amount_tv).setText(amount.toString())
        findViewById<TextView>(R.id.dateTime_tv).setText(date)
        findViewById<TextView>(R.id.categoryValue_tv).setText(category)
        findViewById<TextView>(R.id.notesValue_tv).setText(notes)

        deleteButton = findViewById(R.id.deleteBtn)
        deleteButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        editButton = findViewById(R.id.edit_iv)
        editButton.setOnClickListener {
            val editIntent = Intent(this, EditTransactionActivity::class.java).apply {
                // Pass data that ViewTransactionActivity expects.
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_ID, currentExpenseId)
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_NAME, name)
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_AMOUNT, amount)
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_CATEGORY, category)
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_DATE_TIME, date)
                putExtra(EditTransactionActivity.EXTRA_EXPENSE_NOTES, notes)
            }
            startActivity(editIntent)
            finish()
        }



        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showDeleteConfirmationDialog() {
        if (currentExpenseId == null) {
            Toast.makeText(this, "Expense ID not found, cannot delete.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "currentExpenseId is null, cannot show delete dialog.")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                // Respond to neutral button press (do nothing or dismiss)
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                // Respond to positive button press
                performDeleteExpense()
                dialog.dismiss()
            }
            .show()
    }

    private fun performDeleteExpense() {
        val expenseIdToDelete = currentExpenseId
        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (currentFirebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentFirebaseUser.uid

        if (expenseIdToDelete == null) {
            Toast.makeText(this, "Error: Expense ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            val success = expenseRepository.deleteExpense(expenseIdToDelete, userId)
            if (success) {
                Toast.makeText(this@ViewTransactionActivity, "Expense deleted successfully.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@ViewTransactionActivity, "Failed to delete expense.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}