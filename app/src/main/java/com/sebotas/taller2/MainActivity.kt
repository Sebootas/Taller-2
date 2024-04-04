package com.sebotas.taller2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {


    companion object {
        private const val CONTACTS_PERMISSION_CODE = 1
        private const val PERMISSION_REQUEST_CONTACTS = 2
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the ImageViews
        val button1 = findViewById<ImageView>(R.id.button1)
        val button2 = findViewById<ImageView>(R.id.button2)
        val button3 = findViewById<ImageView>(R.id.button3)

        // Set OnClickListener for each ImageView
        button1.setOnClickListener {
            // Check if permission is already granted
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    CONTACTS_PERMISSION_CODE
                )
            } else {
                // Permission already granted, proceed with the contacts activity
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
            }
        }

        button2.setOnClickListener {
            // Start a new activity when button2 is clicked
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener {
            // Start a new activity when button3 is clicked
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CONTACTS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start the contacts activity
                val intent = Intent(this, ContactsActivity::class.java)
                startActivity(intent)
            } else {
                // Permission denied, show a message asking the user to change the setting in the device settings
                showPermissionDeniedDialog()
            }
        }
    }

    // Function to show a dialog when permission is denied
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Denied")
            .setMessage("Without contacts permission, the app cannot function properly. Do you want to change this in settings?")
            .setPositiveButton("Settings") { _, _ ->
                // Open app settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri: Uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}



