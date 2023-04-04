package com.projecturanus.betterp2p.item

import appeng.api.networking.IGridHost
import appeng.core.CreativeTab
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.gui.widget.GuiScale
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.NONE
import com.projecturanus.betterp2p.network.S2CListP2P
import com.projecturanus.betterp2p.network.hashP2P
import com.projecturanus.betterp2p.util.getPart
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.P2PStatus
import com.projecturanus.betterp2p.util.p2p.areP2PEqual
import com.projecturanus.betterp2p.util.p2p.toInfo
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
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

    private fun sendStatus(status: P2PStatus, info: MemoryInfo, player: EntityPlayerMP) {
        P2PCache.statusMap[player.uniqueID] = status
        ModNetwork.channel.sendTo(
            S2CListP2P(status.listP2P.values.map { p2p -> p2p.toInfo() }, info),
            player
        )
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
                val stack = player.heldItem
                val info = getInfo(stack)
                if (part is PartP2PTunnel<*>) {
                    val status = P2PStatus(player, part.gridNode.grid, part)
                    val p2p = status.listP2P.values.firstOrNull { areP2PEqual(it, status.targetP2P) }
                    if (p2p != null) {
                        info.selectedEntry = hashP2P(p2p)
                        writeInfo(stack, info)

                        sendStatus(status, info, player as EntityPlayerMP)
                        return true
                    }
                }
                // Fallback to sending the whole network
                val node = te.getGridNode(ForgeDirection.getOrientation(side))!!
                info.selectedEntry = NONE
                writeInfo(stack, info)
                sendStatus(P2PStatus(player, node.grid), info, player as EntityPlayerMP)
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

        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        val compound = stack.tagCompound!!
        if (!compound.hasKey("gui")) compound.setByte("gui", GuiScale.DYNAMIC.ordinal.toByte())
        if (!compound.hasKey("selectedIndex", Constants.NBT.TAG_LONG)) compound.setLong("selectedIndex", NONE)
        return MemoryInfo(compound.getLong("selectedIndex"), compound.getLong("frequency"), BetterMemoryCardModes.values()[compound.getInteger("mode")], GuiScale.values()[compound.getByte("gui").toInt()])
    }

    fun writeInfo(stack: ItemStack, info: MemoryInfo) {
        if (stack.item != this) throw ClassCastException("Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}")

        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        val compound = stack.tagCompound!!
        compound.setLong("selectedIndex", info.selectedEntry)
        compound.setLong("frequency", info.frequency)
        compound.setInteger("mode", info.mode.ordinal)
        compound.setByte("gui", info.gui.ordinal.toByte())
    }
}
