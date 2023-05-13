package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.changeAllP2Ps
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2STypeChange(var newType: Int = TUNNEL_ANY, var p2p: Long = NONE_SELECTED): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        newType = buf.readByte().toInt()
        p2p = buf.readLong()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeByte(newType)
        buf.writeLong(p2p)
    }
}

class ServerTypeChangeHandler : IMessageHandler<C2STypeChange, S2CListP2P?> {
    override fun onMessage(message: C2STypeChange, ctx: MessageContext): S2CListP2P? {
        if (message.newType == TUNNEL_ANY) return null
        val status = P2PCache.statusMap[ctx.serverHandler.playerEntity.uniqueID] ?: return null
        val tunnel = status.listP2P[message.p2p] ?: return null
        val type = BetterP2P.proxy.getP2PFromIndex(message.newType) ?: return null
        val newTunnel = changeAllP2Ps(tunnel, type, ctx.serverHandler.playerEntity)
        if (newTunnel) {
            ModNetwork.queueP2PListUpdate(status, ctx.serverHandler.playerEntity)
        }
        return null
    }
}
