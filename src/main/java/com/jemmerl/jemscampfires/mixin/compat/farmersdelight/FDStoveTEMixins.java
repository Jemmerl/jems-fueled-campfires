package com.jemmerl.jemscampfires.mixin.compat.farmersdelight;


import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

import java.util.List;

// Compat mixin done with permission from vectorwing with condition of configurability! :)
@SuppressWarnings("target")
@Mixin(value = StoveBlockEntity.class, priority = 0)
public abstract class FDStoveTEMixins extends SyncedBlockEntity implements IFueledCampfire {

    // Front box, rotates based on stove block rotation
    private static final VoxelShape COLLECTION_AREA_SHAPE_N = Block.box(0.0D, 0.0D, -1.0D, 16.0D, 6.0D, 0.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_S = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 6.0D, 17.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_E = Block.box(15.0D, 0.0D, 0.0D, 17.0D, 6.0D, 16.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_W = Block.box(-1.0D, 0.0D, 0.0D, 0.0D, 6.0D, 16.0D);

    private int fuelTicks = -1;
    private boolean isEternal = false;
    private boolean markChanged = false;

    public FDStoveTEMixins(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public void onLoad() {
        if (!this.level.isClientSide()) {
            // This is the first load of the campfire TE
            // Get settings/properties that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = Math.min((ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get()), ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get());
            }
        }
    }

    @Inject(at = @At("HEAD"), remap = false, method = "cookingTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lvectorwing/farmersdelight/common/block/entity/StoveBlockEntity;)V")
    private static void cookingTick(Level pLevel, BlockPos pPos, BlockState pState, StoveBlockEntity pStove, CallbackInfo ci) {
        // cookTick only fires if the campfire is lit and on the server side
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        if (pLevel == null) return;
        IFueledCampfire fueledCampfire = (IFueledCampfire) pStove;
        fueledCampfire.clearChanged();
        fueledCampfire.getFuel();
        fueledCampfire.normalStuff();
        if (fueledCampfire.getChanged()) pStove.setChanged();
    }

    @Inject(at = @At(value = "FIELD", target = "vectorwing/farmersdelight/common/block/entity/StoveBlockEntity.cookingTimes:[I",
            opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2),
            /*locals = LocalCapture.PRINT,*/ method = "cookAndOutputItems()V", remap = false)
    private void cookAndOutputItems(CallbackInfo ci) {
        IFueledCampfire fueledCampfire = (IFueledCampfire) this;
        if (fueledCampfire.getEternal() && ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get()) {
            fueledCampfire.setEternal(false);
        }
    }

    public void getFuel() {
        // This will ensure fuel is distributed to each campfire equally when lit, if items touch multiple campfires
        // It however will not do the 1/4 tick check if it is freshly lit (lit, but no fuel) to ensure it will get fuel
        if (fuelTicks > 0) {
            int mod = Util.mod(worldPosition.getX(), 2) + ((Util.mod(worldPosition.getZ(), 2) + 1) * 2) - 2;
            if ((level.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for(ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            if (Util.failsFuelFilter(false, itemStack)) continue;

            int baseBurnTicks = Util.getItemFuelVal(itemStack);
            boolean eternalItem = ServerConfig.CAMPFIRE_ALLOW_ETERNAL_ITEMS.get() && itemStack.is(ModTags.JC_ETERNAL) && (!isEternal);

            if ((baseBurnTicks > 0) || eternalItem) {
                int itemCount = itemStack.getCount();
                if (burnFuelItem(baseBurnTicks, eternalItem)) {
                    itemEntity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.random.nextFloat() * 0.4F);
                    doFuelInContainer(itemStack.getItem());

                    int newCount = itemCount - 1;
                    if (newCount <= 0) {
                        itemEntity.discard();
                    } else {
                        ItemStack stackCopy = itemStack.copy();
                        stackCopy.setCount(newCount);
                        itemEntity.setItem(stackCopy);
                    }
                }
                this.markChanged = true;
            }
        }
    }

    private List<ItemEntity> getCaptureItems() {
        return this.level.getEntitiesOfClass(ItemEntity.class, getCollectionShape().bounds()
                .move(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), EntitySelector.ENTITY_STILL_ALIVE);
    }

    private VoxelShape getCollectionShape() {
        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        return switch (direction) {
            case WEST -> COLLECTION_AREA_SHAPE_W;
            case EAST -> COLLECTION_AREA_SHAPE_E;
            case SOUTH -> COLLECTION_AREA_SHAPE_S;
            default -> COLLECTION_AREA_SHAPE_N;
        };
    }

    private boolean burnFuelItem(int baseBurnTicks, boolean eternalItem) {
        if (eternalItem) {
            isEternal = true;
            if (baseBurnTicks <= 0) return true;
        }

        int newCurrFuelTicks = fuelTicks + (int) Math.ceil(baseBurnTicks * ServerConfig.CAMPFIRE_FUEL_MULT.get());
        int maxFuel = ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
        if (newCurrFuelTicks < maxFuel) {
            setFuelTicks(newCurrFuelTicks);
            return true;
        } else if (ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get()) {
            setFuelTicks(maxFuel);
            return true;
        }
        return false;
    }

    private void doFuelInContainer(Item item) {
        Item containerItem = Util.fuelContainers.getOrDefault(item, null);
        if (containerItem == null) return;

        Direction direction = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        double d0 = worldPosition.getX()+0.5+(0.85*direction.getStepX());
        double d1 = worldPosition.getY()+0.3;
        double d2 = worldPosition.getZ()+0.5+(0.85*direction.getStepZ());

        ItemEntity itementity = new ItemEntity(level, d0, d1, d2, new ItemStack(containerItem));
        itementity.setDeltaMovement(direction.getStepX()*0.1, 0.01, direction.getStepZ()*0.1);
        level.addFreshEntity(itementity);
    }

    public void normalStuff() {
        // If the campfire is already out of fuel (was lit without refueling) or it is raining, try to extinguish
        if (!isEternal && (fuelTicks <= 0)) {
            extinguishStove(true);
            return;
        }

        if (isEternal) return;
        fuelTicks--;
        if (fuelTicks <= 0) {
            fuelTicks = 0; // Probably (definitely) unneeded, but kept just in case.
            extinguishStove(false);
        }
        this.markChanged = true;
    }

    public void bonfireStuff() {}

    private void extinguishStove(boolean preventDrops) {
        if (isEternal && ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH.get()) {
            isEternal = false;
        }

        // Copied directly from StoveBlock.extinguish(), as it is not a static method
        level.setBlock(worldPosition, this.getBlockState().setValue(BlockStateProperties.LIT, false), 2);
        double x = (double)worldPosition.getX() + 0.5;
        double y = worldPosition.getY();
        double z = (double)worldPosition.getZ() + 0.5;
        level.playLocalSound(x, y, z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F, false);
    }

    public void doExtinguishDrops() {}

    @Override
    public int getBonfireLimit() {
        return -1;
    }

    @Override
    public boolean isSoul() {
        return false;
    }

    @Override
    public int getFuelTicks() {
        return this.fuelTicks;
    }

    @Override
    public void setFuelTicks(int setTicks) {
        this.fuelTicks = setTicks;
    }

    @Override
    public boolean getEternal() {
        return this.isEternal;
    }

    @Override
    public void setEternal(boolean eternal) {
        this.isEternal = eternal;
    }

    @Override
    public boolean getBonfire() { return false; }

    @Override
    public void setBonfire(boolean bonfire) { }

    @Override
    public int[] fetchCookingVariable() {
        return new int[]{0,0,0,0};
    }

    @Override
    public void clearChanged() {
        markChanged = false;
    }

    @Override
    public boolean getChanged() {
        return markChanged;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Data Handling Stuff                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject(at = @At("RETURN"), method = "load(Lnet/minecraft/nbt/CompoundTag;)V")
    private void loadFueled(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("FuelTicks", 3)) {
            setFuelTicks(compound.getInt("FuelTicks"));
        }
        if (compound.contains("IsEternal", 99)) {
            setEternal(compound.getBoolean("IsEternal"));
        }
    }

    @Inject(at = @At("RETURN"), method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V")
    private void saveFueled(CompoundTag compound, CallbackInfo ci) {
        if (compound != null) {
            compound.putInt("FuelTicks", this.fuelTicks);
            compound.putBoolean("IsEternal", this.isEternal);
        }
    }
}


