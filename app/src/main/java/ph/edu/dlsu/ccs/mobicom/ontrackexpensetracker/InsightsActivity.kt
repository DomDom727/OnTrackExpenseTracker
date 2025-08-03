package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// import androidx.compose.ui.text.intl.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format
import java.util.Locale
import kotlin.text.append

class InsightsActivity : AppCompatActivity() {
    private lateinit var data: ArrayList<Expense>
    private lateinit var expenseDatabase: ExpenseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        expenseDatabase = ExpenseDatabase(this)
        data = expenseDatabase.getExpenses()

        val exportButton: Button = findViewById(R.id.export_btn)
        exportButton.setOnClickListener {
            if (data.isNotEmpty()) {
                exportExpensesToCSV(data)
            } else {
                Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            }
        }

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun convertExpensesToCSV(expenses: List<Expense>): String {
        val sb = StringBuilder()
        sb.append("Name,Date,Category,Amount")
        for (expense in expenses) {
            sb.append("\"${escapeCsvField(expense.name)}\",")
            sb.append("${expense.dateTime},")
            sb.append("\"${escapeCsvField(expense.category)}\",")
            sb.append("\"${escapeCsvField(expense.amount.toString())}\"\n")
        }
        return sb.toString()
    }

    // Helper function to escape characters that have special meaning in CSV
    private fun escapeCsvField(field: String?): String {
        if (field == null) return ""
        var escapedField = field.replace("\"", "\"\"") // Escape double quotes
        if (escapedField.contains(',') || escapedField.contains('\n') || escapedField.contains('"')) {
            escapedField = "\"$escapedField\"" // Enclose in double quotes if it contains comma, newline, or quote
        }
        return escapedField
    }

    private fun exportExpensesToCSV(expenses: List<Expense>) {
        val csv = convertExpensesToCSV(expenses)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "expenses_$timestamp.csv"

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                val contentResolver = applicationContext.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    // or Environment.DIRECTORY_DOCUMENTS
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it).use { outputStream ->
                        if (outputStream == null) {
                            throw Exception("Output stream is null")
                        }
                        outputStream.write(csv.toByteArray())
                        outputStream.flush()
                        Toast.makeText(this, "CSV file exported successfully", Toast.LENGTH_SHORT).show()
                        // outputStream.close()
                    }
                } ?: throw Exception("Content URI is null")
            } else {
                Toast.makeText(this, "Can't export CSV file on this device", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error exporting CSV file", Toast.LENGTH_SHORT).show()
        }
    }
}