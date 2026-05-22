package com.example.sicedroid

import kotlin.js.JsFun

@JsFun("() => Date.now()")
external fun jsDateNow(): Double

actual fun currentTimeMillis(): Long = jsDateNow().toLong()
