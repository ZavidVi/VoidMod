package com.zavidvi.voidmod.block;

import com.mojang.serialization.MapCodec;
import com.zavidvi.voidmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaleCauldronBlock extends Block {
    public static final MapCodec<PaleCauldronBlock> CODEC = simpleCodec(PaleCauldronBlock::new);

    public enum Content implements StringRepresentable {
        EMPTY("empty"),
        WATER("water"),
        LAVA("lava"),
        POWDER_SNOW("powder_snow"),
        LIGHT_WATER("light_water");

        private final String name;

        Content(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final int MAX_CHARGES = 20;

    public static final int MAX_LEVEL = 3;

    private static final int CHARGES_PER_LEVEL = (MAX_CHARGES + MAX_LEVEL - 1) / MAX_LEVEL;

    public static final EnumProperty<Content> CONTENT = EnumProperty.create("content", Content.class);

    public static final IntegerProperty CHARGES = IntegerProperty.create("charges", 0, MAX_CHARGES);

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, MAX_LEVEL);

    private static final float RAIN_FILL_CHANCE = 0.05F;

    private static final float SNOW_FILL_CHANCE = 0.1F;

    private static final VoxelShape INSIDE = box(2.0, 4.0, 2.0, 14.0, 16.0, 14.0);

    private static final VoxelShape SHAPE = Shapes.join(
            Shapes.block(),
            Shapes.or(
                    box(0.0, 0.0, 4.0, 16.0, 3.0, 12.0),
                    box(4.0, 0.0, 0.0, 12.0, 3.0, 16.0),
                    box(2.0, 0.0, 2.0, 14.0, 3.0, 14.0),
                    INSIDE),
            BooleanOp.ONLY_FIRST);

    public PaleCauldronBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(CONTENT, Content.EMPTY)
                .setValue(CHARGES, 0)
                .setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONTENT, CHARGES, LEVEL);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    public static Content content(BlockState state) {
        return state.getValue(CONTENT);
    }

    public static boolean hasWater(BlockState state) {
        return content(state) == Content.WATER && state.getValue(LEVEL) > 0;
    }

    private static boolean isCauldronEmpty(BlockState state) {
        return content(state) == Content.EMPTY;
    }

    public static int lightEmission(BlockState state) {
        return content(state) == Content.LAVA ? 15 : 0;
    }

    public static int displayLevel(BlockState state) {
        return switch (content(state)) {
            case EMPTY -> 0;
            case LAVA -> MAX_LEVEL;
            case LIGHT_WATER -> Math.min(MAX_LEVEL,
                    (state.getValue(CHARGES) + CHARGES_PER_LEVEL - 1) / CHARGES_PER_LEVEL);
            case WATER, POWDER_SNOW -> state.getValue(LEVEL);
        };
    }

    public static int remainingCharges(BlockState state) {
        return content(state) == Content.LIGHT_WATER ? state.getValue(CHARGES) : 0;
    }

    public static boolean spendCharge(Level level, BlockPos pos, BlockState state) {
        return spendCharges(level, pos, state, 1) > 0;
    }

    public static int spendCharges(Level level, BlockPos pos, BlockState state, int amount) {
        int charges = remainingCharges(state);
        int spent = Math.min(charges, Math.max(0, amount));
        if (spent > 0) {
            setContent(level, pos, charges - spent > 0
                    ? state.setValue(CHARGES, charges - spent)
                    : empty(state));
        }
        return spent;
    }

    private static double contentHeight(BlockState state) {
        int level = displayLevel(state);
        return level == 0 ? 0.0 : (6.0 + level * 3.0) / 16.0;
    }

    private static BlockState empty(BlockState state) {
        return state.setValue(CONTENT, Content.EMPTY).setValue(LEVEL, 0).setValue(CHARGES, 0);
    }

    private static void setContent(Level level, BlockPos pos, BlockState state) {
        level.setBlockAndUpdate(pos, state);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand,
                                              BlockHitResult hit) {
        if (stack.is(ModItems.LIGHT_WATER_BUCKET.get())) {
            return fillLightWater(stack, state, level, pos, player, hand);
        }
        if (stack.is(Items.WATER_BUCKET)) {
            return fillFromBucket(stack, state, level, pos, player, hand,
                    Content.WATER, SoundEvents.BUCKET_EMPTY);
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            return fillFromBucket(stack, state, level, pos, player, hand,
                    Content.LAVA, SoundEvents.BUCKET_EMPTY_LAVA);
        }
        if (stack.is(Items.POWDER_SNOW_BUCKET)) {
            return fillFromBucket(stack, state, level, pos, player, hand,
                    Content.POWDER_SNOW, SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
        }
        if (stack.is(Items.BUCKET)) {
            return emptyIntoBucket(stack, state, level, pos, player, hand);
        }
        if (stack.is(Items.GLASS_BOTTLE)) {
            return fillBottle(stack, state, level, pos, player, hand);
        }
        if (stack.is(Items.POTION)) {
            return emptyBottle(stack, state, level, pos, player, hand);
        }
        if (stack.is(ItemTags.CAULDRON_CAN_REMOVE_DYE)) {
            return washDyedItem(stack, state, level, pos, player);
        }
        if (Block.byItem(stack.getItem()) instanceof ShulkerBoxBlock) {
            return washShulkerBox(stack, state, level, pos, player, hand);
        }
        if (stack.has(DataComponents.BANNER_PATTERNS)) {
            return washBanner(stack, state, level, pos, player, hand);
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private InteractionResult fillLightWater(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                 Player player, InteractionHand hand) {
        if (!isCauldronEmpty(state)) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            setContent(level, pos, state.setValue(CONTENT, Content.LIGHT_WATER).setValue(CHARGES, MAX_CHARGES));
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            playAndNotify(level, pos, SoundEvents.BUCKET_EMPTY, GameEvent.FLUID_PLACE);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult fillFromBucket(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                 Player player, InteractionHand hand,
                                                 Content content, SoundEvent sound) {
        if (!isCauldronEmpty(state)) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            setContent(level, pos, state.setValue(CONTENT, content).setValue(LEVEL, MAX_LEVEL));
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            playAndNotify(level, pos, sound, GameEvent.FLUID_PLACE);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult emptyIntoBucket(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                  Player player, InteractionHand hand) {
        ItemStack filled;
        SoundEvent sound = SoundEvents.BUCKET_FILL;

        switch (content(state)) {
            case LIGHT_WATER -> {
                if (MAX_CHARGES != 20) return InteractionResult.TRY_WITH_EMPTY_HAND;
                filled = new ItemStack(ModItems.LIGHT_WATER_BUCKET.get());
            }
            case LAVA -> {
                filled = new ItemStack(Items.LAVA_BUCKET);
                sound = SoundEvents.BUCKET_FILL_LAVA;
            }
            case WATER -> {
                if (state.getValue(LEVEL) < MAX_LEVEL) return InteractionResult.TRY_WITH_EMPTY_HAND;
                filled = new ItemStack(Items.WATER_BUCKET);
            }
            case POWDER_SNOW -> {
                if (state.getValue(LEVEL) < MAX_LEVEL) return InteractionResult.TRY_WITH_EMPTY_HAND;
                filled = new ItemStack(Items.POWDER_SNOW_BUCKET);
                sound = SoundEvents.BUCKET_FILL_POWDER_SNOW;
            }
            default -> {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
        }

        if (!level.isClientSide()) {
            setContent(level, pos, empty(state));
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled));
            player.awardStat(Stats.USE_CAULDRON);
            playAndNotify(level, pos, sound, GameEvent.FLUID_PICKUP);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult fillBottle(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand) {
        if (!hasWater(state)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!level.isClientSide()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player,
                    PotionContents.createItemStack(Items.POTION, Potions.WATER)));
            player.awardStat(Stats.USE_CAULDRON);
            lowerLevel(level, pos, state);
            playAndNotify(level, pos, SoundEvents.BOTTLE_FILL, GameEvent.FLUID_PICKUP);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult emptyBottle(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null || !contents.is(Potions.WATER)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        boolean fillable = isCauldronEmpty(state) || (hasWater(state) && state.getValue(LEVEL) < MAX_LEVEL);
        if (!fillable) {
            return InteractionResult.CONSUME;
        }

        if (!level.isClientSide()) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
            player.awardStat(Stats.USE_CAULDRON);
            setContent(level, pos, state.setValue(CONTENT, Content.WATER)
                    .setValue(LEVEL, state.getValue(LEVEL) + 1));
            playAndNotify(level, pos, SoundEvents.BOTTLE_EMPTY, GameEvent.FLUID_PLACE);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult washDyedItem(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                               Player player) {
        if (!hasWater(state)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!stack.has(DataComponents.DYED_COLOR)) return InteractionResult.CONSUME;

        if (!level.isClientSide()) {
            stack.remove(DataComponents.DYED_COLOR);
            player.awardStat(Stats.CLEAN_ARMOR);
            lowerLevel(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult washBanner(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand) {
        if (!hasWater(state)) return InteractionResult.TRY_WITH_EMPTY_HAND;

        BannerPatternLayers layers = stack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (layers.layers().isEmpty()) return InteractionResult.CONSUME;

        if (!level.isClientSide()) {
            ItemStack washed = stack.copyWithCount(1);
            washed.set(DataComponents.BANNER_PATTERNS, layers.removeLast());
            stack.consume(1, player);
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, washed, false));
            player.awardStat(Stats.CLEAN_BANNER);
            lowerLevel(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult washShulkerBox(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                 Player player, InteractionHand hand) {
        if (!hasWater(state)) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (stack.getCount() != 1) return InteractionResult.CONSUME;

        if (!level.isClientSide()) {
            player.setItemInHand(hand, stack.transmuteCopy(Blocks.SHULKER_BOX, 1));
            player.awardStat(Stats.CLEAN_SHULKER_BOX);
            lowerLevel(level, pos, state);
        }
        return InteractionResult.SUCCESS;
    }

    private static void lowerLevel(Level level, BlockPos pos, BlockState state) {
        int lowered = state.getValue(LEVEL) - 1;
        setContent(level, pos, lowered > 0 ? state.setValue(LEVEL, lowered) : empty(state));
    }

    private static void playAndNotify(Level level, BlockPos pos, SoundEvent sound,
                                      net.minecraft.core.Holder<GameEvent> event) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(null, event, pos);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                net.minecraft.world.entity.InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        if (!isEntityInsideContent(state, pos, entity)) return;

        if (content(state) == Content.LAVA) {
            entity.lavaHurt();
            return;
        }

        if (!entity.isOnFire()) return;
        entity.clearFire();
        if (!entity.mayInteract(serverLevel, pos)) return;

        switch (content(state)) {
            case WATER, POWDER_SNOW -> lowerLevel(level, pos, state);
            case LIGHT_WATER -> spendCharge(level, pos, state);
            default -> { }
        }
    }

    private static boolean isEntityInsideContent(BlockState state, BlockPos pos, Entity entity) {
        return entity.getY() < pos.getY() + contentHeight(state)
                && entity.getBoundingBox().maxY > pos.getY() + 0.25;
    }

    @Override
    public void handlePrecipitation(BlockState state, Level level, BlockPos pos, Biome.Precipitation precipitation) {
        Content filling = switch (precipitation) {
            case RAIN -> Content.WATER;
            case SNOW -> Content.POWDER_SNOW;
            case NONE -> null;
        };
        if (filling == null) return;
        if (content(state) != filling && !isCauldronEmpty(state)) return;
        if (state.getValue(LEVEL) >= MAX_LEVEL) return;

        float chance = precipitation == Biome.Precipitation.RAIN ? RAIN_FILL_CHANCE : SNOW_FILL_CHANCE;
        if (level.getRandom().nextFloat() >= chance) return;

        setContent(level, pos, state.setValue(CONTENT, filling).setValue(LEVEL, state.getValue(LEVEL) + 1));
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos,
                                        net.minecraft.core.Direction direction) {
        return displayLevel(state);
    }
}
