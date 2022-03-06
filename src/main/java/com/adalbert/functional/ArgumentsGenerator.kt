package com.adalbert.functional

import com.adalbert.utils.*
import kotlin.random.Random

object ArgumentsGenerator {

    data class Argument(var name: String, var type: String)

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
        }}
        argsMappings.forEach { if (it.type.contains("$")) it.type = matchKeyWithTypeVariable(it.type, typeVariables) }
        return argsMappings
    }

    private fun matchKeyWithTypeVariable(text: String, typeVariables: Map<String, String>): String {
        if (!text.contains("$")) return text
        val variableName = text.substringAfterLast("$").substringUntil('[', ']', '<', '>')
        return text.replace("\$$variableName", typeVariables[variableName]!!)
    }

    private val randomValuesGeneration = mutableMapOf<String, () -> String>().apply {
        this.putAll(listOf("int", "Integer").associateWith { { "${Random.nextInt(1, 200)}" } })
        this.putAll(listOf("float", "Float").associateWith { { "${(1 .. 200).random().toDouble()}f" } })
        this.putAll(listOf("double", "Double").associateWith { { "${(1 .. 200).random().toDouble()}" } })
        this.putAll(listOf("boolean", "Boolean").associateWith { { "${Random.nextBoolean()}" } })
        this.putAll(listOf("char", "Character").associateWith { { "'${(200 until 400).random().toChar()}'" } })
        this.putAll(listOf("long", "Long").associateWith { { "${(1 .. 200).random()}L" } })
        this["Collection<? extends Integer>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }})"}
        this["Collection<? extends Long>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }})"}
        this["Collection<? extends Float>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }})"}
        this["Collection<? extends Double>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }})"}
        this["Collection<? extends Boolean>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Boolean"]!!() }})"}
        this["Collection<? extends Character>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Character"]!!() }})"}
        this["Collection<? extends String>"] = { "Arrays.asList(${(0 until Random.nextInt(3, 5)).joinToString(",") { this["String"]!!() }})"}
        this["Set<Integer>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }}))"}
        this["Set<Long>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }}))"}
        this["Set<Float>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }}))"}
        this["Set<Double>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }}))"}
        this["Set<Boolean>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Boolean"]!!() }}))"}
        this["Set<Character>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Character"]!!() }}))"}
        this["Set<String>"] = { "new java.util.HashSet<>(Arrays.asList(${(0 until Random.nextInt(3, 5)).joinToString(",") { this["String"]!!() }}))"}
        this["Integer[]"] = { "new Integer[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Integer"]!!() }}}"}
        this["Long[]"] = { "new Long[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Long"]!!() }}}"}
        this["Float[]"] = { "new Float[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Float"]!!() }}}"}
        this["Double[]"] = { "new Double[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Double"]!!() }}}"}
        this["Boolean[]"] = { "new Boolean[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Boolean"]!!() }}}"}
        this["Character[]"] = { "new Character[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["Character"]!!() }}}"}
        this["String[]"] = { "new String[] {${(0 until Random.nextInt(3, 10)).joinToString(",") { this["String"]!!() }}}"}
        this["String"] = { "\"${('a' .. 'z').toList().randomTimes(Random.nextInt(5, 20)).joinToString("")}\"" }
    }

    private val argumentTypesMapping = mutableMapOf<InterProfileMapping, (it: String) -> String> (
        InterProfileMapping("scala", "Collection<? extends String>", "IterableOnce<String>") to { "mutable.ArrayBuffer[String](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Double>", "IterableOnce<Double>") to { "mutable.ArrayBuffer[Double](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Float>", "IterableOnce<Float>") to { "mutable.ArrayBuffer[Float](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Integer>", "IterableOnce<Integer>") to { "mutable.ArrayBuffer[Integer](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Long>", "IterableOnce<Long>") to { "mutable.ArrayBuffer[Long](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Boolean>", "IterableOnce<Boolean>") to { "mutable.ArrayBuffer[Boolean](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Collection<? extends Character>", "IterableOnce<Character>") to { "mutable.ArrayBuffer[Character](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<String>", "Set<String>") to { "mutable.HashSet[String](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Double>", "Set<Double>") to { "mutable.HashSet[Double](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Float>", "Set<Float>") to { "mutable.HashSet[Float](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Integer>", "Set<Integer>") to { "mutable.HashSet[Integer](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Long>", "Set<Long>") to { "mutable.HashSet[Long](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Boolean>", "Set<Boolean>") to { "mutable.HashSet[Boolean](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "Set<Character>", "Set<Character>") to { "mutable.HashSet[Char](${it.substringAfterLast("(").substringUntil(')')})" },
        InterProfileMapping("scala", "String[]", "ClassTag<String>") to { "classTag[String]" },
        InterProfileMapping("scala", "Double[]", "ClassTag<Double>") to { "classTag[Double]" },
        InterProfileMapping("scala", "Float[]", "ClassTag<Float>") to { "classTag[Float]" },
        InterProfileMapping("scala", "Integer[]", "ClassTag<Integer>") to { "classTag[Integer]" },
        InterProfileMapping("scala", "Long[]", "ClassTag<Long>") to { "classTag[Long]" },
        InterProfileMapping("scala", "Boolean[]", "ClassTag<Boolean>") to { "classTag[Boolean]" },
        InterProfileMapping("scala", "Char[]", "ClassTag<Character>") to { "classTag[Char]" }
    )

}