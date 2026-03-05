package net.crioch.fluid_sieve.block;

import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BaseSieve extends Block implements SimpleWaterloggedBlock {
    private static final VoxelShape SELECTION_SHAPE = Shapes.or(
            Block.box(0, 0, 0, 1, 16, 1),
            Block.box(15, 0, 0, 16, 16, 1),
            Block.box(0, 0, 15, 1, 16, 16),
            Block.box(15, 0, 15, 16, 16, 16),
            Block.box(1, 1, 0, 15, 14, 1),
            Block.box(1, 1, 15, 15, 14, 16),
            Block.box(0, 1, 1, 1, 14, 16),
            Block.box(15, 1, 1, 16, 14, 16)
    );

    public BaseSieve(BlockBehaviour.Properties properties, Identifier key) {
        super(properties.randomTicks().noOcclusion().setId(ResourceKey.create(Registries.BLOCK, key)));
        this.registerDefaultState(this.getStateDefinition().any().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            return Fluids.WATER.defaultFluidState();
        }

        return super.getFluidState(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        boolean waterlogged = state.getValue(BlockStateProperties.WATERLOGGED);
        Identifier id = BuiltInRegistries.FLUID.getKey(waterlogged ? Fluids.WATER : Fluids.EMPTY);
        List<ItemStack> loot = this.getLoot(id, world, state, pos, random);

        if (loot.isEmpty()) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos.below());
        if (blockEntity instanceof Container container) {
            int containerSize = container.getContainerSize();
            boolean containerChanged = false;

            Iterator<ItemStack> iterator = loot.iterator();
            int firstEmptySlot = 0;
            while (iterator.hasNext() && firstEmptySlot < containerSize) {
                ItemStack stack = iterator.next();
                int initialCount = stack.getCount();
                firstEmptySlot = insert(stack, container, firstEmptySlot);
                if (stack.isEmpty()) {
                    containerChanged = true;
                    iterator.remove();
                } else if (initialCount - stack.getCount() > 0) {
                    containerChanged = true;
                }
            }

            if (containerChanged) {
                container.setChanged();
            }

            if (!loot.isEmpty()) {
                spawnStacksInWorld(world, pos, loot);
            }
        } else {
            spawnStacksInWorld(world, pos, loot);
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos downPos = pos.below();
        BlockState downState = world.getBlockState(downPos);
        return downState.isFaceSturdy(world, downPos, Direction.UP, SupportType.FULL) || downState.is(Blocks.HOPPER);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader world,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        if (this.canSurvive(state, world, pos)) {
            return state;
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        FluidState fluidState = ctx.getLevel().getFluidState(pos);
        return this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter view, BlockPos pos, CollisionContext context) {
        return SELECTION_SHAPE;
    }

    @Override
    protected VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return SELECTION_SHAPE;
    }

    private List<ItemStack> getLoot(Identifier fluidId, ServerLevel world, BlockState state, BlockPos pos, RandomSource random) {
        Identifier path = fluidId.withPrefix("sieve/");
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, path);
        LootTable lootTable = world.getServer().reloadableRegistries().getLootTable(key);

        if (lootTable == LootTable.EMPTY) {
            return List.of();
        }

        LootParams.Builder builder = new LootParams.Builder(world)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY);

        List<? extends Entity> entitiesWithinBlock = world.getEntities(EntityTypeTest.forClass(Entity.class), entity -> entity.blockPosition().equals(pos));
        if (!entitiesWithinBlock.isEmpty()) {
            builder.withOptionalParameter(LootContextParams.THIS_ENTITY, entitiesWithinBlock.get(random.nextInt(entitiesWithinBlock.size())));
        }

        return lootTable.getRandomItems(builder.create(LootContextParamSets.BLOCK));
    }

    private static void spawnStacksInWorld(ServerLevel world, BlockPos pos, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            Block.popResource(world, pos, stack);
        }
    }

    private static int insert(ItemStack stack, Container container, int firstEmptySlot) {
        if (stack.isStackable()) {
            for (int slotIndex = firstEmptySlot; slotIndex < container.getContainerSize(); slotIndex++) {
                ItemStack slot = container.getItem(slotIndex);
                if (slot.getItem() == stack.getItem()) {
                    int maxStack = Math.min(slot.getMaxStackSize(), container.getMaxStackSize(slot));
                    int amount = Math.min(maxStack - slot.getCount(), stack.getCount());
                    slot.grow(amount);
                    container.setItem(slotIndex, slot);
                    stack.shrink(amount);
                } else if (slot.isEmpty()) {
                    container.setItem(slotIndex, stack.copy());
                    stack.shrink(stack.getCount());
                }

                if (firstEmptySlot - slotIndex == 0 && slot.getMaxStackSize() - slot.getCount() == 0) {
                    firstEmptySlot++;
                }

                if (stack.isEmpty()) {
                    break;
                }
            }
        } else {
            for (int slotIndex = firstEmptySlot; slotIndex < container.getContainerSize(); slotIndex++) {
                ItemStack slot = container.getItem(slotIndex);
                if (slot.isEmpty()) {
                    container.setItem(slotIndex, stack.split(1));
                    if (firstEmptySlot - slotIndex == 0) {
                        firstEmptySlot++;
                    }
                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }
        }

        return firstEmptySlot;
    }
}
