package com.simats.univalut

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.simats.univalut.databinding.FragmentAdminHomeBinding
import org.json.JSONObject

class AdminHomeFragment : Fragment() {

    private var _binding: FragmentAdminHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var context: FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        
        context = activity ?: requireActivity()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch admin details and update UI
        fetchAdminDetails("admin001")

        // Set total students (this can be dynamic based on your data)
        binding.tvTotalStudents.text = "2450"

        // Set up RecyclerView
        binding.rvRecentActivity.layoutManager = LinearLayoutManager(context)
        binding.rvRecentActivity.adapter = RecentActivityAdapter(getDummyActivityList())

        // Button click listeners
        binding.btnAddStudent.setOnClickListener {
            Toast.makeText(context, "Add Student clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddFaculty.setOnClickListener {
            Toast.makeText(context, "Add Faculty clicked", Toast.LENGTH_SHORT).show()
            showAddFacultyDialog()
        }

        binding.btnPostNotice.setOnClickListener {
            Toast.makeText(context, "Post Notice clicked", Toast.LENGTH_SHORT).show()

            // Extract the college name from tvTitle
            val collegeName = binding.tvTitle.text.toString().split(" - ").last()

            // Create an intent to start the AdminPostNotice activity
            val intent = Intent(context, AdminPostNotice::class.java)

            // Pass the college name as an extra
            intent.putExtra("COLLEGE_NAME", collegeName)

            // Start the activity
            startActivity(intent)
        }

        binding.btnUploadFiles.setOnClickListener {
            Toast.makeText(context, "Upload Files clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, FacultyMaterialsFragment::class.java)
            startActivity(intent)
        }
    }

    // Function to fetch admin details using HTTP GET request
    private fun fetchAdminDetails(adminId: String) {
        val url = "http://192.168.103.54/UniValut/getAdminDetails.php?admin_id=$adminId"

        // Create a JSON object request
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    // Get admin details from response
                    val name = response.getString("name")
                    val college = response.getString("college")

                    // Set the text of tvTitle to show admin name and college
                    binding.tvTitle.text = "$name - $college"
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error fetching admin details", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Handle error response
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            })

        // Add the request to the Volley request queue
        Volley.newRequestQueue(context).add(jsonObjectRequest)
    }

    private fun showAddFacultyDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_faculty, null)
        val dialog = android.app.AlertDialog.Builder(context)
            .setTitle("Add Faculty")
            .setView(dialogView)
            .create()

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etLoginId = dialogView.findViewById<EditText>(R.id.etLoginId)
        val etCollege = dialogView.findViewById<EditText>(R.id.etCollege)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitFaculty)

        val college = binding.tvTitle.text.toString().split(" - ").last()
        etCollege.setText(college)

        getNextFacultyId { newId ->
            etLoginId.setText(newId)
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val loginId = etLoginId.text.toString().trim()
            val collegeName = etCollege.text.toString().trim()
            val password = "welcome"

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            postFacultyToServer(name, email, phone, collegeName, loginId, password)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun postFacultyToServer(name: String, email: String, phone: String, college: String, loginId: String, password: String) {
        val url = "http://192.168.103.54/UniValut/faculty_register.php" // Match this with your PHP file name

        val params = JSONObject().apply {
            put("name", name)
            put("email", email)
            put("phone_number", phone)
            put("college", college)
            put("login_id", loginId)
            put("password", password)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, params,
            {
                Toast.makeText(context, "Faculty added successfully", Toast.LENGTH_SHORT).show()
            },
            {
                Toast.makeText(context, "Failed to add faculty", Toast.LENGTH_SHORT).show()
            })

        Volley.newRequestQueue(context).add(request)
    }

    private fun getNextFacultyId(callback: (String) -> Unit) {
        val url = "http://192.168.103.54/UniValut/getNextFacultyId.php"

        val request = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val nextId = response.getString("next_id")
                    callback(nextId)
                } catch (e: Exception) {
                    callback("SSE001")
                }
            },
            {
                callback("SSE001")
            })

        Volley.newRequestQueue(context).add(request)
    }


    private fun getDummyActivityList(): List<String> {
        return listOf(
            "Student John added to CS101",
            "Notice posted: Midterm postponed",
            "Dr. Smith uploaded lecture slides",
            "Faculty Jane added to ME201"
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(s: String): Fragment {
            val fragment = AdminHomeFragment()
            val args = Bundle()
            args.putString("adminId", s)
            fragment.arguments = args
            return fragment
        }
    }
}
