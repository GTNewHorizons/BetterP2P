package com.projecturanus.betterp2p.client.render;

import java.util.Collection;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.projecturanus.betterp2p.util.GlStateManager;

import kotlin.Pair;

/**
 * This class is based on the <a href=
 * "https://github.com/romelo333/notenoughwands1.8.8/blob/20952f50e7c1ab3fd676ed3da302666295e3cac8/src/main/java/romelo333/notenoughwands/Items/GenericWand.java">GenericWand</a>
 * class from a mod with MIT license.
 * <a href="https://github.com/romelo333/notenoughwands1.8.8/blob/master/LICENSE">Source license</a>
 */
public class OutlineRenderer {

    public static void renderOutlines(RenderWorldLastEvent evt, EntityPlayer p, Collection<List<Integer>> coordinates,
            int r, int g, int b) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX);
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY);
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ);
        GlStateManager.pushAttrib();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        renderOutlines(coordinates, r, g, b, 4);
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    private static void renderOutlines(Collection<List<Integer>> coordinates, int r, int g, int b, int thickness) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawing(GL11.GL_LINES);
        GL11.glLineWidth(thickness);
        for (List<Integer> coordinate : coordinates) {
            float x = coordinate.get(0);
            float y = coordinate.get(1);
            float z = coordinate.get(2);
            renderHighLightedBlocksOutline(tessellator, x, y, z, r, g, b, 255); // .02f
        }
        tessellator.draw();
    }

    public static void renderHighLightedBlocksOutline(Tessellator tessellator, float mx, float my, float mz, int r,
            int g, int b, int a) {
        tessellator.setColorRGBA(r, g, b, a);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx + 1, my, mz);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx, my + 1, mz);
        tessellator.addVertex(mx, my, mz);
        tessellator.addVertex(mx, my, mz + 1);
        tessellator.addVertex(mx + 1, my + 1, mz + 1);
        tessellator.addVertex(mx, my + 1, mz + 1);
        tessellator.addVertex(mx + 1, my + 1, mz + 1);
        tessellator.addVertex(mx + 1, my, mz + 1);
        tessellator.addVertex(mx + 1, my + 1, mz + 1);
        tessellator.addVertex(mx + 1, my + 1, mz);
        tessellator.addVertex(mx, my + 1, mz);
        tessellator.addVertex(mx, my + 1, mz + 1);
        tessellator.addVertex(mx, my + 1, mz);
        tessellator.addVertex(mx + 1, my + 1, mz);
        tessellator.addVertex(mx + 1, my, mz);
        tessellator.addVertex(mx + 1, my, mz + 1);
        tessellator.addVertex(mx + 1, my, mz);
        tessellator.addVertex(mx + 1, my + 1, mz);
        tessellator.addVertex(mx, my, mz + 1);
        tessellator.addVertex(mx + 1, my, mz + 1);
        tessellator.addVertex(mx, my, mz + 1);
        tessellator.addVertex(mx, my + 1, mz + 1);
    }

    public static void renderOutlinesWithFacing(RenderWorldLastEvent evt, EntityPlayer p,
            Collection<Pair<List<Integer>, ForgeDirection>> coordinates, int r, int g, int b) {

        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX);
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY);
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ);
        GlStateManager.pushAttrib();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        renderOutlinesWithFacing(coordinates, -doubleX, -doubleY, -doubleZ, r, g, b, 4);
        GlStateManager.popAttrib();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    private static void renderOutlinesWithFacing(Collection<Pair<List<Integer>, ForgeDirection>> coordinates, double x,
            double y, double z, int r, int g, int b, int thickness) {
        Tessellator tessellator = Tessellator.instance;

        for (Pair<List<Integer>, ForgeDirection> coordinate : coordinates) {
            List<Integer> pos = coordinate.component1();
            ForgeDirection facing = coordinate.component2();
            if (pos == null || facing == null || pos.size() != 3) continue;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            tessellator.startDrawing(GL11.GL_LINES);
            GL11.glLineWidth(thickness);
            tessellator.setTranslation(pos.get(0), pos.get(1), pos.get(2));

            renderHighLightedBlocksOutlineForFacing(tessellator, r, g, b, 255);
            tessellator.setTranslation(0, 0, 0);
            GlStateManager.translate(pos.get(0), pos.get(1), pos.get(2));
            GlStateManager.translate(0.5, 0.5, 0.5);

            switch (facing) {
                case DOWN:
                    GL11.glRotated(90, 0, 1, 0);
                    GL11.glRotated(90, 0, 0, 1);
                    break;
                case UP:
                    GL11.glRotated(90, 0, 1, 0);
                    GL11.glRotated(270, 0, 0, 1);
                    break;
                case NORTH:
                    GL11.glRotated(-90, 0, 1, 0);
                    break;
                case SOUTH:
                    GL11.glRotated(90, 0, 1, 0);
                    break;
                case EAST:
                    GL11.glRotated(180, 0, 0, 1);
                    break;
                case WEST:
                    break;
            }
            int[] west_matrix = { 1, 0, 0, 0, 1, 0, 0, 0, 1 };
            int[] east_matrix = { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
            int[] north_matrix = { 0, 0, -1, 0, 1, 0, 1, 0, 0 };
            int[] south_matrix = { 0, 0, 1, 0, 1, 0, -1, 0, 0 };

            GlStateManager.translate(-0.5, -0.5, -0.5);
            GlStateManager.translate(-pos.get(0), -pos.get(1), -pos.get(2));
            tessellator.draw();
            GlStateManager.popMatrix();
        }

    }

    public static void renderHighLightedBlocksOutlineForFacing(Tessellator tessellator, int r, int g, int b, int a) {
        double minX = 0;
        double minY = 0.125;
        double minZ = 0.125;

        double maxX = 0.1875;
        double maxY = 0.875;
        double maxZ = 0.875;

        tessellator.setColorRGBA(r, g, b, a);

        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(maxX, minY, minZ);

        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(minX, maxY, minZ);

        tessellator.addVertex(minX, minY, minZ);
        tessellator.addVertex(minX, minY, maxZ);

        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);

        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(maxX, minY, maxZ);

        tessellator.addVertex(maxX, maxY, maxZ);
        tessellator.addVertex(maxX, maxY, minZ);

        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(minX, maxY, maxZ);

        tessellator.addVertex(minX, maxY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);

        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, minY, maxZ);

        tessellator.addVertex(maxX, minY, minZ);
        tessellator.addVertex(maxX, maxY, minZ);

        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(maxX, minY, maxZ);

        tessellator.addVertex(minX, minY, maxZ);
        tessellator.addVertex(minX, maxY, maxZ);
    }
}
