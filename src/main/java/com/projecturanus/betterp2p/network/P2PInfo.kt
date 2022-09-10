package com.projecturanus.betterp2p.network

import net.minecraft.util.EnumFacing

class P2PInfo(val index: Int, val frequency: Long, val posX: Int, val posY: Int, val posZ: Int,val world: Int, val facing: EnumFacing, val name: String, val output: Boolean, val hasChannel: Boolean) {
}
