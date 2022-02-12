package com.adalbert.utils

data class Tree(val key: String, val children: MutableList<Tree>, var values: MutableList<String>? = null) {
    fun getKeys(keys: List<String>): List<String> {
        if (keys.isEmpty()) return children.map { it.key }
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getKeys(keys.drop(1))
    }

    fun getValues(keys: List<String>): List<String> {
        if (keys.isEmpty()) return values ?: throw IllegalArgumentException("Null values found in tree under given keys $keys!")
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getValues(keys.drop(1))
    }

    fun getValue(keys: List<String>): String {
        val values = getValues(keys)
        if (values.size == 1) return values[0]
        else throw IllegalArgumentException("Couldn't match 'the only' value for keys $keys in the tree!")
    }

    fun getMappings(keys: List<String>): List<Pair<String, List<String>>> {
        if (keys.isEmpty()) return children.map { Pair(it.key, it.values ?: listOf()) }
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getMappings(keys.drop(1))
    }

    fun getSubtree(keys: List<String>): Tree {
        if (keys.isEmpty()) return this
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getSubtree(keys.drop(1))
    }

    fun getKeys(vararg keys: String): List<String> {
        return getKeys(keys.toList())
    }

    fun getValues(vararg keys: String): List<String> {
        return getValues(keys.toList())
    }

    fun getValue(vararg keys: String): String {
        return getValue(keys.toList())
    }

    fun getMappings(vararg keys: String): List<Pair<String, List<String>>> {
        return getMappings(keys.toList())
    }

    fun getFirstMatchingKey(possibleValues: List<String>, vararg keys: String): String {
        val subTree = getSubtree(keys.toList())
        val possibleTreeChildren = subTree.children.map { it.key }
        return possibleValues.firstOrNull { possibleTreeChildren.contains(it) }
            ?: throw IllegalArgumentException("Couldn't match any key from [$possibleValues] in the node with children [$possibleTreeChildren]!")
    }

}