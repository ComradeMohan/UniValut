package com.simats.univalut

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val studentNumberInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.submitLoginButton)
        val signUpTextView = findViewById<TextView>(R.id.signUp)

        signUpTextView.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        loginButton.setOnClickListener {
            val studentNumber = studentNumberInput.text.toString().trim()
            val password = passwordInput.text.toString()

            if (studentNumber.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both student number and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(studentNumber, password)
            }
        }
    }

    private fun loginUser(studentNumber: String, password: String) {
        val url = "http://192.168.103.54/univalut/login.php" // Use 10.0.2.2 for emulator

        val json = JSONObject().apply {
            put("student_number", studentNumber)
            put("password", password)
        }

        val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json.toString())
        val request = Request.Builder().url(url).post(body).build()

        val name : String = studentNumber

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            if (jsonResponse.getBoolean("success")) {
                                val userType = jsonResponse.getString("user_type")
                                when (userType) {

                                    "student" -> {
                                        val intent = Intent(this@LoginActivity, StudentDashboardActivity::class.java)
                                        intent.putExtra("ID", name)
                                        startActivity(intent)
                                    }
                                    "faculty" -> {
                                        val intent = Intent(this@LoginActivity, FacultyDashboardActivity::class.java)
                                        intent.putExtra("ID", name)
                                        startActivity(intent)
                                    }
                                    "admin" -> {
                                        val intent = Intent(this@LoginActivity, AdminDashboardActivity::class.java)
                                        intent.putExtra("ID", name)
                                        startActivity(intent)
                                    }

                                }
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@LoginActivity, "Invalid response from server", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Server error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })

    }
}
