package com.projecturanus.betterp2p.capability

import com.projecturanus.betterp2p.item.BetterMemoryCardModes
import com.projecturanus.betterp2p.network.NONE

data class MemoryInfo(var selectedEntry: Long = NONE,
                      var frequency: Long = 0,
                      var mode: BetterMemoryCardModes = BetterMemoryCardModes.OUTPUT)
