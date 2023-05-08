package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.util.listAllGridP2P
import com.projecturanus.betterp2p.util.listTargetGridP2P
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.toInfo
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Send a request to the server to refresh the p2p list with the
 * given type.
 */
class C2SRefreshP2PList(var type: Int = TUNNEL_ANY): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        type = buf.readByte().toInt()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeByte(type)
    }
}

/**
 * Client -> C2SRefreshP2P -> Server
 * Handler on server side
 */
class ServerRefreshP2PListHandler : IMessageHandler<C2SRefreshP2PList, IMessage?> {
    override fun onMessage(message: C2SRefreshP2PList, ctx: MessageContext): IMessage? {
        if (!P2PCache.statusMap.containsKey(ctx.serverHandler.playerEntity.uniqueID)) return null
        val status = P2PCache.statusMap[ctx.serverHandler.playerEntity.uniqueID]!!
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
