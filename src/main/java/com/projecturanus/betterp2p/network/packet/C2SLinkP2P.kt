package com.projecturanus.betterp2p.network.packet

import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.data.P2PLocation
import com.projecturanus.betterp2p.network.data.readP2PLocation
import com.projecturanus.betterp2p.network.data.writeP2PLocation

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Client->Server packet that links 2 P2Ps together.
 */
class C2SLinkP2P(var input: P2PLocation? = null, var output: P2PLocation? = null): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        input = readP2PLocation(buf)
        output = readP2PLocation(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeP2PLocation(buf, input!!)
        writeP2PLocation(buf, output!!)
    }
}

class ServerLinkP2PHandler : IMessageHandler<C2SLinkP2P, IMessage?> {
    override fun onMessage(message: C2SLinkP2P, ctx: MessageContext): IMessage? {
        // Validate message
        if (message.input == null || message.output == null) {
            return null
        }
        val state = ModNetwork.playerState[ctx.serverHandler.playerEntity.uniqueID] ?: return null
        val result = state.gridCache.linkP2P(message.input!!, message.output!!)

        if (result != null) {
            ModNetwork.requestP2PUpdate(ctx.serverHandler.playerEntity)
        }

        return null
    }
}
