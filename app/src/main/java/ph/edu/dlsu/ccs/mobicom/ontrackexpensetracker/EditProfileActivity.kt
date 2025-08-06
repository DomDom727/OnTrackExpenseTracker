package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var saveButton: Button
    private lateinit var backArrow: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize views from the layout
        usernameInput = findViewById(R.id.usernameInput)
        emailInput = findViewById(R.id.emailInput)
        saveButton = findViewById(R.id.saveButton)
        backArrow = findViewById(R.id.backArrow)

        // Load the current user data into the EditText fields
        loadUserProfile()

        // Set up click listeners
        saveButton.setOnClickListener {
            saveUserProfile()
        }

        backArrow.setOnClickListener {
            finish() // Close this activity and go back
        }
    }

    /**
     * Loads the current user's display name and email from Firebase Auth
     * and populates the EditText fields.
     */
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            // Populate the username field with the user's display name
            usernameInput.setText(user.displayName)
            // Populate the email field
            emailInput.setText(user.email)
        }
    }

    /**
     * Saves the new display name and/or email to the user's Firebase Auth profile.
     */
    private fun saveUserProfile() {
        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show()
            return
        }

        val newDisplayName = usernameInput.text.toString().trim()
        val newEmail = emailInput.text.toString().trim()

        val currentDisplayName = user.displayName
        val currentEmail = user.email

        val isDisplayNameChanged = newDisplayName != currentDisplayName && newDisplayName.isNotEmpty()
        val isEmailChanged = newEmail != currentEmail && newEmail.isNotEmpty()

        if (!isDisplayNameChanged && !isEmailChanged) {
            Toast.makeText(this, "No changes to save.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Handle both updates with separate flows to ensure both can be saved independently
        var profileUpdateCompleted = false
        var emailUpdateCompleted = false

        // Update display name if it has changed
        if (isDisplayNameChanged) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Display name updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to update display name.", Toast.LENGTH_SHORT).show()
                        Log.e("EditProfileActivity", "Failed to update display name", task.exception)
                    }
                    profileUpdateCompleted = true
                    checkCompletion(isDisplayNameChanged, isEmailChanged, profileUpdateCompleted, emailUpdateCompleted)
                }
        }

        // Update email if it has changed
        if (isEmailChanged) {
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        Toast.makeText(this, "Email update request sent. Please verify.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Failed to update email. Please re-authenticate.", Toast.LENGTH_LONG).show()
                        Log.e("EditProfileActivity", "Failed to update email", emailTask.exception)
                    }
                    emailUpdateCompleted = true
                    checkCompletion(isDisplayNameChanged, isEmailChanged, profileUpdateCompleted, emailUpdateCompleted)
                }
        }
    }

    private fun checkCompletion(
        isDisplayNameChanged: Boolean,
        isEmailChanged: Boolean,
        profileUpdateCompleted: Boolean,
        emailUpdateCompleted: Boolean
    ) {
        if ((!isDisplayNameChanged || profileUpdateCompleted) && (!isEmailChanged || emailUpdateCompleted)) {
            // All necessary updates have been attempted.
            finish()
        }
    }
}