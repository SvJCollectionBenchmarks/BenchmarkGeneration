package com.adalbert.generation

import com.adalbert.utils.*
import kotlin.random.Random

data class Argument(var name: String, var type: String)

object ArgumentsGenerator {

    private data class InterProfileMapping(val profile: String, val protoType: String, val targetType: String)

    fun generateArgumentsForProfile(group: String, operationProfile: String, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree): Map<Argument, String> {
        return getArgsMappingsForProfile(group, operationProfile, operation, typeVariables, propertiesTree)
            .associateWith { (randomValuesGeneration[it.type]?.let { it1 -> it1() } ?: "${it.type} not mapped") }
    }

    fun mapArgumentsToProfile(group: String, operationProfile: String, operation: String, typeVariables: Map<String, String>, propertiesTree: Tree, protoArguments: Map<String, Map<Argument, String>>): Map<String, String> {
        val arguments = getArgsMappingsForProfile(group, operationProfile, operation, typeVariables, propertiesTree)

        return arguments.associate { targetArgument: Argument ->
            val protoArgumentPair = protoArguments[operation]?.map { it }?.firstOrNull { it.key.name == targetArgument.name }
                ?: throw IllegalStateException()
            if (protoArgumentPair.key.type != targetArgument.type) {
                val mapping = argumentTypesMapping[InterProfileMapping(operationProfile, protoArgumentPair.key.type, targetArgument.type)]
                if (mapping == null) {
                    println("\t### You may need to map ${protoArgumentPair.key.type} to ${targetArgument.type} also for profile $operationProfile! ###")
                    val value = randomValuesGeneration[targetArgument.type]?.let { it1 -> it1() }
                        ?: "${targetArgument.type} not mapped"
                    targetArgument.name to value
                } else protoArgumentPair.key.name to mapping(protoArgumentPair.value)
            } else protoArgumentPair.key.name to protoArgumentPair.value
        }
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
        this["Collection<? extends Integer>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }
        })"}
        this["Collection<? extends Long>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }
        })"}
        this["Collection<? extends Float>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }
        })"}
        this["Collection<? extends Double>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }
        })"}
        this["Collection<? extends Boolean>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Boolean"]!!() }
        })"}
        this["Collection<? extends String>"] = { "Arrays.asList(${
            (0 until Random.nextInt(3, 5)).joinToString(",") { "\"${this["String"]!!()}\"" }
        })"}
        this["Integer[]"] = { "new Integer[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }
        }}"}
        this["Long[]"] = { "new Long[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }
        }}"}
        this["Float[]"] = { "new Float[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }
        }}"}
        this["Double[]"] = { "new Double[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }
        }}"}
        this["Boolean[]"] = { "new Boolean[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["Boolean"]!!() }
        }}"}
        this["String[]"] = { "new String[] {${
            (0 until Random.nextInt(3, 10)).joinToString(",") { this["String"]!!() }
        }}"}
        this["String"] = { ('a' .. 'z').toList().randomTimes(Random.nextInt(5, 20)).joinToString("") }
    }

    private val argumentTypesMapping = mutableMapOf<InterProfileMapping, (it: String) -> String> (
        InterProfileMapping("scala", "Collection<? extends String>", "IterableOnce<String>") to { "ArrayBuffer[String](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Double>", "IterableOnce<Double>") to { "ArrayBuffer[Double](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Float>", "IterableOnce<Float>") to { "ArrayBuffer[Float](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Integer>", "IterableOnce<Integer>") to { "ArrayBuffer[Integer](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Long>", "IterableOnce<Long>") to { "ArrayBuffer[Long](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Boolean>", "IterableOnce<Boolean>") to { "ArrayBuffer[Boolean](${it.substringFromLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "String[]", "ClassTag<String>") to { "ClassTag(String.getClass)" },
        InterProfileMapping("scala", "Double[]", "ClassTag<Double>") to { "ClassTag(Double.getClass)" },
        InterProfileMapping("scala", "Float[]", "ClassTag<Float>") to { "ClassTag(Float.getClass)" },
        InterProfileMapping("scala", "Integer[]", "ClassTag<Integer>") to { "ClassTag(Integer.getClass)" },
        InterProfileMapping("scala", "Long[]", "ClassTag<Long>") to { "ClassTag(Long.getClass)" },
        InterProfileMapping("scala", "Boolean[]", "ClassTag<Boolean>") to { "ClassTag(Boolean.getClass)" }
    )

}