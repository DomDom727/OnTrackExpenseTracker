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
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
import kotlinx.coroutines.launch
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.text.format
import java.util.Locale
import java.util.Locale.getDefault
import kotlin.text.append
import java.util.Calendar

class InsightsActivity : AppCompatActivity() {
    private lateinit var data: ArrayList<Expense>
    private lateinit var expenseRepository: ExpenseRepository

    private var categoryString: String? = "none"
    private var monthString: String? = "none"
    private var yearString: String? = "none"

    private lateinit var categorySpinner: Spinner
    private lateinit var monthSpinner: Spinner
    private lateinit var yearSpinner: Spinner

    val modelProducer = CartesianChartModelProducer()
    private lateinit var chartView: CartesianChartView
    private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights)

        expenseRepository = ExpenseRepository()
        data = ArrayList()
        chartView = findViewById(R.id.chart_view)
        val bottomAxisValueFormatter = CartesianValueFormatter{ context, x, _ ->
            context.model.extraStore[BottomAxisLabelKey][x.toInt()]
        }

        with(chartView) {
            chart =
                chart!!.copy(
                    bottomAxis =
                        (chart!!.bottomAxis as HorizontalAxis).copy(
                            valueFormatter = bottomAxisValueFormatter
                        )
                )
            //this.modelProducer = modelProducer
        }
        chartView.modelProducer = modelProducer

        categorySpinner = findViewById(R.id.filterCategoriesSpinner)
        monthSpinner = findViewById(R.id.filterMonthSpinner)
        yearSpinner = findViewById(R.id.filterYearSpinner)

        setupSpinners()

        lifecycleScope.launch {
            val fetchedData = expenseRepository.getExpenses()
            val filteredData = filterData(fetchedData)
            data.clear()
            data.addAll(filteredData)
            val aggregatedData = aggregateExpensesByMonth(data)
            // val monthlyTotals = getMonthlyTotalsForChart(aggregatedData)
            // val monthLabels = getMonthLabelsForChart(aggregatedData)
            if (aggregatedData.isNotEmpty()) {
                modelProducer.runTransaction {
                    val monthLabels = aggregatedData.keys.toList()
                    val monthlyTotals = aggregatedData.values.toList()
                    columnSeries { series(monthlyTotals) }
                    extras { it[BottomAxisLabelKey] = monthLabels }
                }
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
                displayFilteredData()
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
                displayFilteredData()
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
                displayFilteredData()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                yearString = null // Or a default value
            }
        }
    }

    private fun filterData(expensesToFilter: List<Expense>): List<Expense> {
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", getDefault())
        return expensesToFilter.filter { expense ->
            var matchesCategory = true
            var matchesMonth = true
            var matchesYear = true

            // Filter by Category
            if (categoryString != null && categoryString != "none") {
                matchesCategory = expense.category.equals(categoryString, ignoreCase = true)
            }

            // Parse expense date for month and year filtering
            val expenseDate = try {
                inputFormatter.parse(expense.dateTime)
            } catch (e: Exception) {
                Log.e("InsightsActivity", "Error parsing expense date: ${expense.dateTime}", e)
                null
            }

            if (expenseDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = expenseDate

                // Filter by Month
                if (monthString != null && monthString != "none") {
                    val expenseMonth = SimpleDateFormat("MMMM", getDefault()).format(calendar.time)
                    matchesMonth = expenseMonth.equals(monthString, ignoreCase = true)
                }

                // Filter by Year
                if (yearString != null && yearString != "none") {
                    val expenseYear = calendar.get(Calendar.YEAR).toString()
                    matchesYear = expenseYear == yearString
                }
            } else {
                // If date can't be parsed, it doesn't match month/year filters unless they are "none"
                matchesMonth = (monthString == "none")
                matchesYear = (yearString == "none")
            }
            matchesCategory && matchesMonth && matchesYear
        }
    }

    private fun displayFilteredData() {
        lifecycleScope.launch {
            val fetchedData = expenseRepository.getExpenses()
            val filteredData = filterData(fetchedData)
            data.clear()
            data.addAll(filteredData)
            val aggregatedData = aggregateExpensesByMonth(data)

            if (aggregatedData.isNotEmpty()) {
                modelProducer.runTransaction {
                    val monthLabels = aggregatedData.keys.toList()
                    val monthlyTotals = aggregatedData.values.toList()
                    columnSeries { series(monthlyTotals) }
                    extras { it[BottomAxisLabelKey] = monthLabels }
                }
            }
        }
    }

    private fun aggregateExpensesByMonth(expenses: List<Expense>): Map<String, Double> {
        val monthlyExpenses = mutableMapOf<String, Double>()
        val inputFormatter = SimpleDateFormat("yyyy-MM-dd", getDefault())
        val monthYearFormatter = SimpleDateFormat("MMM yyyy", getDefault())
        // val monthlyExpensesAggregator = mutableMapOf<String, Double>()
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

        val entriesToSort = monthlyExpenses.entries.mapNotNull { entry ->
            try {
                val dateFromKey = monthYearFormatter.parse(entry.key)
                if (dateFromKey!= null) {
                    Pair(dateFromKey, entry)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
        val sortedByDate = entriesToSort.sortedByDescending { it.first }
        val result = LinkedHashMap<String, Double>()
        for ((_, originalEntry) in sortedByDate) {
            result[originalEntry.key] = originalEntry.value
        }
        return result
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