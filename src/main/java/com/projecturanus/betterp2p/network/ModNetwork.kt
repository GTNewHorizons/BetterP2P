package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.util.p2p.P2PStatus
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import java.util.UUID
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

object ModNetwork {
    val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)
    private var netCache: MutableMap<UUID, NetworkState> = mutableMapOf()
    private lateinit var networkWorker: ScheduledThreadPoolExecutor

    fun registerNetwork() {
        channel.registerMessage(ClientOpenGuiHandler::class.java, S2CListP2P::class.java, 0, Side.CLIENT)
        channel.registerMessage(ClientRefreshInfoHandler::class.java, S2CRefreshInfo::class.java, 1, Side.CLIENT)
        channel.registerMessage(ServerLinkP2PHandler::class.java, C2SLinkP2P::class.java, 2, Side.SERVER)
        channel.registerMessage(ServerCloseGuiHandler::class.java, C2SCloseGui::class.java, 3, Side.SERVER)
        channel.registerMessage(ServerUpdateInfoHandler::class.java, C2SUpdateInfo::class.java, 4, Side.SERVER)
        channel.registerMessage(ServerTransportHandler::class.java, C2STransportPlayer::class.java, 5, Side.SERVER)
        channel.registerMessage(ServerRenameP2PTunnel::class.java, C2SP2PTunnelInfo::class.java, 6, Side.SERVER)
        channel.registerMessage(ServerRefreshP2PListHandler::class.java, C2SRefreshP2PList::class.java, 7, Side.SERVER)
        channel.registerMessage(ServerUnlinkP2PHandler::class.java, C2SUnlinkP2P::class.java, 8, Side.SERVER)
        networkWorker = ScheduledThreadPoolExecutor(1, ThreadFactory {
            val th = Thread(it)
            th.name = "BetterP2P-NetworkWorker"
            th.isDaemon = true
            th.priority = Thread.MIN_PRIORITY
            th
        })
    }

    /**
     * Utility function that asks for a p2p update. Multiple requests are bundled into 1.
     */
    fun queueP2PListUpdate(status: P2PStatus, player: EntityPlayer, info: MemoryInfo? = null) {
        synchronized(netCache) {
            val state = netCache.getOrPut(player.uniqueID) { NetworkState() }
            if (state.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                channel.sendTo(S2CListP2P(status.refresh(status.lastP2PType)), player as EntityPlayerMP)
                state.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!state.updatePending) {
                state.updatePending = true
                networkWorker.schedule({
                    if (info == null) {
                        channel.sendTo(S2CListP2P(status.refresh(status.lastP2PType)), player as EntityPlayerMP)
                    } else {
                        channel.sendTo(S2CListP2P(status.refresh(status.lastP2PType), info), player as EntityPlayerMP)
                    }
                    synchronized(netCache) {
                        state.updatePending = false
                    }
                }, state.updateReady - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            }
        }
    }

    fun stop() {
        networkWorker.shutdown()
    }
}

internal data class NetworkState(
    internal var updatePending: Boolean = false,
    internal var updateReady: Long = System.currentTimeMillis()
)

/**
 * Network cooldown time in milliseconds
 */
const val NETWORK_CD = 250L

