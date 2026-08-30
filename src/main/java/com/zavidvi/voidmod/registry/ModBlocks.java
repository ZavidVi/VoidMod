package com.zavidvi.voidmod.registry;

import com.zavidvi.voidmod.VoidMod;
import com.zavidvi.voidmod.block.LightWaterBlock;
import com.zavidvi.voidmod.block.OtherworldlyForgeBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(VoidMod.MOD_ID);

    public static final DeferredBlock<Block> OTHERWORLDLY_FORGE = BLOCKS.registerBlock("extramundane_forge",
            OtherworldlyForgeBlock::new,
            props -> props.strength(2.0f).requiresCorrectToolForDrops().noOcclusion());

    public static final DeferredBlock<LightWaterBlock> LIGHT_WATER = BLOCKS.registerBlock("light_water",
            props -> new LightWaterBlock(ModFluids.LIGHT_WATER.get(), props),
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER));

    private static final float VOID_BLOCK_HARDNESS = 1.8F;

    private static final float CLEANED_BLOCK_HARDNESS = VOID_BLOCK_HARDNESS * 1.5F;

    public static final DeferredBlock<Block> VOID_BLOCK = registerBlock("void_block",
            Block::new,
            props -> props
                    .strength(VOID_BLOCK_HARDNESS, VOID_BLOCK_HARDNESS)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> CLEANED_BLOCK = registerBlock("cleaned_block",
            com.zavidvi.voidmod.block.CleanedBlock::new,
            props -> props
                    .strength(CLEANED_BLOCK_HARDNESS, CLEANED_BLOCK_HARDNESS)
                    .requiresCorrectToolForDrops());

    public static final DeferredBlock<Block> LIGHTED_WOOL = registerBlock("lighted_wool",
            Block::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL));

    public static final DeferredBlock<Block> PALE_CAULDRON = registerBlock("pale_cauldron",
            com.zavidvi.voidmod.block.PaleCauldronBlock::new,
            () -> BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)
                    .lightLevel(com.zavidvi.voidmod.block.PaleCauldronBlock::lightEmission));

    static {
        ModItems.ITEMS.registerItem("extramundane_forge",
                props -> new com.zavidvi.voidmod.item.OtherworldlyForgeBlockItem(
                        OTHERWORLDLY_FORGE.get(), props.useBlockDescriptionPrefix()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(
            String name,
            Function<BlockBehaviour.Properties, ? extends T> block,
            UnaryOperator<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, block, properties);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(
            String name,
            Function<BlockBehaviour.Properties, ? extends T> block,
            Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, block, properties);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.registerItem(name, props -> new BlockItem(block.get(), props.useBlockDescriptionPrefix()));
    }

    public static final DeferredBlock<com.zavidvi.voidmod.block.GraveBlock> GRAVE =
            BLOCKS.registerBlock("grave",
                    com.zavidvi.voidmod.block.GraveBlock::new,
                    props -> props.strength(-1.0F, 3600000.0F).noOcclusion().noLootTable());

    public static final DeferredBlock<com.zavidvi.voidmod.block.GravePartBlock> GRAVE_PART =
            BLOCKS.registerBlock("grave_part",
                    com.zavidvi.voidmod.block.GravePartBlock::new,
                    props -> props.strength(-1.0F, 3600000.0F).noOcclusion().noLootTable());

    public static final DeferredBlock<Block> GRAVE_GRASS_BLOCK = registerBlock("grave_grass_block",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK));

    public static final DeferredBlock<Block> GRAVE_PODZOL = registerBlock("grave_podzol",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.PODZOL));

    public static final DeferredBlock<Block> GRAVE_STONE = registerBlock("grave_stone",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));

    public static final DeferredBlock<Block> GRAVE_SAND = registerBlock("grave_sand",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SAND));

    public static final DeferredBlock<Block> GRAVE_RED_SAND = registerBlock("grave_red_sand",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.RED_SAND));

    public static final DeferredBlock<Block> GRAVE_SNOW = registerBlock("grave_snow",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.SNOW_BLOCK));

    public static final DeferredBlock<Block> GRAVE_DIRT = registerBlock("grave_dirt",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIRT));

    public static final DeferredBlock<Block> PALE_ORE = registerBlock("pale_ore",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE));

    public static final DeferredBlock<Block> DEEPSLATE_PALE_ORE = registerBlock("deepslate_pale_ore",
            Block::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
