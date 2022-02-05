package com.adalbert.generation

import com.adalbert.utils.Tree
import com.adalbert.utils.randomTimes
import com.adalbert.utils.substringFromLast
import com.adalbert.utils.substringUntil
import kotlin.random.Random

data class Argument(var name: String, var type: String)

object ArgumentsGenerator {

    fun generateArgumentsForProfile(group: String, operationProfile: String, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree): Map<Argument, String> {
        return getArgsMappingsForProfile(group, operationProfile, operation, typeVariables, propertiesTree)
            .associateWith { (randomValuesGeneration[it.type]?.let { it1 -> it1() } ?: "${it.type} not mapped") }
    }

    fun mapArgumentsToProfile(group: String, possibleProfiles: List<String>, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree, protoArguments: Map<String, Map<Argument, String>>): Map<String, String> {
        val operationProfile = propertiesTree.getFirstMatchingKey(possibleProfiles, "groups", group, "operations", operation)
        val arguments = getArgsMappingsForProfile(group, operationProfile, operation, typeVariables, propertiesTree)

        val notMappedTypes = arguments.forEach { profileArgument ->
            val protoArgument = protoArguments[operation]?.keys?.firstOrNull { profileArgument.name == it.name } ?: throw IllegalStateException()
            if (protoArgument.type != profileArgument.type && argumentTypesMapping[Pair(protoArgument.type, profileArgument.type)] == null)
                println("You need to map ${protoArgument.type} to ${profileArgument.type} also!")
        }

        return mapOf()
    }

    private fun getArgsMappingsForProfile(group: String, operationProfile: String, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree): List<Argument> {
        val rawArgsMappings = propertiesTree.getMappings("groups", group, "operations", operation, operationProfile, "args")
        val argsMappings = rawArgsMappings?.let { argsInner -> (0 until argsInner.size / 2).map { index ->
            if (argsInner[2 * index].first == "name")
                Argument (argsInner[2 * index].second.first(), argsInner[(2 * index) + 1].second.first())
            else if (argsInner[(2 * index) + 1].first == "name")
                Argument (argsInner[(2 * index) + 1].second.first(), argsInner[2 * index].second.first())
            else throw IllegalStateException("Wrong args mapping found!")
        }} ?: throw IllegalStateException("Didn't find argument mappings for operation $operation from $group group!")
        argsMappings.forEach { if (it.type.contains("$")) it.type = matchKeyWithTypeVariable(it.type, typeVariables) }
        return argsMappings
    }

    private fun matchKeyWithTypeVariable(text: String, typeVariables: Map<String, String>): String {
        if (!text.contains("$")) return text
        val variableName = text.substringFromLast("$").substringUntil('[', ']', '<', '>')
        return text.replace("\$$variableName", typeVariables[variableName]!!)
    }

    private val randomValuesGeneration = mutableMapOf<String, () -> String>().apply {
        this.putAll(listOf("int", "Integer").associateWith { { "${Random.nextInt(1, 10)}" } })
        this.putAll(listOf("float", "Float").associateWith { { "${Random.nextDouble(10.0)}" } })
        this.putAll(listOf("double", "Double").associateWith { { "${Random.nextDouble(10.0)}" } })
        this.putAll(listOf("boolean", "Boolean").associateWith { { "${Random.nextBoolean()}" } })
        this.putAll(listOf("long", "Long").associateWith { { "${Random.nextLong(Long.MAX_VALUE)}" } })
        this["Collection<? extends Integer>"] = { "new ArrayList<Integer>(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }
        })"}
        this["Collection<? extends Long>"] = { "new ArrayList<Long>(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }
        })"}
        this["Collection<? extends Float>"] = { "new ArrayList<Float>(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }
        })"}
        this["Collection<? extends Double>"] = { "new ArrayList<Double>(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }
        })"}
        this["Collection<? extends String>"] = { "new ArrayList<String>(${
            (0 until Random.nextInt(3, 5)).joinToString(",") { "\"${this["String"]!!()}\"" }
        })"}
        this["String"] = { ('a' .. 'z').toList().randomTimes(Random.nextInt(5, 20)).joinToString("") }
    }

    private val argumentTypesMapping = mutableMapOf<Pair<String, String>, () -> String>().apply {

    }

//    fun generateArguments(group: String, profiles: List<String>, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree): Map<String, String> {
//        val operationProfile = propertiesTree.getFirstMatchingKey(profiles, "groups", group, "operations", operation)
//        val argsMappings = propertiesTree.getMappings("groups", group, "operations", operation, operationProfile, "args")
//        val arguments = argsMappings?.let { argsInner -> (0 until argsInner.size / 2).map { index ->
//            if (argsInner[2 * index].first == "name")
//                Argument (argsInner[2 * index].second.first(), argsInner[(2 * index) + 1].second.first())
//            else if (argsInner[(2 * index) + 1].first == "name")
//                Argument (argsInner[(2 * index) + 1].second.first(), argsInner[2 * index].second.first())
//            else throw IllegalStateException("Wrong args mapping found!")
//        }}?.toMutableList() ?: throw IllegalStateException("Didn't find argument mappings for operation $operation from $group group!")
//        arguments.forEach { if (it.type.contains("$")) it.type = matchKeyWithTypeVariable(it.type, typeVariables) }
//        // TODO: If mapping from type to random value is null, we should throw an exception
//        return arguments.associate { it.name to (randomValuesGeneration[it.type]?.let { it1 -> it1() } ?: "${it.type} not mapped")  }
//    }

}