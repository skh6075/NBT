package org.kotrock.nbt

import kotlin.jvm.JvmInline

enum class NbtType(val id: Byte) {
    END(0), BYTE(1), SHORT(2), INT(3), LONG(4),
    FLOAT(5), DOUBLE(6), BYTE_ARRAY(7), STRING(8),
    LIST(9), COMPOUND(10), INT_ARRAY(11);

    companion object {
        fun fromId(id: Byte): NbtType = entries.firstOrNull { it.id == id }
            ?: throw IllegalArgumentException("NBT: Unknown registry type id (0x${Integer.toHexString(id.toInt())})")
    }
}

sealed interface NbtTag {
    val type: NbtType
}

@JvmInline value class NbtByte(val value: Byte) : NbtTag { override val type get() = NbtType.BYTE }
@JvmInline value class NbtShort(val value: Short) : NbtTag { override val type get() = NbtType.SHORT }
@JvmInline value class NbtInt(val value: Int) : NbtTag { override val type get() = NbtType.INT }
@JvmInline value class NbtLong(val value: Long) : NbtTag { override val type get() = NbtType.LONG }
@JvmInline value class NbtFloat(val value: Float) : NbtTag { override val type get() = NbtType.FLOAT }
@JvmInline value class NbtDouble(val value: Double) : NbtTag { override val type get() = NbtType.DOUBLE }
@JvmInline value class NbtString(val value: String) : NbtTag { override val type get() = NbtType.STRING }

@JvmInline value class NbtByteArray(val value: ByteArray) : NbtTag { override val type get() = NbtType.BYTE_ARRAY }
@JvmInline value class NbtIntArray(val value: IntArray) : NbtTag { override val type get() = NbtType.INT_ARRAY }

class NbtList<out T : NbtTag>(
    private val backingList: List<T>
) : NbtTag, Collection<T> by backingList {
    override val type: NbtType get() = NbtType.LIST
    operator fun get(index: Int): T = backingList[index]
    override fun toString(): String = backingList.toString()
}

class NbtCompound(
    private val backingMap: Map<String, NbtTag>
) : NbtTag {
    override val type: NbtType get() = NbtType.COMPOUND
    operator fun get(key: String): NbtTag? = backingMap[key]
    operator fun iterator(): Iterator<Map.Entry<String, NbtTag>> = backingMap.iterator()

    val entries: Set<Map.Entry<String, NbtTag>> get() = backingMap.entries
    val keys: Set<String> get() = backingMap.keys
    val values: Collection<NbtTag> get() = backingMap.values
    val size: Int get() = backingMap.size
    fun isEmpty(): Boolean = backingMap.isEmpty()

    override fun toString(): String = backingMap.toString()
}

object NbtEnd : NbtTag {
    override val type: NbtType get() = NbtType.END
}