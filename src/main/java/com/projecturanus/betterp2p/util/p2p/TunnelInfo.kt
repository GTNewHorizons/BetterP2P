package com.projecturanus.betterp2p.util.p2p

import appeng.parts.p2p.PartP2PTunnel
import net.minecraft.item.ItemStack
import net.minecraft.util.IIcon

/**
 * Common tunnel info to be used on server
 */
open class TunnelInfo(val index: Int, val stack: ItemStack, val clazz: Class<out PartP2PTunnel<*>>) {
    val dispName: String =
        stack.item.getItemStackDisplayName(stack).split(" - ").getOrNull(1) ?: "§c<Unknown P2P Type>"
}

/**
 * Client tunnel info contains icon info too. Because textures are not
 * loaded until after postInit, we need to use a supplier, unfortunately.
 */
class ClientTunnelInfo(index: Int, stack: ItemStack, clazz: Class<out PartP2PTunnel<*>>,
                       val icon: () -> IIcon): TunnelInfo(index, stack, clazz)
