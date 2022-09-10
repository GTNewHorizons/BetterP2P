package com.projecturanus.betterp2p.client.gui

import appeng.client.gui.widgets.MEGuiTextField
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.client.TextureBound
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation

class GuiRenameItem(msg: InfoWrapper): GuiScreen(), TextureBound {

    private val xSize = 238
    private val ySize = 206
    private val guiLeft: Int by lazy { (width - this.xSize) / 2 }
    private val guiTop: Int by lazy { (height - this.ySize) / 2 }
    private lateinit var searchBar: MEGuiTextField

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }
    private fun drawBackground() {
        bindTexture(MODID, "textures/gui/renamer.png")
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, this.xSize, this.ySize)
    }
    override fun initGui() {
        super.initGui()
        searchBar = MEGuiTextField(256, 20)
        searchBar.setMaxStringLength(255)
        searchBar.x = guiLeft + 5
        searchBar.y = guiTop + 30
    }

}
