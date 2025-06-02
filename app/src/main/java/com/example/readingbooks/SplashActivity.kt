package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // 로그인된 사용자 → 메인화면
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 로그인 안된 사용자 → 로그인 화면
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
