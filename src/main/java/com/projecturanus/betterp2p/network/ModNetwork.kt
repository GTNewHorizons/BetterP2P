package com.projecturanus.betterp2p.network

import appeng.api.networking.IGrid
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.network.data.GridServerCache
import com.projecturanus.betterp2p.network.data.MemoryInfo
import com.projecturanus.betterp2p.network.packet.C2SCloseGui
import com.projecturanus.betterp2p.network.packet.C2SLinkP2P
import com.projecturanus.betterp2p.network.packet.C2SRefreshP2PList
import com.projecturanus.betterp2p.network.packet.C2SRenameP2P
import com.projecturanus.betterp2p.network.packet.C2STypeChange
import com.projecturanus.betterp2p.network.packet.C2SUnlinkP2P
import com.projecturanus.betterp2p.network.packet.C2SUpdateMemoryInfo
import com.projecturanus.betterp2p.network.packet.ClientOpenGuiHandler
import com.projecturanus.betterp2p.network.packet.ClientUpdateP2PHandler
import com.projecturanus.betterp2p.network.packet.S2COpenGui
import com.projecturanus.betterp2p.network.packet.S2CUpdateP2P
import com.projecturanus.betterp2p.network.packet.ServerCloseGuiHandler
import com.projecturanus.betterp2p.network.packet.ServerLinkP2PHandler
import com.projecturanus.betterp2p.network.packet.ServerRefreshP2PListHandler
import com.projecturanus.betterp2p.network.packet.ServerRenameP2PTunnel
import com.projecturanus.betterp2p.network.packet.ServerTypeChangeHandler
import com.projecturanus.betterp2p.network.packet.ServerUnlinkP2PHandler
import com.projecturanus.betterp2p.network.packet.ServerUpdateInfoHandler

import net.minecraft.entity.player.EntityPlayer

import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper
import cpw.mods.fml.relauncher.Side
import net.minecraft.entity.player.EntityPlayerMP
import java.util.*

/**
 * Network cooldown time in milliseconds
 */
const val NETWORK_CD = 250L

/**
 * Mod network manager. Handles server <-> client communication.
 */
object ModNetwork {
    val channel: SimpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(MODID)

    /** For client requests (changing viewed p2p) */
    val playerState: MutableMap<UUID, PlayerRequest> = Collections.synchronizedMap(WeakHashMap())

    /** Network thread. */
    private lateinit var networkWorker: ScheduledThreadPoolExecutor

    fun registerNetwork() {
        var id = 0
        channel.registerMessage(ClientOpenGuiHandler::class.java, S2COpenGui::class.java, id++, Side.CLIENT)
        channel.registerMessage(ClientUpdateP2PHandler::class.java, S2CUpdateP2P::class.java, id++, Side.CLIENT)
        channel.registerMessage(ServerLinkP2PHandler::class.java, C2SLinkP2P::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerCloseGuiHandler::class.java, C2SCloseGui::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerUpdateInfoHandler::class.java, C2SUpdateMemoryInfo::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerRenameP2PTunnel::class.java, C2SRenameP2P::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerRefreshP2PListHandler::class.java, C2SRefreshP2PList::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerUnlinkP2PHandler::class.java, C2SUnlinkP2P::class.java, id++, Side.SERVER)
        channel.registerMessage(ServerTypeChangeHandler::class.java, C2STypeChange::class.java, id, Side.SERVER)
        networkWorker = ScheduledThreadPoolExecutor(1, ThreadFactory {
            val th = Thread(it)
            th.name = "BetterP2P-NetworkWorker"
            th.isDaemon = true
            th.priority = Thread.MIN_PRIORITY
            th
        })
    }

    /**
     * Utility function that asks for a full refresh of a specific p2p type.
     */
    fun requestP2PList(player: EntityPlayer, type: Int) {
        synchronized(playerState) {
            val playerState = playerState[player.uniqueID] ?: return
            val cache = playerState.gridCache

            cache.type = type
            if (playerState.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                channel.sendTo(S2CUpdateP2P(cache.retrieveP2PList(), true), player as EntityPlayerMP)
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
                println("Sending now")
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule({
                    synchronized(ModNetwork.playerState) {
                        channel.sendTo(S2CUpdateP2P(cache.retrieveP2PList(), true), player as EntityPlayerMP)
                        playerState.updatePending = false
                    }
                }, playerState.updateReady - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            }
        }
    }

    /**
     * Utility function that asks for a p2p update. Multiple requests are bundled into 1.
     * If dirty, only an incremental update is sent.
     */
    fun requestP2PUpdate(player: EntityPlayer) {
        synchronized(playerState) {
            val playerState = playerState[player.uniqueID] ?: return
            val cache = playerState.gridCache

            if (playerState.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                channel.sendTo(S2CUpdateP2P(cache.getP2PUpdates()), player as EntityPlayerMP)
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule({
                    synchronized(ModNetwork.playerState) {
                        channel.sendTo(S2CUpdateP2P(cache.getP2PUpdates()), player as EntityPlayerMP)
                        playerState.updatePending = false
                    }
                }, playerState.updateReady - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            }
        }
    }

    /**
     * Sets up a connection.
     */
    fun initConnection(player: EntityPlayer, grid: IGrid, info: MemoryInfo) {
        val cache = GridServerCache(grid, player, info.type)

        playerState[player.uniqueID] = PlayerRequest(gridCache = cache)
        channel.sendTo(S2COpenGui(cache.retrieveP2PList(), info), player as EntityPlayerMP)
    }

    fun removeConnection(player: EntityPlayer) {
        playerState.remove(player.uniqueID)
    }

    fun stop() {
        networkWorker.shutdown()
    }
}

/**
 * Keeps track of when to send network updates.
 */
data class PlayerRequest (
    internal var updatePending: Boolean = false,
    internal var updateReady: Long = System.currentTimeMillis(),
    val gridCache: GridServerCache,
)
