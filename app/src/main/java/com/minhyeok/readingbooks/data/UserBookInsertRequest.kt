package com.minhyeok.readingbooks.data


data class UserBookInsertRequest(
    val user_id: String,
    val isbn: String,  // ✅ 기존 구조 유지
    val review: String,
    val read_page: Int
)



