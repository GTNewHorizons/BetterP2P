package com.projecturanus.betterp2p.network
import appeng.api.networking.IGridHost
import appeng.api.parts.IPartHost
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.toInfo
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection

/**
 * Handler for renaming p2p tunnels.
 */
class ServerRenameP2PTunnel : IMessageHandler<C2SP2PTunnelInfo, IMessage?> {
    override fun onMessage(message: C2SP2PTunnelInfo, ctx: MessageContext): IMessage? {
        val world: World =  DimensionManager.getWorld(message.info.world)
        val te: TileEntity = world.getTileEntity(message.info.posX, message.info.posY, message.info.posZ)
        val cache = P2PCache.statusMap[ctx.serverHandler.playerEntity.uniqueID] ?: return null
        if (te is IGridHost && te.getGridNode(ForgeDirection.getOrientation(message.info.side)) != null) {
            val p = (te as IPartHost).getPart(ForgeDirection.getOrientation(message.info.side)) as PartP2PTunnel<*>
            p.customName = message.info.name
            ModNetwork.channel.sendTo(
                S2CListP2P(cache.refresh(cache.lastP2PType).map { it.toInfo() },
                ItemAdvancedMemoryCard.getInfo(cache.player.heldItem)),
                cache.player as EntityPlayerMP)
        }
        return null
    }
}
