package com.projecturanus.betterp2p.capability

import com.projecturanus.betterp2p.client.gui.widget.GuiScale
import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.NONE_SELECTED

const val TUNNEL_ANY: Int = -1
data class MemoryInfo(var selectedEntry: Long = NONE_SELECTED,
                      var frequency: Long = 0,
                      var mode: BetterMemoryCardModes = BetterMemoryCardModes.OUTPUT,
                      var gui: GuiScale = GuiScale.DYNAMIC,
                      var type: Int = TUNNEL_ANY)
