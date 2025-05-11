package com.simats.univalut

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.json.JSONException

class CourseMaterialsActivity : AppCompatActivity() {

    private lateinit var containerLayout: LinearLayout
    private var collegeName: String? = null
    private var courseCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.course_material_view)

        findViewById<ImageView>(R.id.backButton).setOnClickListener { finish() }

        collegeName = intent.getStringExtra("collegeName")
        courseCode = intent.getStringExtra("courseCode")

        findViewById<TextView>(R.id.textViewCourseTitle).text = courseCode

        containerLayout = findViewById(R.id.containerLayout)

        if (collegeName != null && courseCode != null) {
            fetchPDFs(collegeName!!, courseCode!!)
        }
    }


    override fun onResume() {
        super.onResume()
        findViewById<BottomNavigationView>(R.id.bottomNavigationView)?.selectedItemId = R.id.nav_courses
    }

    private fun fetchPDFs(college: String, course: String) {
        val url = "http://192.168.103.54/UniValut/list_pdfs.php?college=$college&course=$course"
        val request = JsonObjectRequest(url, null,
            { response ->
                try {
                    if (response.getBoolean("success")) {
                        val filesArray = response.getJSONArray("files")
                        for (i in 0 until filesArray.length()) {
                            val obj = filesArray.getJSONObject(i)
                            val name = obj.getString("name")
                            val url = obj.getString("url")
                            val date = obj.getString("date")
                            addPDFRow(name, url, date)
                        }
                    } else {
                        Toast.makeText(this, "No files found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(this).add(request)
    }

    private fun addPDFRow(name: String, url: String, date: String) {
        val inflater = layoutInflater
        val row = inflater.inflate(R.layout.item_pdf_row, containerLayout, false)

        row.findViewById<TextView>(R.id.fileName).text = name
        row.findViewById<TextView>(R.id.fileDetails).text = "PDF • $date"

        row.findViewById<Button>(R.id.viewButton).setOnClickListener {
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("pdf_url", url)  // Pass the PDF URL
            startActivity(intent)
        }


        containerLayout.addView(row)
    }
}
