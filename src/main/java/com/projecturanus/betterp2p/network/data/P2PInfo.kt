package com.projecturanus.betterp2p.network.data

import appeng.me.GridNode
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.util.p2p.getTypeIndex
import com.projecturanus.betterp2p.util.p2p.hasChannel
import io.netty.buffer.ByteBuf
import net.minecraftforge.common.util.ForgeDirection

/**
 * Sent over the network to clients.
 */
data class P2PInfo(
        val frequency: Long,
        val posX: Int,
        val posY: Int,
        val posZ: Int,
        val dim: Int,
        val facing: ForgeDirection,
        val name: String,
        val output: Boolean,
        val hasChannel: Boolean,
        // # of channels if ME P2P, or -1 else
        val channels: Int,
        // type of p2p, corresponds to registry in proxy
        val type: Int) {

    override fun hashCode(): Int {
        return hashP2P(posX, posY, posZ, facing.ordinal, dim).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as P2PInfo
        if (this.posX != other.posX) return false
        if (this.posY != other.posY) return false
        if (this.posZ != other.posZ) return false
        if (facing != other.facing) return false
        return dim == other.dim
    }

}

/**
 * Deserialize the P2P info over network traffic.
 */
fun readP2PInfo(buf: ByteBuf): P2PInfo? {
    try {
        val freq = buf.readLong()
        val posX = buf.readInt()
        val posY = buf.readInt()
        val posZ = buf.readInt()
        val world = buf.readInt()
        val facing = ForgeDirection.values()[buf.readInt()]
        val nameLength = buf.readShort() - 1
        val name: StringBuilder = StringBuilder()
        for (i in 0..nameLength) {
            name.append(buf.readChar())
        }
        val output = buf.readBoolean()
        val hasChannel = buf.readBoolean()
        val channels = buf.readByte().toInt()
        val type = buf.readByte().toInt()
        return P2PInfo(freq, posX, posY, posZ,world, facing, name.toString(), output, hasChannel, channels, type)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * Serialize the P2P over the network
 */
fun writeP2PInfo(buf: ByteBuf, info: P2PInfo) {
    buf.writeLong(info.frequency)
    buf.writeInt(info.posX)
    buf.writeInt(info.posY)
    buf.writeInt(info.posZ)
    buf.writeInt(info.dim)
    buf.writeInt(info.facing.ordinal)
    buf.writeShort(info.name.length)
    for (c in info.name) {
        buf.writeChar(c.code)
    }
    buf.writeBoolean(info.output)
    buf.writeBoolean(info.hasChannel)
    buf.writeByte(info.channels)
    buf.writeByte(info.type)
}

/**
 * Convert to [P2PInfo] by extracting the necessary fields
 */
fun PartP2PTunnel<*>.toInfo() = P2PInfo(
    frequency,
    location.x,
    location.y,
    location.z,
    location.dimension,
    side,
    customName,
    isOutput,
    hasChannel,
    (externalFacingNode as? GridNode)?.usedChannels() ?: -1,
    getTypeIndex())
