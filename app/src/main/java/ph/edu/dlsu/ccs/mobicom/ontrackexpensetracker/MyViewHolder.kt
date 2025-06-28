package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.view.View
import android.widget.TextView
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.intl.Locale
import androidx.recyclerview.widget.RecyclerView
import kotlin.text.format
import java.text.SimpleDateFormat

class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val transactionName: TextView = itemView.findViewById(R.id.transaction_name)
    private val transactionCategory: TextView = itemView.findViewById(R.id.transaction_category)
    private val transactionDateTime: TextView = itemView.findViewById(R.id.transaction_datetime)

    fun bindData(expense: Expense) {
        transactionName.text = expense.name
        transactionCategory.text = expense.amount.toString()
        transactionDateTime.text = expense.dateTime.toString()
    }
}