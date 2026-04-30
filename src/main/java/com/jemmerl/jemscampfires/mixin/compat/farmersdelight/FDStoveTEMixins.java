package com.jemmerl.jemscampfires.mixin.compat.farmersdelight;

import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.tile.FDSyncedTileEntity;
import vectorwing.farmersdelight.tile.StoveTileEntity;

import java.util.List;

// Compat mixin done with permission from vectorwing with condition of configurability! :)
@SuppressWarnings("target")
@Mixin(value = StoveTileEntity.class, priority = 0)
public abstract class FDStoveTEMixins extends FDSyncedTileEntity implements IFueledCampfire {

    // Front box, rotates based on stove block rotation
    private static final VoxelShape COLLECTION_AREA_SHAPE_N = Block.makeCuboidShape(0.0D, 0.0D, -1.0D, 16.0D, 6.0D, 0.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_S = Block.makeCuboidShape(0.0D, 0.0D, 15.0D, 16.0D, 6.0D, 17.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_E = Block.makeCuboidShape(15.0D, 0.0D, 0.0D, 17.0D, 6.0D, 16.0D);
    private static final VoxelShape COLLECTION_AREA_SHAPE_W = Block.makeCuboidShape(-1.0D, 0.0D, 0.0D, 0.0D, 6.0D, 16.0D);
    private static final VoxelShape[] COLLECTION_SHAPES =
            { COLLECTION_AREA_SHAPE_S, COLLECTION_AREA_SHAPE_W,
            COLLECTION_AREA_SHAPE_N, COLLECTION_AREA_SHAPE_E};

    private int fuelTicks = -1;
    private boolean isEternal = false;
    private boolean markChanged = false;

    public FDStoveTEMixins(TileEntityType<?> pType) {
        super(pType);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.world.isRemote()) {
            // This is the first load of the campfire TE
            // Get settings/properties that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = Math.min((ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get()), ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get());
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (!ServerConfig.FARMERS_DELIGHT_STOVE_COMPAT.get()) return;
        if (world == null) return;
        if (world.isRemote) return;
        if (!this.getBlockState().get(BlockStateProperties.LIT)) return;

        markChanged = false;
        getFuel();
        normalStuff();
        if (markChanged) this.markDirty();
    }

//    @Inject(at = @At(value = "FIELD", target = "vectorwing/farmersdelight/common/block/entity/StoveBlockEntity.cookingTimes:[I",
//            opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2), locals = LocalCapture.PRINT, method = "cookAndOutputItems()V", remap = false)
    @Inject(method = "cookAndOutputItems()V", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isEmpty()Z", shift = At.Shift.BY, by = 2))
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
            int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
            if ((world.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for(ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            if (Util.failsFuelFilter(false, item)) continue;

            int baseBurnTicks = Util.getItemFuelVal(itemStack, item);
            boolean eternalItem = ServerConfig.CAMPFIRE_ALLOW_ETERNAL_ITEMS.get() && item.isIn(ModTags.JC_ETERNAL) && (!isEternal);

            if ((baseBurnTicks > 0) || eternalItem) {
                int itemCount = itemStack.getCount();
                if (burnFuelItem(baseBurnTicks, eternalItem)) {
                    itemEntity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
                    doFuelInContainer(itemStack.getItem());

                    int newCount = itemCount - 1;
                    if (newCount <= 0) {
                        itemEntity.remove();
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
        return this.world.getEntitiesWithinAABB(ItemEntity.class, getCollectionShape().getBoundingBox()
                .offset(pos.getX(), pos.getY(), pos.getZ()), EntityPredicates.IS_ALIVE);
    }

    private VoxelShape getCollectionShape() {
        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        return COLLECTION_SHAPES[direction.getHorizontalIndex()];
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

        Direction direction = this.getBlockState().get(BlockStateProperties.HORIZONTAL_FACING);
        double d0 = pos.getX()+0.5+(0.85*direction.getXOffset());
        double d1 = pos.getY()+0.3;
        double d2 = pos.getZ()+0.5+(0.85*direction.getZOffset());

        ItemEntity itementity = new ItemEntity(world, d0, d1, d2, new ItemStack(containerItem));
        itementity.addVelocity(direction.getXOffset()*0.1, 0.01, direction.getZOffset()*0.1);
        world.addEntity(itementity);
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
        world.setBlockState(pos, this.getBlockState().with(BlockStateProperties.LIT, false), 2);
        double x = (double)pos.getX() + 0.5;
        double y = pos.getY();
        double z = (double)pos.getZ() + 0.5;
        world.playSound(x, y, z, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 0.5F, 2.6F, false);
        markChanged = true;
    }

    public void doExtinguishDrops() {}

    @Override
    public int getBonfireLimit() {
        return -1;
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
    public void updateLighting() {}

    @Override
    public void setFuelLightLevel(int lightLevel, boolean lightingUpdate) {}

    @Override
    public int getFuelLightLevel() {
        return 15;
    }

    @Override
    public void dynamicLightLevelUpdate(boolean lightingUpdate) {}

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Data Handling Stuff                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readFueled(BlockState state, CompoundNBT compound, CallbackInfo ci) {
        if (compound.contains("FuelTicks", 3)) {
            setFuelTicks(compound.getInt("FuelTicks"));
        }
        if (compound.contains("IsEternal", 99)) {
            setEternal(compound.getBoolean("IsEternal"));
        }
    }

    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    private void writeFueled(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", this.fuelTicks);
            nbt.putBoolean("IsEternal", this.isEternal);
        }
        cir.setReturnValue(nbt);
    }
}