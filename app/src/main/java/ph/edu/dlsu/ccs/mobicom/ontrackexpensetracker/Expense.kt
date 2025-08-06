package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker
import java.util.Date // For standard Date
import java.util.UUID
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// import java.time.LocalDateTime

class Expense(
    @DocumentId var id: String? = null,
    var userId: String? = null,
    var name: String,
    var amount: Double,
    var category: String,
    var dateTime: String,
    var notes: String,
    @ServerTimestamp var createdAt: Date? = null
) {
    // no-arguement Constructor for Firestore
    constructor() : this(null, null, "", 0.0, "", "", "")

    // Secondary constructor for convenience
    constructor(
        name: String,
        amount: Double,
        category: String,
        dateTime: String
    ) : this(
        id = null,
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        notes = "",
        createdAt = null
    )

    constructor(
        userId: String,
        name: String,
        amount: Double,
        category: String,
        dateTime: String
    ) : this(
        id = null,
        userId = userId,
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        notes = "",
        createdAt =null
    )

    constructor(
        userId: String,
        name: String,
        amount: Double,
        category: String,
        dateTime: String,
        notes: String
    ) : this(
        id = null,
        userId = userId,
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        notes = notes,
        createdAt =null
    )

    constructor(
        id: String,
        userId: String,
        name: String,
        amount: Double,
        category: String,
        dateTime: String,
        notes: String
    ) : this(
        id = id,
        userId = userId,
        name = name,
        amount = amount,
        category = category,
        dateTime = dateTime,
        notes = notes,
        createdAt =null
    )

    override fun toString(): String {
        return "Expense(id=$id, userId=$userId, name='$name', amount=$amount, category='$category', dateTime='$dateTime', createdAt=$createdAt)"
    }
}





