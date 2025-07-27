package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.databinding.ActivityTransactionsBinding

class TransactionsActivity : ComponentActivity() {
    private lateinit var data: ArrayList<Expense>         // Holds the data for the app
    private lateinit var myAdapter: MyAdapter               // Holds the adapter for the RecyclerView
    // private lateinit var viewBinding: ActivityTransactionsBinding   // Holds the views of the ActivityMainBinding
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        // viewBinding = ActivityTransactionsBinding.inflate(layoutInflater)
        // setContentView(viewBinding.root)

        val expenseDatabase = ExpenseDatabase(applicationContext)
        this.data = expenseDatabase.getExpenses()
        this.myAdapter = MyAdapter(this.data)
        this.recyclerView = findViewById(R.id.recyclerView)
        this.recyclerView.adapter = this.myAdapter
        this.recyclerView.layoutManager = LinearLayoutManager(this)

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }
    }
}