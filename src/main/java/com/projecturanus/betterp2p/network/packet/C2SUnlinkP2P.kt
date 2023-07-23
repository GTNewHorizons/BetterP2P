package com.projecturanus.betterp2p.network.packet

import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.data.P2PLocation
import com.projecturanus.betterp2p.network.data.TUNNEL_ANY
import com.projecturanus.betterp2p.network.data.readP2PLocation
import com.projecturanus.betterp2p.network.data.writeP2PLocation

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Unlink input from outputs message (set freq to 0)
 */
class C2SUnlinkP2P(var p2p: P2PLocation? = null, var type: Int = TUNNEL_ANY): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        p2p = readP2PLocation(buf)
        type = buf.readByte().toInt()
    }

    override fun toBytes(buf: ByteBuf) {
        // Clientside can crash if they want to send faulty packets :P
        writeP2PLocation(buf, p2p!!)
        buf.writeByte(type)
    }
}

/**
 * Client -> Server
 * Handler on server side
 */
class ServerUnlinkP2PHandler : IMessageHandler<C2SUnlinkP2P, S2COpenGui?> {
    override fun onMessage(message: C2SUnlinkP2P, ctx: MessageContext): S2COpenGui? {
        // message validation
        if (message.p2p == null) {
            return null
        }
        val cache = ModNetwork.playerState[ctx.serverHandler.playerEntity.uniqueID] ?: return null

        cache.gridCache.unlinkP2P(message.p2p!!)
        ModNetwork.requestP2PUpdate(ctx.serverHandler.playerEntity)

        return null
    }
}
