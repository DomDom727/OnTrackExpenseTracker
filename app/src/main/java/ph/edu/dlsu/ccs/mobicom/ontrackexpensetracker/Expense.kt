package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker
import java.util.Date // For standard Date
import java.util.UUID

// import java.time.LocalDateTime

class Expense(
    var id: String,
    var name: String,
    var amount: Double,
    var category: String,
    var dateTime: Date,
    var photoPath: String?  // Nullable String to store the path/URI to the photo, if any
) {
    // Secondary constructor for convenience
    constructor(
        name: String,
        amount: Double,
        category: String,
        dateTime: Date,
        photoPath: String? = null
    ) : this(
        id = UUID.randomUUID().toString(),
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        photoPath = photoPath
    )

    override fun toString(): String {
        return "Expense(id='$id', name='$name', amount=$amount, category='$category', dateTime=$dateTime, photoPath=$photoPath)"
    }
}





