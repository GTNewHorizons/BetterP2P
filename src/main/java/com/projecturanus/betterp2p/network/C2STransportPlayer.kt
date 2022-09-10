package com.projecturanus.betterp2p.network
import com.projecturanus.betterp2p.capability.P2PTunnelInfo
import cpw.mods.fml.common.network.simpleimpl.IMessage
import io.netty.buffer.ByteBuf

class C2STransportPlayer(var info: P2PTunnelInfo = P2PTunnelInfo()):IMessage{
    override fun fromBytes(buf: ByteBuf) {
        info = readP2PTunnelInfo(buf)
    }


    override fun toBytes(buf: ByteBuf) {
        writeP2PTunnelInfo(buf,info)
    }
}
