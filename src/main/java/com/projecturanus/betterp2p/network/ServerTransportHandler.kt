package com.projecturanus.betterp2p.network


import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.server.MinecraftServer
import net.minecraft.util.ChatComponentText

class ServerTransportHandler : IMessageHandler<C2STransportPlayer, IMessage?> {

    override fun onMessage(message: C2STransportPlayer, ctx: MessageContext): IMessage? {
        val player:EntityPlayerMP = ctx.serverHandler.playerEntity
        val op = MinecraftServer.getServer().configurationManager.func_152596_g(player.gameProfile)
        if(!op){
            player.addChatMessage(ChatComponentText("You are not OP"))
        }else{
            if(player.worldObj.provider.dimensionId!= message.info.world)
                player.travelToDimension(message.info.world)
            player.setPositionAndUpdate(message.info.posX.toDouble(), message.info.posY.toDouble(), message.info.posZ.toDouble())
        }
        return null
    }
}
