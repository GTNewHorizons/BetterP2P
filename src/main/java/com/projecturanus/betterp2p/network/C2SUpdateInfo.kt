package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.gui.widget.GuiScale
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

fun writeMemoryInfo(buf: ByteBuf, info: MemoryInfo) {
    buf.writeLong(info.selectedEntry)
    buf.writeLong(info.frequency)
    buf.writeInt(info.mode.ordinal)
    buf.writeByte(info.gui.ordinal)
}

fun readMemoryInfo(buf: ByteBuf): MemoryInfo {
    return MemoryInfo(buf.readLong(), buf.readLong(),
                      BetterMemoryCardModes.values()[buf.readInt()],
                      GuiScale.values()[buf.readByte().toInt()])
}

class C2SUpdateInfo(var info: MemoryInfo = MemoryInfo()) : IMessage {
    override fun fromBytes(buf: ByteBuf) {
        info = readMemoryInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeMemoryInfo(buf, info)
    }
}
