package com.simats.univalut

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.Request
import com.android.volley.toolbox.Volley


class FacultyUploadMaterial : AppCompatActivity() {

    // Request code for file selection
    private val FILE_REQUEST_CODE = 1001
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upload_material)

        // Get the course code and college name from the Intent
        val courseCode = intent.getStringExtra("COURSE_CODE")
        val collegeName = intent.getStringExtra("COLLEGE_NAME")

        // Set the retrieved course code in the TextView
        val courseCodeTextView: TextView = findViewById(R.id.course_code)
        courseCodeTextView.text = courseCode ?: "No Course Selected"

        // Back button handling
        val backButton: ImageView = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()  // Go back to the previous screen
        }

        // Resource Type selection
        val tvSelectType: TextView = findViewById(R.id.tvSelectType)
        tvSelectType.setOnClickListener {
            // Display a dialog for resource type selection
            val items = arrayOf("Lecture Notes", "Assignments", "PDF Resources")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select Resource Type")
            builder.setItems(items) { dialog, which ->
                tvSelectType.text = items[which]  // Set selected type in TextView
            }
            builder.show()
        }

        // Upload area click handling (file selection)
        val uploadArea: LinearLayout = findViewById(R.id.uploadArea)
        uploadArea.setOnClickListener {
            openFilePicker()  // Open file picker when the upload area is clicked
        }

        // Submit button functionality
        val submitButton: Button = findViewById(R.id.btnSubmitResources)
        submitButton.setOnClickListener {
            val selectedType = tvSelectType.text.toString()
            if (selectedType == "Select type") {
                // If no type is selected, prompt the user to select one
                showError("Please select a resource type.")
            } else {
                if (selectedFileUri != null) {
                    // Proceed with file upload if a file is selected
                    uploadFile(selectedType)
                } else {
                    // Show error if no file is selected
                    showError("Please select a file to upload.")
                }
            }
        }
    }

    // Open file picker to allow the user to select a file
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"  // This will allow selection of any file type
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    // Handle result of file selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == FILE_REQUEST_CODE) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                val fileName = getFileName(uri)

                // Update the TextView with the selected file name
                val selectedFileTextView: TextView = findViewById(R.id.tvSelectedFile)
                selectedFileTextView.text = fileName ?: "Unknown file"

                Toast.makeText(this, "Selected file: $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Get the file name from the URI (you can also check for file size and mime type here)
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst()) {
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    // Logic to handle the file upload (to be extended with your backend code)
    private fun uploadFile(resourceType: String) {
        val collegeName = intent.getStringExtra("COLLEGE_NAME") ?: return
        val courseCode = intent.getStringExtra("COURSE_CODE") ?: return
        val fileUri = selectedFileUri ?: return

        val fileName = getFileName(fileUri) ?: "file_${System.currentTimeMillis()}"
        val inputStream = contentResolver.openInputStream(fileUri)
        val fileData = inputStream?.readBytes() ?: return

        val url = "http://192.168.224.54/UniValut/upload_material.php"

        val request = VolleyFileUpload(
            Request.Method.POST, url,
            Response.Listener {
                showSuccess("Uploaded to $collegeName/$courseCode/$fileName")
            },
            Response.ErrorListener {
                showError("Upload failed: ${it.message}")
            }
        )

        request.setParams(
            mapOf(
                "college" to collegeName,
                "course" to courseCode,
                "type" to resourceType
            )
        )

        request.setFile(fileData, fileName)

        Volley.newRequestQueue(this).add(request)
    }


    private fun showError(message: String) {
        // Display error message
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSuccess(message: String) {
        // Display success message
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
