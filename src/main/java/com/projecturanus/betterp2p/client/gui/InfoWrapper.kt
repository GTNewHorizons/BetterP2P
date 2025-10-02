package com.projecturanus.betterp2p.client.gui

import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.network.data.P2PInfo
import com.projecturanus.betterp2p.network.data.P2PLocation
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import net.minecraft.util.IIcon

@SideOnly(Side.CLIENT)
class InfoWrapper(info: P2PInfo) {
    var frequency: Long = info.frequency
        set(value) {
            if (error || value == 0L) {
                hoverInfo[4] = "§c" + I18n.format("gui.advanced_memory_card.p2p_status.unbound")
            } else {
                hoverInfo[4] = "§a" + I18n.format("gui.advanced_memory_card.p2p_status.bound")
            }
            field = value
        }

    val hasChannel = info.hasChannel
    val loc: P2PLocation = P2PLocation(info.posX, info.posY, info.posZ, info.facing, info.dim)
    val output: Boolean = info.output
    val type: Int = info.type

    /**
     * The backing icon (quartz, iron, etc)
     */
    val icon: IIcon

    /**
     * The p2p overlay texture (frame)
     */
    val overlay: IIcon

    val description: String
    val freqDisplay: String by lazy {
        buildString {
            append(I18n.format("item.advanced_memory_card.selected"))
            append(" ")
            if (frequency != 0L) {
                val hex: String = buildString {
                    append((frequency shr 32).toUInt().toString(16).uppercase())
                    append(frequency.toUInt().toString(16).uppercase())
                }.format4()
                append(hex)
            } else {
                append(I18n.format("gui.advanced_memory_card.desc.not_set"))
            }
        }
    }

    val hoverInfo: MutableList<String>

    val channels: String? by lazy {
        if (info.channels >= 0) {
            I18n.format("gui.advanced_memory_card.extra.channel", info.channels)
        } else {
            null
        }
    }

    var name: String = info.name
    var error: Boolean = false

    // Widgets
    val bindButton = GuiButton(0, 0, 0, 34, 20, I18n.format("gui.advanced_memory_card.bind"))
    val renameButton = GuiButton(0, 0, 0, 0, 0,"")
    val unbindButton = GuiButton(0, 0, 0, 34, 20, I18n.format("gui.advanced_memory_card.unbind"))

    init {
        val p2pType: ClientTunnelInfo = BetterP2P.proxy.getP2PFromIndex(info.type) as ClientTunnelInfo
        icon = p2pType.icon()
        overlay = p2pType.stack.iconIndex
        description = buildString {
            append(I18n.format("gui.advanced_memory_card.p2p_status.pre1"))
            append(p2pType.dispName)
            append(I18n.format("gui.advanced_memory_card.p2p_status.pre2"))
            if (output)
                append(I18n.format("gui.advanced_memory_card.p2p_status.output"))
            else
                append(I18n.format("gui.advanced_memory_card.p2p_status.input"))
        }
        val online = info.hasChannel
        hoverInfo = mutableListOf(
            "§bP2P - ${p2pType.dispName}",
            "§e" + I18n.format("gui.advanced_memory_card.pos", info.posX, info.posY, info.posZ),
            "§e" + I18n.format("gui.advanced_memory_card.side", info.facing.name),
            "§e" + I18n.format("gui.advanced_memory_card.dim", info.dim)
        )
        if (error || frequency == 0L) {
            hoverInfo.add("§c" + I18n.format("gui.advanced_memory_card.p2p_status.unbound"))
        } else {
            hoverInfo.add("§a" + I18n.format("gui.advanced_memory_card.p2p_status.bound"))
        }
        if (!online) hoverInfo.add("§c" + I18n.format("gui.advanced_memory_card.p2p_status.offline"))
    }

    override fun hashCode(): Int {
        return loc.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (this.javaClass != other?.javaClass) return false
        other as InfoWrapper

        return this.loc == other.loc
    }
}

fun String.format4(): String {
    val format = StringBuilder()
    for (index in this.indices) {
        if (index % 4 == 0 && index != 0) {
            format.append(" ")
        }
        format.append(this[index])
    }
    return format.toString()
}
