package com.simats.univalut

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sf = getSharedPreferences("user_sf", MODE_PRIVATE)
        val isLoggedIn = sf.getBoolean("isLoggedIn",false)
        val userType = sf.getString("userType","")
        val userId = sf.getString("userID","")

        if (isLoggedIn){
            if (userType.equals("admin")){
                val intent = Intent(this@MainActivity, AdminDashboardActivity::class.java)
                intent.putExtra("ID", userId)
                startActivity(intent)
            }else if (userType.equals("student")){
                val intent = Intent(this@MainActivity, StudentDashboardActivity::class.java)
                intent.putExtra("ID", userId)
                startActivity(intent)
            }else if (userType.equals("faculty")){
                val intent = Intent(this@MainActivity, FacultyDashboardActivity::class.java)
                intent.putExtra("ID", userId)
                startActivity(intent)
            }
        }

        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val signUpButton = findViewById<MaterialButton>(R.id.signUpButton)

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
