package com.projecturanus.betterp2p.client

import net.minecraftforge.common.util.ForgeDirection

object ClientCache {
    const val NO_DIMENSION = Int.MIN_VALUE

    val positions = mutableListOf<Pair<List<Int?>, ForgeDirection>>()
    var overlayDimension: Int = NO_DIMENSION
    var selectedPosition: List<Int?>? = null
    var selectedFacing: ForgeDirection? = null
    var searchText: String= ""
    fun clear() {
        positions.clear()
        overlayDimension = NO_DIMENSION
        selectedPosition = null
        selectedFacing = null
    }
}
