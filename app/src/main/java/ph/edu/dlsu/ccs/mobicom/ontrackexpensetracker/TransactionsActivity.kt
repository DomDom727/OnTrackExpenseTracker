package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.databinding.ActivityTransactionsBinding

class TransactionsActivity : AppCompatActivity() {
    private lateinit var data: ArrayList<Expense>         // Holds the data for the app
    private lateinit var myAdapter: MyAdapter               // Holds the adapter for the RecyclerView
    private lateinit var recyclerView: RecyclerView

    private lateinit var expenseRepository: ExpenseRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)

        expenseRepository = ExpenseRepository()
        this.data = ArrayList()
        this.myAdapter = MyAdapter(this.data)
        this.recyclerView = findViewById(R.id.recyclerView)
        this.recyclerView.adapter = this.myAdapter
        this.recyclerView.layoutManager = LinearLayoutManager(this)

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }

        loadExpenses()
    }

    private fun loadExpenses() {
        lifecycleScope.launch {
            try {
                val fetchedExpenses = expenseRepository.getExpenses()

                data.clear()
                data.addAll(fetchedExpenses)

                if (data.isEmpty()) {
                    //
                } else {
                    myAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("TransactionsActivity", "Error loading expenses", e)
                Toast.makeText(this@TransactionsActivity, "Error loading expenses", Toast.LENGTH_SHORT).show()
            }
        }
    }
}