package com.simats.univalut

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class AdminCoursesFragment : Fragment() {
    private var adminId: String? = null
    private var collegeName: String? = null

    private lateinit var btnAddCourse: Button
    private lateinit var layoutAddCourseForm: LinearLayout
    private lateinit var btnSaveCourse: Button
    private lateinit var coursesListLayout: LinearLayout // This will hold course cards

    private lateinit var etCourseCode: EditText
    private lateinit var etSubjectName: EditText
    private lateinit var etStrength: EditText
    private lateinit var spinnerFaculty: Spinner

    private var isFormVisible = false

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_courses, container, false)

        // Initialize views
        btnAddCourse = view.findViewById(R.id.btnAddCourse)
        layoutAddCourseForm = view.findViewById(R.id.layoutAddCourseForm)
        btnSaveCourse = view.findViewById(R.id.btnSaveCourse)
        coursesListLayout = view.findViewById(R.id.coursesListLayout) // Layout for course cards

        etCourseCode = view.findViewById(R.id.etCourseCode)
        etSubjectName = view.findViewById(R.id.etSubjectName)
        etStrength = view.findViewById(R.id.etStrength)
        spinnerFaculty = view.findViewById(R.id.spinnerFaculty)

        // Get admin ID from arguments
        adminId = arguments?.getString("adminId")

        // Fetch admin details
        adminId?.let { fetchAdminDetails(it) }

        // Toggle add course form
        btnAddCourse.setOnClickListener {
            isFormVisible = !isFormVisible
            layoutAddCourseForm.visibility = if (isFormVisible) View.VISIBLE else View.GONE
        }

        btnSaveCourse.setOnClickListener {
            val code = etCourseCode.text.toString()
            val subject = etSubjectName.text.toString()
            val faculty = spinnerFaculty.selectedItem.toString()
            val strength = etStrength.text.toString()

            if (code.isBlank() || subject.isBlank() || strength.isBlank() || faculty == "Select Faculty") {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val strengthInt: Int
            try {
                strengthInt = strength.toInt()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Strength must be a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val courseData = mapOf(
                "course_code" to code,        // String
                "subject_name" to subject,    // String
                "faculty" to faculty,         // String
                "strength" to strengthInt,    // Int
                "college" to collegeName      // String
            ) as Map<String, Any>  // Explicitly cast to Map<String, Any>


            // Save course logic
            saveCourse(courseData)
        }

        return view
    }

    private fun fetchAdminDetails(adminId: String) {
        val url = "http://192.168.103.54/UniValut/getAdminDetails.php?admin_id=$adminId"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val name = response.getString("name")
                    collegeName = response.getString("college")
                    Toast.makeText(requireContext(), "Welcome $name from $collegeName", Toast.LENGTH_SHORT).show()
                    loadCoursesForCollege(collegeName!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error fetching admin details", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show() })

        Volley.newRequestQueue(requireContext()).add(jsonObjectRequest)
    }

    private fun loadCoursesForCollege(college: String) {
        // Make sure the college name is properly URL-encoded
        val url = "http://192.168.103.54/UniValut/getCoursesByCollege.php?college=$college"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val coursesJsonArray = response.getJSONArray("courses")
                        populateCourseList(coursesJsonArray)
                    } else {
                        Toast.makeText(requireContext(), "No courses found for $college", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show() })

        Volley.newRequestQueue(requireContext()).add(request)
    }


    private fun populateCourseList(coursesJsonArray: JSONArray) {
        coursesListLayout.removeAllViews()  // Clear previous course cards

        for (i in 0 until coursesJsonArray.length()) {
            val course = coursesJsonArray.getJSONObject(i)
            val courseCardView = LayoutInflater.from(requireContext()).inflate(R.layout.course_card_item, coursesListLayout, false)

            val courseCodeTextView = courseCardView.findViewById<TextView>(R.id.tvCourseCode)
            val subjectNameTextView = courseCardView.findViewById<TextView>(R.id.tvSubjectName)
            val facultyNameTextView = courseCardView.findViewById<TextView>(R.id.tvFacultyName)

            courseCodeTextView.text = course.getString("course_code")
            subjectNameTextView.text = course.getString("subject_name")
            facultyNameTextView.text = course.getString("faculty_name")

            coursesListLayout.addView(courseCardView)
        }
    }

    private fun saveCourse(courseData: Map<String, Any>) {
        val url = "http://192.168.103.54/UniValut/addCourse.php"
        val jsonObject = JSONObject(courseData)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonObject,
            { response ->
                try {
                    if (response.getBoolean("success")) {
                        Toast.makeText(requireContext(), "Course added successfully", Toast.LENGTH_SHORT).show()
                        clearForm()  // Clear the form
                        loadCoursesForCollege(collegeName!!)  // Refresh course list
                        layoutAddCourseForm.visibility = View.GONE
                        isFormVisible = false
                    } else {
                        Toast.makeText(requireContext(), response.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error -> Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show() })

        Volley.newRequestQueue(requireContext()).add(request)
    }

    private fun clearForm() {
        etCourseCode.text.clear()
        etSubjectName.text.clear()
        etStrength.text.clear()
        spinnerFaculty.setSelection(0)
    }

    companion object {
        fun newInstance(adminId: String): AdminCoursesFragment {
            val fragment = AdminCoursesFragment()
            val args = Bundle()
            args.putString("adminId", adminId)
            fragment.arguments = args
            return fragment
        }
    }
}
