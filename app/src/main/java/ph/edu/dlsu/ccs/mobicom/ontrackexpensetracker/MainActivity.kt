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


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

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
