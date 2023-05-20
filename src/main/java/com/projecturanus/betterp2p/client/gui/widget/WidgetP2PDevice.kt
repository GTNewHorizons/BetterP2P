package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.capability.TUNNEL_ANY
import com.projecturanus.betterp2p.client.gui.*
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.C2STypeChange
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.resources.I18n
import org.lwjgl.opengl.GL11
import kotlin.reflect.KProperty0

object P2PEntryConstants {
    const val HEIGHT = 41
    const val WIDTH = 254
    const val OUTPUT_COLOR = 0x4566ccff
    const val SELECTED_COLOR = 0x4545DA75
    const val ERROR_COLOR = 0x45DA4527
    const val INACTIVE_COLOR = 0x45FFEA05
    const val LEFT_ALIGN = 24
}

class WidgetP2PDevice(private val selectedInfoProperty: KProperty0<InfoWrapper?>,
                      val modeSupplier: () -> BetterMemoryCardModes,
                      val infoSupplier: () -> InfoWrapper?,
                      val gui: GuiAdvancedMemoryCard,
                      var x: Int, var y: Int): Widget(), ITypeReceiver {

    var renderNameTextField = true

    private val selectedInfo: InfoWrapper?
        get() = selectedInfoProperty.get()

    /**
     * Update the button visibility
     */
    fun updateButtonVisibility() {
        val info = infoSupplier() ?: return
        val mode = modeSupplier()
        if (selectedInfo == null) {
            // No selected, so we don't show buttons
            info.bindButton.enabled = false
            info.unbindButton.enabled = false
        } else if (mode == BetterMemoryCardModes.UNBIND) {
            // Only unbinds allowed in unbind mode
            info.bindButton.enabled = false
            info.unbindButton.enabled = info.frequency != 0L
        } else if (mode == BetterMemoryCardModes.COPY &&
            ((!info.output && info.frequency != 0L) || selectedInfo!!.output)) {
            // Copy mode
            // If this info is (input && set freq) || selected info is an output
            // Disable all buttons
            info.bindButton.enabled = false
            info.unbindButton.enabled = false
        } else {
            // Other modes:
            // Bind allowed only if currently not selected && selected is unbound; OR not bound to selected
            info.bindButton.enabled = info.code != selectedInfo!!.code &&
                    (selectedInfo!!.frequency == 0L ||
                    info.frequency != selectedInfo!!.frequency)
            info.unbindButton.enabled = false
        }
    }

    fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val info = infoSupplier()
        if (info != null) {
            // Draw the background first
            if (selectedInfo?.code == info.code)
                GuiScreen.drawRect(x, y, x + P2PEntryConstants.WIDTH, y + P2PEntryConstants.HEIGHT, P2PEntryConstants.SELECTED_COLOR)
            else if (info.error) {
                // P2P output without an input, or unbound
                GuiScreen.drawRect(x, y, x + P2PEntryConstants.WIDTH, y + P2PEntryConstants.HEIGHT, P2PEntryConstants.ERROR_COLOR)
            } else if (!info.hasChannel && info.frequency != 0L) {
                // No channel
                GuiScreen.drawRect(x, y, x + P2PEntryConstants.WIDTH, y + P2PEntryConstants.HEIGHT, P2PEntryConstants.INACTIVE_COLOR)
            } else if (selectedInfo?.frequency == info.frequency && info.frequency != 0.toLong()) {
                // Show same frequency
                GuiScreen.drawRect(x, y, x + P2PEntryConstants.WIDTH, y + P2PEntryConstants.HEIGHT, P2PEntryConstants.OUTPUT_COLOR)
            }
            GL11.glColor3f(255f, 255f, 255f)
            // Draw our icons
            drawBlockIcon(gui.mc, info.icon, info.overlay, x + 3, y + 3)
            gui.bindTexture(gui.background)
            if (info.output) {
                drawTexturedQuad(Tessellator.instance, x.toDouble(), y + 4.0, x + 16.0, y + 20.0,
                    144.0 / GUI_WIDTH, 200.0 / GUI_TEX_HEIGHT, 160.0 / GUI_WIDTH, 216.0 / GUI_TEX_HEIGHT)
            } else {
                drawTexturedQuad(Tessellator.instance, x.toDouble(), y + 4.0, x + 16.0, y + 20.0,
                    128.0 / GUI_WIDTH, 200.0 / GUI_TEX_HEIGHT, 144.0 / GUI_WIDTH, 216.0 / GUI_TEX_HEIGHT)
            }
            if (info.error || info.frequency == 0L || !info.hasChannel) {
                drawTexturedQuad(Tessellator.instance, x + 3.0, y + 20.0, x + 19.0, y + 36.0,
                    144.0 / GUI_WIDTH, 216.0 / GUI_TEX_HEIGHT, 160.0 / GUI_WIDTH, 232.0 / GUI_TEX_HEIGHT)
            } else {
                drawTexturedQuad(Tessellator.instance, x + 3.0, y + 20.0, x + 19.0, y + 36.0,
                    128.0 / GUI_WIDTH, 216.0 / GUI_TEX_HEIGHT, 144.0 / GUI_WIDTH, 232.0 / GUI_TEX_HEIGHT)
            }
            // Now draw the stuff that messes up our GL state (aka text)
            val fontRenderer = gui.mc.fontRenderer
            val leftAlign = x + P2PEntryConstants.LEFT_ALIGN
            if (renderNameTextField) {
                fontRenderer.drawString(I18n.format("gui.advanced_memory_card.name", info.name), leftAlign, y + 3, 0)
            } else {
                fontRenderer.drawString(I18n.format("gui.advanced_memory_card.name", ""), leftAlign, y + 3, 0)
            }
            fontRenderer.drawString(info.description, leftAlign, y + 13, 0)
            fontRenderer.drawString(info.freqDisplay, leftAlign, y + 23, 0)
            if (info.channels != null) {
                fontRenderer.drawString(info.channels, leftAlign, y + 33, 0)
            }
            updateButtonVisibility()
            drawButtons(gui, info, mouseX, mouseY, partialTicks)
        }
    }

    private fun drawButtons(gui: GuiScreen, info: InfoWrapper, mouseX: Int, mouseY: Int, partialTicks: Float) {
        info.renameButton.xPosition = x + 50
        info.renameButton.width = 120
        info.renameButton.yPosition = y + 1
        info.renameButton.height = 12
        if (info.bindButton.enabled) {
            info.bindButton.enabled = true
            info.bindButton.xPosition = x + 190
            info.bindButton.width = 56
            info.bindButton.yPosition = y + 14
            info.bindButton.drawButton(gui.mc, mouseX, mouseY)
        } else if (info.unbindButton.enabled) {
            info.unbindButton.enabled = true
            info.unbindButton.xPosition = x + 190
            info.unbindButton.width = 56
            info.unbindButton.yPosition = y + 14
            info.unbindButton.drawButton(gui.mc, mouseX, mouseY)
        }
    }

    override fun accept(type: ClientTunnelInfo?) {
        ModNetwork.channel.sendToServer(C2STypeChange(type?.index ?: TUNNEL_ANY, infoSupplier()!!.code))
        gui.closeTypeSelector()
    }

    override fun x(): Int {
        return this.x + 40
    }

    override fun y(): Int {
        return this.y
    }
}
