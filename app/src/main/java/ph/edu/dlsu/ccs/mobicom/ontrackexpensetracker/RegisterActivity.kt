package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RegisterActivity : AppCompatActivity() {
    // Declare Firebase Auth
    private lateinit var auth: FirebaseAuth

    // Declare UI elements
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginButton: Button

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Ensure this matches your XML file name

        // Initialize Firebase Auth instance
        auth = FirebaseAuth.getInstance()

        // Find the UI elements by their IDs from the layout
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        registerButton = findViewById(R.id.registerButton)
        loginButton = findViewById(R.id.loginButton)

        // Set an OnClickListener for the register button
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if(TextUtils.isEmpty(email)){
                Toast.makeText(this@RegisterActivity, "Enter email", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(this@RegisterActivity, "Enter password", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            // Check if the password is at least 6 characters long
            if (password.length < 6) {
                Toast.makeText(this@RegisterActivity, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(TextUtils.isEmpty(confirmPassword)){
                Toast.makeText(this@RegisterActivity, "Enter confirm password", Toast.LENGTH_SHORT).show();
                return@setOnClickListener;
            }

            if (password != confirmPassword) {
                Toast.makeText(this@RegisterActivity, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }
        // Set an OnClickListener for the login button
        loginButton.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    /**
     * This function attempts to register a user with Firebase Authentication.
     */
    private fun registerUser(email: String, password: String) {
        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this@RegisterActivity, "Account created.", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "createUserWithEmail:success")
                    // The user object is not used here, but can be useful for later actions.
                    val user: FirebaseUser? = auth.currentUser

                    // The user has a new variable 'loginIntent' so we must use this to start the activity.
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    // The 'finish()' function ensures the user cannot return to the register page with the back button.
                    finish()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    // Display the specific error message from the Firebase exception.
                    Toast.makeText(
                        baseContext,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            }
    }
}
