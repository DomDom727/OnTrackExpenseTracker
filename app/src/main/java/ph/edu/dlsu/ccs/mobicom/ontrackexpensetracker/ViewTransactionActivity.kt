package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.ComponentActivity
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewTransactionActivity : ComponentActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
    }
    private lateinit var expenseDatabase: ExpenseDatabase

    private lateinit var nameTextView: TextView
    private lateinit var amountTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var categoryTextView: TextView
    // private lateinit var notesTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_transaction)

        expenseDatabase = ExpenseDatabase(applicationContext)
        nameTextView = findViewById(R.id.expense_name_tv)
        amountTextView = findViewById(R.id.amount_tv)
        dateTextView = findViewById(R.id.dateTime_tv)
        categoryTextView = findViewById(R.id.categoryValue_tv)

        val expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L)
        val expense = expenseDatabase.getExpenseFromId(expenseId.toInt())

        loadExpenseDetails(expense)


        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun loadExpenseDetails(expense: Expense?) {
        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                expense?.let {
                    nameTextView.text = it.name
                    amountTextView.text = it.amount.toString()
                    dateTextView.text = it.dateTime
                    categoryTextView.text = it.category
                    // notesTextView.text = it.notes
                } ?: run {
                    nameTextView.text = "Expense details not found."
                    amountTextView.text = ""
                    dateTextView.text = ""
                    categoryTextView.text = ""
                    //notesTextView.text = ""
                }
            }
        }
    }
}