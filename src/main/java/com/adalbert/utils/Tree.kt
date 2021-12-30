package com.adalbert.utils

data class Tree(val key: String, val children: MutableList<Tree>, var values: MutableList<String>? = null) {
    private fun getKeys(keys: List<String>): List<String>? {
        if (keys.isEmpty()) return children.map { it.key }
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getKeys(keys.drop(1))
    }

    private fun getValues(keys: List<String>): List<String>? {
        if (keys.isEmpty()) return values
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getValues(keys.drop(1))
    }

    private fun getMappings(keys: List<String>): List<Pair<String, List<String>>>? {
        if (keys.isEmpty()) return children.map { Pair(it.key, it.values ?: listOf()) }
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getMappings(keys.drop(1))
    }

    fun getKeys(vararg keys: String): List<String>? {
        return getKeys(keys.toList())
    }

    fun getValues(vararg keys: String): List<String>? {
        return getValues(keys.toList())
    }

    fun getMappings(vararg keys: String): List<Pair<String, List<String>>>? {
        return getMappings(keys.toList())
    }
}