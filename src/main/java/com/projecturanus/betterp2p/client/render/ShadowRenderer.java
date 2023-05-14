package com.projecturanus.betterp2p.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.projecturanus.betterp2p.util.GlStateManager;

/**
 * This class uses the code from BuildersGuides.
 * <a href="https://github.com/Ipsis/BuildersGuides">Source code</a>
 * <a href="https://github.com/Ipsis/BuildersGuides/blob/master/COPYING.LESSER">Source license</a>
 * In particular, two functions of
 * <a href="https://github.com/Ipsis/BuildersGuides/blob/master/src/main/java/ipsis/buildersguides/util/RenderUtils.java">RenderUtils</a>
 * were used.
 */
@SuppressWarnings("serial")
public class ShadowRenderer {

    public static void renderBlockPos(List<Integer> p, List<Integer> center, double relX, double relY, double relZ,
            float red, float green, float blue) {
        if (p == null) {
            return;
        }
        ArrayList<List<Integer>> blockPosList = new ArrayList<>();
        blockPosList.add(p);
        renderBlockList(blockPosList, center, relX, relY, relZ, red, green, blue);
    }

    public static void renderBlockPhantom(World world, final List<Integer> pos, ItemStack stack, final double relX,
            final double relY, final double relZ, List<Integer> target, boolean isSolid) {
        if (stack.getItem() instanceof ItemBlock) {
            int stateFromStack = stack.getItemDamage();
            renderBlockPhantom(world, pos, stateFromStack, relX, relY, relZ, target, isSolid);
        }
    }

    public static void renderBlockPhantom(World world, final List<Integer> pos, int state, final double relX,
            final double relY, final double relZ, List<Integer> target, boolean isSolid) {
        int xOffset = target.get(0) - pos.get(0);
        int yOffset = target.get(1) - pos.get(1);
        int zOffset = target.get(2) - pos.get(2);
        // final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.instance;
        // BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        // this first translate is to make relative to TE and everything
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F);
        RenderHelper.disableStandardItemLighting();
        if (!isSolid) {
            GlStateManager.blendFunc(770, 775);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
        }
        tessellator.startDrawing(GL11.GL_QUADS);
        // move into frame and then back to zero - so world relative
        tessellator.setTranslation(-0.5 - pos.get(0) + xOffset, -.5 - pos.get(1) + yOffset, -.5 - pos.get(2) + zOffset);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        // TODO: pos below is the targetPos, other rel and pos are TE
        // blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, bufferBuilder, false);
        tessellator.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public static void renderBlockList(List<List<Integer>> blockPosList, List<Integer> center, double relX, double relY,
            double relZ, float red, float green, float blue) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        // translate to center or te
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F);
        GlStateManager.disableLighting(); // so colors are correct
        GlStateManager.disableTexture2D(); // no texturing needed
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        float alpha = 0.5F;
        GlStateManager.color(red, green, blue, alpha);
        if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(GL11.GL_SMOOTH);
        else GlStateManager.shadeModel(GL11.GL_FLAT);
        for (List<Integer> p : blockPosList) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    (center.get(0) - p.get(0)) * -1.0F,
                    (center.get(1) - p.get(1)) * -1.0F,
                    (center.get(2) - p.get(2)) * -1.0F);
            shadedCube(0.4F);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private static void shadedCube(float scale) {
        float size = scale;
        Tessellator tessellator = Tessellator.instance;
        // BufferBuilder worldRenderer = tessellator.getBuffer();
        // Front - anticlockwise vertices
        // Back - clockwise vertices
        tessellator.startDrawing(GL11.GL_QUADS);
        // xy anti-clockwise - front
        tessellator.addVertex(-size, -size, size);
        tessellator.addVertex(size, -size, size);
        tessellator.addVertex(size, size, size);
        tessellator.addVertex(-size, size, size);
        // xy clockwise - back
        tessellator.addVertex(-size, -size, -size);
        tessellator.addVertex(-size, size, -size);
        tessellator.addVertex(size, size, -size);
        tessellator.addVertex(size, -size, -size);
        // anti-clockwise - left
        tessellator.addVertex(-size, -size, -size);
        tessellator.addVertex(-size, -size, size);
        tessellator.addVertex(-size, size, size);
        tessellator.addVertex(-size, size, -size);
        // clockwise - right
        tessellator.addVertex(size, -size, -size);
        tessellator.addVertex(size, size, -size);
        tessellator.addVertex(size, size, size);
        tessellator.addVertex(size, -size, size);
        // anticlockwise - top
        tessellator.addVertex(-size, size, -size);
        tessellator.addVertex(-size, size, size);
        tessellator.addVertex(size, size, size);
        tessellator.addVertex(size, size, -size);
        // clockwise - bottom
        tessellator.addVertex(-size, -size, -size);
        tessellator.addVertex(size, -size, -size);
        tessellator.addVertex(size, -size, size);
        tessellator.addVertex(-size, -size, size);
        tessellator.draw();
    }
}
