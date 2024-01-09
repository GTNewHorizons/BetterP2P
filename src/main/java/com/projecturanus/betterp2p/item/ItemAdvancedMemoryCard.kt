package com.projecturanus.betterp2p.item

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGridHost
import appeng.api.networking.security.ISecurityGrid
import appeng.core.CreativeTab
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.gui.widget.GuiScale
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.data.*
import com.projecturanus.betterp2p.util.getPart
import com.projecturanus.betterp2p.util.p2p.getTypeIndex
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.common.util.ForgeDirection
import java.util.*

object ItemAdvancedMemoryCard : Item() {
    init {
        maxStackSize = 1
        unlocalizedName = "advanced_memory_card"
        creativeTab = CreativeTab.instance
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected)
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, player: EntityPlayer, tooltip: MutableList<Any?>, boolean: Boolean) {
        val info = getInfo(stack)
        tooltip += StatCollector.translateToLocal("gui.advanced_memory_card.mode.${info.mode.name.lowercase(Locale.getDefault())}")
    }

    @SideOnly(Side.CLIENT)
    private fun clearClientCache() {
        ClientCache.clear()
    }

    override fun onItemRightClick(itemstack: ItemStack, worldIn: World, playerIn: EntityPlayer): ItemStack {
        if (playerIn.isSneaking && worldIn.isRemote) {
            clearClientCache()
        }
        return super.onItemRightClick(itemstack, worldIn, playerIn)
    }

    override fun onItemUse(itemstack: ItemStack, player: EntityPlayer, w: World, x: Int, y: Int, z: Int, side: Int, hx: Float, hy: Float, hz: Float): Boolean {
        if (!w.isRemote) {
            val te = w.getTileEntity(x, y, z)
            if (te is IGridHost && te.getGridNode(ForgeDirection.getOrientation(side)) != null) {
                val part = getPart(w, x, y, z, hx, hy, hz)
                val grid = part?.gridNode?.grid ?: return false

                if (grid is ISecurityGrid && !grid.hasPermission(player, SecurityPermissions.BUILD)) {
                    // Check security grid permissions. Only BUILD is required.
                    return false
                }

                val stack = player.heldItem
                val info = getInfo(stack)
                val type: Int

                if (part is PartP2PTunnel<*>) {
                    type = part.getTypeIndex()
                    info.selectedEntry = part.toLoc()
                } else {
                    type = TUNNEL_ANY
                    info.selectedEntry = null
                }
                info.type = type
                writeInfo(stack, info)
                ModNetwork.initConnection(player, grid, info)
                return true
            }
        }
        return false
    }

    override fun doesSneakBypassUse(world: World?, x: Int, y: Int, z: Int, player: EntityPlayer?): Boolean {
        return true
    }

    @SideOnly(Side.CLIENT)
    override fun registerIcons(ri: IIconRegister) {
        itemIcon = ri.registerIcon("$MODID:advanced_memory_card")
    }

    fun getInfo(stack: ItemStack): MemoryInfo {
        if (stack.item != this) throw ClassCastException("Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}")
        // Initialize NBT if it isn't already a thing
        if (stack.tagCompound == null) {
            stack.tagCompound = NBTTagCompound()
        }
        val compound = stack.tagCompound!!
        if (!compound.hasKey("gui")) {
            compound.setByte("gui", GuiScale.DYNAMIC.ordinal.toByte())
        }
        if (!compound.hasKey("selectedIndex", Constants.NBT.TAG_COMPOUND)) {
            compound.setTag("selectedIndex", NBTTagCompound())
        }
        return MemoryInfo(
                selectedEntry = readP2PLocation(compound.getCompoundTag("selectedIndex")),
                frequency = compound.getLong("frequency"),
                mode = BetterMemoryCardModes.values()[compound.getInteger("mode")],
                guiScale = GuiScale.values()[compound.getByte("gui").toInt()])
    }

    fun writeInfo(stack: ItemStack, info: MemoryInfo) {
        if (stack.item != this) throw ClassCastException("Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}")

        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        val compound = stack.tagCompound!!
        compound.setTag("selectedIndex", writeP2PLocation(info.selectedEntry))
        compound.setLong("frequency", info.frequency)
        compound.setInteger("mode", info.mode.ordinal)
        compound.setByte("gui", info.guiScale.ordinal.toByte())
    }
}
