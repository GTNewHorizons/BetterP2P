package com.projecturanus.betterp2p.util.p2p

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import appeng.api.parts.PartItemStack
import appeng.me.GridAccessException
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.network.P2PInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.util.ForgeDirection

fun linkP2P(player: EntityPlayer, inputIndex: Int, outputIndex: Int, status: P2PStatus) : Pair<PartP2PTunnel<*>, PartP2PTunnel<*>>? {
    val input = status.listP2P[inputIndex]
    val output = status.listP2P[outputIndex]

    val grid: IGrid? = input.gridNode?.grid
    if (grid is ISecurityGrid) {
        if (!grid.hasPermission(player, SecurityPermissions.BUILD) || !grid.hasPermission(player, SecurityPermissions.SECURITY)) {
            return null
        }
    }

    // TODO Change to exception
    if (input.javaClass != output.javaClass) {
        // Cannot pair two different type of P2P
        return null
    }
    if (input == output) {
        // Network loop
        return null
    }
    var frequency = input.frequency
    val cache = input.proxy.p2P
    // TODO reduce changes
    if (input.frequency.toInt() == 0 || input.isOutput) {
        frequency = System.currentTimeMillis()
        updateP2P(input, frequency, false, player, input.customName)
    }
    if (cache.getInput(frequency) != null) {
        val originalInput = cache.getInput(frequency)
        if (originalInput != input)
            updateP2P(originalInput, frequency, true, player, input.customName)
    }

    return updateP2P(input, frequency, false, player, input.customName) to updateP2P(output, frequency, true, player, input.customName)
}

/**
 * Due to Applied Energistics' limit
 */
fun updateP2P(tunnel: PartP2PTunnel<*>, frequency: Long, output: Boolean, player: EntityPlayer, name: String): PartP2PTunnel<*> {
    val side = tunnel.side

    tunnel.host.removePart(side, true)

    val data = NBTTagCompound()
    val p2pItem: ItemStack = tunnel.getItemStack(PartItemStack.Wrench)
    val type: String = p2pItem.unlocalizedName
    tunnel.outputProperty = output
    tunnel.customName = name

    p2pItem.writeToNBT(data)
    data.setLong("freq", frequency)

    val newType = ItemStack.loadItemStackFromNBT(data)
    val dir: ForgeDirection = tunnel.host?.addPart(newType, side, player) ?: throw RuntimeException("Cannot bind")
    val newBus: IPart = tunnel.host.getPart(dir)

    if (newBus is PartP2PTunnel<*>) {
        newBus.outputProperty = output
        try {
            val p2p = newBus.proxy.p2P
            p2p.updateFreq(newBus, frequency)
        } catch (e: GridAccessException) {
            // :P
        }
        newBus.onTunnelNetworkChange()
        return newBus
    } else {
        throw RuntimeException("Cannot bind")
    }
}

var PartP2PTunnel<*>.outputProperty
    get() = isOutput
    set(value) {
        val field = PartP2PTunnel::class.java.getDeclaredField("output")
        field.isAccessible = true
        field.setBoolean(this, value)
    }

fun ForgeDirection.getEnumIndex(): Int {
    if (this == ForgeDirection.WEST) {
        return 5
    }
    if (this == ForgeDirection.EAST) {
        return 4
    }
    return this.ordinal
}

val PartP2PTunnel<*>.hasChannel
    get() = isPowered && isActive

fun PartP2PTunnel<*>.getInfo(index: Int)
    = P2PInfo(index, frequency, location.x, location.y, location.z,location.dimension,EnumFacing.getFront(side.getEnumIndex()), customName, isOutput, hasChannel)
