package com.projecturanus.betterp2p.util.p2p

import appeng.api.networking.IGrid
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.network.hashP2P
import com.projecturanus.betterp2p.util.listAllGridP2P
import com.projecturanus.betterp2p.util.listTargetGridP2P
import net.minecraft.entity.player.EntityPlayer

class P2PStatus(player: EntityPlayer, grid: IGrid, val targetP2P: PartP2PTunnel<*>? = null) {
    val listP2P: MutableMap<Long, PartP2PTunnel<*>> = mutableMapOf()

    init {
        if (targetP2P == null) {
            listAllGridP2P(grid, player).forEach { listP2P[hashP2P(it)] = it }
        } else {
            listTargetGridP2P(grid, player, targetP2P.javaClass)
                .forEach { listP2P[hashP2P(it)] = it }
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
