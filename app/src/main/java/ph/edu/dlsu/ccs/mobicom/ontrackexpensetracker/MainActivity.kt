/*
MOBICOM MCO - OnTrack Expense Tracker
Members:
Clemente, Daniel Gavrie (S18)
Feliciano, Jan Robee (S18)
Roque, Dominic Angelo (S17)
Valdellon, Derrick (S18)
*/

package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import com.patrykandpatrick.vico.core.cartesian.CartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer // For Column Chart
// import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer // For Line Chart
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.views.cartesian.CartesianChartView
//import com.patrykandpatrick.vico.views.common.theme.ThemeHandler
import android.graphics.Color // For setting colors programmatically if needed
import android.graphics.Typeface
import android.util.Log
import android.widget.TextView
import androidx.compose.animation.core.copy
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
//import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
//import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.TreeMap
import java.util.Locale.getDefault


class MainActivity : AppCompatActivity() {

    val modelProducer = CartesianChartModelProducer()
    private lateinit var chartView: CartesianChartView
    private lateinit var data: ArrayList<Expense>
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var usernameTextView: TextView
    private lateinit var auth: FirebaseAuth

    private val BottomAxisLabelKey = ExtraStore.Key<List<String>>()

    companion object {
        const val  EXTRA_TRIGGER_SCAN = "ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.TRIGGER_SCAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        // Initialize TextView
        usernameTextView = findViewById(R.id.user_tv)

        // This is called on creation to initially populate the UI
        updateUserNameUI()

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

        lifecycleScope.launch {
            val fetchedData = expenseRepository.getExpenses()
            data.clear()
            data.addAll(fetchedData)
            val aggregatedData: Map<String, Double> = aggregateExpensesByMonth(fetchedData)
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


        val transactionsCardView: CardView = findViewById(R.id.cardView3)
        transactionsCardView.setOnClickListener {
            val intent = Intent(this, TransactionsActivity::class.java)
            startActivity(intent)
        }

        val insightsCardView: CardView = findViewById(R.id.cardView4)
        insightsCardView.setOnClickListener {
            val intent = Intent(this, InsightsActivity::class.java)
            startActivity(intent)
        }

        val cameraButton: Button = findViewById(R.id.camera_btn)
        cameraButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            intent.putExtra(EXTRA_TRIGGER_SCAN, true)
            startActivity(intent)
        }

        val addTransactionButton: Button = findViewById(R.id.newtransaction_btn)
        addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        val profileButton: Button = findViewById(R.id.profile_btn)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // This will be called every time MainActivity returns to the foreground,
        // ensuring the username is always up-to-date.
        updateUserNameUI()
    }

    /**
     * A helper function to load and display the user's display name.
     */
    private fun updateUserNameUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if the user has a display name, otherwise use the email
            val displayName = currentUser.displayName
            if (displayName != null && displayName.isNotEmpty()) {
                usernameTextView.text = displayName.trim()
            } else {
                // Fallback to displaying the user's email if no display name is set
                usernameTextView.text = currentUser.email.toString().trim()
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
}