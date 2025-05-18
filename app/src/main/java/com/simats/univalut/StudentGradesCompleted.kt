package com.simats.univalut

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class StudentGradesCompleted : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var courseType: String
    private lateinit var SID: String
    private lateinit var DID: String
    private var completedCourses: MutableList<CompletedCourse> = mutableListOf()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_grades_completed)

        // Initialize views
        listView = findViewById(R.id.coursesListView)

        // Retrieve passed data
        SID = intent.getStringExtra("SID") ?: ""
        DID = intent.getStringExtra("DID") ?: ""
        courseType = intent.getStringExtra("courseType") ?: ""

        // Fetch completed courses from the backend
        fetchCompletedCourses(SID, DID)
    }

    private fun fetchCompletedCourses(studentId: String, departmentId: String) {
        val url = "http://192.168.224.54/UniValut/student_grades_completed.php?student_id=$studentId&department_id=$departmentId"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@StudentGradesCompleted, "Error fetching data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody)

                    runOnUiThread {
                        if (jsonResponse.getBoolean("success")) {
                            val coursesArray: JSONArray = jsonResponse.getJSONArray("courses")

                            for (i in 0 until coursesArray.length()) {
                                val course = coursesArray.getJSONObject(i)

                                // Make sure you're extracting the "name" and "grade" fields correctly
                                val courseName = course.getString("name")
                                val grade = course.getString("grade")

                                // Adding to the completedCourses list
                                completedCourses.add(CompletedCourse(courseName, grade))
                                // String added, but list expects CompletedCourse

                            }

                            // Set up the adapter for ListView
                            val adapter = CompletedCoursesAdapter(this@StudentGradesCompleted, completedCourses)
                            listView.adapter = adapter
                        } else {
                            Toast.makeText(this@StudentGradesCompleted, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }


}
