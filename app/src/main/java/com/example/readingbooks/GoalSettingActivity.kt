package com.example.readingbooks

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GoalSettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_setting)

        val editGoal = findViewById<EditText>(R.id.editGoalPage)
        val btnSave = findViewById<Button>(R.id.btnSaveGoal)

        btnSave.setOnClickListener {
            val page = editGoal.text.toString().toIntOrNull()
            if (page != null && page > 0) {
                val prefs = getSharedPreferences("reading_goal", MODE_PRIVATE)
                prefs.edit().putInt("dailyPageGoal", page).apply()
                Toast.makeText(this, "목표가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "유효한 숫자를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
