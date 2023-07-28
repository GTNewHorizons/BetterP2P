package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.client.gui.GUI_TEX_HEIGHT
import com.projecturanus.betterp2p.client.gui.GUI_TEX_WIDTH
import com.projecturanus.betterp2p.client.gui.GuiAdvancedMemoryCard
import com.projecturanus.betterp2p.client.gui.drawTexturedQuad
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

/**
 * Widget button for stuff.
 * @param x
 * @param y
 * @param width
 * @param height
 * @param hoverText - unlocalized text to display when mouse hovering
 */
abstract class WidgetButton(val gui: GuiAdvancedMemoryCard, x: Int, y: Int, width: Int, height: Int):
    GuiButton(0, x, y, width, height, "") {

    private var texX = 0.0
    private var texY = 0.0
    var hoverText: List<String> = listOf()

    abstract fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean

    open fun draw(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
        val tessellator = Tessellator.instance
        drawBG(tessellator, mouseX, mouseY, partialTicks)
        // Draw button icon
        drawTexturedQuad(tessellator, xPosition + 1.0, yPosition + 1.0,
            xPosition + width - 1.0, yPosition + height - 1.0,
            u0 = texX / GUI_TEX_WIDTH, v0 = texY / GUI_TEX_HEIGHT,
            u1 = (texX + width) / GUI_TEX_WIDTH, v1 = (texY + height) / GUI_TEX_HEIGHT)
    }

    open fun drawBG(tessellator: Tessellator, mouseX: Int, mouseY: Int, partialTicks: Float) {
        gui.bindTexture(gui.background)
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        field_146123_n = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height
        val k = getHoverState(field_146123_n)
        GL11.glEnable(GL11.GL_BLEND)
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
        // Draw button backdrop
        drawTexturedQuad(tessellator,
                x0 = xPosition.toDouble(),
                y0 = yPosition.toDouble(),
                x1 = (xPosition + width).toDouble(),
                y1 = (yPosition + height).toDouble(),
                u0 = (32.0 * k) / GUI_TEX_WIDTH, v0 = (232.0) / GUI_TEX_HEIGHT,
                u1 = (32.0 * (k + 1)) / GUI_TEX_WIDTH, v1 = (232.0 + height) / GUI_TEX_HEIGHT)
    }

    fun setPosition(x: Int, y: Int) {
        this.xPosition = x
        this.yPosition = y
    }

    fun setTexCoords(x: Double, y: Double) {
        this.texX = x
        this.texY = y
    }

    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

    fun isHovering(mouseX: Int, mouseY: Int): Boolean {
        return mouseX > xPosition && mouseX < xPosition + width && mouseY > yPosition && mouseY < yPosition + height
    }
}
