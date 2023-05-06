package com.projecturanus.betterp2p.client.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import com.projecturanus.betterp2p.client.ClientCache;
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import kotlin.Pair;

public class RenderHandler {

    @SubscribeEvent
    public void renderOverlays(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player.getHeldItem() != null && player.getHeldItem().getItem() == ItemAdvancedMemoryCard.INSTANCE) {
            if (!ClientCache.INSTANCE.getPositions().isEmpty() || ClientCache.INSTANCE.getSelectedPosition() != null) {
                if (ClientCache.INSTANCE.getSelectedPosition() != null) {
                    List<Pair<List<Integer>, ForgeDirection>> tmp = new ArrayList<>();
                    tmp.add(
                            new Pair<>(
                                    ClientCache.INSTANCE.getSelectedPosition(),
                                    ClientCache.INSTANCE.getSelectedFacing()));
                    OutlineRenderer.renderOutlinesWithFacing(event, player, tmp, 0x45, 0xDA, 0x75);
                }
                OutlineRenderer
                        .renderOutlinesWithFacing(event, player, ClientCache.INSTANCE.getPositions(), 0x66, 0xCC, 0xFF);
            }
        }
    }

    public static void register() {
        RenderHandler handler = new RenderHandler();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }
}
