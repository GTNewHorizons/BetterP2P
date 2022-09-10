package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.client.gui.InfoWrapper
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.resources.I18n
import java.awt.Color
import kotlin.reflect.KMutableProperty0

class WidgetP2PDevice(private val selectedInfoProperty: KMutableProperty0<InfoWrapper?>, val modeSupplier: () -> BetterMemoryCardModes, val infoSupplier: () -> InfoWrapper?, var x: Int, var y: Int): Widget() {
    private val outputColor = 0x4566ccff
    private val selectedColor = 0x4545DA75
    private val errorColor = 0x45DA4527
    private val inactiveColor = 0x45FFEA05

    private val rowWidth = 203
    private val rowHeight = 32
    public var renderNameTextField = true

    private var selectedInfo: InfoWrapper?
        get() = selectedInfoProperty.get()
        set(value) {
            selectedInfoProperty.set(value)
        }

    fun render(gui: GuiScreen, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val info = infoSupplier()
        if (info != null) {
            val fontRenderer = gui.mc.fontRenderer
            if (selectedInfo?.index == info.index)
                GuiScreen.drawRect(x, y, x + rowWidth, y + rowHeight, selectedColor)
            else if (info.error) {
                // P2P output without an input
                GuiScreen.drawRect(x, y, x + rowWidth, y + rowHeight, errorColor)
            }
            else if (!info.hasChannel && info.frequency != 0.toLong()) {
                // No channel
                GuiScreen.drawRect(x, y, x + rowWidth, y + rowHeight, inactiveColor)
            }
            else if (selectedInfo?.frequency == info.frequency && info.frequency != 0.toLong()) {
                // Show same frequency
                GuiScreen.drawRect(x, y, x + rowWidth, y + rowHeight, outputColor)
            }

            fontRenderer.drawString(info.description, x + 24, y + 3, 0)
            fontRenderer.drawString(I18n.format("gui.advanced_memory_card.pos", info.posX, info.posY, info.posZ, info.facing.name), x + 24, y + 12, 0)
            if(renderNameTextField){
                fontRenderer.drawString(I18n.format("gui.advanced_memory_card.name", info.name), x + 24, y + 21, 0)
            }else{
                fontRenderer.drawString(I18n.format("gui.advanced_memory_card.name", ""), x + 24, y + 21, 0)
            }

            if (selectedInfo == null) {
                info.bindButton.enabled = false
                info.selectButton.enabled = true
            } else if (info.index != selectedInfo?.index) {
                info.bindButton.enabled = true
                info.selectButton.enabled = true

            } else {
                // TODO Unbind
                info.bindButton.enabled = false
                info.selectButton.enabled = false
            }

            val mode = modeSupplier()
            if (mode == BetterMemoryCardModes.COPY && !info.output && info.frequency != 0.toLong()) {
                info.bindButton.enabled = false
            }
            drawButtons(gui, info, mouseX, mouseY, partialTicks)
            drawP2PColors(gui, info.frequency, x, y)
        }
    }

    private fun drawP2PColors(gui: GuiScreen, frequency: Long, x: Int, y: Int) {
        drawRectBorder(x + 9, y + 9, 3, 3, Color.BLACK.rgb)
        for (row in 0..1) {
            for (col in 0..1) {
                for (colorIndex in 0..3) {
                    val offsetX: Int = colorIndex % 2
                    val offsetY: Int = colorIndex / 2
                    drawHorizontalLine(x + 7 + col * 6 + offsetX, x + 8 + col * 6 + offsetX, y + 7 + row * 6 + offsetY, Color.BLACK.rgb)
                }
                drawRectBorder(x + 6 + col * 6, y + 6 + row * 6, 3, 3, Color.BLACK.rgb)
            }
        }
    }

    private fun drawButtons(gui: GuiScreen, info: InfoWrapper, mouseX: Int, mouseY: Int, partialTicks: Float) {
        info.renameButton.xPosition = x
        info.renameButton.width = rowWidth
        info.renameButton.yPosition = y
        info.renameButton.height = rowHeight
        if (!info.bindButton.enabled && info.selectButton.enabled) {
            info.bindButton.enabled = false
            info.selectButton.enabled = true

            info.selectButton.xPosition = x + 172
            info.selectButton.width = 28
            info.selectButton.yPosition = y + 6
            info.selectButton.drawButton(gui.mc, mouseX, mouseY)
        } else if (info.bindButton.enabled && info.selectButton.enabled) {
            info.bindButton.enabled = true
            info.selectButton.enabled = true

            // Select button on the left
            info.selectButton.xPosition = x + 141
            info.selectButton.width = 28
            info.selectButton.yPosition = y + 6
            info.selectButton.drawButton(gui.mc, mouseX, mouseY)

            // Bind button on the right
            info.bindButton.xPosition = x + 172
            info.bindButton.width = 28
            info.bindButton.yPosition = y + 6
            info.bindButton.drawButton(gui.mc, mouseX, mouseY)
        } else if (!info.selectButton.enabled && !info.bindButton.enabled) {
            // TODO Unbind
            info.bindButton.enabled = false
            info.selectButton.enabled = false
        }
    }

}
