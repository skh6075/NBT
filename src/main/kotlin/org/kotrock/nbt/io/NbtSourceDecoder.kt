package org.kotrock.nbt.io

import kotlinx.io.Source
import kotlinx.io.readByteArray

fun Source.readVarInt(): Int {
    var value = 0
    var size = 0
    while (true) {
        val b = this.readByte().toInt()
        value = value or ((b and 0x7F) shl (size++ * 7))
        if (size > 5) throw IllegalArgumentException("NBT: VarInt specification overflow")
        if ((b and 0x80) == 0) return value
    }
}

fun Source.readNbtString(): String {
    val length = this.readVarInt()
    return this.readByteArray(length).decodeToString()
}