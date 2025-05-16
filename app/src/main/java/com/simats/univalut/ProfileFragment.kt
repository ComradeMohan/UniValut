package com.simats.univalut

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var departmentTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var regNoTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var joinedDateTextView: TextView
    private lateinit var academicRecordsButton: LinearLayout
    private lateinit var helpSupportButton: LinearLayout
    private lateinit var settingsButton: LinearLayout
    private lateinit var logoutButton: LinearLayout


    private var studentID: String? = null
    private var department: String? = null
    private var collegeName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_student_profile, container, false)
    }

    companion object {
        fun newInstance(studentID: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString("studentID", studentID)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        statusTextView = view.findViewById(R.id.statusTextView)
        departmentTextView = view.findViewById(R.id.departmentTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        phoneTextView = view.findViewById(R.id.phoneTextView)
        regNoTextView = view.findViewById(R.id.regNoTextView)
        yearTextView = view.findViewById(R.id.yearTextView)
        academicRecordsButton = view.findViewById(R.id.academicRecordsButton)
        helpSupportButton = view.findViewById(R.id.helpSupportButton)
        settingsButton = view.findViewById(R.id.settingsButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        // Set data
        nameTextView.text = "Arun Kumar"
        statusTextView.text = "Student"
        departmentTextView.text = "Dept: "
        emailTextView.text = "arun.sse@saveetha.com"
        phoneTextView.text = "Dept: CSE"
        regNoTextView.text = "Reg No: 192XXXXX"
        yearTextView.text = "Year 3"

        val studentID = arguments?.getString("studentID")
        if (studentID != null) {
            fetchStudentData(studentID)
        } else {
            Toast.makeText(requireContext(), "Student ID not found", Toast.LENGTH_SHORT).show()
        }
        
        // Button click listeners
        academicRecordsButton.setOnClickListener {
            if (studentID != null && department != null && collegeName != null) {
                val intent = Intent(requireContext(), AcadmicRecordActivity::class.java).apply {
                    putExtra("studentID", studentID)
                    putExtra("department", department)
                    putExtra("collegeName", collegeName)
                }
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(), "Data is still loading...", Toast.LENGTH_SHORT).show()
            }
        }


        val helpSupportButton = view.findViewById<LinearLayout>(R.id.helpSupportButton)
        helpSupportButton.setOnClickListener {
            val changePasswordFragment = ChangePasswordFragment()
            val bundle = Bundle()
            bundle.putString("ID", studentID)  // Pass the faculty ID
            bundle.putString("userType", "student")  // Specify the user type
            changePasswordFragment.arguments = bundle

            // Navigate to Change Password Fragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, changePasswordFragment)
                .addToBackStack(null)
                .commit()
        }
        settingsButton.setOnClickListener {
            Toast.makeText(requireContext(), "Settings Clicked", Toast.LENGTH_SHORT).show()
        }

        logoutButton.setOnClickListener {
            Toast.makeText(requireContext(), "Logout Clicked", Toast.LENGTH_SHORT).show()
            val sharedPreferences = requireContext().getSharedPreferences("user_sf", Context.MODE_PRIVATE)
            sharedPreferences.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun fetchStudentData(studentNumber: String) {
        val client = OkHttpClient()
        val url = "http://192.168.103.54/UniValut/get_student.php?student_number=$studentNumber"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                activity?.runOnUiThread {
                    if (responseData != null) {
                        parseAndDisplayData(responseData)
                    }
                }
            }
        })
    }
    private fun parseAndDisplayData(jsonData: String) {
        try {
            val jsonObject = JSONObject(jsonData)
            if (jsonObject.getBoolean("success")) {
                val studentData = jsonObject.getJSONObject("data")

                // Update UI with actual data
                nameTextView.text = studentData.getString("full_name")
                regNoTextView.text = "Reg No: ${studentData.getString("student_number")}"
                phoneTextView.text = "Dept: ${studentData.getString("department")}"
                emailTextView.text = studentData.getString("email")
                yearTextView.text = "${studentData.getString("year_of_study")}"
                // college field can be added if available in your UI

                studentID = studentData.getString("student_number")
                department = studentData.getString("department")
                collegeName = studentData.getString("college")

            } else {
                Toast.makeText(requireContext(), "Student not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error parsing data", Toast.LENGTH_SHORT).show()
        }
    }
}
