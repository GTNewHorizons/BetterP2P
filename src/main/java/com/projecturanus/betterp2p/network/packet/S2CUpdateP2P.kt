package com.projecturanus.betterp2p.network.packet

import net.minecraft.client.Minecraft

import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import com.projecturanus.betterp2p.network.data.P2PInfo
import com.projecturanus.betterp2p.network.data.readP2PInfo
import com.projecturanus.betterp2p.network.data.writeP2PInfo

import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import io.netty.buffer.ByteBuf

/**
 * Server -> Client packet for sending P2P updates. More lightweight than OpenGui.
 */
class S2CUpdateP2P(var infos: List<P2PInfo> = emptyList(), var clear: Boolean = false): IMessage {
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
        clear = buf.readBoolean()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(infos.size)
        infos.forEach { writeP2PInfo(buf, it) }
        buf.writeBoolean(clear)
    }
}

/**
 * Handler on client side for updating P2Ps.
 */
class ClientUpdateP2PHandler : IMessageHandler<S2CUpdateP2P, IMessage?> {
    @SideOnly(Side.CLIENT)
    override fun onMessage(message: S2CUpdateP2P, ctx: MessageContext): IMessage? {
        val gui = Minecraft.getMinecraft().currentScreen

        if (gui is GuiAdvancedMemoryCard) {
            if (message.clear) {
                gui.refreshInfo(message.infos)
            } else {
                gui.updateInfo(message.infos)
            }
        }

        return null
    }
}
