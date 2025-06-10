package com.minhyeok.readingbooks

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // 로그인된 사용자라면 메인화면으로 이동
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 로그인되지 않았다면 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}
