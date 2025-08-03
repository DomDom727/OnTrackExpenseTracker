package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val data: List<Expense>) : RecyclerView.Adapter<MyViewHolder>() {
    companion object{
        const val nameKey = "name_key"
        const val categoryKey = "category_key"
        const val dateTimeKey = "dateTime_key"
        const val positionKey = "position_key"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_transaction, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentExpense = data[position]
        holder.bindData(currentExpense)
        //holder.itemView.tag = data[position]
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ViewTransactionActivity::class.java)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_ID, currentExpense.id)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_NAME, currentExpense.name)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_AMOUNT, currentExpense.amount)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_CATEGORY, currentExpense.category)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_DATE_TIME, currentExpense.dateTime)
            intent.putExtra(ViewTransactionActivity.EXTRA_EXPENSE_NOTES, currentExpense.notes)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}