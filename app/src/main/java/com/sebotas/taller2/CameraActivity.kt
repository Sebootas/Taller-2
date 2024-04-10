package com.sebotas.taller2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraActivity : AppCompatActivity() {

    private lateinit var btn_camera: Button
    private lateinit var ivImage: ImageView
    private lateinit var galleryButton: Button

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1
        private const val PERMISSION_REQUEST_CAMERA = 2
        private const val PERMISSION_REQUEST_GALLERY = 3

        fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        btn_camera = findViewById(R.id.btn_camera)
        ivImage = findViewById(R.id.iv_image)
        galleryButton = findViewById(R.id.btnGallery)

        btn_camera.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, PERMISSION_REQUEST_CAMERA)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            startActivityForResult(intent, PERMISSION_REQUEST_GALLERY)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, PERMISSION_REQUEST_CAMERA)
            } else {
                Toast.makeText(this, "Oooops, access to camera denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PERMISSION_REQUEST_CAMERA) {
                val imageBitmap = data?.extras?.get("data") as? Bitmap
                // Resize the bitmap to 3000x4000
                val resizedBitmap = resizeBitmap(imageBitmap!!, 3000, 4000)
                ivImage.setImageBitmap(resizedBitmap)
                // Save the captured image to the gallery
                saveImageToGallery(resizedBitmap)
            } else if (requestCode == PERMISSION_REQUEST_GALLERY) {
                val selectedImageUri = data?.data
                ivImage.setImageURI(selectedImageUri)
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap?) {
        bitmap?.let {
            MediaStore.Images.Media.insertImage(
                contentResolver,
                it,
                "Image_${System.currentTimeMillis()}",
                "Image taken by Camera"
            )
            Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
}
