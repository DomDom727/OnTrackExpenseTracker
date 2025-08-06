package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    // Declare a lateinit variable for Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Declare UI elements for the login page
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var passwordToggle: ImageButton

    private var isPasswordVisible: Boolean = false

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Find the UI elements
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        passwordToggle = findViewById(R.id.passwordToggle)

        // Set an OnClickListener for the login button
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Basic input validation
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@LoginActivity, "Please enter your email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this@LoginActivity, "Please enter your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Attempt to log in the user
            loginUser(email, password)
        }

        // Set an OnClickListener for the register button to navigate to the RegisterActivity
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        forgotPasswordButton.setOnClickListener {
            // Get the email from the emailEditText field.
            val email = emailEditText.text.toString().trim()

            // We could add another view here for just changing the password

            // Check if the email field is empty.
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this@LoginActivity, "Please enter your email to reset password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call the Firebase method to send a password reset email.
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email was sent successfully.
                        Toast.makeText(this@LoginActivity, "Password reset email sent to $email", Toast.LENGTH_LONG).show()
                    } else {
                        // Handle any errors that occur.
                        Toast.makeText(this@LoginActivity, "Failed to send reset email: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Logic for the password toggle
        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // Toggle the state
            if (isPasswordVisible) {
                // Show password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility_24) // Change icon to 'visible'
            } else {
                // Hide password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                passwordToggle.setImageResource(R.drawable.visibility_off_24) // Change icon to 'hidden'
            }
            // Move cursor to the end of the text
            passwordEditText.setSelection(passwordEditText.text.length)
        }
    }

    /**
     * This function attempts to log in a user with Firebase Authentication.
     */
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login was successful.
                    Log.d(TAG, "signInWithEmail:success")
                    Toast.makeText(this@LoginActivity, "Login successful.", Toast.LENGTH_SHORT).show()

                    // Navigate to the MainActivity
                    val mainIntent = Intent(this, MainActivity::class.java)
                    startActivity(mainIntent)
                    finish() // Prevent the user from returning to the login screen
                } else {
                    // Login failed.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
}
