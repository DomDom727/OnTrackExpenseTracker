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
import androidx.lifecycle.lifecycleScope
//import com.patrykandpatrick.vico.core.cartesian.axis.AxisPosition
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
//import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.shape.Shape
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    val modelProducer = CartesianChartModelProducer()
    private lateinit var chartView: CartesianChartView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        chartView = findViewById(R.id.chart_view)
        chartView.modelProducer = modelProducer
        lifecycleScope.launch {
            modelProducer.runTransaction {
                columnSeries { series(5, 6, 5, 2, 11, 8, 5, 2, 15, 11, 8, 13, 12, 10, 2, 7) }
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
            val intent = Intent(this, TakePhotoActivity::class.java)
            startActivity(intent)
        }

        val addTransactionButton: Button = findViewById(R.id.newtransaction_btn)
        addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }
    }
}
