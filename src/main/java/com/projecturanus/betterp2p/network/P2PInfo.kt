package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.client.gui.InfoWrapper
import net.minecraftforge.common.util.ForgeDirection

class P2PInfo(val frequency: Long,
              val posX: Int,
              val posY: Int,
              val posZ: Int,
              val world: Int,
              val facing: ForgeDirection,
              val name: String,
              val output: Boolean,
              val hasChannel: Boolean) {

    override fun hashCode(): Int {
        return hashP2P(posX, posY, posZ, facing.ordinal, world).hashCode()
    }
    override fun equals(other: Any?): Boolean {
        return if (other is InfoWrapper) {
            this.posX == other.posX &&
                this.posY == other.posY &&
                this.posZ == other.posZ &&
                this.facing == other.facing
        } else {
            false
        }
    }

}
