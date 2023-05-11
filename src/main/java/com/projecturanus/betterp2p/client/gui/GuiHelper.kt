package com.projecturanus.betterp2p.client.gui

import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.IIcon
import org.lwjgl.opengl.GL11

/**
 * Draws a textured quad.
 *
 * x0, y0 - top left corner
 * x1, y1 - bottom right corner
 * u0, v0 - top left texture corner
 * u1, v1 - bottom right texture corner
 */
@SideOnly(Side.CLIENT)
fun drawTexturedQuad(tessellator: Tessellator,
                     x0: Double, y0: Double, x1: Double, y1: Double,
                     u0: Double, v0: Double, u1: Double, v1: Double) {
    tessellator.startDrawingQuads()
    tessellator.addVertexWithUV(x0, y1, 0.0, u0, v1)
    tessellator.addVertexWithUV(x1, y1, 0.0, u1, v1)
    tessellator.addVertexWithUV(x1, y0, 0.0, u1, v0)
    tessellator.addVertexWithUV(x0, y0, 0.0, u0, v0)
    tessellator.draw()
}

/**
 * Draw the icon on the Block Texture Atlas
 */
fun drawBlockIcon(mc: Minecraft, icon: IIcon, overlay: IIcon,
                  x: Int, y: Int, width: Double = 16.0, height: Double = 16.0) {
    val tessellator = Tessellator.instance
    mc.textureManager.bindTexture(mc.renderEngine.getResourceLocation(0))
    GL11.glPushAttrib(GL11.GL_BLEND or GL11.GL_TEXTURE_2D or GL11.GL_COLOR)
    GL11.glEnable(GL11.GL_BLEND)
    GL11.glEnable(GL11.GL_TEXTURE_2D)
    GL11.glColor3f(255f, 255f, 255f)
    OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
    drawTexturedQuad(tessellator,
            x0 = x.toDouble() + 2,
            y0 = y.toDouble() + 2,
            x1 = x + width - 2,
            y1 = y + height - 2,
            u0 = icon.minU.toDouble(), v0 = icon.minV.toDouble(),
            u1 = icon.maxU.toDouble(), v1 = icon.maxV.toDouble())
    drawTexturedQuad(tessellator,
            x0 = x.toDouble(),
            y0 = y.toDouble(),
            x1 = x + width,
            y1 = y + height,
            u0 = overlay.minU.toDouble(), v0 = overlay.minV.toDouble(),
            u1 = overlay.maxU.toDouble(), v1 = overlay.maxV.toDouble())
    GL11.glPopAttrib()
}
