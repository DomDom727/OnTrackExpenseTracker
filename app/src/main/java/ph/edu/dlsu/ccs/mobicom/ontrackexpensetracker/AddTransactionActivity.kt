package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.text.toDoubleOrNull
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.util.Date
import android.Manifest
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddTransactionActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, AdapterView.OnItemSelectedListener {

    private lateinit var nameEditText: EditText
    private lateinit var amountEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var categoryString: String
    private lateinit var scanButton: Button
    private lateinit var notesEditText: EditText

    private lateinit var expenseRepository: ExpenseRepository

    private val calendar = Calendar.getInstance()
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private lateinit var categories: Array<String>

    private var currentPhotoPath: String? = null
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var resultingText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        // expenseDatabase = ExpenseDatabase(applicationContext)
        expenseRepository = ExpenseRepository()

        nameEditText = findViewById(R.id.editTextName)
        amountEditText = findViewById(R.id.editTextAmount)
        //categoryEditText = findViewById(R.id.editTextCategory)
        dateEditText = findViewById(R.id.editTextDate)
        scanButton = findViewById(R.id.scan_btn)
        notesEditText = findViewById(R.id.editTextTextMultiLine2)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->
            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            success ->
            if (success) {
                currentPhotoPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    recognizeText(bitmap)
                }
            }
        }

        categories = resources.getStringArray(R.array.categories)

        val spinner = findViewById<Spinner>(R.id.spinnerCategory)
        spinner.onItemSelectedListener = this


        val spinnerAdapter = ArrayAdapter<Any?>(this,
            android.R.layout.simple_spinner_dropdown_item, categories)

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }

        val addButton: Button = findViewById(R.id.add_btn)
        addButton.setOnClickListener {
            addTransactionAndProceed()
        }

        scanButton.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        findViewById<EditText>(R.id.editTextDate).setOnClickListener {
            android.app.DatePickerDialog(this,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()

        }

        if (intent.getBooleanExtra(MainActivity.EXTRA_TRIGGER_SCAN, false)) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun captureImage() {
        val photoFile: File? = try {
            createImageFile()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
            null
        }
        photoFile?.also {
            val PhotoUri: Uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePictureLauncher.launch(PhotoUri)
        }
    }

    private fun recognizeText(bitmap: Bitmap){
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener { ocrText ->
            resultingText = ocrText.text
            notesEditText.setText(resultingText)
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error recognizing text: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        categoryString = categories[position]
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        Log.e("Calender", "$year/$month/$dayOfMonth")
        calendar.set(year, month, dayOfMonth)
        setDateTime(calendar.timeInMillis)
    }

    private fun setDateTime(timestamp: Long) {
        findViewById<EditText>(R.id.editTextDate).setText(formatter.format(timestamp))
    }

    private fun addTransactionAndProceed() {
        val name = nameEditText.text.toString().trim()
        val amountStr = amountEditText.text.toString().trim()
        val category = categoryString
        val date = dateEditText.text.toString().trim()
        var notes = notesEditText.text.toString().trim()


        // Input Validation
        if (name.isEmpty()) {
            nameEditText.error = "Name cannot be empty"
            nameEditText.requestFocus()
            return
        }
        if (amountStr.isEmpty()) {
            amountEditText.error = "Amount cannot be empty"
            amountEditText.requestFocus()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            amountEditText.error = "Enter a valid positive amount"
            amountEditText.requestFocus()
            return
        }
        if (category.isEmpty()) {
            // Assuming category can be anything, but not empty if required
            categoryEditText.error = "Category cannot be empty"
            categoryEditText.requestFocus()
            return
        }
        if (date.isEmpty()) {
            // This might not be strictly necessary if date picker always sets a date
            dateEditText.error = "Date cannot be empty"
            return
        }
        if (notes.isEmpty()) {
            notes = ""
        }

        val currentFirebaseUser = FirebaseAuth.getInstance().currentUser
        if (currentFirebaseUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        val userId = currentFirebaseUser.uid

        val expense = Expense(
            userId = userId,
            name = name,
            amount = amount,
            category = category,
            dateTime = date,
            notes = notes
        )

        lifecycleScope.launch {
            val expenseId = expenseRepository.addExpense(expense)

            if (expenseId != null) {
                Toast.makeText(this@AddTransactionActivity, "Expense added successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@AddTransactionActivity, ViewTransactionActivity::class.java).apply {
                    // Pass data that ViewTransactionActivity expects.
                    putExtra(ViewTransactionActivity.EXTRA_EXPENSE_NAME, name)
                    putExtra(ViewTransactionActivity.EXTRA_EXPENSE_AMOUNT, amount)
                    putExtra(ViewTransactionActivity.EXTRA_EXPENSE_CATEGORY, category)
                    putExtra(ViewTransactionActivity.EXTRA_EXPENSE_DATE_TIME, date)
                    putExtra(ViewTransactionActivity.EXTRA_EXPENSE_NOTES, notes)

                }
                startActivity(intent)
                finish() // Finish AddTransactionActivity
            } else {
                Toast.makeText(this@AddTransactionActivity, "Failed to add expense", Toast.LENGTH_SHORT).show()
            }
        }

    }


}