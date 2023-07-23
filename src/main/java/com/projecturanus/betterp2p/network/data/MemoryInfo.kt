package com.projecturanus.betterp2p.network.data

import io.netty.buffer.ByteBuf

import com.projecturanus.betterp2p.client.gui.widget.GuiScale
import com.projecturanus.betterp2p.item.BetterMemoryCardModes

/**
 * Using `-1` to represent any tunnel type.
 */
const val TUNNEL_ANY: Int = -1

/**
 * A data class for serializing p2p
 * @param selectedEntry Points to the index of the selected p2p in the list.
 * @param frequency Selected Frequency. 0 = not selected
 * @param mode The mode
 * @param guiScale The saved gui scale
 * @param type The P2P type, indexed in TunnelInfo.kt
 */
data class MemoryInfo(
        var selectedEntry: P2PLocation? = null,
        var frequency: Long = 0,
        var mode: BetterMemoryCardModes = BetterMemoryCardModes.OUTPUT,
        var guiScale: GuiScale = GuiScale.DYNAMIC,
        var type: Int = TUNNEL_ANY)
fun readMemoryInfo(buf: ByteBuf): MemoryInfo {
    var selectedEntry: P2PLocation? = null
    if (buf.readBoolean()) {
        selectedEntry = readP2PLocation(buf)
    }
    val frequency = buf.readLong()
    val mode = try {
        BetterMemoryCardModes.values()[buf.readInt()]
    } catch (e: Exception) {
        BetterMemoryCardModes.OUTPUT
    }
    val guiScale = try {
        GuiScale.values()[buf.readByte().toInt()]
    } catch (e: ArrayIndexOutOfBoundsException) {
        GuiScale.DYNAMIC
    }
    val type = buf.readByte().toInt()
    return MemoryInfo(selectedEntry, frequency, mode, guiScale, type)
}

fun writeMemoryInfo(buf: ByteBuf, info: MemoryInfo) {
    val hasSelected = info.selectedEntry != null

    buf.writeBoolean(hasSelected)
    if (hasSelected) {
        writeP2PLocation(buf, info.selectedEntry!!)
    }
    buf.writeLong(info.frequency)
    buf.writeInt(info.mode.ordinal)
    buf.writeByte(info.guiScale.ordinal)
    buf.writeByte(info.type)
}
