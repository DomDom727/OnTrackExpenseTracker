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
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
//import androidx.privacysandbox.tools.core.generator.build
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

    companion object {
        const val  EXTRA_TRIGGER_SCAN = "ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.TRIGGER_SCAN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val expenseDatabase = ExpenseDatabase(applicationContext)
        this.data = expenseDatabase.getExpenses()

        val aggregatedData = aggregateExpensesByMonth(data)
        val monthlyTotals = getMonthlyTotalsForChart(aggregatedData)
        val monthLabels = getMonthLabelsForChart(aggregatedData)

        chartView = findViewById(R.id.chart_view)
        chartView.modelProducer = modelProducer
        lifecycleScope.launch {
            modelProducer.runTransaction {
                columnSeries { series(monthlyTotals) }
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
}
