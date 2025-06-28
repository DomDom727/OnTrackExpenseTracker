package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DataHelper {
    companion object {
        private val categories = listOf("Food", "Transport", "Groceries", "Entertainment", "Bills", "Shopping", "Other")
        private val sampleNames = mapOf(
            "Food" to listOf("Coffee", "Lunch Meeting", "Dinner with Friends", "Breakfast Burrito", "Snacks"),
            "Transport" to listOf("Bus Fare", "Train Ticket", "Gas Fill-up", "Taxi Ride", "Parking Fee"),
            "Groceries" to listOf("Weekly Groceries", "Milk & Eggs", "Fruits and Vegetables", "Cleaning Supplies"),
            "Entertainment" to listOf("Movie Ticket", "Concert", "Book Purchase", "Streaming Subscription", "Video Game"),
            "Bills" to listOf("Rent Payment", "Electricity Bill", "Internet Bill", "Phone Bill", "Water Bill"),
            "Shopping" to listOf("New Shoes", "T-shirt", "Electronics", "Gift"),
            "Other" to listOf("Donation", "Pet Food", "Household Repair")
        )

        fun generateData(count: Int = 10): ArrayList<Expense> {
            val data = ArrayList<Expense>()
            val random = Random(System.currentTimeMillis())
            val now = System.currentTimeMillis()

            for (i in 1..count) {
                val randomCategory = categories.random(random)
                val randomNameOptions = sampleNames[randomCategory] ?: listOf("$i")
                val randomName = randomNameOptions.random(random)
                val randomAmount = String.format("%.2f", 1.0 + (150.0 - 1.0) * random.nextDouble()).toDouble()

                val randomDaysAgo = random.nextInt(30)
                val expenseDate = Date(now - TimeUnit.DAYS.toMillis(randomDaysAgo.toLong()))

                val hasPhoto = random.nextBoolean() && i % 3 == 0
                val photoPath = if (hasPhoto) "sample/photo_path_$i.jpg" else null

                data.add(
                    Expense(
                        name = randomName,
                        amount = randomAmount,
                        category = randomCategory,
                        dateTime = expenseDate,
                        photoPath = photoPath
                    )
                )
            }
            return data
        }
    }
}