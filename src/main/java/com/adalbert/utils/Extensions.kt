package com.adalbert.utils

fun String.substringUntilLast(text: String): String {
    return this.substring(0 until this.lastIndexOf(text))
}