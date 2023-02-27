package com.projecturanus.betterp2p.client.gui

import appeng.client.gui.widgets.MEGuiTextField
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.TextureBound
import com.projecturanus.betterp2p.client.gui.widget.*
import com.projecturanus.betterp2p.network.*
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*

const val GUI_WIDTH = 288
const val GUI_TEX_HEIGHT = 200
class GuiAdvancedMemoryCard(msg: S2CListP2P) : GuiScreen(), TextureBound {

    private lateinit var _guiLeft: Lazy<Int>
    private var guiLeft: Int
        get() = _guiLeft.value
        set(value) {
            _guiLeft = lazyOf(value)
        }

    private lateinit var _guiTop: Lazy<Int>
    private var guiTop: Int
        get() = _guiTop.value
        set(value) {
            _guiTop = lazyOf(value)
        }

    private val tableX = 9
    private val tableY = 19

    private var scale = msg.memoryInfo.gui
    private val resizeButton: WidgetButton

    private var _ySize: Lazy<Int> = lazy { 242 }
    private var ySize: Int
        get() = _ySize.value
        set(value) {
            _ySize = lazy { value }
        }

    private val scrollBar: WidgetScrollBar
    private val searchBar: MEGuiTextField

    private val infos = InfoList(msg.infos.map(::InfoWrapper), ::searchText)

    private val searchText: String
        get() = searchBar.text

    private var col: WidgetP2PColumn

    private val descriptionLines: MutableList<String> = mutableListOf()
    private var mode = msg.memoryInfo.mode
    private var modeString = getModeString()
    private val modeButton by lazy { GuiButton(0, 0, 0, 256, 20, modeString) }
    private val sortRules: List<String> by lazy {
        listOf(
            "§b§n" + I18n.format("gui.advanced_memory_card.sortinfo1"),
            "§9@in§7 - " + I18n.format("gui.advanced_memory_card.sortinfo2"),
            "§6@out§7 - " + I18n.format("gui.advanced_memory_card.sortinfo3"),
            "§a@b§7 - " + I18n.format("gui.advanced_memory_card.sortinfo4"),
            "§c@u§7 - " + I18n.format("gui.advanced_memory_card.sortinfo5"),
            "§7" + I18n.format("gui.advanced_memory_card.sortinfo6")
        )
    }

    private val selectedInfo: InfoWrapper?
        get() = infos.selectedInfo

    init {
        infos.select(msg.memoryInfo.selectedEntry)
        scrollBar = WidgetScrollBar(0, 0)
        col = WidgetP2PColumn(this, infos,0, 0,
            ::selectedInfo, ::mode, scrollBar)
        searchBar = MEGuiTextField(100, 10)
        resizeButton = object: WidgetButton(0, 0, 32, 32, { "gui.advanced_memory_card.gui_scale.resize" }) {
            override fun mousePressed(mouseX: Int, mouseY: Int) {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    scale = when(scale) {
                        GuiScale.DYNAMIC -> GuiScale.SMALL
                        GuiScale.SMALL -> GuiScale.NORMAL
                        GuiScale.NORMAL -> GuiScale.LARGE
                        GuiScale.LARGE -> GuiScale.DYNAMIC
                    }
                    initGui()
                    super.func_146113_a(mc.soundHandler)
                }
            }
        }
    }

    // Note this is called on resize too.
    override fun initGui() {
        super.initGui()
        checkInfo()
        val h = height.coerceAtLeast(256)
        if (scale.minHeight > h) {
            scale = GuiScale.DYNAMIC
        }
        val numEntries = scale.size(height - 75)
        ySize = (numEntries * P2P_ENTRY_HEIGHT) + 75 + (numEntries - 1)
        guiTop = (h - ySize) / 2
        guiLeft = (width - GUI_WIDTH) / 2

        scrollBar.displayX = guiLeft + 268
        scrollBar.displayY = guiTop + 19
        scrollBar.height = numEntries * P2P_ENTRY_HEIGHT + (numEntries - 1) - 7
        scrollBar.setRange(0, infos.size.coerceIn(0, (infos.size - numEntries).coerceAtLeast(0)), 23)

        searchBar.setMaxStringLength(40)
        searchBar.x = guiLeft + 163
        searchBar.y = guiTop + 5
        searchBar.text = ClientCache.searchText

        col.resize(scale, h - 75)
        col.setPosition(guiLeft + tableX, guiTop + tableY)

        modeButton.xPosition = guiLeft + 8
        modeButton.yPosition = guiTop + ySize - 52

        resizeButton.setPosition(guiLeft - 32, guiTop + 2)
        infos.refresh()
        checkInfo()
        refreshOverlay()
    }

    private fun checkInfo() {
        infos.filtered.forEach { it.error = false }
        // A P2P entry is considered "errored" if it is an output, and has no inputs.
        infos.filtered.groupBy { it.frequency }.filter { it.value.none { x -> !x.output } }.forEach { it.value.forEach { info ->
            info.error = true
        } }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        this.infos.rebuild(infos.map(::InfoWrapper))
        checkInfo()
        refreshOverlay()
    }

    private fun syncMemoryInfo() {
        ModNetwork.channel.sendToServer(C2SUpdateInfo(MemoryInfo(infos.selectedEntry, selectedInfo?.frequency ?: 0, mode, scale)))
    }

    private fun drawInformation() {
        var y = 214
        for (line in descriptionLines) {
            fontRendererObj.drawString(line, guiLeft + 8, modeButton.yPosition + 20 + 3, 0)
            y += fontRendererObj.FONT_HEIGHT
        }
    }

    override fun onGuiClosed() {
        ClientCache.searchText = searchBar.text
        col.onGuiClosed()
        syncMemoryInfo()
        ModNetwork.channel.sendToServer(C2SCloseGui())
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawBackground()
        scrollBar.draw(this)
        searchBar.drawTextBox()
        col.render(this, mouseX, mouseY, partialTicks)
        modeButton.drawButton(mc, mouseX, mouseY)
        resizeButton.draw(mouseX, mouseY, partialTicks)
        fontRendererObj.drawString(I18n.format("item.advanced_memory_card.name"), guiLeft + tableX, guiTop + 6, 0)
        if (modeButton.func_146115_a()) {
            descriptionLines.clear()
            descriptionLines += I18n.format("gui.advanced_memory_card.desc.mode", I18n.format("gui.advanced_memory_card.mode.${mode.next().name.lowercase(Locale.getDefault())}"))
        } else {
            descriptionLines.clear()
        }
        drawInformation()
        if (searchBar.isMouseIn(mouseX, mouseY)) {
            drawHoveringText(sortRules, guiLeft, guiTop + ySize - 40, fontRendererObj)
        } else if (resizeButton.isHovering(mouseX, mouseY)) {
            drawHoveringText(listOf(I18n.format(resizeButton.hoverText())), mouseX, mouseY, fontRendererObj)
        }
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

    /**
     * Selects the info with the hashCode, if it exists. Otherwise,
     * deselect the entry.
     */
    fun selectInfo(hash: Long) {
        infos.select(hash)
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
        ClientCache.positions.addAll(infos.sorted.filter { it.frequency == selectedInfo?.frequency && it != selectedInfo }.map { arrayListOf(it.posX, it.posY, it.posZ) to it.facing })
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        col.mouseClicked(mouseX, mouseY, mouseButton)
        if (modeButton.mousePressed(mc, mouseX, mouseY)) {
            switchMode()
        }
        scrollBar.click(mouseX, mouseY)
        resizeButton.mousePressed(mouseX, mouseY)
        searchBar.mouseClicked(mouseX, mouseY, mouseButton)
        if (mouseButton == 1 && searchBar.isMouseIn(mouseX, mouseY)) {
            this.searchBar.text = ""
            infos.refilter()
        }
        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    override fun mouseMovedOrUp(mouseX: Int, mouseY: Int, state: Int) {
        super.mouseMovedOrUp(mouseX, mouseY, state)
        if (state != -1) scrollBar.moving = false
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
            col.finishRename()
        }
    }

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    private fun drawBackground() {
        bindTexture(MODID, "textures/gui/advanced_memory_card.png")
        val tessellator = Tessellator.instance
        // Draw the top part
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop + 60.0, 0.0, 0.0, 60.0 / GUI_TEX_HEIGHT)
        tessellator.addVertexWithUV(((guiLeft + GUI_WIDTH).toDouble()), guiTop + 60.0, 0.0, 1.0, 60.0 / GUI_TEX_HEIGHT)
        tessellator.addVertexWithUV((guiLeft + GUI_WIDTH).toDouble(), guiTop.toDouble(), 0.0, 1.0, 0.0)
        tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop.toDouble(), 0.0, 0.0, 0.0)
        tessellator.draw()
        // Draw P2P segments
        val p2pHeight = P2P_ENTRY_HEIGHT + 1.0
        for (i in 0 until scale.size(ySize - 75) - 2) {
            tessellator.startDrawingQuads()
            tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop + 60.0 + p2pHeight * (i + 1), 0.0, 0.0, 102.0 / GUI_TEX_HEIGHT)
            tessellator.addVertexWithUV(((guiLeft + GUI_WIDTH).toDouble()), guiTop + 60.0 + p2pHeight * (i + 1), 0.0, 1.0, 102.0 / GUI_TEX_HEIGHT)
            tessellator.addVertexWithUV((guiLeft + GUI_WIDTH).toDouble(), guiTop + 60.0 + p2pHeight * i, 0.0, 1.0, 60.0 / GUI_TEX_HEIGHT)
            tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop + 60.0 + p2pHeight * i, 0.0, 0.0, 60.0 / GUI_TEX_HEIGHT)
            tessellator.draw()
        }
        // Draw Bottom
        tessellator.startDrawingQuads()
        tessellator.addVertexWithUV(guiLeft.toDouble(), (guiTop + ySize).toDouble(), 0.0, 0.0, 1.0)
        tessellator.addVertexWithUV(((guiLeft + GUI_WIDTH).toDouble()), (guiTop + ySize).toDouble(), 0.0, 1.0, 1.0)
        tessellator.addVertexWithUV((guiLeft + GUI_WIDTH).toDouble(), guiTop + ySize - 98.0, 0.0, 1.0, 102.0 / GUI_TEX_HEIGHT)
        tessellator.addVertexWithUV(guiLeft.toDouble(), guiTop + ySize - 98.0, 0.0, 0.0, 102.0 / GUI_TEX_HEIGHT)
        tessellator.draw()
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun keyTyped(char: Char, key: Int) {
        if(key == Keyboard.KEY_LSHIFT) return
        if (!col.keyTyped(char, key) && !(char.isWhitespace() && searchBar.text.isEmpty()) && searchBar.textboxKeyTyped(char, key)){
            infos.refilter()
        }
        return super.keyTyped(char, key)
    }
}
