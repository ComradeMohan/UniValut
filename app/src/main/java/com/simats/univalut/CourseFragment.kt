import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.simats.univalut.Course
import com.simats.univalut.CourseAdapter
import com.simats.univalut.CourseMaterialsActivity
import com.simats.univalut.R
import org.json.JSONException

class CourseFragment : Fragment() {

    private var studentID: String? = null
    private var collegeName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_course, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve arguments
        studentID = arguments?.getString("studentID")
        collegeName = arguments?.getString("collegeName")

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewCourses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val courseList = mutableListOf<Course>()
        val adapter = CourseAdapter(courseList) { course ->
            val intent = Intent(requireContext(), CourseMaterialsActivity::class.java)
            intent.putExtra("courseCode", course.Code)
            intent.putExtra("collegeName", collegeName)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        when {
            !collegeName.isNullOrEmpty() -> {
                // Admin or Faculty flow
                fetchCourses(collegeName!!, adapter)
            }
            !studentID.isNullOrEmpty() -> {
                // Student flow
                fetchCollegeName(studentID!!, adapter)
            }
            else -> {
                Toast.makeText(requireContext(), "No student ID or college name provided", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCollegeName(studentID: String, adapter: CourseAdapter) {
        val url = "http://192.168.224.54/UniValut/fetch_student_name.php?studentID=$studentID"
        val queue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        collegeName = response.getString("college")
                        Toast.makeText(requireContext(), "College: $collegeName", Toast.LENGTH_SHORT).show()
                        collegeName?.let {
                            fetchCourses(it, adapter)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error fetching college name", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Error parsing response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    private fun fetchCourses(collegeName: String, adapter: CourseAdapter) {
        val url = "http://192.168.224.54/UniValut/fetch_courses.php?college=$collegeName"
        val queue = Volley.newRequestQueue(requireContext())

        val jsonObjectRequest = JsonObjectRequest(
            com.android.volley.Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val coursesArray = response.getJSONArray("courses")
                        val courseList = mutableListOf<Course>()
                        for (i in 0 until coursesArray.length()) {
                            val courseObject = coursesArray.getJSONObject(i)
                            val courseCode = courseObject.getString("course_code")
                            val subjectName = courseObject.getString("subject_name")
                            val credits = courseObject.getInt("strength") // Update to match actual API field if needed
                            courseList.add(Course(courseCode, subjectName, credits))
                        }
                        adapter.updateCourseList(courseList)
                    } else {
                        Toast.makeText(requireContext(), "Error fetching courses", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: JSONException) {
                    Toast.makeText(requireContext(), "Error parsing courses response", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(jsonObjectRequest)
    }

    companion object {
        fun newInstance(studentID: String): CourseFragment {
            val fragment = CourseFragment()
            val args = Bundle()
            args.putString("studentID", studentID)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceWithCollegeName(collegeName: String): CourseFragment {
            val fragment = CourseFragment()
            val args = Bundle()
            args.putString("collegeName", collegeName)
            fragment.arguments = args
            return fragment
        }
    }
}