package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker
import java.util.Date // For standard Date
import java.util.UUID

// import java.time.LocalDateTime

class ExpenseOld(
    var id: Int,
    var name: String,
    var amount: Double,
    var category: String,
    var dateTime: String
    // var photoPath: String?  // Nullable String to store the path/URI to the photo, if any
) {
    companion object {
        private const val DEFAULT_ID = -1
    }
    // Secondary constructor for convenience
    constructor(
        name: String,
        amount: Double,
        category: String,
        dateTime: String
    ) : this(
        id = DEFAULT_ID,
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        // photoPath = null
    )

    override fun toString(): String {
        return "Expense(id='$id', name='$name', amount=$amount, category='$category', dateTime=$dateTime)"
    }
}





