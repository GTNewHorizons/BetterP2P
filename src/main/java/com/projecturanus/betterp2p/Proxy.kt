package com.projecturanus.betterp2p

import appeng.api.AEApi
import appeng.api.config.TunnelType
import appeng.api.definitions.IItemDefinition
import appeng.parts.p2p.PartP2PGT5Power
import appeng.parts.p2p.PartP2PIC2Power
import appeng.parts.p2p.PartP2PInterface
import appeng.parts.p2p.PartP2PItems
import appeng.parts.p2p.PartP2PLight
import appeng.parts.p2p.PartP2PLiquids
import appeng.parts.p2p.PartP2POpenComputers
import appeng.parts.p2p.PartP2PPressure
import appeng.parts.p2p.PartP2PRFPower
import appeng.parts.p2p.PartP2PRedstone
import appeng.parts.p2p.PartP2PTunnel
import appeng.parts.p2p.PartP2PTunnelME
import com.projecturanus.betterp2p.client.render.RenderHandler
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import com.projecturanus.betterp2p.network.ServerPlayerDisconnectHandler
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.ShapelessRecipes
import net.minecraft.util.IIcon

/**
 * A proxy for the server
 */
open class CommonProxy {
    /**
     * Tunnels available in this instance. These are used to communicate p2p
     * information between server/client.
     */
    protected val tunnelTypes = mutableMapOf<Class<*>, TunnelInfo>()

    /**
     * Same as above, but maps ints -> tunnel info.
     */
    protected val tunnelIndices = mutableMapOf<Int, TunnelInfo>()

    open fun postInit() {
        ServerPlayerDisconnectHandler.register()
        GameRegistry.addRecipe(ShapelessRecipes(ItemStack(ItemAdvancedMemoryCard), listOf(
            AEApi.instance().definitions().items().networkTool().maybeStack(1).get(),
            AEApi.instance().definitions().items().memoryCard().maybeStack(1).get())))
        initTunnels()
    }
    /**
     * Discover what tunnels are available.
     */
    open fun initTunnels() {
        val partDefs = AEApi.instance().definitions().parts()
        registerTunnel(
            def = partDefs.p2PTunnelME(),
            type = TunnelType.ME,
            classType = PartP2PTunnelME::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelEU(),
            type = TunnelType.IC2_POWER,
            classType = PartP2PIC2Power::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelRF(),
            type = TunnelType.RF_POWER,
            classType = PartP2PRFPower::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelRedstone(),
            type = TunnelType.REDSTONE,
            classType = PartP2PRedstone::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelLiquids(),
            type = TunnelType.FLUID,
            classType = PartP2PLiquids::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelItems(),
            type = TunnelType.ITEM,
            classType = PartP2PItems::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelLight(),
            type = TunnelType.LIGHT,
            classType = PartP2PLight::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelOpenComputers(),
            type = TunnelType.COMPUTER_MESSAGE,
            classType = PartP2POpenComputers::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelPneumaticCraft(),
            type = TunnelType.PRESSURE,
            classType = PartP2PPressure::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelGregtech(),
            type = TunnelType.GT_POWER,
            classType = PartP2PGT5Power::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelMEInterface(),
            type = TunnelType.ME_INTERFACE,
            classType = PartP2PInterface::class.java)
    }

    private fun registerTunnel(def: IItemDefinition, type: TunnelType, classType: Class<out PartP2PTunnel<*>>) {
        if (def.isEnabled) {
            val stack = def.maybeStack(1).get()
            val info = TunnelInfo(type.ordinal, stack, classType)
            tunnelTypes[classType] = info
            tunnelIndices[type.ordinal] = info
        }
    }

    fun getP2PFromIndex(index: Int): TunnelInfo? {
        return tunnelIndices[index]
    }

    fun getP2PFromClass(clazz: Class<*>): TunnelInfo? {
        return tunnelTypes[clazz]
    }

    fun getP2PTypeList(): List<TunnelInfo> {
        return tunnelIndices.values.toList()
    }
}

/**
 * A proxy for the client
 */
class ClientProxy: CommonProxy() {

    override fun postInit() {
        RenderHandler.register()
        super.postInit()
    }

    /**
     * Keeps a cache of icons to use in GUI.
     */
    override fun initTunnels() {
        val partDefs = AEApi.instance().definitions().parts()
        registerTunnel(
            def = partDefs.p2PTunnelME(),
            type = TunnelType.ME,
            classType = PartP2PTunnelME::class.java,
            icon = { PartP2PTunnelME(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelEU(),
            type = TunnelType.IC2_POWER,
            classType = PartP2PIC2Power::class.java,
            icon = { PartP2PIC2Power(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelRF(),
            type = TunnelType.RF_POWER,
            classType = PartP2PRFPower::class.java,
            icon = { PartP2PRFPower(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelRedstone(),
            type = TunnelType.REDSTONE,
            classType = PartP2PRedstone::class.java,
            icon = { PartP2PRedstone(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelLiquids(),
            type = TunnelType.FLUID,
            classType = PartP2PLiquids::class.java,
            icon = { PartP2PLiquids(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelItems(),
            type = TunnelType.ITEM,
            classType = PartP2PItems::class.java,
            icon = { PartP2PItems(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelLight(),
            type = TunnelType.LIGHT,
            classType = PartP2PLight::class.java,
            icon = { PartP2PLight(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelOpenComputers(),
            type = TunnelType.COMPUTER_MESSAGE,
            classType = PartP2POpenComputers::class.java,
            icon = { PartP2POpenComputers(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelPneumaticCraft(),
            type = TunnelType.PRESSURE,
            classType = PartP2PPressure::class.java,
            icon = { PartP2PPressure(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelGregtech(),
            type = TunnelType.GT_POWER,
            classType = PartP2PGT5Power::class.java,
            icon = { PartP2PGT5Power(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelMEInterface(),
            type = TunnelType.ME_INTERFACE,
            classType = PartP2PInterface::class.java,
            icon = { PartP2PInterface(it).typeTexture })
    }

    private inline fun registerTunnel(def: IItemDefinition,
                                      type: TunnelType,
                                      classType: Class<out PartP2PTunnel<*>>,
                                      crossinline icon: (ItemStack) -> IIcon) {
        if (def.isEnabled) {
            val stack = def.maybeStack(1).get()
            val info = ClientTunnelInfo(type.ordinal, stack, classType) { icon(stack) }
            tunnelTypes[classType] = info
            tunnelIndices[type.ordinal] = info
        }
    }
}
