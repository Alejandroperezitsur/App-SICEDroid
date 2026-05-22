package com.example.sicedroid

actual fun currentTimeMillis(): Long = kotlin.js.Date.now().toLong()
