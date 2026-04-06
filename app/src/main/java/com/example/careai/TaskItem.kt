package com.example.careai

data class askItem(
    val id: Long = System.currentTimeMillis(),
    var taskText: String = "",
    var time: String = "00:00",
    var hour: Int = 0,
    var minute: Int = 0
)