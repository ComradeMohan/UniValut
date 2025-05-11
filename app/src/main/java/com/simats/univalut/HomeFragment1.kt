package com.simats.univalut

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import java.util.Calendar

class HomeFragment1 : Fragment() {

    private lateinit var tvGreeting: TextView
    private lateinit var tvStudentName: TextView
    private lateinit var etSearch: EditText
    private lateinit var courseCode1: TextView
    private lateinit var courseCode2: TextView

    private lateinit var tvNoticeTitle: TextView
    private lateinit var tvNoticeDescription: TextView

    private var collegeName: String? = null
    private var studentID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        studentID = arguments?.getString("studentID") // Get student ID from arguments
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home1, container, false)

        // Initialize Views
        tvGreeting = view.findViewById(R.id.tvGreeting)
        tvStudentName = view.findViewById(R.id.tvStudentName)
        etSearch = view.findViewById(R.id.etSearch)
        courseCode1 = view.findViewById(R.id.tvCourseCode1)
        courseCode2 = view.findViewById(R.id.tvCourseCode2)
        tvNoticeTitle = view.findViewById(R.id.tvNoticeTitle)
        tvNoticeDescription = view.findViewById(R.id.tvNoticeDescription)

        val notificationIcon: View = view.findViewById(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            val intent = Intent(requireContext(), StudentNotificationsActivity::class.java)
            intent.putExtra("college", collegeName) // Pass college name to the next activity
            startActivity(intent)
        }


        tvGreeting.text = getGreetingMessage()

        // Fetch student name and college
        studentID?.let {
            fetchStudentName(it)
        } ?: run {
            tvStudentName.text = "ID not found"
        }

        return view
    }

    private fun fetchStudentName(studentID: String) {
        val url = "http://192.168.103.54/UniValut/fetch_student_name.php?studentID=$studentID"
        val queue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                val success = response.getBoolean("success")
                if (success) {
                    val name = response.getString("name")
                    collegeName = response.getString("college")

                    tvStudentName.text = name

                    // Fetch notice after getting college
                    collegeName?.let { fetchLatestNotice(it) }
                } else {
                    tvStudentName.text = "Student not found"
                }
            },
            {
                Toast.makeText(context, "Error fetching student data", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun fetchLatestNotice(college: String) {
        val url = "http://192.168.103.54/UniValut/get_latest_notice.php?college=$college"
        val ctx = context ?: return  // Safely get context or return if fragment is not attached
        val queue = Volley.newRequestQueue(ctx)

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                if (!isAdded) return@JsonObjectRequest  // Fragment no longer valid
                val success = response.getBoolean("success")
                if (success) {
                    val title = response.getString("title")
                    val description = response.getString("description")
                    tvNoticeTitle.text = title
                    tvNoticeDescription.text = description
                } else {
                    tvNoticeTitle.text = "No Notices"
                    tvNoticeDescription.text = ""
                }
            },
            {
                if (!isAdded) return@JsonObjectRequest
                tvNoticeTitle.text = "Error"
                tvNoticeDescription.text = "Failed to fetch notice."
            }
        )
        queue.add(jsonObjectRequest)
    }


    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..16 -> "Good Afternoon,"
            in 17..20 -> "Good Evening,"
            else -> "Good Night,"
        }
    }

    companion object {
        fun newInstance(studentID: String): HomeFragment1 {
            val fragment = HomeFragment1()
            val args = Bundle()
            args.putString("studentID", studentID)
            fragment.arguments = args
            return fragment
        }
    }
}
