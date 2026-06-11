package org.kotrock.nbt.io

import kotlinx.io.Sink

fun Sink.writeVarInt(value: Int) {
    var v = value
    while ((v and -0x80) != 0) {
        this.writeByte(((v and 0x7F) or 0x80).toByte())
        v = v ushr 7
    }
    this.writeByte((v and 0x7F).toByte())
}

fun Sink.writeNbtString(value: String) {
    val bytes = value.toByteArray(Charsets.UTF_8)
    this.writeVarInt(bytes.size)
    this.write(bytes)
}