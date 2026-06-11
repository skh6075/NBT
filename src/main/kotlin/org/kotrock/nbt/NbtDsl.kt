package org.kotrock.nbt

@DslMarker
annotation class NbtDsl

@NbtDsl
class NbtCompoundBuilder {
    private val tags = mutableMapOf<String, NbtTag>()

    fun byte(key: String, value: Byte) { tags[key] = NbtByte(value) }
    fun short(key: String, value: Short) { tags[key] = NbtShort(value) }
    fun int(key: String, value: Int) { tags[key] = NbtInt(value) }
    fun long(key: String, value: Long) { tags[key] = NbtLong(value) }
    fun float(key: String, value: Float) { tags[key] = NbtFloat(value) }
    fun double(key: String, value: Double) { tags[key] = NbtDouble(value) }
    fun string(key: String, value: String) { tags[key] = NbtString(value) }
    fun boolean(key: String, value: Boolean) { tags[key] = NbtByte(if (value) 1 else 0) }
    fun byteArray(key: String, vararg values: Byte) { tags[key] = NbtByteArray(values) }
    fun intArray(key: String, vararg values: Int) { tags[key] = NbtIntArray(values) }

    fun nbtCompound(key: String, block: NbtCompoundBuilder.() -> Unit) {
        tags[key] = NbtCompoundBuilder().apply(block).build()
    }

    fun nbtCompound(key: String, readyCompound: NbtCompound) {
        tags[key] = readyCompound
    }

    fun nbtList(key: String, block: NbtListBuilder.() -> Unit) {
        tags[key] = NbtListBuilder().apply(block).build()
    }

    fun stringList(key: String, vararg elements: String) {
        tags[key] = NbtList(elements.map { NbtString(it) })
    }

    fun intList(key: String, vararg elements: Int) {
        tags[key] = NbtList(elements.map { NbtInt(it) })
    }

    fun build(): NbtCompound = NbtCompound(tags.toMap())
}

@NbtDsl
class NbtListBuilder {
    private val list = mutableListOf<NbtTag>()

    fun addTag(tag: NbtTag) { list.add(tag) }

    fun addCompound(compound: NbtCompound) { list.add(compound) }

    fun addCompound(block: NbtCompoundBuilder.() -> Unit) {
        list.add(NbtCompoundBuilder().apply(block).build())
    }

    fun byte(value: Byte) { list.add(NbtByte(value)) }
    fun short(value: Short) { list.add(NbtShort(value)) }
    fun int(value: Int) { list.add(NbtInt(value)) }
    fun long(value: Long) { list.add(NbtLong(value)) }
    fun float(value: Float) { list.add(NbtFloat(value)) }
    fun double(value: Double) { list.add(NbtDouble(value)) }
    fun string(value: String) { list.add(NbtString(value)) }

    fun build(): NbtList<NbtTag> = NbtList(list.toList())
}

fun nbtCompound(block: NbtCompoundBuilder.() -> Unit): NbtCompound =
    NbtCompoundBuilder().apply(block).build()

inline fun <reified T : NbtTag> NbtCompound.getStructure(key: String): T? = this[key] as? T

fun NbtCompound.getString(key: String, default: String = ""): String =
    (this[key] as? NbtString)?.value ?: default

fun NbtCompound.getInt(key: String, default: Int = 0): Int =
    (this[key] as? NbtInt)?.value ?: default

fun NbtCompound.getBoolean(key: String, default: Boolean = false): Boolean =
    (this[key] as? NbtByte)?.value?.let { it == 1.toByte() } ?: default