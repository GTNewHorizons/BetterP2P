package com.projecturanus.betterp2p.client.gui.widget

import com.projecturanus.betterp2p.client.TextureBound
import net.minecraft.client.gui.Gui

class WidgetScrollBar(var displayX: Int, var displayY: Int) {
    var width = 12
    var height = 16
    var pageSize = 1

    var maxScroll = 0
    var minScroll = 0

    var onScroll: () -> Unit = {}

    var currentScroll = 0
    var moving = false
    fun <T> draw(g: T) where T: TextureBound, T: Gui {
        g.bindTexture("minecraft", "textures/gui/container/creative_inventory/tabs.png")
        if (getRange() == 0) {
            g.drawTexturedModalRect(displayX, displayY, 232 + width, 0, width, 15)
        } else {
            val offset = (currentScroll - minScroll) * (height - 15) / getRange()
            g.drawTexturedModalRect(displayX, offset + displayY, 232, 0, width, 15)
        }
    }

    private fun getRange(): Int {
        return maxScroll - minScroll
    }

    fun setRange(min: Int, max: Int, pageSize: Int) {
        minScroll = min
        maxScroll = max
        this.pageSize = pageSize
        if (minScroll > maxScroll) {
            maxScroll = minScroll
        }
        applyRange()
    }

    private fun applyRange() {
        currentScroll = currentScroll.coerceIn(minScroll, maxScroll)
        onScroll()
    }

    fun click(x: Int, y: Int) {
        if (getRange() == 0) {
            return
        }
        if (y > displayY && y <= displayY + height) {
            if (moving || (x > displayX && x <= displayX + width)) {
                currentScroll = y - displayY
                currentScroll = minScroll + currentScroll * 2 * getRange() / height
                currentScroll = currentScroll + 1 shr 1
                applyRange()
                moving = true
            }
        }
    }

    fun wheel(delta: Int) {
        var delta = delta
        delta = (-delta).coerceIn(-1, 1)
        currentScroll += delta
        applyRange()
    }
}
