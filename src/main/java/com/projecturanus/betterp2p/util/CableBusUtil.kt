package com.projecturanus.betterp2p.util

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import appeng.api.parts.IPartHost
import appeng.api.parts.SelectedPart
import appeng.parts.AEBasePart
import appeng.parts.ICableBusContainer
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelNormal
import appeng.tile.networking.TileCableBus
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraft.world.IBlockAccess

/**
 * @see appeng.block.networking.BlockCableBus.cb
 */
fun getCableBus(w: IBlockAccess, posX: Int, posY: Int, posZ: Int): ICableBusContainer? {
    val te = w.getTileEntity(posX, posY, posZ)
    var out: ICableBusContainer? = null
    if (te is TileCableBus) {
        out = te.cableBus
    }
    return out
}

fun getPart(w: IBlockAccess, posX: Int, posY: Int, posZ: Int, hitX: Float, hitY: Float, hitZ: Float): IPart? {
    val vec = Vec3.createVectorHelper(hitX.toDouble(), hitY.toDouble(), hitZ.toDouble())
    val te = w.getTileEntity(posX, posY, posZ)
    if (te !is IPartHost) return null
    val p: SelectedPart? = (te as IPartHost).selectPart(vec)
    return p?.part
}

/**
 * Return a list of p2p in the part's target grid
 * @param grid Grid
 * @param player Player to check permission
 * @param clazz P2P class type
 * @return a list of p2p tunnel in the target grid, or an empty list
 */
fun listTargetGridP2P(grid: IGrid?, player: EntityPlayer, clazz: Class<out PartP2PTunnel<*>>): List<PartP2PTunnel<*>> {
    if (grid is ISecurityGrid)
        if (!grid.hasPermission(player, SecurityPermissions.BUILD))
            return emptyList()

    return grid?.getMachines(clazz)?.map { it.machine as PartP2PTunnel<*> }?.toList() ?: emptyList()
}

fun listAllGridP2P(grid: IGrid?, player: EntityPlayer): List<PartP2PTunnel<*>> {
    if (grid is ISecurityGrid && !grid.hasPermission(player, SecurityPermissions.BUILD))
        return emptyList()
    val classes = grid?.machinesClasses?.filter {
        // Find all P2P tunnels...
        c -> c.superclass.superclass == PartP2PTunnel::class.java
    } ?: emptyList()
    val ret: MutableList<PartP2PTunnel<*>> = mutableListOf()
    classes.forEach{ c -> ret.addAll(grid?.getMachines(c)?.map { it.machine as PartP2PTunnel<*> } ?: emptyList()) }
    return ret
}

val AEBasePart.facingPosX: Int?
    get() =
        host?.location?.x?.and(side?.offsetX ?: EnumFacing.UP.frontOffsetX)

val AEBasePart.facingPosY: Int?
    get() =
        host?.location?.y?.and(side?.offsetY ?: EnumFacing.UP.frontOffsetY)

val AEBasePart.facingPosZ: Int?
    get() =
        host?.location?.z?.and(side?.offsetZ ?: EnumFacing.UP.frontOffsetZ)

val AEBasePart.facingTile: TileEntity?
    get() {
        if (host.isInWorld) {
            val posX = facingPosX
            val posY = facingPosY
            val posZ = facingPosZ
            if (posX != null && posY != null && posZ != null)
                return host?.location?.world?.getTileEntity(posX, posY, posZ)
        }
        return null
    }
