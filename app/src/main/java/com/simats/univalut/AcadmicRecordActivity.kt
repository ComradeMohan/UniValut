package com.simats.univalut

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class AcadmicRecordActivity : AppCompatActivity() {

    private lateinit var progressS: ProgressBar
    private lateinit var progressA: ProgressBar
    private lateinit var progressB: ProgressBar
    private lateinit var progressC: ProgressBar
    private lateinit var progressD: ProgressBar
    private lateinit var progressE: ProgressBar
    private lateinit var downloadButton: Button
    private lateinit var backButton: ImageView

    private lateinit var pending: ConstraintLayout
    private lateinit var completed: ConstraintLayout

    private var SID:String? = null
    private var DID:String? = null


    private val client = OkHttpClient()
    private var collegeId: String? = null
    private var departmentName: String? = null// To store fetched college ID

    private val courseNames = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.academic_record)

        val studentID = intent.getStringExtra("studentID")
        val department = intent.getStringExtra("department")
        val collegeName = intent.getStringExtra("collegeName")

        SID = studentID
        departmentName = department
        // Initialize views
        progressS = findViewById(R.id.progressS)
        progressA = findViewById(R.id.progressA)
        progressB = findViewById(R.id.progressB)
        progressC = findViewById(R.id.progressC)
        progressD = findViewById(R.id.progressD)
        progressE = findViewById(R.id.progressE)
        downloadButton = findViewById(R.id.downloadTranscriptButton)
        backButton = findViewById(R.id.backButton)

        // Set dummy progress values
        progressS.progress = 45
        progressA.progress = 30
        progressB.progress = 45
        progressC.progress = 15
        progressD.progress = 20
        progressE.progress = 10


        pending = findViewById(R.id.pendingCourses) // make sure it's initialized!

        pending.setOnClickListener {
            if (!SID.isNullOrEmpty() && !DID.isNullOrEmpty()) {
                Toast.makeText(this@AcadmicRecordActivity, "Pending Clicked", Toast.LENGTH_SHORT).show()
                // TODO: Trigger API call to fetch pending courses here
            } else {
                Toast.makeText(this@AcadmicRecordActivity, "Missing Student ID or Department ID", Toast.LENGTH_SHORT).show()
            }
        }

        completed = findViewById(R.id.completedCourses) // make sure it's initialized!

        completed.setOnClickListener {
            if (!SID.isNullOrEmpty() && !DID.isNullOrEmpty()) {
                Toast.makeText(this@AcadmicRecordActivity, "Completed Clicked", Toast.LENGTH_SHORT).show()
                // TODO: Trigger API call to fetch pending courses here
            } else {
                Toast.makeText(this@AcadmicRecordActivity, "Missing Student ID or Department ID", Toast.LENGTH_SHORT).show()
            }
        }


        // Handle button clicks
        downloadButton.setOnClickListener {
            // TODO: Implement download logic
        }

        backButton.setOnClickListener {
            finish()
        }



        // Fetch the college ID
        collegeName?.let {
            fetchCollegeId(it)
        }
    }

    private fun fetchCollegeId(collegeName: String) {
        val url = "http://192.168.103.54/UniValut/get_college_id.php"

        val formBody = FormBody.Builder()
            .add("college_name", collegeName)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AcadmicRecordActivity, "Failed to fetch college ID", Toast.LENGTH_SHORT).show()
                    Log.e("CollegeID", "Network Error: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val json = JSONObject(responseBody)
                    if (json.getBoolean("success")) {
                        collegeId = json.getString("college_id")
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, "College ID: $collegeId", Toast.LENGTH_SHORT).show()

                            departmentName?.let {
                                fetchDepartmentId(collegeId!!, it)
                            }
                        }
                    }
                    else {
                        val message = json.optString("message", "College not found")
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AcadmicRecordActivity, "Invalid response format", Toast.LENGTH_SHORT).show()
                        Log.e("CollegeID", "JSON Parsing Error: ${e.message}", e)
                    }
                }
            }
        })
    }

    private fun fetchDepartmentId(collegeId: String, departmentName: String) {
        val url = "http://192.168.103.54/UniValut/get_department_id.php"

        val formBody = FormBody.Builder()
            .add("college_id", collegeId)
            .add("name", departmentName)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AcadmicRecordActivity, "Failed to fetch department ID", Toast.LENGTH_SHORT).show()
                    Log.e("DepartmentID", "Network Error: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val json = JSONObject(responseBody)
                    if (json.getBoolean("success")) {
                        val departmentId = json.getString("department_id")
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, "Department ID: $departmentId", Toast.LENGTH_SHORT).show()
                            DID = departmentId
                            fetchCourses(departmentId) // Call to load course names
                        }
                    }
                    else {
                        val message = json.optString("message", "Department not found")
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AcadmicRecordActivity, "Invalid department response", Toast.LENGTH_SHORT).show()
                        Log.e("DepartmentID", "JSON Error: ${e.message}", e)
                    }
                }
            }
        })
    }

    private fun fetchCourses(departmentId: String) {
        val url = "http://192.168.103.54/UniValut/get_courses_by_department.php"

        val formBody = FormBody.Builder()
            .add("department_id", departmentId)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@AcadmicRecordActivity, "Failed to fetch courses", Toast.LENGTH_SHORT).show()
                    Log.e("Courses", "Network Error: ${e.message}", e)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val json = JSONObject(responseBody)
                    if (json.getBoolean("success")) {
                        val coursesArray = json.getJSONArray("courses")
                        courseNames.clear()
                        for (i in 0 until coursesArray.length()) {
                            val courseObj = coursesArray.getJSONObject(i)
                            val courseName = courseObj.getString("name").trim()
                            courseNames.add(courseName)
                        }
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, "Courses loaded: ${courseNames.size}", Toast.LENGTH_SHORT).show()
                            Log.d("Courses", courseNames.joinToString(", "))
                        }
                    } else {
                        val message = json.optString("message", "No courses found")
                        runOnUiThread {
                            Toast.makeText(this@AcadmicRecordActivity, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@AcadmicRecordActivity, "Error parsing courses", Toast.LENGTH_SHORT).show()
                        Log.e("Courses", "JSON Error: ${e.message}", e)
                    }
                }
            }
        })
    }


}
