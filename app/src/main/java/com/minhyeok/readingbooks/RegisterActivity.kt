package com.minhyeok.readingbooks

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.editEmail)
        val password = findViewById<EditText>(R.id.editPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // 회원가입 버튼 클릭 시 이메일과 비밀번호로 계정 생성
        btnRegister.setOnClickListener {
            val emailText = email.text.toString()
            val pwText = password.text.toString()

            auth.createUserWithEmailAndPassword(emailText, pwText)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 회원가입 성공 시 메인 화면으로 이동
                        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // 실패 메시지 출력
                        Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
