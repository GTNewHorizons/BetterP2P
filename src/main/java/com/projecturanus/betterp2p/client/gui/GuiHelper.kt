package com.projecturanus.betterp2p.client.gui

import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.ResourceLocation

fun bindTexture(loc: ResourceLocation) {

}

/**
 * Draws a textured quad.
 *
 * x0, y0 - top left corner
 * x1, y1 - bottom right corner
 * u0, v0 - top left texture corner
 * u1, v1 - bottom right texture corner
 */
fun drawTexturedQuad(tessellator: Tessellator, x0 : Double, y0: Double, x1: Double, y1: Double, u0: Double, v0: Double, u1: Double, v1: Double) {
    tessellator.startDrawingQuads()
    tessellator.addVertexWithUV(x0, y1, 0.0, u0, v1)
    tessellator.addVertexWithUV(x1, y1, 0.0, u1, v1)
    tessellator.addVertexWithUV(x1, y0, 0.0, u1, v0)
    tessellator.addVertexWithUV(x0, y0, 0.0, u0, v0)
    tessellator.draw()
}
