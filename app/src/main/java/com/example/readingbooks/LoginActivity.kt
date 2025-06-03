package com.example.readingbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.editEmail)
        val password = findViewById<EditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // 로그인 버튼 클릭 시 이메일과 비밀번호로 로그인 시도
        btnLogin.setOnClickListener {
            val emailText = email.text.toString()
            val pwText = password.text.toString()

            auth.signInWithEmailAndPassword(emailText, pwText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 로그인 성공 시 메인 화면으로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // 로그인 실패 시 메시지 출력
                        Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        val btnGoRegister = findViewById<Button>(R.id.btnGoRegister)
        // 회원가입 화면으로 이동
        btnGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        // 로그아웃 처리 및 로그인 화면으로 이동
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
