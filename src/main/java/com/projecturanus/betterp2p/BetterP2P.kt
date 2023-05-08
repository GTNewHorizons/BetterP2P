package com.projecturanus.betterp2p

import com.projecturanus.betterp2p.config.BetterP2PConfig
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import com.projecturanus.betterp2p.network.ModNetwork
import cpw.mods.fml.common.Mod
import cpw.mods.fml.common.SidedProxy
import cpw.mods.fml.common.event.FMLPostInitializationEvent
import cpw.mods.fml.common.event.FMLPreInitializationEvent
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.Logger

const val MODID = "betterp2p"

/**
 * Better P2P is created by LasmGratel.
 * GlodBlock backported this to 1.7.10
 *
 * MODVER below is handled by gradle, ignore IDE errors.
 */
@Mod(modid = MODID, version = MODVER, modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter", dependencies = "required-after:appliedenergistics2; required-after:forgelin;")
object BetterP2P {

    lateinit var logger: Logger
    @SidedProxy(serverSide = "com.projecturanus.betterp2p.CommonProxy", clientSide = "com.projecturanus.betterp2p.ClientProxy")
    lateinit var proxy: CommonProxy
    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog
        ModNetwork.registerNetwork()
        BetterP2PConfig.loadConfig(Configuration(event.suggestedConfigurationFile))
        GameRegistry.registerItem(ItemAdvancedMemoryCard, "advanced_memory_card", MODID)
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent) {
        proxy.postInit()
    }
}
