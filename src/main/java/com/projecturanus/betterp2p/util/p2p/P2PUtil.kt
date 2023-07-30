package com.projecturanus.betterp2p.util.p2p

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.network.data.TUNNEL_ANY

import appeng.parts.p2p.PartP2PTunnel

var PartP2PTunnel<*>.outputProperty
    get() = isOutput
    set(value) {
        val field = PartP2PTunnel::class.java.getDeclaredField("output")
        field.isAccessible = true
        field.setBoolean(this, value)
    }

val PartP2PTunnel<*>.hasChannel
    get() = isPowered && isActive

/**
 * Get the type index or use TUNNEL_ANY
 */
fun PartP2PTunnel<*>.getTypeIndex()
    = BetterP2P.proxy.getP2PFromClass(this.javaClass)?.index ?: TUNNEL_ANY

