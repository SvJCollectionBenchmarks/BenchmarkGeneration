package com.adalbert.utils

data class Tree(val key: String, val children: MutableList<Tree>, var values: MutableList<String>? = null) {
    fun getKeys(keys: List<String>): List<String>? {
        if (keys.isEmpty()) return children.map { it.key }
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getKeys(keys.drop(1))
    }

    fun getValues(keys: List<String>): List<String>? {
        if (keys.isEmpty()) return values
        val child = children.firstOrNull { it.key == keys.first() }
        if (child == null) throw IllegalArgumentException("Child with key '${keys.first()}' not found in node '$key'")
        else return child.getValues(keys.drop(1))
    }

    fun getMappings(keys: List<String>): List<Pair<String, List<String>>>? {
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

    fun getKeys(vararg keys: String): List<String>? {
        return getKeys(keys.toList())
    }

    fun getValues(vararg keys: String): List<String>? {
        return getValues(keys.toList())
    }

    fun getMappings(vararg keys: String): List<Pair<String, List<String>>>? {
        return getMappings(keys.toList())
    }

    fun getMatchingTree(possibleValues: List<String>, vararg keys: String): Tree {
        val subTree = getSubtree(keys.toList())
        val possibleTreeChildren = subTree.children.map { it.key }
        val matchedKey = possibleValues.firstOrNull { possibleTreeChildren.contains(it) }
            ?: throw IllegalArgumentException("Couldn't match any key from [$possibleValues] in the node with children [$possibleTreeChildren]!")
        return subTree.children.first { it.key == matchedKey }
    }


}