package com.projecturanus.betterp2p.client.gui

import appeng.client.gui.widgets.MEGuiTextField
import appeng.parts.p2p.PartP2PGT5Power
import appeng.parts.p2p.PartP2PInterface
import appeng.parts.p2p.PartP2PLiquids
import appeng.parts.p2p.PartP2PRedstone
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.BetterP2P
import com.projecturanus.betterp2p.MODID
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.client.TextureBound
import com.projecturanus.betterp2p.client.gui.widget.*
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.item.MAX_TOOLTIP_LENGTH
import com.projecturanus.betterp2p.network.*
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

const val GUI_WIDTH = 288
const val GUI_TEX_HEIGHT = 264
class GuiAdvancedMemoryCard(msg: S2CListP2P) : GuiScreen(), TextureBound {

    private var guiLeft: Int = 0

    private lateinit var _guiTop: Lazy<Int>
    private var guiTop: Int
        get() = _guiTop.value
        set(value) {
            _guiTop = lazyOf(value)
        }

    private val tableX = 9
    private val tableY = 19

    private var _ySize: Lazy<Int> = lazy { 242 }
    private var ySize: Int
        get() = _ySize.value
        set(value) {
            _ySize = lazy { value }
        }

    private var scale = msg.memoryInfo.gui
    private val resizeButton: WidgetButton

    private var mode = msg.memoryInfo.mode
    private val modeButton: WidgetButton

    private var type: ClientTunnelInfo? = BetterP2P.proxy.getP2PFromIndex(msg.memoryInfo.type) as? ClientTunnelInfo
    private val typeButton: WidgetButton

    private val refreshButton: WidgetButton

    private val scrollBar: WidgetScrollBar
    private val searchBar: MEGuiTextField

    private val infos = InfoList(msg.infos.map(::InfoWrapper), ::searchText)

    private val typeSelector: WidgetTypeSelector

    private val searchText: String
        get() = searchBar.text

    private var col: WidgetP2PColumn

    private val sortRules: List<String> by lazy {
        listOf(
            "§b§n" + I18n.format("gui.advanced_memory_card.sortinfo1"),
            "§9@in§7 - " + I18n.format("gui.advanced_memory_card.sortinfo2"),
            "§6@out§7 - " + I18n.format("gui.advanced_memory_card.sortinfo3"),
            "§a@b§7 - " + I18n.format("gui.advanced_memory_card.sortinfo4"),
            "§c@u§7 - " + I18n.format("gui.advanced_memory_card.sortinfo5"),
            "§e@type=<name1>[;<name2>;]...§7 - " + I18n.format("gui.advanced_memory_card.sortinfo6"),
            "§7" + I18n.format("gui.advanced_memory_card.sortinfo7")
        )
    }

    val BACKGROUND: ResourceLocation = ResourceLocation(MODID, "textures/gui/advanced_memory_card.png")
    private val selectedInfo: InfoWrapper?
        get() = infos.selectedInfo

    init {
        infos.select(msg.memoryInfo.selectedEntry)
        scrollBar = WidgetScrollBar(0, 0)
        col = WidgetP2PColumn(this, infos,0, 0,
            ::selectedInfo, ::mode, scrollBar)
        searchBar = MEGuiTextField(100, 10)
        resizeButton = object: WidgetButton(this, 0, 0, 32, 32) {
            override fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    if (button == 0) {
                        scale = when (scale) {
                            GuiScale.DYNAMIC -> GuiScale.SMALL
                            GuiScale.SMALL -> GuiScale.NORMAL
                            GuiScale.NORMAL -> GuiScale.LARGE
                            GuiScale.LARGE -> GuiScale.DYNAMIC
                        }
                    } else if (button == 1) {
                        scale = when (scale) {
                            GuiScale.DYNAMIC -> GuiScale.LARGE
                            GuiScale.LARGE -> GuiScale.NORMAL
                            GuiScale.NORMAL -> GuiScale.SMALL
                            GuiScale.SMALL -> GuiScale.DYNAMIC
                        }
                    }
                    initGui()
                    super.func_146113_a(mc.soundHandler)
                    return true
                }
                return false
            }
        }

        modeButton = object: WidgetButton(this, 0, 0, 32, 32) {
            val modeDescriptions: List<List<String>> = listOf(
                fmtTooltips(
                    title = BetterMemoryCardModes.OUTPUT.unlocalizedName,
                    keys = BetterMemoryCardModes.OUTPUT.unlocalizedDesc,
                    maxChars = MAX_TOOLTIP_LENGTH,
                ),
                fmtTooltips(
                    title = BetterMemoryCardModes.INPUT.unlocalizedName,
                    keys = BetterMemoryCardModes.INPUT.unlocalizedDesc,
                    maxChars = MAX_TOOLTIP_LENGTH,
                ),
                fmtTooltips(
                    title = BetterMemoryCardModes.COPY.unlocalizedName,
                    keys = BetterMemoryCardModes.COPY.unlocalizedDesc,
                    maxChars = MAX_TOOLTIP_LENGTH,
                ),
                fmtTooltips(
                    title = BetterMemoryCardModes.UNBIND.unlocalizedName,
                    keys = BetterMemoryCardModes.UNBIND.unlocalizedDesc,
                    maxChars = MAX_TOOLTIP_LENGTH,
                )
            )

            init {
                hoverText = modeDescriptions[mode.ordinal]
            }

            override fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    mode = when (button) {
                        0 -> mode.next()
                        1 -> mode.next(true)
                        else -> return false
                    }
                    hoverText = modeDescriptions[mode.ordinal]
                    setTexCoords((mode.ordinal + 3) * 32.0, 232.0)
                    syncMemoryInfo()
                    super.func_146113_a(mc.soundHandler)
                    return true
                }
                return false
            }
        }

        typeButton = object: WidgetButton(this, 0, 0, 32, 32), ITypeReceiver {
            val types = BetterP2P.proxy.getP2PTypeList()
            private var index =
                if (type != null) {
                    types.first { it.index == type?.index}.index
                } else {
                    types.size
                }
            private val me = BetterP2P.proxy.getP2PFromClass(PartP2PTunnelME::class.java) as ClientTunnelInfo
            private val fluid = BetterP2P.proxy.getP2PFromClass(PartP2PLiquids::class.java) as ClientTunnelInfo
            private val redstone = BetterP2P.proxy.getP2PFromClass(PartP2PRedstone::class.java) as ClientTunnelInfo

            init {
                hoverText = if (type == null) {
                    mutableListOf(
                        I18n.format("gui.advanced_memory_card.types.filtered",
                            I18n.format("gui.advanced_memory_card.types.any")))
                } else {
                    mutableListOf(
                        I18n.format("gui.advanced_memory_card.types.filtered", "§a" + type!!.stack.displayName))
                }
            }

            private fun nextType(reverse: Boolean): ClientTunnelInfo? {
                // range: [0, types.size]
                return if (reverse) {
                    index = (index - 1).mod(types.size + 1)
                    types.getOrNull(index) as? ClientTunnelInfo
                } else {
                    index = (index + 1).mod(types.size + 1)
                    types.getOrNull(index) as? ClientTunnelInfo
                }
            }

            override fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    if (button == 1) {
                        // open type selector?
                        openTypeSelector(this, true)
                        return true
                    } else {
                        type = nextType(false)
                    }
                    commitType()
                    return true
                }
                return false
            }

            override fun draw(mc: Minecraft, mouseX: Int, mouseY: Int, partialTicks: Float) {
                val tessellator = Tessellator.instance
                drawBG(tessellator, mouseX, mouseY, partialTicks)
                if (type != null) {
                    drawBlockIcon(mc, type!!.icon(), type!!.stack.iconIndex,
                        x = this.xPosition + 2,
                        y = this.yPosition + 2,
                        width = 28.0,
                        height = 28.0)
                } else {
                    drawBlockIcon(mc, redstone.icon(), types[0].stack.iconIndex,
                        x = this.xPosition + 12,
                        y = this.yPosition + 12,
                        width = 18.0,
                        height = 18.0)
                    drawBlockIcon(mc, fluid.icon(), types[0].stack.iconIndex,
                        x = this.xPosition + 7,
                        y = this.yPosition + 7,
                        width = 18.0,
                        height = 18.0)
                    drawBlockIcon(mc, me.icon(), types[0].stack.iconIndex,
                        x = this.xPosition + 2,
                        y = this.yPosition + 2,
                        width = 18.0,
                        height = 18.0)
                }
            }

            private fun commitType() {
                if (type == null) {
                    (hoverText as MutableList)[0] = I18n.format("gui.advanced_memory_card.types.any")
                } else {
                    (hoverText as MutableList)[0] =
                            I18n.format("gui.advanced_memory_card.types.filtered", "§a" + type!!.stack.displayName)
                }
                ModNetwork.channel.sendToServer(C2SRefreshP2PList(type?.index ?: TUNNEL_ANY))
                super.func_146113_a(mc.soundHandler)
            }

            override fun accept(type: ClientTunnelInfo?) {
                this.index = type?.index ?: TUNNEL_ANY
                gui.type = type
                commitType()
                gui.closeTypeSelector()
            }

            override fun x(): Int {
                return guiLeft
            }

            override fun y(): Int {
                return this.yPosition
            }
        }

        refreshButton = object: WidgetButton(this, 0, 0, 32, 32) {
            override fun mousePressed(mouseX: Int, mouseY: Int, button: Int): Boolean {
                if (super.mousePressed(mc, mouseX, mouseY)) {
                    ModNetwork.channel.sendToServer(C2SRefreshP2PList(type?.index ?: TUNNEL_ANY))
                    return true
                }
                return false
            }
        }

        val typeSelectorList = mutableListOf<ClientTunnelInfo>()
        var toAdd = BetterP2P.proxy.getP2PFromClass(PartP2PTunnelME::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        toAdd = BetterP2P.proxy.getP2PFromClass(PartP2PLiquids::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        toAdd = BetterP2P.proxy.getP2PFromClass(PartP2PGT5Power::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        toAdd = BetterP2P.proxy.getP2PFromClass(PartP2PInterface::class.java) as? ClientTunnelInfo
        if (toAdd != null) {
            typeSelectorList.add(toAdd)
        }
        BetterP2P.proxy.getP2PTypeList().forEach {
            if (!typeSelectorList.contains(it)) {
                typeSelectorList.add(it as ClientTunnelInfo)
            }
        }
        typeSelector = WidgetTypeSelector(0, 0, typeSelectorList)
        typeSelector.parent = typeButton
    }

    /**
     * Called on resize. Initializes GUI elements.
     */
    override fun initGui() {
        super.initGui()
        checkInfo()
        val h = height.coerceAtLeast(256)
        if (scale.minHeight > h) {
            scale = GuiScale.DYNAMIC
        }
        resizeButton.hoverText = listOf(I18n.format(scale.unlocalizedName))
        val numEntries = scale.size(height - 75)
        ySize = (numEntries * P2PEntryConstants.HEIGHT) + 75 + (numEntries - 1)
        guiTop = (h - ySize) / 2
        guiLeft = (width - GUI_WIDTH) / 2

        scrollBar.displayX = guiLeft + 268
        scrollBar.displayY = guiTop + 19
        scrollBar.height = numEntries * P2PEntryConstants.HEIGHT + (numEntries - 1) - 7
        scrollBar.setRange(0, infos.size.coerceIn(0, (infos.size - numEntries).coerceAtLeast(0)), 23)

        searchBar.setMaxStringLength(40)
        searchBar.x = guiLeft + 163
        searchBar.y = guiTop + 5
        searchBar.text = ClientCache.searchText

        col.resize(scale, h - 75)
        col.setPosition(guiLeft + tableX, guiTop + tableY)

        resizeButton.setPosition(guiLeft - 32, guiTop + 2)
        resizeButton.setTexCoords(scale.ordinal * 32.0, 200.0)
        buttonList.add(resizeButton)

        modeButton.setPosition(guiLeft - 32, guiTop + 34)
        modeButton.setTexCoords((mode.ordinal + 3) * 32.0, 232.0)
        buttonList.add(modeButton)

        typeButton.setPosition(guiLeft - 32, guiTop + 66)
        buttonList.add(typeButton)

        refreshButton.setPosition(guiLeft - 32, guiTop + 98)
        refreshButton.setTexCoords(32 * 5.0, 200.0)
        buttonList.add(refreshButton)

        if (typeSelector.parent != typeButton) {
            closeTypeSelector()
        } else {
            typeSelector.setPos(typeSelector.parent.x(), typeSelector.parent.y())
        }
        infos.refresh()
        checkInfo()
        refreshOverlay()
        col.entries.forEach { it.updateButtonVisibility() }
    }

    fun openTypeSelector(parent: ITypeReceiver, useAny: Boolean) {
        typeSelector.parent = parent
        typeSelector.setPos(parent.x(), parent.y())
        typeSelector.useAny = useAny
        typeSelector.visible = true
    }

    fun closeTypeSelector() {
        typeSelector.visible = false
    }

    private fun checkInfo() {
        // A P2P entry is considered "errored" if it is an output, and has no inputs.
        infos.filtered.forEach {
            it.error = it.frequency != 0L && if (it.output) {
                col.findInput(it.frequency) == null
            } else {
                col.findOutput(it.frequency) == null
            }
        }
    }

    fun refreshInfo(infos: List<P2PInfo>) {
        this.infos.rebuild(infos.map(::InfoWrapper))
        checkInfo()
        refreshOverlay()
    }

    private fun syncMemoryInfo() {
        ModNetwork.channel.sendToServer(
            C2SUpdateInfo(MemoryInfo(infos.selectedEntry, selectedInfo?.frequency ?: 0, mode, scale, type?.index ?: TUNNEL_ANY)))
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
        // Draw the stuff that resets GL state first
        fontRendererObj.drawString(I18n.format("item.advanced_memory_card.name"), guiLeft + tableX, guiTop + 6, 0)
        searchBar.drawTextBox()
        buttonList.forEach { it as WidgetButton
            it.draw(mc, mouseX, mouseY, partialTicks)
        }

        // Now draw our P2P entries
        GL11.glPushAttrib(GL11.GL_BLEND or GL11.GL_TEXTURE_2D or GL11.GL_COLOR)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glColor3f(255f, 255f, 255f)
        scrollBar.draw(this)

        col.render(mouseX, mouseY, partialTicks)
        // The GL state is already messed up here by string drawing but oh well
        GL11.glPopAttrib()
        if (typeSelector.visible) {
            typeSelector.render(this, mouseX, mouseY, partialTicks)
            return
        }
        var drewHoverText = false
        run finished@ {
            buttonList.forEach { it as WidgetButton
                if (it.isHovering(mouseX, mouseY)) {
                    drawHoveringText(it.hoverText, mouseX, mouseY + 10, fontRendererObj)
                    drewHoverText = true
                    return@finished
                }
            }
        }
        if (!drewHoverText) {
            if (searchBar.isMouseIn(mouseX, mouseY)) {
                drawHoveringText(sortRules, guiLeft, guiTop + ySize - 40, fontRendererObj)
            } else {
                col.mouseHovered(mouseX, mouseY)
            }
        }
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
        ClientCache.positions.addAll(infos.sorted.filter {
            it.frequency == selectedInfo?.frequency &&
            it != selectedInfo &&
            it.dim == mc.thePlayer.dimension
        }.map { arrayListOf(it.posX, it.posY, it.posZ) to it.facing })
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (typeSelector.visible) {
            typeSelector.mousePressed(mouseX, mouseY, mouseButton)
            return
        }
        col.mouseClicked(mouseX, mouseY, mouseButton)
        scrollBar.click(mouseX, mouseY)
        for (button in buttonList) {
            button as WidgetButton
            if (button.mousePressed(mouseX, mouseY, mouseButton)) {
                return
            }
        }
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
        if (i != 0) {
            scrollBar.wheel(i)
            col.finishRename()
        }
    }

    override fun bindTexture(modid: String, location: String) {
        val loc = ResourceLocation(modid, location)
        mc.textureManager.bindTexture(loc)
    }

    fun bindTexture(loc: ResourceLocation) {
        mc.textureManager.bindTexture(loc)
    }

    private fun drawBackground() {
        bindTexture(BACKGROUND)
        val tessellator = Tessellator.instance
        // Draw the top part
        drawTexturedQuad(tessellator,
            x0 = guiLeft.toDouble(),
            y0 = guiTop.toDouble(),
            x1 = (guiLeft + GUI_WIDTH).toDouble(),
            y1 = guiTop + 60.0,
            u0 = 0.0, v0 = 0.0,
            u1 = 1.0, v1 = 60.0 / GUI_TEX_HEIGHT)
        // Draw P2P segments
        val p2pHeight = P2PEntryConstants.HEIGHT + 1.0
        for (i in 0 until scale.size(ySize - 75) - 2) {
            drawTexturedQuad(tessellator,
                x0 = guiLeft.toDouble(),
                y0 = guiTop + 60.0 + p2pHeight * i,
                x1 = (guiLeft + GUI_WIDTH).toDouble(),
                y1 = guiTop + 60.0 + p2pHeight * (i + 1),
                u0 = 0.0, v0 = 60.0 / GUI_TEX_HEIGHT,
                u1 = 1.0, v1 = 102.0 / GUI_TEX_HEIGHT)
        }
        // Draw Bottom
        drawTexturedQuad(tessellator,
            x0 = guiLeft.toDouble(),
            y0 = guiTop + ySize - 98.0,
            x1 = (guiLeft + GUI_WIDTH).toDouble(),
            y1 = (guiTop + ySize).toDouble(),
            u0 = 0.0, v0 = 102.0 / GUI_TEX_HEIGHT,
            u1 = 1.0, v1 = 200.0 / GUI_TEX_HEIGHT)
    }

    override fun doesGuiPauseGame(): Boolean {
        return false
    }

    override fun keyTyped(char: Char, key: Int) {
        if (key == Keyboard.KEY_ESCAPE && typeSelector.visible) {
            closeTypeSelector()
            return
        }
        if (key == Keyboard.KEY_LSHIFT || col.keyTyped(char, key)) return
        if (!(char.isWhitespace() && searchBar.text.isEmpty()) && searchBar.textboxKeyTyped(char, key)){
            infos.refilter()
        } else if (char == 'e') {
            mc.displayGuiScreen(null as GuiScreen?)
            mc.setIngameFocus()
            return
        }
        return super.keyTyped(char, key)
    }

    public override fun drawHoveringText(textLines: List<Any?>?, x: Int, y: Int, font: FontRenderer?) {
        super.drawHoveringText(textLines, x, y, font)
    }

    fun getTypeID(): Int {
        return type?.index ?: TUNNEL_ANY
    }
}

/**
 * Format multiple lines of tooltips by the given max chars.
 */
fun fmtTooltips(title: String, vararg keys: String, maxChars: Int): List<String> {
    val result: MutableList<String> = mutableListOf()
    result.add(I18n.format(title))
    for (key in keys) {
        val words = I18n.format(key).split(' ')
        var i = 0
        if (key.length < maxChars) {
            result.add(key)
        }
        while (i < words.size) {
            val s = StringBuilder()
            perWord@
            while (s.length < maxChars) {
                s.append(words[i])
                i += 1
                if (i >= words.size) break@perWord
                s.append(" ")
            }
            if (!s.startsWith('§')) {
                s.insert(0, "§7")
            }
            result.add(s.toString())
        }
    }
    return result
}
