package com.projecturanus.betterp2p.network.data

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.security.ISecurityGrid
import appeng.me.GridAccessException
import appeng.me.cache.P2PCache
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelNormal
import appeng.parts.p2p.PartP2PTunnelStatic
import appeng.util.Platform
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import com.projecturanus.betterp2p.util.p2p.getTypeIndex
import com.projecturanus.betterp2p.util.p2p.outputProperty
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ChatComponentTranslation
import net.minecraftforge.common.util.ForgeDirection
import java.util.stream.Collectors

/**
 * When the player uses the adv memory card, this is cached on the server side
 * to provide access to the Grid when the player performs actions in the GUI
 * Each player has a list of p2ps that will be sent. These are tracked in [listP2P].
 */
class GridServerCache(private val grid: IGrid, val player: EntityPlayer, var type: Int) {
    /**
     * The P2P list. On init, this is the full list.
     */
    private val listP2P: MutableMap<P2PLocation, PartP2PTunnel<*>> = mutableMapOf()

    /**
     * The dirty P2P list. Updates are accumulated here and sent altogether.
     */
    private val dirtyP2P: MutableSet<P2PLocation> = mutableSetOf()

    init {
        rebuildList(type)
    }

    /**
     * Refreshes the global p2p list
     */
    private fun rebuildList(type: Int) {
        synchronized(listP2P) {
            listP2P.clear()
            dirtyP2P.clear()
            grid.machinesClasses.forEach {
                // Find all P2P tunnels...
                if (PartP2PTunnel::class.java.isAssignableFrom(it) &&
                        (type == TUNNEL_ANY || BetterP2P.proxy.getP2PFromIndex(type)?.clazz == it)) {
                    grid.getMachines(it).forEach { gridNode ->
                        val p2p = gridNode.machine as PartP2PTunnel<*>
                        listP2P[p2p.toLoc()] = p2p
                    }
                }
            }
        }
    }

    /**
     * Refreshes and gets the p2p list of the targeted type
     * If [type] is [TUNNEL_ANY] or invalid, returns the full list; else filters the list to the
     * targeted type
     */
    fun retrieveP2PList(): List<P2PInfo> {
        if (grid is ISecurityGrid && !grid.hasPermission(player, SecurityPermissions.BUILD)) {
            return emptyList()
        }
        rebuildList(type)

        return listP2P.values.mapNotNull {
            val index = BetterP2P.proxy.getP2PFromClass(it.javaClass)?.index ?: TUNNEL_ANY
            if (type == TUNNEL_ANY || index == type) {
                it.toInfo()
            } else null
        }
    }

    /**
     * Sets the entry to the p2p tunnel and marks it as dirty to be sent in the next network update.
     * Only p2ps of the currently targeted type can be shown
     */
    fun markDirty(key: P2PLocation, p2p: PartP2PTunnel<*>) {
        synchronized(listP2P) {
            if (type == TUNNEL_ANY || p2p.getTypeIndex() == type) {
                listP2P[key] = p2p
                dirtyP2P.add(key)
            }
        }
    }

    /**
     * Returns the list of P2Ps that are currently marked dirty, and clears the dirty list.
     */
    fun getP2PUpdates(): List<P2PInfo> {
        val result = dirtyP2P.mapNotNull {
            listP2P[it]?.toInfo()
        }

        dirtyP2P.clear()

        return result;
    }

    /**
     * Link the two P2P tunnels together. Returns the pair of P2P tunnels on success, or null otherwise.
     */
    fun linkP2P(inputIndex: P2PLocation, outputIndex: P2PLocation):
            Pair<PartP2PTunnel<*>, PartP2PTunnel<*>>? {
        // If these calls mess up we have bigger problems...
        val input = listP2P[inputIndex] ?: return null
        var output = listP2P[outputIndex] ?: return null

        // Double check security permissions. In theory, the cache won't be set up in the first place
        // for intruders.
        if (grid is ISecurityGrid && !grid.hasPermission(player, SecurityPermissions.BUILD)) {
            return null
        }

        //change type if necessary
        if (input.javaClass != output.javaClass) {
            output = changeP2PType(output, BetterP2P.proxy.getP2PFromClass(input.javaClass)!!) ?: return null
        }

        // Network loop
        if (input == output) {
            return null
        }

        // Perform the link
        val memoryCardData : NBTTagCompound

        val inputResult = input.convertToInput(player, null)
        if(inputResult != null) {
            memoryCardData = inputResult.memoryCardData
            markDirty(inputIndex, inputResult)
        } else {
            throw IllegalStateException("Cannot bind")
        }
        val newType = ItemStack.loadItemStackFromNBT(memoryCardData)
        val freq = memoryCardData.getLong("freq")
        val outputResult = output.convertToOutput(player, newType, freq)
        if (outputResult != null) {
            markDirty(outputIndex, outputResult)
        } else {
            throw IllegalStateException("Cannot bind")
        }

        return inputResult to outputResult
    }

    fun unlinkP2P(p2pIndex: P2PLocation): PartP2PTunnel<*>? {
        val tunnel = listP2P[p2pIndex] ?: return null
        val newTunnel = tunnel.unbind(player)
        if(newTunnel != tunnel) {
            markDirty(p2pIndex, newTunnel)
        }
        return newTunnel
    }

    /**
     * Converts one P2P into the type
     */
    private fun changeP2PType(tunnel: PartP2PTunnel<*>, newType: TunnelInfo): PartP2PTunnel<*>? {
        if (BetterP2P.proxy.getP2PFromClass(tunnel.javaClass) == newType) {
            player.addChatMessage(ChatComponentTranslation("gui.advanced_memory_card.error.same_type"))
            return null
        }
        var converted = false
        // Change to a static P2P type
        if (PartP2PTunnelStatic::class.java.isAssignableFrom(newType.clazz)) {
            for ((i, stack) in player.inventory.mainInventory.withIndex()) {
                if (stack?.isItemEqual(newType.stack) == true) {
                    player.inventory.decrStackSize(i, 1)
                    converted = true
                    break
                }
            }
            if (!converted) {
                player.addChatMessage(ChatComponentTranslation("gui.advanced_memory_card.error.missing_items", 1, newType.stack.displayName))
                return null
            }
        }
        // Change from a static P2P type
        if (tunnel is PartP2PTunnelStatic<*>) {
            val attunables = BetterP2P.proxy.getP2PTypeList().stream()
                .filter{e -> PartP2PTunnelNormal::class.java.isAssignableFrom(e.clazz)}
                // move exact type to the front so we check for it first
                .sorted { o1, o2 -> if(o1.equals(newType)) -1 else if(o2.equals(newType)) 1 else 0 }
                .collect(Collectors.toList())
            outer@ for (attunable in attunables) {
                for ((i, stack) in player.inventory.mainInventory.withIndex()) {
                    if (stack?.isItemEqual(attunable.stack) == true) {
                        player.inventory.decrStackSize(i, 1)
                        converted = true
                        break@outer
                    }
                }
            }
            if (!converted) {
                player.addChatMessage(ChatComponentTranslation("gui.advanced_memory_card.error.missing_items", 1, newType.stack.displayName))
                return null
            }
        }

        // handle drops
        if (converted) {
            val drop = ItemStack.copyItemStack(tunnel.itemStack)
            drop.stackSize = 1
            val drops = mutableListOf<ItemStack>(drop)
            tunnel.getDrops(drops, false)
            for (j in drops) {
                player.inventory.addItemStackToInventory(j)
            }
            Platform.spawnDrops(player.worldObj, player.posX.toInt(), player.posY.toInt(), player.posZ.toInt(), drops)
        }

        val host = tunnel.host
        host.removePart(tunnel.side, false)
        val dir = host.addPart(newType.stack, tunnel.side, player)
        val newPart = host.getPart(dir)
        if (newPart is PartP2PTunnel<*>) {
            newPart.outputProperty = tunnel.isOutput
            try {
                val p2p: P2PCache = newPart.proxy.p2P
                p2p.updateFreq(newPart, tunnel.frequency)
                return newPart
            } catch (e: GridAccessException) {
                // :P
            }
        }
        return null
    }

    /**
     * Converts all connected P2Ps to a new type
     */
    fun changeAllP2Ps(p2p: P2PLocation, newType: TunnelInfo): Boolean {
        if (grid is ISecurityGrid && !grid.hasPermission(player, SecurityPermissions.BUILD)) {
            return false
        }

        var tunnel = listP2P[p2p] ?: return false

        try {
            if (tunnel.isOutput && tunnel.input != null) {
                tunnel = tunnel.input
            }

            val outputs = tunnel.outputs.toMutableList()
            if (PartP2PTunnelStatic::class.java.isAssignableFrom(newType.clazz)) {
                val amt = outputs.size + 1
                var hasItems = 0
                for (stack in player.inventory.mainInventory) {
                    if (stack?.isItemEqual(newType.stack) == true) {
                        hasItems += stack.stackSize
                        if (hasItems >= amt) {
                            break
                        }
                    }
                }
                if (hasItems < amt) {
                    player.addChatMessage(ChatComponentTranslation("gui.advanced_memory_card.error.missing_items", amt, newType.stack.displayName))
                    return false
                }
            }
            changeP2PType(tunnel, newType)
            for (o in outputs) {
                changeP2PType(o, newType)
            }
            return true
        } catch (e: GridAccessException) {
            // :P
        }
        return false
    }
}
