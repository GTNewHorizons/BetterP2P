package com.projecturanus.betterp2p.network.packet

import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.data.TUNNEL_ANY

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Send a request to the server to refresh the p2p list with the given type. This must be validated
 * by the server side. This is a full refresh (not just an update).
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
 * Server handler.
 * Handler on server side
 */
class ServerRefreshP2PListHandler : IMessageHandler<C2SRefreshP2PList, IMessage?> {
    override fun onMessage(message: C2SRefreshP2PList, ctx: MessageContext): IMessage? {
        ModNetwork.requestP2PList(ctx.serverHandler.playerEntity, message.type)

        return null
    }
}
