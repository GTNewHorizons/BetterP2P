package com.projecturanus.betterp2p.network.packet

import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import com.projecturanus.betterp2p.network.data.MemoryInfo
import com.projecturanus.betterp2p.network.data.readMemoryInfo
import com.projecturanus.betterp2p.network.data.writeMemoryInfo

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import io.netty.buffer.ByteBuf

/**
 * Client->Server: Update the advanced memory card data
 */
class C2SUpdateMemoryInfo(var info: MemoryInfo = MemoryInfo()) : IMessage {

    override fun fromBytes(buf: ByteBuf) {
        info = readMemoryInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeMemoryInfo(buf, info)
    }
}

class ServerUpdateInfoHandler : IMessageHandler<C2SUpdateMemoryInfo, IMessage?> {

    override fun onMessage(message: C2SUpdateMemoryInfo, ctx: MessageContext): IMessage? {
        val player = ctx.serverHandler.playerEntity
        val stack = player.heldItem

        if (stack != null && stack.item is ItemAdvancedMemoryCard) {
            ItemAdvancedMemoryCard.writeInfo(stack, message.info)
        }

        return null
    }
}
