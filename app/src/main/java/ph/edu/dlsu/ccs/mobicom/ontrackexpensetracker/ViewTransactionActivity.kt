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

class ViewTransactionActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXPENSE_ID = "extra_expense_id"
        const val EXTRA_EXPENSE_NAME = "extra_expense_name"
        const val EXTRA_EXPENSE_AMOUNT = "extra_expense_amount"
        const val EXTRA_EXPENSE_CATEGORY = "extra_expense_category"
        const val EXTRA_EXPENSE_DATE_TIME = "extra_expense_date_time"
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
        // nameTextView = findViewById(R.id.expense_name_tv)
        // amountTextView = findViewById(R.id.amount_tv)
        // dateTextView = findViewById(R.id.dateTime_tv)
        // categoryTextView = findViewById(R.id.categoryValue_tv)
        val intent = intent
        val expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, -1L)
        //val expense = expenseDatabase.getExpenseFromId(expenseId.toInt())

        val name = intent.getStringExtra(EXTRA_EXPENSE_NAME)
        val amount = intent.getDoubleExtra(EXTRA_EXPENSE_AMOUNT, 0.0)
        val date = intent.getStringExtra(EXTRA_EXPENSE_DATE_TIME)
        val category = intent.getStringExtra(EXTRA_EXPENSE_CATEGORY)

        findViewById<TextView>(R.id.expense_name_tv).setText(name)
        findViewById<TextView>(R.id.amount_tv).setText(amount.toString())
        findViewById<TextView>(R.id.dateTime_tv).setText(date)
        findViewById<TextView>(R.id.categoryValue_tv).setText(category)


        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}