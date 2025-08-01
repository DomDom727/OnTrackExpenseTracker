package ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import ph.edu.dlsu.ccs.mobicom.ontrackexpensetracker.R

class TakePhotoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_takephoto)

        val backButton: Button = findViewById(R.id.back_btn)
        backButton.setOnClickListener {
            //finish()
            onBackPressedDispatcher.onBackPressed()
        }

        val addTransactionButton: Button = findViewById(R.id.takePhoto_btn)
        addTransactionButton.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}