package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Declare UI elements
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var confirmChangeButton: Button
    private lateinit var backButton: Button

    // Declare toggle ImageButtons
    private lateinit var currentPasswordToggle: ImageButton
    private lateinit var newPasswordToggle: ImageButton
    private lateinit var confirmPassToggle: ImageButton

    // State variables to track visibility for each field
    private var isCurrentPasswordVisible: Boolean = false
    private var isNewPasswordVisible: Boolean = false
    private var isConfirmPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_change_password)

        auth = FirebaseAuth.getInstance()

        // Initialize UI elements
        currentPasswordEditText = findViewById(R.id.editTextCurrentPassword)
        newPasswordEditText = findViewById(R.id.editTextNewPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPass)
        confirmChangeButton = findViewById(R.id.buttonConfirmChange)
        backButton = findViewById(R.id.back_btn2)

        // Initialize toggle ImageButtons
        currentPasswordToggle = findViewById(R.id.currentPasswordToggle)
        newPasswordToggle = findViewById(R.id.newPasswordToggle)
        confirmPassToggle = findViewById(R.id.confirmPassToggle)

        // Back Button
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // --- Logic for the Password Toggles ---

        // Listener for Current Password toggle
        currentPasswordToggle.setOnClickListener {
            isCurrentPasswordVisible = !isCurrentPasswordVisible
            if (isCurrentPasswordVisible) {
                // Show password
                currentPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                currentPasswordToggle.setImageResource(R.drawable.visibility_24)
            } else {
                // Hide password
                currentPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                currentPasswordToggle.setImageResource(R.drawable.visibility_off_24)
            }
            currentPasswordEditText.setSelection(currentPasswordEditText.text.length)
        }

        // Listener for New Password toggle
        newPasswordToggle.setOnClickListener {
            isNewPasswordVisible = !isNewPasswordVisible
            if (isNewPasswordVisible) {
                // Show password
                newPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                newPasswordToggle.setImageResource(R.drawable.visibility_24)
            } else {
                // Hide password
                newPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                newPasswordToggle.setImageResource(R.drawable.visibility_off_24)
            }
            newPasswordEditText.setSelection(newPasswordEditText.text.length)
        }

        // Listener for Confirm Password toggle
        confirmPassToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                // Show password
                confirmPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                confirmPassToggle.setImageResource(R.drawable.visibility_24)
            } else {
                // Hide password
                confirmPasswordEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                confirmPassToggle.setImageResource(R.drawable.visibility_off_24)
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        // --- Placeholder for Password Change Logic ---

        confirmChangeButton.setOnClickListener {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentPass = currentPasswordEditText.text.toString().trim()
            val newPass = newPasswordEditText.text.toString().trim()
            val confirmPass = confirmPasswordEditText.text.toString().trim()

            // 1. Basic validation
            if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass != confirmPass) {
                Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // 2. Re-authenticate the user with their current password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPass)

            user.reauthenticateAndRetrieveData(credential)
                .addOnCompleteListener { reauthorize ->
                    if (reauthorize.isSuccessful) {
                        // Re-authentication was successful, now update the password
                        user.updatePassword(newPass)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // Password updated successfully
                                    Toast.makeText(
                                        this,
                                        "Password updated successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish() // Close the activity
                                } else {
                                    // Failed to update password
                                    Toast.makeText(
                                        this,
                                        "Failed to update password: ${updateTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                    } else {
                        // Re-authentication failed
                        Toast.makeText(this, "Incorrect current password.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
        }
    }
}