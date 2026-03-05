package net.crioch.fluid_sieve.block;

import net.crioch.fluid_sieve.FluidSieveMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.PushReaction;

public class FluidSieveBlocks {
    public static Block STRING_SIEVE;
    public static Block DENSE_SIEVE;

    public static void register() {
        STRING_SIEVE = register(
                "string_sieve",
                new BaseSieve(
                        Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).pushReaction(PushReaction.DESTROY).setId(key("string_sieve")),
                        id("string_sieve")
                )
        );
        DENSE_SIEVE = register(
                "dense_sieve",
                new BaseSieve(
                        Block.Properties.ofFullCopy(Blocks.OAK_PLANKS).pushReaction(PushReaction.DESTROY).setId(key("dense_sieve")),
                        id("dense_sieve")
                )
        );
    }

    private static Block register(String path, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, id(path), block);
    }

    private static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK, id(path));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(FluidSieveMod.MOD_ID, path);
    }
}
