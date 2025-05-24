package com.simats.univalut

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class FacultyProfileFragment : Fragment() {

    private lateinit var nameTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var departmentTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var regNoTextView: TextView

    private lateinit var logoutButton: LinearLayout
    private lateinit var settingsButton: LinearLayout

    private var facultyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        facultyId = arguments?.getString("ID")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faculty_profile, container, false)

<<<<<<< HEAD
=======
        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        departmentTextView = view.findViewById(R.id.departmentTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        regNoTextView = view.findViewById(R.id.regNoTextView)
        logoutButton = view.findViewById(R.id.logoutButton)
        settingsButton = view.findViewById(R.id.settingsButton)

        // Set default placeholder (e.g., 'F' for Faculty)
        profileImageView.setImageDrawable(getLetterDrawable('F'))

>>>>>>> 6d2b464 (grades pages)
        val helpSupportButton = view.findViewById<LinearLayout>(R.id.helpSupportButton)
        helpSupportButton.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            val bundle = Bundle()
            bundle.putString("ID", facultyId)  // Pass the faculty ID
            bundle.putString("userType", "faculty")  // Specify the user type
            changePasswordFragment.arguments = bundle

            // Navigate to Change Password Fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, changePasswordFragment)
                .addToBackStack(null)
                .commit()
        }
        //changes made in online mode
        settingsButton.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_feedback, null)
            val etFeedback = dialogView.findViewById<EditText>(R.id.etFeedback)
            val tvUserId = dialogView.findViewById<TextView>(R.id.tvUserId)

<<<<<<< HEAD

        // Initialize views
        nameTextView = view.findViewById(R.id.nameTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        departmentTextView = view.findViewById(R.id.departmentTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        regNoTextView = view.findViewById(R.id.regNoTextView)

        logoutButton = view.findViewById(R.id.logoutButton)

=======
            // Set user ID
            tvUserId.text = "User ID: ${facultyId ?: "Unknown"}"

            val dialog = android.app.AlertDialog.Builder(requireContext())
                .setTitle("Submit Feedback")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val feedback = etFeedback.text.toString().trim()
                    val userId = facultyId ?: ""

                    if (feedback.isEmpty()) {
                        Toast.makeText(requireContext(), "Feedback cannot be empty", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Send feedback using OkHttp
                    val client = OkHttpClient()
                    val requestBody = FormBody.Builder()
                        .add("user_id", userId)
                        .add("feedback", feedback)
                        .build()

                    val request = okhttp3.Request.Builder()
                        .url("https://api-9buk.onrender.com/submit_feedback.php") // Replace with your actual URL
                        .post(requestBody)
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Failed to submit feedback", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            val responseBody = response.body?.string()
                            activity?.runOnUiThread {
                                if (response.isSuccessful && responseBody?.contains("success") == true) {
                                    Toast.makeText(requireContext(), "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Server error: $responseBody", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    })
                }
                .setNegativeButton("Cancel", null)
                .create()

            dialog.show()
        }
>>>>>>> 6d2b464 (grades pages)
        logoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Logout Clicked", Toast.LENGTH_SHORT).show()
            val sharedPreferences = requireContext().getSharedPreferences("user_sf", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        facultyId?.let {
            fetchFacultyDetails(it)
        }

        return view
    }

    private fun fetchFacultyDetails(facultyId: String) {
        val url = "http://192.168.103.54/UniValut/get_faculty_by_id.php?facultyId=$facultyId"

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Check if the response contains success as true
                    val success = response.getBoolean("success")
                    if (success) {
                        val faculty = response.getJSONObject("data")

                        // Set the values to the TextViews
                        nameTextView.text = faculty.getString("name")
                        statusTextView.text = "Faculty"
                        departmentTextView.text = faculty.optString("college", "Department not available")  // Use 'college' field or fallback
                        emailTextView.text = faculty.optString("email", "Email not available")
                        phoneTextView.text = faculty.optString("phone_number", "Phone not available")
                        regNoTextView.text = "Staff ID: ${faculty.getString("login_id")}"

                    } else {
                        Toast.makeText(requireContext(), "Faculty not found", Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

}
