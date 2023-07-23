package com.projecturanus.betterp2p.network

import net.minecraftforge.common.MinecraftForge

import cpw.mods.fml.common.FMLCommonHandler
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.PlayerEvent

object ServerPlayerDisconnectHandler {

    @SubscribeEvent
    fun onLoggedOut(event: PlayerEvent.PlayerLoggedOutEvent) {
        ModNetwork.removeConnection(event.player)
    }

    fun register() {
        val handler = ServerPlayerDisconnectHandler

        MinecraftForge.EVENT_BUS.register(handler)
        FMLCommonHandler.instance().bus().register(handler)
    }
}
