package com.adalbert.utils

data class Tree(val key: String, val children: MutableList<Tree>, var values: MutableList<String>? = null) {
    private fun getValues(keys: List<String>): List<String>? {
        if (keys.isEmpty()) return values
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getValues(keys.drop(1))
    }

    fun getValues(vararg keys: String): List<String>? {
        return getValues(keys.toList())
    }
}