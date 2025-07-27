package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.semantics.error
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import kotlin.text.toDoubleOrNull

class AddTransactionActivity : ComponentActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var dateEditText: EditText

    private lateinit var expenseDatabase: ExpenseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        expenseDatabase = ExpenseDatabase(applicationContext)

        nameEditText = findViewById(R.id.editTextName)
        amountEditText = findViewById(R.id.editTextAmount)
        categoryEditText = findViewById(R.id.editTextCategory)
        dateEditText = findViewById(R.id.editTextDate)

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }

        val addButton: Button = findViewById(R.id.add_btn)
        addButton.setOnClickListener {
            AddTransactionAndProceed()
        }

        val cameraButton: Button = findViewById(R.id.scan_btn)
        cameraButton.setOnClickListener {
            val intent = Intent(this, TakePhotoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun AddTransactionAndProceed() {
        val name = nameEditText.text.toString().trim()
        val amountStr = amountEditText.text.toString().trim()
        val category = categoryEditText.text.toString().trim()
        val date = dateEditText.text.toString().trim()


        // Input Validation
        if (name.isEmpty()) {
            nameEditText.error = "Name cannot be empty"
            nameEditText.requestFocus()
            return
        }
        if (amountStr.isEmpty()) {
            amountEditText.error = "Amount cannot be empty"
            amountEditText.requestFocus()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountEditText.error = "Enter a valid positive amount"
            amountEditText.requestFocus()
            return
        }
        if (category.isEmpty()) {
            // Assuming category can be anything, but not empty if required
            categoryEditText.error = "Category cannot be empty"
            categoryEditText.requestFocus()
            return
        }
        if (date.isEmpty()) {
            // This might not be strictly necessary if date picker always sets a date
            dateEditText.error = "Date cannot be empty"
            return
        }

        val expense = Expense(name, amount, category, date)
        val rowId = expenseDatabase.addExpense(expense)

        if (rowId != -1) {
            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ViewTransactionActivity::class.java).apply {
                putExtra(ViewTransactionActivity.EXTRA_EXPENSE_ID, rowId)
            }
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Failed to add expense", Toast.LENGTH_SHORT).show()
        }
    }
}