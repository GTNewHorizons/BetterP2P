package com.projecturanus.betterp2p.item

const val MAX_TOOLTIP_LENGTH = 40

enum class BetterMemoryCardModes(val unlocalizedName: String, vararg val unlocalizedDesc: String) {
    /**
     * Select an input P2P and bind its output
     */
    OUTPUT("gui.advanced_memory_card.mode.output",
        "gui.advanced_memory_card.mode.output.desc.1",
            "gui.advanced_memory_card.mode.output.desc.2",
            "gui.advanced_memory_card.mode.output.desc.3"
    ),

    /**
     * Select an output P2P and bind its input
     */
    INPUT("gui.advanced_memory_card.mode.input",
        "gui.advanced_memory_card.mode.input.desc.1",
        "gui.advanced_memory_card.mode.input.desc.2",
        "gui.advanced_memory_card.mode.input.desc.3"
    ),

    /**
     * Copy same output frequency
     */
    COPY("gui.advanced_memory_card.mode.copy",
        "gui.advanced_memory_card.mode.copy.desc.1",
        "gui.advanced_memory_card.mode.copy.desc.2",
        "gui.advanced_memory_card.mode.copy.desc.3",
        "gui.advanced_memory_card.mode.copy.desc.4",
    );

    fun next(): BetterMemoryCardModes {
        return values()[ordinal.plus(1) % values().size]
    }
}
