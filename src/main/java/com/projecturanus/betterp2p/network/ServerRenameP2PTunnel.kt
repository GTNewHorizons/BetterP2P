package com.projecturanus.betterp2p.network
import appeng.api.networking.IGrid
import appeng.api.networking.IGridHost
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.util.p2p.getEnumIndex
import cpw.mods.fml.common.network.simpleimpl.IMessage
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler
import cpw.mods.fml.common.network.simpleimpl.MessageContext
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World
import net.minecraftforge.common.DimensionManager
import net.minecraftforge.common.util.ForgeDirection
fun getPart(grid: IGrid,message: C2SP2PTunnelInfo): PartP2PTunnel<*>? {
    val listP2P: List<PartP2PTunnel<*>>? = grid.getMachines( PartP2PTunnelME::class.java)?.map { it.machine as PartP2PTunnel<*> }?.toList()
    for (part in listP2P!!){
        var a =part.side.getEnumIndex()
        if(part.side.getEnumIndex() == message.info.side){ return part}
    }
    return null
}

class ServerRenameP2PTunnel : IMessageHandler<C2SP2PTunnelInfo, IMessage?> {
    override fun onMessage(message: C2SP2PTunnelInfo, ctx: MessageContext): IMessage? {
        val world: World =  DimensionManager.getWorld(message.info.world)
        val te: TileEntity = world.getTileEntity(message.info.posX,message.info.posY,message.info.posZ)
        if (te is IGridHost && te.getGridNode(ForgeDirection.getOrientation(message.info.side)) != null) {
            val grid = te.getGridNode(ForgeDirection.getOrientation(message.info.side)).grid
            val p = getPart(grid,message)
            if(p!=null){
                p.customName = message.info.name
            }
        }
        return null
    }
}
