package net.crioch.fluid_sieve.client;

import net.crioch.fluid_sieve.block.FluidSieveBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.render.BlockRenderLayer;

public class FluidSieveClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlocks(BlockRenderLayer.CUTOUT, FluidSieveBlocks.STRING_SIEVE, FluidSieveBlocks.DENSE_SIEVE);
    }
}
