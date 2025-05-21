package com.simats.univalut

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class FacultyProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var departmentTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var regNoTextView: TextView
    private lateinit var logoutButton: LinearLayout

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

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        departmentTextView = view.findViewById(R.id.departmentTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        regNoTextView = view.findViewById(R.id.regNoTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Set default placeholder (e.g., 'F' for Faculty)
        profileImageView.setImageDrawable(getLetterDrawable('F'))

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
        val url = "https://api-9buk.onrender.com/get_faculty_by_id.php?facultyId=$facultyId"

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val faculty = response.getJSONObject("data")

                        // Set the values to the TextViews
                        val facultyName = faculty.getString("name")
                        nameTextView.text = facultyName
                        statusTextView.text = "Faculty"
                        departmentTextView.text = faculty.optString("college", "Department not available")
                        emailTextView.text = faculty.optString("email", "Email not available")
                        phoneTextView.text = faculty.optString("phone_number", "Phone not available")
                        regNoTextView.text = "Staff ID: ${faculty.getString("login_id")}"

                        // Set profile image as first letter of name
                        if (facultyName.isNotEmpty()) {
                            val firstLetter = facultyName[0].uppercaseChar()
                            profileImageView.setImageDrawable(getLetterDrawable(firstLetter))
                        }
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

    // Function to create a circular drawable with the given letter
    private fun getLetterDrawable(letter: Char, size: Int = 120): BitmapDrawable {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw a colored circle
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = ContextCompat.getColor(requireContext(), R.color.teal_700) // Use your color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // Draw the letter
        paint.color = Color.WHITE
        paint.textSize = size * 0.5f
        paint.textAlign = Paint.Align.CENTER
        val fontMetrics = paint.fontMetrics
        val x = size / 2f
        val y = size / 2f - (fontMetrics.ascent + fontMetrics.descent) / 2
        canvas.drawText(letter.toString(), x, y, paint)

        return BitmapDrawable(resources, bitmap)
    }
}
