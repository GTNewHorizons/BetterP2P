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
