package com.projecturanus.betterp2p.util.p2p

import appeng.api.networking.IGrid
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.network.hashP2P
import com.projecturanus.betterp2p.util.listAllGridP2P
import net.minecraft.entity.player.EntityPlayer

/**
 * When the player uses the adv memory card, this is cached on the server side
 * to provide access to the Grid when the player performs actions in the GUI.
 */
class P2PStatus(val grid: IGrid, val player: EntityPlayer, type: Int) {
    /**
     * The P2P list. This is a list of all P2Ps, and only the requested
     * ones are sent.
     */
    val listP2P: MutableMap<Long, PartP2PTunnel<*>> = mutableMapOf()

    /**
     * Server copy of the p2p currently selected in client
     */
    var lastP2PType: Int = type


    /**
     * Refreshes the p2p list
     */
    private fun rebuildList() {
        listP2P.clear()
        listAllGridP2P(grid, player).forEach { listP2P[hashP2P(it)] = it }
    }

    /**
     * Refreshes and gets the p2p list of the targeted type
     * @param type if TUNNEL_ANY or invalid, returns the full list; else
     *             filters the list to the targeted type
     */
    fun refresh(type: Int): List<PartP2PTunnel<*>> {
        rebuildList()
        return if (type == TUNNEL_ANY) {
            listP2P.values.toList()
        } else {
            listP2P.values.filter {
                (BetterP2P.proxy.getP2PFromClass(it.javaClass)?.index ?: TUNNEL_ANY) == type
            }.toList()
        }
    }
}

fun areP2PEqual(a: PartP2PTunnel<*>?, b: PartP2PTunnel<*>?): Boolean {
    if (a == b) {
        return true;
    } else if(a != null && b != null) {
        return hashP2P(a) == hashP2P(b)
    }
    return false;
}
