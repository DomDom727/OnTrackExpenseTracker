package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var changePasswordRow: LinearLayout
    private lateinit var usernameTextView: TextView
    private lateinit var signOutText: TextView

    companion object {
        private const val TAG = "ProfileActivity" // Changed the TAG for clarity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Username Logic
        usernameTextView = findViewById(R.id.usernameTextView)

        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Check if the user has a display name, otherwise use the email
            val displayName = currentUser.displayName
            if (displayName != null && displayName.isNotEmpty()) {
                usernameTextView.text = displayName
            } else {
                // Fallback to displaying the user's email if no display name is set
                usernameTextView.text = currentUser.email
            }
        }

        // Change Password Logic
        changePasswordRow = findViewById(R.id.changePasswordRow)

        // Sign Out Logic
        signOutText = findViewById(R.id.signOutText)

        signOutText.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

    }
}