package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import com.projecturanus.betterp2p.client.gui.drawBlockIcon
import com.projecturanus.betterp2p.network.data.TUNNEL_ANY
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import net.minecraft.init.Blocks
import kotlin.math.min

const val ICONS_PER_ROW = 5

/**
 * Select a type.
 */
class WidgetTypeSelector(var x: Int, var y: Int,
                         val p2pTypes: List<ClientTunnelInfo>): Widget() {
    var visible = false
    val width: Int
    val height: Int
    var hoveredIdx: Int = 0
    var useAny = false
    /**
     * Feeds the input into this parent.
     */
    lateinit var parent: ITypeReceiver
    private val translated: List<List<String>>

    init {
        val typeCount = p2pTypes.size //we'll use 5 icons per row?
        width = min(ICONS_PER_ROW, typeCount) * 18 + 8
        height = (typeCount + ICONS_PER_ROW - 1) * 18 / ICONS_PER_ROW + 8
        val list = p2pTypes.map { listOf(it.dispName) }.toMutableList()
        list.add(listOf(I18n.format("gui.advanced_memory_card.types.any")))
        translated = list
    }

    fun render(gui: GuiAdvancedMemoryCard, mouseX: Int, mouseY: Int, partialTicks: Float) {
        // Background
        GuiScreen.drawRect(x, y, x + width, y + height, 0xAA000000.toInt())
        hoveredIdx = -1
        for ((i, type) in p2pTypes.withIndex()) {
            val iconPosX = x + 4 + (i % ICONS_PER_ROW) * 18
            val iconPosY = y + 4 + (i / ICONS_PER_ROW) * 18
            val iconHover = mouseX > iconPosX && mouseX < iconPosX + 18 && mouseY > iconPosY && mouseY < iconPosY + 18
            if (iconHover) {
                hoveredIdx = i
                GuiScreen.drawRect(iconPosX, iconPosY, iconPosX + 18, iconPosY + 18, 0xFF00FF00.toInt())
            }
            drawBlockIcon(gui.mc, type.icon(), type.stack.iconIndex,
                x = iconPosX + 1,
                y = iconPosY + 1)
        }
        if (useAny) {
            val iconPosX = x + 4 + (p2pTypes.size % ICONS_PER_ROW) * 18
            val iconPosY = y + 4 + (p2pTypes.size / ICONS_PER_ROW) * 18
            val iconHover = mouseX > iconPosX && mouseX < iconPosX + 18 && mouseY > iconPosY && mouseY < iconPosY + 18
            if (iconHover) {
                hoveredIdx = p2pTypes.size
                GuiScreen.drawRect(iconPosX, iconPosY, iconPosX + 18, iconPosY + 18, 0xFF00FF00.toInt())
            }
            drawBlockIcon(gui.mc, Blocks.coal_block.getIcon(0, 0), p2pTypes[0].stack.iconIndex,
                    x = iconPosX + 1,
                    y = iconPosY + 1)
            gui.mc.fontRenderer.drawString("?", iconPosX + 6, iconPosY + 6, 0xFFFF0000.toInt())
        }
        if (hoveredIdx != -1) {
            gui.drawHoveringText(translated[hoveredIdx], mouseX, mouseY, gui.mc.fontRenderer)
        }
    }

    fun setPos(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    fun mousePressed(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height) {
            // now check which one we clicked on
            if (hoveredIdx == -1) return
            parent.accept(p2pTypes.getOrNull(hoveredIdx))
        } else {
            this.visible = false
        }
    }
}

interface ITypeReceiver {

    fun accept(type: ClientTunnelInfo?)

    fun x(): Int

    fun y(): Int
}
