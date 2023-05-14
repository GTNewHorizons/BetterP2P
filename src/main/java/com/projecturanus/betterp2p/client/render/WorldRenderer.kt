package com.projecturanus.betterp2p.client.render

import com.projecturanus.betterp2p.util.GlStateManager
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

/**
 * Render things in world
 * Author: Cyclic
 */
object WorldRenderer {
    fun renderBlockList(blockPosList: List<List<Int>>, center: List<Int>, relX: Double, relY: Double, relZ: Double, red: Float, green: Float, blue: Float) {
        GlStateManager.pushAttrib()
        GlStateManager.pushMatrix()
        // translate to center or te
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F)
        GlStateManager.disableLighting() // so colors are correct
        GlStateManager.disableTexture2D() // no texturing needed
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.enableBlend()
        val alpha = 0.5F
        GlStateManager.color(red, green, blue, alpha)
        if (Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(GL11.GL_SMOOTH)
        else
            GlStateManager.shadeModel(GL11.GL_FLAT)
        for (p in blockPosList) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(
                (center[0] - p[0]) * -1.0F,
                (center[1] - p[1]) * -1.0F,
                (center[2] - p[2]) * -1.0F)
            shadedCube(0.4F)
            GlStateManager.popMatrix()
        }
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        GlStateManager.popMatrix()
        GlStateManager.popAttrib()
    }

    fun shadedCube(scale: Float) {
        val size = 1.0 * scale
        val tessellator = Tessellator.instance
        // Front - anticlockwise vertices
        // Back - clockwise vertices
        tessellator.startDrawing(GL11.GL_QUADS)
        // xy anti-clockwise - front
        tessellator.addVertex(-size, -size, size)
        tessellator.addVertex(size, -size, size)
        tessellator.addVertex(size, size, size)
        tessellator.addVertex(-size, size, size)
        // xy clockwise - back
        tessellator.addVertex(-size, -size, -size)
        tessellator.addVertex(-size, size, -size)
        tessellator.addVertex(size, size, -size)
        tessellator.addVertex(size, -size, -size)
        // anti-clockwise - left
        tessellator.addVertex(-size, -size, -size)
        tessellator.addVertex(-size, -size, size)
        tessellator.addVertex(-size, size, size)
        tessellator.addVertex(-size, size, -size)
        // clockwise - right
        tessellator.addVertex(size, -size, -size)
        tessellator.addVertex(size, size, -size)
        tessellator.addVertex(size, size, size)
        tessellator.addVertex(size, -size, size)
        // anticlockwise - top
        tessellator.addVertex(-size, size, -size)
        tessellator.addVertex(-size, size, size)
        tessellator.addVertex(size, size, size)
        tessellator.addVertex(size, size, -size)
        // clockwise - bottom
        tessellator.addVertex(-size, -size, -size)
        tessellator.addVertex(size, -size, -size)
        tessellator.addVertex(size, -size, size)
        tessellator.addVertex(-size, -size, size)
        tessellator.draw()
    }

}
