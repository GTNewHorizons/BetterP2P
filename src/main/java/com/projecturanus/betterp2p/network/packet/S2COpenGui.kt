package com.projecturanus.betterp2p.network.packet

import net.minecraft.client.Minecraft

import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import com.projecturanus.betterp2p.network.data.*
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import io.netty.buffer.ByteBuf

/**
 * A Server->Client packet that sends a list of P2Ps over the net.
 */
class S2COpenGui(var infos: List<P2PInfo> = emptyList(),
                 var memoryInfo: MemoryInfo = MemoryInfo()) : IMessage {
    override fun fromBytes(buf: ByteBuf) {
        val length = buf.readInt()
        val list = ArrayList<P2PInfo>(length)

        for (i in 0 until length) {
            val info = readP2PInfo(buf)

            if (info != null) {
                list.add(info)
            }
        }

        infos = list
        memoryInfo = readMemoryInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(infos.size)
        infos.forEach { writeP2PInfo(buf, it) }
        writeMemoryInfo(buf, memoryInfo)
    }
}

/**
 * Handler on client side for GUI open.
 */
class ClientOpenGuiHandler : IMessageHandler<S2COpenGui, IMessage?> {
    @SideOnly(Side.CLIENT)
    override fun onMessage(message: S2COpenGui, ctx: MessageContext): IMessage? {
        val gui = Minecraft.getMinecraft().currentScreen
        if (gui is GuiAdvancedMemoryCard) {
            gui.refreshInfo(message.infos)
        } else {
            Minecraft.getMinecraft().displayGuiScreen(GuiAdvancedMemoryCard(message))
        }
        return null
    }
}

