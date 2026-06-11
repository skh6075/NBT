package org.kotrock.nbt.io

import org.kotrock.nbt.*

fun NbtCompound.toSnbt(): String = buildString {
    append("{")
    val entriesList = entries.toList()
    for (i in entriesList.indices) {
        val (key, tag) = entriesList[i]
        append(key).append(":")
        appendTagValue(tag)
        if (i < entriesList.lastIndex) append(",")
    }
    append("}")
}

private fun StringBuilder.appendTagValue(tag: NbtTag) {
    when (tag) {
        is NbtByte -> append(tag.value).append("b")
        is NbtShort -> append(tag.value).append("s")
        is NbtInt -> append(tag.value)
        is NbtLong -> append(tag.value).append("l")
        is NbtFloat -> append(tag.value).append("f")
        is NbtDouble -> append(tag.value).append("d")
        is NbtString -> append("\"").append(tag.value.replace("\"", "\\\"")).append("\"")
        is NbtCompound -> append(tag.toSnbt())

        is NbtList<*> -> {
            append("[")
            tag.joinTo(buffer = this, separator = ",") { element ->
                buildString { appendTagValue(element as NbtTag) }
            }
            append("]")
        }

        is NbtByteArray -> {
            append("[B;")
            tag.value.joinTo(this, separator = ",") { "${it}b" }
            append("]")
        }
        is NbtIntArray -> {
            append("[I;")
            tag.value.joinTo(this, separator = ",") { "$it" }
            append("]")
        }

        NbtEnd -> append("")
    }
}

fun String.toNbtCompound(): NbtCompound {
    val trimmed = this.trim()
    if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
        throw IllegalArgumentException("SNBT: Serialization scope violation, missing enclosing brackets '{}'")
    }
    return parseCompoundContent(trimmed.substring(1, trimmed.length - 1))
}

private fun parseCompoundContent(content: String): NbtCompound = nbtCompound {
    if (content.isBlank()) return@nbtCompound

    val pairs = splitTopLevel(content)
    for (pair in pairs) {
        val parts = pair.split(":", limit = 2)
        if (parts.size != 2) continue

        val key = parts[0].trim()
        val valueStr = parts[1].trim()

        when {
            valueStr.startsWith("{") && valueStr.endsWith("}") -> {
                nbtCompound(key, valueStr.toNbtCompound())
            }

            valueStr.startsWith("[B;") && valueStr.endsWith("]") -> {
                val rawNumbers = valueStr.substring(3, valueStr.length - 1)
                val bytes = if (rawNumbers.isBlank()) byteArrayOf() else rawNumbers.split(",")
                    .map { it.trim().dropLast(if (it.trim().endsWith("b")) 1 else 0).toByte() }
                    .toByteArray()
                byteArray(key, *bytes)
            }

            valueStr.startsWith("[I;") && valueStr.endsWith("]") -> {
                val rawNumbers = valueStr.substring(3, valueStr.length - 1)
                val ints = if (rawNumbers.isBlank()) intArrayOf() else rawNumbers.split(",")
                    .map { it.trim().toInt() }
                    .toIntArray()
                intArray(key, *ints)
            }

            valueStr.startsWith("[") && valueStr.endsWith("]") -> {
                val listContent = valueStr.substring(1, valueStr.length - 1)
                nbtList(key) {
                    if (listContent.isNotBlank()) {
                        splitTopLevel(listContent).forEach { elem ->
                            if (elem.trim().startsWith("{")) {
                                addCompound(parseCompoundContent(elem.trim().substring(1, elem.trim().length - 1)))
                            } else {
                                addTag(parsePrimitiveValue(elem.trim()))
                            }
                        }
                    }
                }
            }

            else -> {
                when {
                    valueStr.startsWith("\"") -> string(key, valueStr.removeSurrounding("\""))
                    valueStr.endsWith("b") -> byte(key, valueStr.dropLast(1).toByte())
                    valueStr.endsWith("s") -> short(key, valueStr.dropLast(1).toShort())
                    valueStr.endsWith("l") -> long(key, valueStr.dropLast(1).toLong())
                    valueStr.endsWith("f") -> float(key, valueStr.dropLast(1).toFloat())
                    valueStr.endsWith("d") -> double(key, valueStr.dropLast(1).toDouble())
                    valueStr == "true" || valueStr == "false" -> boolean(key, valueStr.toBoolean())
                    else -> int(key, valueStr.toIntOrNull() ?: 0)
                }
            }
        }
    }
}

private fun parsePrimitiveValue(valueStr: String): NbtTag = when {
    valueStr.startsWith("\"") -> NbtString(valueStr.removeSurrounding("\""))
    valueStr.endsWith("b") -> NbtByte(valueStr.dropLast(1).toByte())
    valueStr.endsWith("s") -> NbtShort(valueStr.dropLast(1).toShort())
    valueStr.endsWith("l") -> NbtLong(valueStr.dropLast(1).toLong())
    valueStr.endsWith("f") -> NbtFloat(valueStr.dropLast(1).toFloat())
    valueStr.endsWith("d") -> NbtDouble(valueStr.dropLast(1).toDouble())
    valueStr == "true" || valueStr == "false" -> NbtByte(if (valueStr.toBoolean()) 1 else 0)
    else -> NbtInt(valueStr.toIntOrNull() ?: 0)
}

private fun splitTopLevel(content: String): List<String> {
    val result = mutableListOf<String>()
    val current = StringBuilder()
    var depth = 0
    var inQuotes = false

    for (ch in content) {
        when (ch) {
            '"' -> inQuotes = !inQuotes
            '{', '[' -> if (!inQuotes) depth++
            '}', ']' -> if (!inQuotes) depth--
            ',' -> if (!inQuotes && depth == 0) {
                result.add(current.toString().trim())
                current.clear()
                continue
            }
        }
        current.append(ch)
    }
    if (current.isNotBlank()) result.add(current.toString().trim())
    return result
}