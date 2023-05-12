package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.util.listAllGridP2P
import com.projecturanus.betterp2p.util.listTargetGridP2P
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.toInfo
import com.projecturanus.betterp2p.util.p2p.unlinkP2P
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Unlink input from outputs message (set freq to 0)
 */
class C2SUnlinkP2P(var p2pId: Long = NONE_SELECTED, var type: Int = TUNNEL_ANY): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        p2pId = buf.readLong()
        type = buf.readByte().toInt()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeLong(p2pId)
        buf.writeByte(type)
    }
}

/**
 * Client -> C2SUnlinkP2P -> Server
 * Handler on server side
 */
class ServerUnlinkP2PHandler : IMessageHandler<C2SUnlinkP2P, S2CListP2P?> {
    override fun onMessage(message: C2SUnlinkP2P, ctx: MessageContext): S2CListP2P? {
        val status = P2PCache.statusMap[ctx.serverHandler.playerEntity.uniqueID] ?: return null

        unlinkP2P(ctx.serverHandler.playerEntity, message.p2pId, status)
        status.lastP2PType = message.type
        val tunnelType = BetterP2P.proxy.getP2PFromIndex(message.type)
        if (tunnelType != null) {
            listTargetGridP2P(status.grid, status.player, tunnelType.clazz)
        } else {
            listAllGridP2P(status.grid, status.player)
        }
        return S2CListP2P(status.refresh(status.lastP2PType).map { it.toInfo() })
    }
}
