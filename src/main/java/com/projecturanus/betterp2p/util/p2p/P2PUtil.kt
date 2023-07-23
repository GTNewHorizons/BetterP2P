package com.projecturanus.betterp2p.util.p2p

import appeng.api.config.SecurityPermissions
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import appeng.api.parts.PartItemStack
import appeng.me.GridAccessException
import appeng.me.GridNode
import appeng.me.cache.P2PCache
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelStatic
import appeng.util.Platform
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.network.data.TUNNEL_ANY
import com.projecturanus.betterp2p.network.data.P2PInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatComponentTranslation
import net.minecraftforge.common.util.ForgeDirection
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

