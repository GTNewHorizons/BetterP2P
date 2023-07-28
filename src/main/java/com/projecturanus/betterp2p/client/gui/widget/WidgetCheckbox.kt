package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.client.gui.GUI_TEX_HEIGHT
import com.projecturanus.betterp2p.client.gui.GUI_TEX_WIDTH
import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import com.projecturanus.betterp2p.client.gui.drawTexturedQuad
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

/**
 * A simple checkbox widget. Toggles when clicked
 * (x, y) is the top left of the widget.
 */
class WidgetCheckbox(gui: GuiAdvancedMemoryCard, x: Int, y: Int, val localized: String, initVal: Boolean = false):
    WidgetButton(gui, x, y, 0, 0) {

    var isChecked = initVal
        private set

    fun init() {
        width = gui.mc.fontRenderer.getStringWidth(localized) + 16
        height = gui.mc.fontRenderer.FONT_HEIGHT + 3
    }
    override fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean {
        if (super.mousePressed(gui.mc, mouseX, mouseY)) {
            isChecked = !isChecked
            super.func_146113_a(gui.mc.soundHandler)
            gui.updateInfo()
            return true
        } else {
            return false
        }
    }

    override fun draw(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val textColor: Int = if (super.mousePressed(gui.mc, mouseX, mouseY)) {
            0xFF777777.toInt()
        } else {
            0xFF000000.toInt()
        }
        drawRect(xPosition, yPosition, xPosition + 12, yPosition + 12, 0xFF000000.toInt())
        if (isChecked) {
            gui.bindTexture(gui.background)
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
            drawTexturedQuad(Tessellator.instance,
                    x0 = xPosition.toDouble() - 1, y0 = yPosition - 4.0,
                    x1 = xPosition + 15.0, y1 = yPosition + 12.0,
                    256.0 / GUI_TEX_WIDTH, 200.0 / GUI_TEX_HEIGHT,
                    1.0, 231.0 / GUI_TEX_HEIGHT)
        }
        gui.mc.fontRenderer.drawString(localized, xPosition + 16, yPosition + 2, textColor)
    }
}