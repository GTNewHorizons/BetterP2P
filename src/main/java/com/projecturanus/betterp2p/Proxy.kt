package com.projecturanus.betterp2p

import appeng.api.AEApi
import appeng.api.definitions.IItemDefinition
import appeng.parts.p2p.*
import com.glodblock.github.common.parts.PartFluidP2PInterface
import com.glodblock.github.loader.ItemAndBlockHolder
import com.projecturanus.betterp2p.client.render.RenderHandler
import com.projecturanus.betterp2p.item.ItemAdvancedMemoryCard
import com.projecturanus.betterp2p.network.ServerPlayerDisconnectHandler
import com.projecturanus.betterp2p.util.p2p.ClientTunnelInfo
import com.projecturanus.betterp2p.util.p2p.TunnelInfo
import cpw.mods.fml.common.Loader
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
        var typeId = 0
        registerTunnel(
            def = partDefs.p2PTunnelME(),
            type = typeId++,
            classType = PartP2PTunnelME::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelEU(),
            type = typeId++,
            classType = PartP2PIC2Power::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelRF(),
            type = typeId++,
            classType = PartP2PRFPower::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelRedstone(),
            type = typeId++,
            classType = PartP2PRedstone::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelLiquids(),
            type = typeId++,
            classType = PartP2PLiquids::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelItems(),
            type = typeId++,
            classType = PartP2PItems::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelLight(),
            type = typeId++,
            classType = PartP2PLight::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelOpenComputers(),
            type = typeId++,
            classType = PartP2POpenComputers::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelPneumaticCraft(),
            type = typeId++,
            classType = PartP2PPressure::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelGregtech(),
            type = typeId++,
            classType = PartP2PGT5Power::class.java)
        registerTunnel(
            def = partDefs.p2PTunnelMEInterface(),
            type = typeId++,
            classType = PartP2PInterface::class.java)
        if (Loader.isModLoaded("ae2fc")) {
            val item = ItemAndBlockHolder.FLUID_INTERFACE_P2P
            if (item != null) {
                val clazz = PartFluidP2PInterface::class.java
                val type = typeId++
                val info = TunnelInfo(type, item.stack(), clazz)
                tunnelTypes[clazz] = info
                tunnelIndices[type] = info
            }
        }
    }

    private fun registerTunnel(def: IItemDefinition, type: Int, classType: Class<out PartP2PTunnel<*>>) {
        if (def.isEnabled) {
            val stack = def.maybeStack(1).get()
            val info = TunnelInfo(type, stack, classType)
            tunnelTypes[classType] = info
            tunnelIndices[type] = info
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
        var typeId = 0
        registerTunnel(
            def = partDefs.p2PTunnelME(),
            type = typeId++,
            classType = PartP2PTunnelME::class.java,
            icon = { PartP2PTunnelME(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelEU(),
            type = typeId++,
            classType = PartP2PIC2Power::class.java,
            icon = { PartP2PIC2Power(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelRF(),
            type = typeId++,
            classType = PartP2PRFPower::class.java,
            icon = { PartP2PRFPower(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelRedstone(),
            type = typeId++,
            classType = PartP2PRedstone::class.java,
            icon = { PartP2PRedstone(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelLiquids(),
            type = typeId++,
            classType = PartP2PLiquids::class.java,
            icon = { PartP2PLiquids(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelItems(),
            type = typeId++,
            classType = PartP2PItems::class.java,
            icon = { PartP2PItems(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelLight(),
            type = typeId++,
            classType = PartP2PLight::class.java,
            icon = { PartP2PLight(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelOpenComputers(),
            type = typeId++,
            classType = PartP2POpenComputers::class.java,
            icon = { PartP2POpenComputers(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelPneumaticCraft(),
            type = typeId++,
            classType = PartP2PPressure::class.java,
            icon = { PartP2PPressure(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelGregtech(),
            type = typeId++,
            classType = PartP2PGT5Power::class.java,
            icon = { PartP2PGT5Power(it).typeTexture })
        registerTunnel(
            def = partDefs.p2PTunnelMEInterface(),
            type = typeId++,
            classType = PartP2PInterface::class.java,
            icon = { PartP2PInterface(it).typeTexture })
        if (Loader.isModLoaded("ae2fc")) {
            val item = ItemAndBlockHolder.FLUID_INTERFACE_P2P
            if (item != null) {
                val clazz = PartFluidP2PInterface::class.java
                val stack = item.stack()
                val type = typeId++
                val info = ClientTunnelInfo(type, stack, clazz) { PartFluidP2PInterface(stack).typeTexture }
                tunnelTypes[clazz] = info
                tunnelIndices[type] = info
            }
        }
    }

    private inline fun registerTunnel(def: IItemDefinition,
                                      type: Int,
                                      classType: Class<out PartP2PTunnel<*>>,
                                      crossinline icon: (ItemStack) -> IIcon) {
        if (def.isEnabled) {
            val stack = def.maybeStack(1).get()
            val info = ClientTunnelInfo(type, stack, classType) { icon(stack) }
            tunnelTypes[classType] = info
            tunnelIndices[type] = info
        }
    }
}
