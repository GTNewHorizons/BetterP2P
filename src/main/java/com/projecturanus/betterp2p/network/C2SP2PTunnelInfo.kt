package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.capability.P2PTunnelInfo
import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

fun readP2PTunnelInfo(buf: ByteBuf): P2PTunnelInfo {
    return P2PTunnelInfo(buf.readInt(),buf.readInt(),buf.readInt(),buf.readInt(),buf.readInt(), buf.readBytes(buf.readInt()).toString(Charsets.UTF_8))

}

fun writeP2PTunnelInfo(buf: ByteBuf,info: P2PTunnelInfo) {
    buf.writeInt(info.posX)
    buf.writeInt(info.posY)
    buf.writeInt(info.posZ)
    buf.writeInt(info.world)
    buf.writeInt(info.side)
    if(info.name ==null) {
        buf.writeInt(0)
    }else{
        val arr = info.name?.toByteArray(Charsets.UTF_8)
        arr?.let { buf.writeInt(it.size) }
        buf.writeBytes(arr)
    }
}


class C2SP2PTunnelInfo(var info: P2PTunnelInfo = P2PTunnelInfo()): IMessage {

    override fun fromBytes(buf: ByteBuf) {
        info = readP2PTunnelInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeP2PTunnelInfo(buf,info)
    }
}
