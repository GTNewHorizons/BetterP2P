package com.projecturanus.betterp2p.client.gui

import appeng.api.parts.IPart
import appeng.api.parts.IPartItem
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.network.P2PInfo
import com.projecturanus.betterp2p.network.hashP2P
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.resources.I18n
import net.minecraft.init.Blocks
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection

class InfoWrapper(info: P2PInfo) {
    // Basic information
    val code: Long by lazy {
        hashP2P(posX, posY, posZ, facing.ordinal, dim)
    }
    val frequency: Long = info.frequency
    val hasChannel = info.hasChannel
    val posX: Int = info.posX
    val posY: Int = info.posY
    val posZ: Int = info.posZ
    val dim: Int = info.world
    val facing: ForgeDirection = info.facing
    val output: Boolean = info.output

    val icon: IIcon
    val overlay: IIcon

    val typeName: String

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
        set(value) {
            if (value) {
                hoverInfo[4] = "§c" + I18n.format("gui.advanced_memory_card.p2p_status.unbound")
            } else {
                hoverInfo[4] = "§a" + I18n.format("gui.advanced_memory_card.p2p_status.bound")
            }
            field = value
        }

    // Widgets
    val bindButton = GuiButton(0, 0, 0, 34, 20, I18n.format("gui.advanced_memory_card.bind"))
    val renameButton = GuiButton(0, 0, 0, 0, 0,"")

    init {
        val p: IPart? = (info.stack.item as? IPartItem)?.createPartFromItemStack(info.stack)
        if (p is PartP2PTunnel<*>) {
            icon = p.typeTexture
            overlay = info.stack.iconIndex
            typeName =
                info.stack.item.getItemStackDisplayName(info.stack).split(" - ").getOrNull(1)
                    ?: "§c<Something broke...>"
            description = buildString {
                append("Type: ")
                append(typeName)
                append(" - ")
                if (output)
                    append(I18n.format("gui.advanced_memory_card.p2p_status.output"))
                else
                    append(I18n.format("gui.advanced_memory_card.p2p_status.input"))
            }
            val online = info.hasChannel
            hoverInfo = mutableListOf(
                "§bP2P - $typeName",
                "§e" + I18n.format("gui.advanced_memory_card.pos", info.posX, info.posY, info.posZ),
                "§e" + I18n.format("gui.advanced_memory_card.side", info.facing.name),
                "§e" + I18n.format("gui.advanced_memory_card.dim", info.world)
            )
            if (error || frequency == 0L) {
                hoverInfo.add("§c" + I18n.format("gui.advanced_memory_card.p2p_status.unbound"))
            } else {
                hoverInfo.add("§a" + I18n.format("gui.advanced_memory_card.p2p_status.bound"))
            }
            if (!online) hoverInfo.add("§c" + I18n.format("gui.advanced_memory_card.p2p_status.offline"))
        } else {
            icon = Blocks.air.getIcon(0, 0)
            overlay = Blocks.air.getIcon(0, 0)
            typeName = "§c<Something broke...>"
            description = "§c<Unknown>"
            hoverInfo = mutableListOf(
                "§b$typeName§r",
                "§e" + I18n.format("gui.advanced_memory_card.pos", info.posX, info.posY, info.posZ),
                "§e" + I18n.format("gui.advanced_memory_card.side", info.facing.name),
                "§e" + I18n.format("gui.advanced_memory_card.dim", info.world)
            )
        }
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is InfoWrapper) {
            this.posX == other.posX &&
            this.posY == other.posY &&
            this.posZ == other.posZ &&
            this.dim == other.dim &&
            this.facing == other.facing
        } else {
            false
        }
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
