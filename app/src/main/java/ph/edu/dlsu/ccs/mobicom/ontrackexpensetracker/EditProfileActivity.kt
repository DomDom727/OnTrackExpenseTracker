package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
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

    override fun onResume() {
        super.onResume()
        reloadAndLoadUserProfile()
    }

    private fun reloadAndLoadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val latestUser = auth.currentUser
                    usernameInput.setText(latestUser?.displayName)
                    emailInput.setText(latestUser?.email)
                } else {
                    Log.e("EditProfileActivity", "Failed to reload user profile.", task.exception)
                    loadUserProfile()
                }
            }
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            usernameInput.setText(user.displayName)
            emailInput.setText(user.email)
        }
    }

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

        var tasksToComplete = 0
        var tasksCompleted = 0
        var allTasksSuccessful = true

        val onTaskComplete = { success: Boolean ->
            tasksCompleted++
            if (!success) {
                allTasksSuccessful = false
            }

            if (tasksCompleted == tasksToComplete) {
                if (allTasksSuccessful) {
                    // Check if an email change was part of the updates
                    if (isEmailChanged) {
                        // Sign out the user and go to the login screen
                        auth.signOut()
                        Toast.makeText(this, "Profile updated. Please log in again with your new email.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        // Only display name was changed, just finish the activity
                        Toast.makeText(this, "Display name updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    // One or more tasks failed, just finish the activity
                    finish()
                }
            }
        }

        if (isDisplayNameChanged) {
            tasksToComplete++
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newDisplayName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onTaskComplete(true)
                    } else {
                        Log.e("EditProfileActivity", "Failed to update display name", task.exception)
                        onTaskComplete(false)
                    }
                }
        }

        if (isEmailChanged) {
            tasksToComplete++
            user.verifyBeforeUpdateEmail(newEmail)
                .addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        // The onTaskComplete lambda will now handle the sign-out logic
                        onTaskComplete(true)
                    } else {
                        Toast.makeText(this, "Failed to send verification email. Please re-authenticate.", Toast.LENGTH_LONG).show()
                        Log.e("EditProfileActivity", "Failed to update email", emailTask.exception)
                        onTaskComplete(false)
                    }
                }
        }

        if (tasksToComplete == 0) {
            finish()
        }
    }
}