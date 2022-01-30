package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.MODID
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side

object ModNetwork {
    val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)

    fun registerNetwork() {
        channel.registerMessage(ClientOpenGuiHandler::class.java, S2CListP2P::class.java, 0, Side.CLIENT)
        channel.registerMessage(ClientRefreshInfoHandler::class.java, S2CRefreshInfo::class.java, 1, Side.CLIENT)

        channel.registerMessage(ServerLinkP2PHandler::class.java, C2SLinkP2P::class.java, 2, Side.SERVER)
        channel.registerMessage(ServerCloseGuiHandler::class.java, C2SCloseGui::class.java, 3, Side.SERVER)
        channel.registerMessage(ServerUpdateInfoHandler::class.java, C2SUpdateInfo::class.java, 4, Side.SERVER)
    }
}
