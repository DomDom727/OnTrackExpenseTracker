package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.ContentValues
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// import androidx.compose.ui.text.intl.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.coroutines.launch
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.text.append

class InsightsActivity : AppCompatActivity() {
    private lateinit var data: ArrayList<Expense>
    private lateinit var expenseRepository: ExpenseRepository

    private var categoryString: String? = null
    private var monthString: String? = null
    private var yearString: String? = null

    private lateinit var categorySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    val modelProducer = CartesianChartModelProducer()
    private lateinit var chartView: CartesianChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        expenseRepository = ExpenseRepository()
        data = ArrayList()
        chartView = findViewById(R.id.chart_view)
        chartView.modelProducer = modelProducer

        categorySpinner = findViewById(R.id.filterCategoriesSpinner)
        monthSpinner = findViewById(R.id.filterMonthSpinner)
        yearSpinner = findViewById(R.id.filterYearSpinner)

        setupSpinners()

        lifecycleScope.launch {
            val fetchedData = expenseRepository.getExpenses()
            data.clear()
            data.addAll(fetchedData)
            val aggregatedData = aggregateExpensesByMonth(data)
            val monthlyTotals = getMonthlyTotalsForChart(aggregatedData)
            val monthLabels = getMonthLabelsForChart(aggregatedData)
            if (monthlyTotals.isNotEmpty()) {
                modelProducer.runTransaction {
                    columnSeries { series(monthlyTotals) }
                }
                //updateChart()
            }
        }

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

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            this,
            R.array.filterCategories,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                categoryString = parent.getItemAtPosition(position).toString()
                // Optionally, trigger data filtering or UI update here
                // Toast.makeText(this@InsightsActivity, "Category: $categoryString", Toast.LENGTH_SHORT).show()
                // filterAndDisplayData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                categoryString = null // Or a default value
            }
        }

        // Month Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.filterMonths, // Reference to your string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            monthSpinner.adapter = adapter
        }

        monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                monthString = parent.getItemAtPosition(position).toString()
                // Optionally, trigger data filtering or UI update here
                // Toast.makeText(this@InsightsActivity, "Month: $monthString", Toast.LENGTH_SHORT).show()
                // filterAndDisplayData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                monthString = null // Or a default value
            }
        }

        // Year Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.filterYears, // Reference to your string array
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = adapter
        }

        yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                yearString = parent.getItemAtPosition(position).toString()
                // Optionally, trigger data filtering or UI update here
                // Toast.makeText(this@InsightsActivity, "Year: $yearString", Toast.LENGTH_SHORT).show()
                // filterAndDisplayData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                yearString = null // Or a default value
            }
        }
    }

    private fun filterAndDisplayData() {

        val filteredList = data.filter { expense ->
            val categoryMatch = categoryString == "All Categories" || categoryString == null || expense.category == categoryString

            val expenseDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(expense.dateTime)

            val expenseMonth = expenseDate?.let { SimpleDateFormat("MMMM", Locale.getDefault()).format(it) }
            val monthMatch = monthString == "All Months" || monthString == null || expenseMonth == monthString

            val expenseYear = expenseDate?.let { SimpleDateFormat("yyyy", Locale.getDefault()).format(it) }
            val yearMatch = yearString == "All Years" || yearString == null || expenseYear == yearString

            categoryMatch && monthMatch && yearMatch
        }
    }

    private fun aggregateExpensesByMonth(expenses: List<Expense>): Map<String, Double> {
        val monthlyExpenses = mutableMapOf<String, Double>()
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", getDefault())
        val monthYearFormatter = SimpleDateFormat("MMMM yyyy", getDefault())
        for (expense in expenses) {
            try {
                val date = inputFormatter.parse(expense.dateTime)
                if (date != null) {
                    val monthYearKey = monthYearFormatter.format(date)
                    monthlyExpenses[monthYearKey] = (monthlyExpenses[monthYearKey] ?: 0.0) + expense.amount
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error parsing date: ${e.message}")
            }
        }
        return monthlyExpenses.toSortedMap()
    }

    private fun getMonthlyTotalsForChart(monthlyAggregatedExpenses: Map<String, Double>): List<Number> {
        return monthlyAggregatedExpenses.values.toList()
    }

    private fun getMonthLabelsForChart(monthlyAggregatedExpenses: Map<String, Double>): List<String> {
        return monthlyAggregatedExpenses.keys.toList()
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