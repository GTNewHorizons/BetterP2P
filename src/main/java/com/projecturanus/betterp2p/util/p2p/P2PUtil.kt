package com.projecturanus.betterp2p.util.p2p

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import appeng.api.parts.PartItemStack
import appeng.me.GridAccessException
import appeng.me.GridNode
import appeng.me.cache.P2PCache
import appeng.parts.automation.UpgradeInventory
import appeng.parts.p2p.PartP2PInterface
import appeng.parts.p2p.PartP2PTunnel
import appeng.tile.inventory.AppEngInternalInventory
import appeng.util.Platform
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.network.P2PInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

fun linkP2P(player: EntityPlayer, inputIndex: Long, outputIndex: Long, status: P2PStatus) : Pair<PartP2PTunnel<*>, PartP2PTunnel<*>>? {
    // If these calls mess up we have bigger problems...
    val input = status.listP2P[inputIndex]!!
    var output = status.listP2P[outputIndex]!!

    val grid: IGrid? = input.gridNode?.grid
    if (grid is ISecurityGrid) {
        if (!grid.hasPermission(player, SecurityPermissions.BUILD) || !grid.hasPermission(player, SecurityPermissions.SECURITY)) {
            return null
        }
    }

    // TODO Change to exception
    if (input.javaClass != output.javaClass) {
        //change output to input
        val host = output.host
        host.removePart(output.side, false)
        val dir = host.addPart(input.getItemStack(PartItemStack.Wrench), output.side, player)
        val newPart = host.getPart(dir)
        if (newPart is PartP2PTunnel<*>) {
            newPart.outputProperty = true
            newPart.onTunnelNetworkChange()
            try {
                val p2p: P2PCache = newPart.proxy.p2P
                p2p.updateFreq(newPart, output.frequency)
                output = newPart
            } catch (e: GridAccessException) {
                // :P
            }

            println("part changed")
        } else {
            // error
            return null
        }
    }
    if (input == output) {
        // Network loop
        return null
    }
    var frequency = input.frequency
    val cache = input.proxy.p2P
    // TODO reduce changes
    if (input.frequency == 0L || input.isOutput) {
        frequency = System.currentTimeMillis()
    }
    // If tunnel was already bound, unbind that one.
    if (cache.getInput(frequency) != null) {
        val originalInput = cache.getInput(frequency)
        if (originalInput != input) {
            updateP2P(originalInput, frequency, true, player, input.customName)
        }
    }
    val inputResult: PartP2PTunnel<*> = updateP2P(input, frequency, false, player, input.customName)
    val outputResult: PartP2PTunnel<*> = updateP2P(output, frequency, true, player, input.customName)
    if (input is PartP2PInterface && output is PartP2PInterface) {
        // For input and output, retain upgrades, items, and settings.
        inputResult as PartP2PInterface; outputResult as PartP2PInterface
        val upgradesIn = input.interfaceDuality.getInventoryByName("upgrades") as UpgradeInventory
        upgradesIn.forEachIndexed { index, stack ->
            (inputResult.interfaceDuality.getInventoryByName("upgrades") as UpgradeInventory).setInventorySlotContents(index, stack)
        }
        val upgradesOut = output.interfaceDuality.getInventoryByName("upgrades") as UpgradeInventory
        upgradesOut.forEachIndexed { index, stack ->
            (outputResult.interfaceDuality.getInventoryByName("upgrades") as UpgradeInventory).setInventorySlotContents(index, stack)
        }
        val itemsIn = input.interfaceDuality.storage as AppEngInternalInventory
        itemsIn.forEachIndexed { index, stack ->
            (inputResult.interfaceDuality.storage as AppEngInternalInventory).setInventorySlotContents(index, stack)
        }
        val itemsOut = output.interfaceDuality.storage as AppEngInternalInventory
        itemsOut.forEachIndexed { index, stack ->
            (outputResult.interfaceDuality.storage as AppEngInternalInventory).setInventorySlotContents(index, stack)
        }
        val settingsIn = input.interfaceDuality.configManager
        settingsIn.settings.forEach {
            inputResult.configManager.putSetting(it, settingsIn.getSetting(it))
        }
        val settingsOut = output.interfaceDuality.configManager
        settingsOut.settings.forEach {
            outputResult.configManager.putSetting(it, settingsOut.getSetting(it))
        }

        // For input, just copy the patterns over.
        val patternsIn = input.interfaceDuality.patterns as AppEngInternalInventory
        patternsIn.forEachIndexed { index, stack ->
            (inputResult.interfaceDuality.patterns as AppEngInternalInventory).setInventorySlotContents(index, stack)
        }
        // For output, drop items.
        val dropItems = mutableListOf<ItemStack>()
        val patternsOut = output.interfaceDuality.patterns as AppEngInternalInventory
        dropItems.addAll(patternsOut)
        Platform.spawnDrops(player.worldObj, output.location.x, output.location.y, output.location.z, dropItems)
    }
    return inputResult to outputResult
}

/**
 * Due to Applied Energistics' limit
 */
fun updateP2P(tunnel: PartP2PTunnel<*>, frequency: Long, output: Boolean, player: EntityPlayer, name: String): PartP2PTunnel<*> {
    val side = tunnel.side
    val data = NBTTagCompound()

    tunnel.host.removePart(side, true)

    val p2pItem: ItemStack = tunnel.getItemStack(PartItemStack.Wrench)

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

val PartP2PTunnel<*>.hasChannel
    get() = isPowered && isActive

fun PartP2PTunnel<*>.toInfo()
    = P2PInfo(frequency, location.x, location.y, location.z, location.dimension, side,
              customName, isOutput, hasChannel, (externalFacingNode as? GridNode)?.usedChannels() ?: -1,
              getTypeIndex())

/**
 * Get the type index or use TUNNEL_ANY
 */
fun PartP2PTunnel<*>.getTypeIndex()
    = BetterP2P.proxy.getP2PFromClass(this.javaClass)?.index ?: TUNNEL_ANY

