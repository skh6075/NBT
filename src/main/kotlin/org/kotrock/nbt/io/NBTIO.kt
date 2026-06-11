package org.kotrock.nbt.io

import org.kotrock.nbt.*
import kotlinx.io.*

object NBTIO {
    fun write(sink: Sink, compound: NbtCompound, isNetwork: Boolean = true) {
        if (!isNetwork) {
            sink.writeByte(NbtType.COMPOUND.id)
            sink.writeNbtString("")
        }
        writeCompoundRaw(sink, compound, isNetwork, Sink::writeVarInt)
    }

    private fun writeCompoundRaw(sink: Sink, compound: NbtCompound, isNetwork: Boolean, writeVarInt: Sink.(Int) -> Unit) {
        compound.entries.asSequence().forEach { (key, tag) ->
            sink.writeByte(tag.type.id)
            sink.writeNbtString(key)
            writeTagRaw(sink, tag, isNetwork, writeVarInt)
        }
        sink.writeByte(NbtType.END.id)
    }

    private fun writeTagRaw(sink: Sink, tag: NbtTag, isNetwork: Boolean, writeVarInt: Sink.(Int) -> Unit) {
        when (tag) {
            is NbtByte -> sink.writeByte(tag.value)
            is NbtShort -> sink.writeShortLe(tag.value)
            is NbtInt -> if (isNetwork) sink.writeVarInt(tag.value) else sink.writeIntLe(tag.value)
            is NbtLong -> sink.writeLongLe(tag.value)
            is NbtFloat -> sink.writeFloatLe(tag.value)
            is NbtDouble -> sink.writeDoubleLe(tag.value)
            is NbtString -> sink.writeNbtString(tag.value)
            is NbtByteArray -> {
                sink.writeVarInt(tag.value.size)
                sink.write(tag.value)
            }
            is NbtIntArray -> {
                sink.writeVarInt(tag.value.size)
                tag.value.forEach { if (isNetwork) sink.writeVarInt(it) else sink.writeIntLe(it) }
            }
            is NbtCompound -> writeCompoundRaw(sink, tag, isNetwork, writeVarInt)
            is NbtList<*> -> {
                val contentType = tag.firstOrNull()?.type ?: NbtType.END
                sink.writeByte(contentType.id)
                sink.writeVarInt(tag.size)
                tag.forEach { writeTagRaw(sink, it, isNetwork, writeVarInt) }
            }
            NbtEnd -> sink.writeByte(NbtType.END.id)
        }
    }

    fun read(source: Source, isNetwork: Boolean = true): NbtCompound {
        if (!isNetwork) {
            val typeId = source.readByte()
            if (typeId != NbtType.COMPOUND.id) {
                throw IllegalStateException("NBT: Invalid root tag header type (0x${Integer.toHexString(typeId.toInt())})")
            }
            source.readNbtString()
        }
        return readCompoundRaw(source, isNetwork)
    }

    private fun readCompoundRaw(source: Source, isNetwork: Boolean): NbtCompound {
        val map = buildMap {
            while (true) {
                val typeId = source.readByte()
                val type = NbtType.fromId(typeId)
                if (type == NbtType.END) break

                val key = source.readNbtString()
                put(key, readTagRaw(source, type, isNetwork))
            }
        }
        return NbtCompound(map)
    }

    private fun readTagRaw(source: Source, type: NbtType, isNetwork: Boolean): NbtTag = when (type) {
        NbtType.BYTE -> NbtByte(source.readByte())
        NbtType.SHORT -> NbtShort(source.readShortLe())
        NbtType.INT -> NbtInt(if (isNetwork) source.readVarInt() else source.readIntLe())
        NbtType.LONG -> NbtLong(source.readLongLe())
        NbtType.FLOAT -> NbtFloat(source.readFloatLe())
        NbtType.DOUBLE -> NbtDouble(source.readDoubleLe())
        NbtType.STRING -> NbtString(source.readNbtString())
        NbtType.BYTE_ARRAY -> NbtByteArray(source.readByteArray(source.readVarInt()))
        NbtType.INT_ARRAY -> {
            val size = source.readVarInt()
            NbtIntArray(IntArray(size) { if (isNetwork) source.readVarInt() else source.readIntLe() })
        }
        NbtType.COMPOUND -> readCompoundRaw(source, isNetwork)
        NbtType.LIST -> {
            val contentType = NbtType.fromId(source.readByte())
            val size = source.readVarInt()
            val list = List(size) { readTagRaw(source, contentType, isNetwork) }
            NbtList(list)
        }
        NbtType.END -> NbtEnd
    }
}