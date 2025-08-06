package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.contract.ActivityResultContracts
import android.app.Activity
import android.widget.Button

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var editProfileRow: LinearLayout
    private lateinit var changePasswordRow: LinearLayout
    private lateinit var usernameTextView: TextView
    private lateinit var signOutText: TextView
    private lateinit var backButton: Button

    companion object {
        private const val TAG = "ProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize TextView
        usernameTextView = findViewById(R.id.usernameTextView)

        // Set up click listeners for the rows
        editProfileRow = findViewById(R.id.editProfileRow)
        editProfileRow.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Back Button
        backButton = findViewById(R.id.back_btn3)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        changePasswordRow = findViewById(R.id.changePasswordRow)
        changePasswordRow.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Set up click listener for Sign Out
        signOutText = findViewById(R.id.signOutText)
        signOutText.setOnClickListener {
            auth.signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // This function will be called whenever ProfileActivity comes to the foreground,
        // ensuring the username is always up-to-date.
        updateUserProfileUI()
    }

    /**
     * A helper function to load and display the user's profile information.
     */
    private fun updateUserProfileUI() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val displayName = currentUser.displayName
            if (displayName != null && displayName.isNotEmpty()) {
                usernameTextView.text = displayName
            } else {
                usernameTextView.text = currentUser.email
            }
        }
    }
}