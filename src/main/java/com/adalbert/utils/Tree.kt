package com.adalbert.utils

data class Tree(val key: String, val children: MutableList<Tree>, var value: String? = null)