package com.projecturanus.betterp2p.network

import appeng.parts.p2p.PartP2PTunnel

/**
 * Generates a 64-bit hash code from X, Y, Z, Facing, and Dimension info.
 * bits 0-15: x
 * bits 16-31: y
 * bits 32-47: z
 * bits 48-59: dim
 * bits 60-62: facing
 * bit 63: Reserved
 */
fun hashP2P(x: Int, y: Int, z: Int, facing: Int, dim: Int): Long {
    var ret = facing.toLong() shl 60
    ret = ret or xorCollapse16(x).toLong()
    ret = ret or (xorCollapse16(y).toLong() shl 16)
    ret = ret or (xorCollapse16(z).toLong() shl 32)
    ret = ret or (xorCollapse12(dim).toLong() shl 48)
    return ret
}

fun hashP2P(p: PartP2PTunnel<*>): Long
    = hashP2P(p.location.x, p.location.y, p.location.z, p.side.ordinal, p.location.dimension)

/**
 * Collapse an 32-bit integer into a 16-bit integer through
 * the XOR operator.
 */
private fun xorCollapse16(input: Int): Int {
    var ret = input and 0xFFFF
    ret = ret xor ((input ushr 16) and 0xFFFF)
    return ret
}

/**
 * Collapse an 32-bit integer into a 12-bit integer through
 * the XOR operator.
 */
private fun xorCollapse12(input: Int): Int {
    var ret = input and 0xFFF
    ret = ret xor ((input ushr 12) and 0xFFF)
    ret = ret xor ((input ushr 12) and 0xFFF)
    return ret
}

/**
 * Using `0x80000000` to represent none is selected (aka MSB set)
 */
const val NONE_SELECTED: Long = Long.MIN_VALUE
