package com.projecturanus.betterp2p.network.packet

import com.projecturanus.betterp2p.network.ModNetwork

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

class C2SCloseGui : IMessage {
    override fun fromBytes(buf: ByteBuf) {
    }

    override fun toBytes(buf: ByteBuf) {
    }
}

class ServerCloseGuiHandler : IMessageHandler<C2SCloseGui, IMessage?> {
    override fun onMessage(message: C2SCloseGui, ctx: MessageContext): IMessage? {
        ModNetwork.playerState.remove(ctx.serverHandler.playerEntity.uniqueID)

        return null
    }
}
