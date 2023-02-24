package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft

class ClientOpenGuiHandler : IMessageHandler<S2CListP2P, IMessage?> {
    @SideOnly(Side.CLIENT)
    override fun onMessage(message: S2CListP2P, ctx: MessageContext): IMessage? {
        val gui = Minecraft.getMinecraft().currentScreen
        if(gui is GuiAdvancedMemoryCard){
            gui.refreshInfo(message.infos)
        }else {
            Minecraft.getMinecraft().displayGuiScreen(GuiAdvancedMemoryCard(message))
        }
        return null
    }
}
