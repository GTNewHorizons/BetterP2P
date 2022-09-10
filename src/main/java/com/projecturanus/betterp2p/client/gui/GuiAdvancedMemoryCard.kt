package com.projecturanus.betterp2p.client.gui

import appeng.client.gui.widgets.MEGuiTextField
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.capability.P2PTunnelInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.TextureBound
import com.projecturanus.betterp2p.client.gui.widget.IGuiTextField
import com.projecturanus.betterp2p.client.gui.widget.WidgetP2PDevice
import com.projecturanus.betterp2p.client.gui.widget.WidgetScrollBar
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*

class GuiAdvancedMemoryCard(msg: S2CListP2P) : GuiScreen(), TextureBound {

    private val xSize = 288
    private val ySize = 206
    private val guiLeft: Int by lazy { (width - this.xSize) / 2 }
    private val guiTop: Int by lazy { (height - this.ySize) / 2 }

    private val tableX = 9
    private val tableY = 19

    private var selectedIndex = -1

    private lateinit var scrollBar: WidgetScrollBar
    private lateinit var searchBar: MEGuiTextField
    private lateinit var renameBar: IGuiTextField

    private val infos = msg.infos.map(::InfoWrapper).toMutableList()

    private var sortedInfo = infos.toList()

    private var selectedInfo: InfoWrapper?
        get() = infos.getOrNull(selectedIndex)
        set(value) {
            selectedIndex = value?.index ?: -1
        }

    private val widgetDevices: List<WidgetP2PDevice>
    private var infoOnScreen: List<InfoWrapper>

    private val descriptionLines: MutableList<String> = mutableListOf()
    private var mode = msg.memoryInfo.mode
    private var modeString = getModeString()
    private val modeButton by lazy { GuiButton(0, guiLeft + 8, guiTop + 154, 256, 20, modeString) }

    init {
        selectInfo(msg.memoryInfo.selectedIndex)
        sortInfo()
        infoOnScreen = sortedInfo.take(5)

        val list = mutableListOf<WidgetP2PDevice>()
        for (i in 0..3) {
            list += WidgetP2PDevice(::selectedInfo, ::mode, { sortedInfo.getOrNull(i + scrollBar.currentScroll) }, 0, 0)
        }
        widgetDevices = list.toList()
    }

    override fun initGui() {
        super.initGui()
        checkInfo()
        renameBar = IGuiTextField(0,0)
        renameBar.setMaxStringLength(50)
        scrollBar = WidgetScrollBar()
        searchBar = MEGuiTextField(65, 10)
        searchBar.setMaxStringLength(25)
        searchBar.x = guiLeft + 198
        searchBar.y = guiTop + 5
        scrollBar.displayX = guiLeft + 268
        scrollBar.displayY = guiTop + 19
        scrollBar.height = 114
        scrollBar.setRange(0, infos.size.coerceIn(0..(infos.size - 4).coerceAtLeast(0)), 23)

        for (i in 0..3) {
            widgetDevices[i].x = guiLeft + tableX
            widgetDevices[i].y = guiTop + tableY + 33 * i
        }
    }

    private fun reGenInfoFromText() {
        var tmpInfo = infos.filter {
            it.frequency.toHexString().contains(searchBar.text.uppercase()) ||
            it.frequency.toHexString().format4().contains(searchBar.text.uppercase()) ||
            it.name.lowercase().contains(searchBar.text.lowercase())
        }
        if (searchBar.text.isEmpty())
            tmpInfo = infos
        sortedInfo = tmpInfo.sortedBy {
            if (it.index == selectedIndex) {
                -2 // Put the selected p2p in the front
            } else if (it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency && !it.output) {
                -3 // Put input in the beginning
            } else if (it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency) {
                -1 // Put same frequency in the front
            } else {
                it.frequency + Short.MAX_VALUE
            }
        }
    }

    private fun sortInfo() {
        sortedInfo = infos.sortedBy {
            if (it.index == selectedIndex) {
                -2 // Put the selected p2p in the front
            } else if (it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency && !it.output) {
                -3 // Put input in the beginning
            } else if (it.frequency != 0.toLong() && it.frequency == selectedInfo?.frequency) {
                -1 // Put same frequency in the front
            } else {
                it.frequency + Short.MAX_VALUE
            }
        }
    }

    private fun checkInfo() {
        infos.forEach { it.error = false }
        infos.groupBy { it.frequency }.filter { it.value.none { x -> !x.output } }.forEach { it.value.forEach { info ->
            info.error = true
        } }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        for (info in infos) {
            val wrapper = InfoWrapper(info)
            this.infos[info.index] = wrapper
        }
        checkInfo()
        sortInfo()
        refreshOverlay()
    }

    private fun syncMemoryInfo() {
        ModNetwork.channel.sendToServer(C2SUpdateInfo(MemoryInfo(selectedIndex, selectedInfo?.frequency ?: 0, mode)))
    }

    private fun drawInformation() {
        val x = 8
        var y = 178
        for (line in descriptionLines) {
            fontRendererObj.drawString(line, guiLeft + x, guiTop + y, 0)
            y += fontRendererObj.FONT_HEIGHT
        }
    }

    override fun onGuiClosed() {
        saveP2PChannel()
        syncMemoryInfo()
        ModNetwork.channel.sendToServer(C2SCloseGui())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        scrollBar.draw(this)
        searchBar.drawTextBox()
        for (widget in widgetDevices) {
            widget.render(this, mouseX, mouseY, partialTicks)
        }
        modeButton.drawButton(mc, mouseX, mouseY)

        if (modeButton.func_146115_a()) {
            descriptionLines.clear()
            descriptionLines += I18n.format("gui.advanced_memory_card.desc.mode", I18n.format("gui.advanced_memory_card.mode.${mode.next().name.lowercase(Locale.getDefault())}"))
        } else {
            descriptionLines.clear()
        }
        drawInformation()
        if(renameBar.isVisible && !renameBar.isFocused) renameBar.setFocus(true)
        renameBar.drawTextBox()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun switchMode() {
        // Switch mode
        mode = mode.next()
        modeString = getModeString()
        modeButton.displayString = modeString
        syncMemoryInfo()
    }

    private fun getModeString(): String {
        return I18n.format("gui.advanced_memory_card.mode.${mode.name.lowercase(Locale.getDefault())}")
    }

    private fun findInput(frequency: Long) =
        infos.find { it.frequency == frequency && !it.output }

    private fun selectInfo(index: Int) {
        selectedIndex = index
        syncMemoryInfo()
        refreshOverlay()
    }

    private fun refreshOverlay() {
        if (selectedInfo == null) {
            ClientCache.selectedPosition = null
            ClientCache.selectedFacing = null
        }
        else {
            ClientCache.selectedPosition = arrayListOf(selectedInfo?.posX, selectedInfo?.posY, selectedInfo?.posZ)
            ClientCache.selectedFacing = selectedInfo?.facing
        }
        ClientCache.positions.clear()
        ClientCache.positions.addAll(infos.filter { it.frequency == selectedInfo?.frequency && it != selectedInfo }.map { arrayListOf(it.posX, it.posY, it.posZ) to it.facing })
    }

    private fun onSelectButtonClicked(info: InfoWrapper) {
        selectInfo(info.index)
    }
    private fun onRenameButtonClicked(info:InfoWrapper,index: Int){
        if(isShiftKeyDown()){
            return transportPlayer(info)
        }
        renameBar.isVisible = true
        renameBar.y = (this.guiTop + 6) + (index + 1) * 33
        renameBar.x = this.guiLeft + 60
        renameBar.w = 120
        renameBar.h = 12
        renameBar.text = info.name
        renameBar.setFocus(true,0)
        renameBar.info = info
    }

    private fun saveP2PChannel(){
        for (widget in widgetDevices){
            widget.renderNameTextField = true
        }
        if(renameBar.info != null && renameBar.info.name != renameBar.text){
            val info:InfoWrapper = renameBar.info
            renameBar.text = renameBar.text.trim()
            info.name = renameBar.text
            ModNetwork.channel.sendToServer(C2SP2PTunnelInfo(P2PTunnelInfo(info.posX,info.posY,info.posZ,info.world,info.facing.ordinal,info.name)))
        }
        renameBar.isVisible = false
        renameBar.text = ""
        renameBar.setFocus(false)
        renameBar.info = null
    }

    private fun onBindButtonClicked(info: InfoWrapper) {
        if (selectedIndex == -1) return
        when (mode) {
            BetterMemoryCardModes.INPUT -> {
                BetterP2P.logger.debug("Bind ${info.index} as input")
                ModNetwork.channel.sendToServer(C2SLinkP2P(info.index, selectedIndex))
            }
            BetterMemoryCardModes.OUTPUT -> {
                BetterP2P.logger.debug("Bind ${info.index} as output")
                ModNetwork.channel.sendToServer(C2SLinkP2P(selectedIndex, info.index))
            }
            BetterMemoryCardModes.COPY -> {
                val input = selectedInfo?.frequency?.let { findInput(it) }
                if (input != null)
                    ModNetwork.channel.sendToServer(C2SLinkP2P(input.index, info.index))
            }
        }
    }

    private fun transportPlayer(info: InfoWrapper){
        Minecraft.getMinecraft().thePlayer.closeScreen()
        ModNetwork.channel.sendToServer(C2STransportPlayer(P2PTunnelInfo(info.posX,info.posY,info.posZ,info.world,0)))
    }


    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        var clickRenameButton = false
        for ((index,widget) in widgetDevices.withIndex()) {
            val info = widget.infoSupplier()
            if (info?.selectButton?.mousePressed(mc, mouseX, mouseY) == true) {
                onSelectButtonClicked(widget.infoSupplier()!!)
            }else if (info?.bindButton?.mousePressed(mc, mouseX, mouseY) == true) {
                onBindButtonClicked(widget.infoSupplier()!!)
            }else if (info?.renameButton?.mousePressed(mc, mouseX,mouseY) == true) {
                if(renameBar.info != widget.infoSupplier()!!)
                    saveP2PChannel()
                widget.renderNameTextField = false
                onRenameButtonClicked(widget.infoSupplier()!!,index)
                clickRenameButton = true
            }
        }
        if(!clickRenameButton && renameBar.isVisible){
            saveP2PChannel()
        }
        if (modeButton.mousePressed(mc, mouseX, mouseY)) {
            switchMode()
        }
        scrollBar.click(mouseX, mouseY)
        searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        renameBar.mouseClicked(mouseX,mouseY,mouseButton)
        if (mouseButton == 1 && searchBar.isMouseIn(mouseX, mouseY)) {
            this.searchBar.text = ""
            reGenInfoFromText()
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseClickMove(mouseX: Int, mouseY: Int, clickedMouseButton: Int, timeSinceLastClick: Long) {
        scrollBar.click(mouseX, mouseY)
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val i = Mouse.getEventDWheel()
        if (i != 0 && isShiftKeyDown()) {
            val x = Mouse.getEventX() * width / mc.displayWidth
            val y = height - Mouse.getEventY() * height / mc.displayHeight - 1
//            this.mouseWheelEvent(x, y, i / Math.abs(i))
        } else if (i != 0) {
            scrollBar.wheel(i)
            saveP2PChannel()
        }
    }

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    private fun drawBackground() {
        bindTexture(MODID, "textures/gui/advanced_memory_card.png")
        val tessellator = Tessellator.instance
        val f = 1f / this.xSize.toFloat()
        val f1 = 1f / this.ySize.toFloat()
        val u = 0
        val v = 0
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(guiLeft.toDouble(), (guiTop + this.ySize).toDouble(), 0.0, (u.toFloat() * f).toDouble(), ((v + this.ySize).toFloat() * f1).toDouble())
        tessellator.addVertexWithUV(((guiLeft + this.xSize).toDouble()), ((guiTop + this.ySize).toDouble()), 0.0, ((u + this.xSize).toFloat() * f).toDouble(), ((v + this.ySize).toFloat() * f1).toDouble())
        tessellator.addVertexWithUV((guiLeft + this.xSize).toDouble(), guiTop.toDouble(), 0.0, ((u + this.xSize).toFloat() * f).toDouble(), (v.toFloat() * f1).toDouble())
        tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop.toDouble(), 0.0, (u.toFloat() * f).toDouble(), (v.toFloat() * f1).toDouble())
        tessellator.draw()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun keyTyped(char: Char, key: Int) {
        if(key == Keyboard.KEY_LSHIFT) return
        if(renameBar.isFocused){
            if(key == Keyboard.KEY_RETURN){
                saveP2PChannel()
            }else{
                renameBar.textboxKeyTyped(char, key)
            }
        }else if (searchBar.isFocused && !(char == ' ' && searchBar.text.isEmpty())){
            searchBar.textboxKeyTyped(char, key)
            reGenInfoFromText()
        }
        return super.keyTyped(char, key)
    }
}
