package com.simats.univalut

import CourseFragment
import android.content.Intent
import android.os.Bundle
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.simats.univalut.databinding.FragmentAdminHomeBinding
import com.simats.univalut.databinding.FragmentFacultyHomeBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FacultyHomeFragment : Fragment() {

    private var facultyId: String? = null
    private var collegeName: String? = null  // To store the fetched college name

    private var _binding: FragmentFacultyHomeBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_faculty_home, container, false)

        // Get faculty ID from fragment arguments
        facultyId = arguments?.getString("ID")
        val facultyNameTextView: TextView = view.findViewById(R.id.facultyNameTextView)

        // Fetch faculty name and college name from server
        facultyId?.let {
            FacultyNameTask(facultyNameTextView, this).execute(it)
        }

        val notificationIcon: View = view.findViewById(R.id.notificationIcon)
        notificationIcon.setOnClickListener {
            val intent = Intent(requireContext(), StudentNotificationsActivity::class.java)
            intent.putExtra("college", collegeName) // Pass college name to the next activity
            startActivity(intent)
        }

        val uploadButton: LinearLayout = view.findViewById(R.id.uploadResourcesButton)
        uploadButton.setOnClickListener {
            if (collegeName != null) {
                val courseFragment = CourseFragment.newInstanceWithCollegeName(collegeName!!)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, courseFragment) // replace with your actual container ID
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "College name not available", Toast.LENGTH_SHORT).show()
            }
        }
        val sendAnnounce: LinearLayout = view.findViewById(R.id.sendAnnouncementButton)
        sendAnnounce.setOnClickListener {
            val calender =  StudentCalenderFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,calender)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    // AsyncTask to fetch faculty name and college from server
    private class FacultyNameTask(
        private val facultyNameTextView: TextView,
        private val fragment: FacultyHomeFragment
    ) : AsyncTask<String, Void, Pair<String, String>>() {

        override fun doInBackground(vararg params: String?): Pair<String, String>? {
            val facultyId = params[0] ?: return null
            val urlString = "http://192.168.103.54/UniValut/get_faculty_name.php?facultyId=$facultyId"

            return try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(response.toString())
                if (jsonResponse.getBoolean("success")) {
                    // Return both faculty name and college name
                    val facultyName = jsonResponse.getString("name")
                    val collegeName = jsonResponse.getString("college")
                    Pair(facultyName, collegeName)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        override fun onPostExecute(result: Pair<String, String>?) {
            super.onPostExecute(result)
            if (result != null) {
                val (facultyName, collegeName) = result
                facultyNameTextView.text = facultyName  // Set the faculty name
                fragment.collegeName = collegeName  // Store the college name in the fragment
            } else {
                Toast.makeText(facultyNameTextView.context, "Failed to fetch faculty name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(id: String): Fragment {
            val fragment = FacultyHomeFragment()
            val args = Bundle()
            args.putString("ID", id)  // Corrected the argument key to "ID" instead of "adminId"
            fragment.arguments = args
            return fragment
        }
    }
}
