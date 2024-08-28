package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Random;

@Mixin(value = CampfireTileEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends TileEntity implements IFueledCampfire {
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.makeCuboidShape(0.0D, 7.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    // First-time only settings
    private boolean isSoul = false; // Just in case somehow it is not initialized
    private boolean isEternal = false;


    // General settings
    private int stdMaxFuelTicks; // Maximum fuel assuming without bonfire
    private int trueMaxFuelTicks; // Maximum fuel BASED ON if it can become a bonfire. Trust me this simplifies stuff.
    private double fuelMult;
    private boolean alwaysBurnFuelItems;
    private boolean igniteAround;

    private boolean breakWhenUnlit;
    private boolean rainAffectEternal;
    private int rainFuelLoss;

    private boolean canBonfire;
    private int bonfireFuelUse;
    private boolean bonfireFireSpread;

    // Properties
    private int fuelTicks = -1;
    private boolean isBonfire = false;


    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    public abstract void dropAllItems();

    @Override
    public void onLoad() {
        //super.onLoad();
        if (!this.world.isRemote()) {
            // This is the first load of the campfire TE
            // Get settings that only matter when the campfire is first placed
            if (fuelTicks < 0) {
                isSoul = (this.getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE.getBlock());
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get();
            }

            // Get setting that can change if the server config is changed
            rainAffectEternal = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get() : ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get();
            rainFuelLoss = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS.get() : ServerConfig.CAMPFIRE_RAIN_FUEL_TICK_LOSS.get();
            igniteAround = isSoul ? ServerConfig.SOUL_CAMPFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_FIRESPREAD.get();
            breakWhenUnlit = isSoul ? ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get() : ServerConfig.CAMPFIRE_BREAK_UNLIT.get();
            alwaysBurnFuelItems = isSoul ? ServerConfig.SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get() : ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get();
            fuelMult = isSoul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get();
            canBonfire = isSoul ? ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get() : ServerConfig.CAMPFIRE_CAN_BONFIRE.get();
            bonfireFuelUse = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_BURN_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_BURN_MULT.get();
            bonfireFireSpread = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_BONFIRE_FIRESPREAD.get();
            stdMaxFuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
            trueMaxFuelTicks = stdMaxFuelTicks;
            if (canBonfire) {
                trueMaxFuelTicks += (isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS.get());
            }

        }
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (world == null) return;
        if (!this.getBlockState().get(CampfireBlock.LIT)) return;
        getFuel();
        normalStuff();
        if (isBonfire) bonfireStuff();
    }

    private void getFuel() {
        // This will ensure fuel is distributed to each campfire equally when lit, if items touch multiple campfires
        // It however will not do the 1/4 tick check if it is freshly lit (lit, but no fuel) to ensure it will get fuel
        if (fuelTicks > 0) {
            int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
            if ((this.world.getGameTime() % 4) != mod) {
                return;
            }
        }


        for(ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            int baseBurnTicks = ForgeHooks.getBurnTime(itemStack, null);

            if (baseBurnTicks > 0) {
                // client stuff
                if (this.world.isRemote) {

                }

                // server stuff
                if (!this.world.isRemote) {
                    int itemCount = itemStack.getCount();
                    if (burnFuelItem(baseBurnTicks)) {
                        int newCount = itemCount - 1;
                        if (newCount <= 0) {
                            itemEntity.remove();
                        } else {
                            ItemStack stackCopy = itemStack.copy();
                            stackCopy.setCount(newCount);
                            itemEntity.setItem(stackCopy);
                        }
                    }
                }
            }
        }
    }

    private List<ItemEntity> getCaptureItems() {
        return this.world.getEntitiesWithinAABB(ItemEntity.class, COLLECTION_AREA_SHAPE.getBoundingBox()
                .offset(pos.getX(), pos.getY(), pos.getZ()), EntityPredicates.IS_ALIVE);
    }

    // TODO items with very high fuel values are generally unusable in campfires. fix? config?
    private boolean burnFuelItem(int baseBurnTicks) {
        int newCurrFuelTicks = getFuelTicks() + (int) Math.ceil(baseBurnTicks * fuelMult);
        if (newCurrFuelTicks < trueMaxFuelTicks) {
            setFuelTicks(newCurrFuelTicks);
            return true;
        }
        if (alwaysBurnFuelItems && this.getBlockState().get(CampfireBlock.LIT)) {
            setFuelTicks(trueMaxFuelTicks);
            return true;
        }
        return false;
    }

    private void normalStuff() {
        if ((!this.world.isRemote())) {
            if (isEternal && !rainAffectEternal) {
                return;
            } else if (fuelTicks == 0) {
                extinguishCampfire(false);
                return;
            }

            // Returns true if the rain extinguishes the campfire
            if (feelTheRainOnYourCampfire()) {
                return;
            }

            //todo add option to remove excess bonfire fuel when extinguished?

            isBonfire = canBonfire && (fuelTicks > stdMaxFuelTicks);
            if (igniteAround && !isBonfire && (this.world.rand.nextInt(70) == 0)) {
                Direction dir = Direction.byHorizontalIndex(world.rand.nextInt(4));
                ignitePos(pos.offset(dir), false);
            }

            fuelTicks -= isBonfire ? bonfireFuelUse : 1;
            if (fuelTicks <= 0) {
                fuelTicks = 0;
                if (breakWhenUnlit) {
                    breakCampfire();
                } else {
                    extinguishCampfire(true);
                }
            }

//                if (!isEternal) {
//                    if ((ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get()) && (fuelTicks > ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get())) {
//                    } else {
//                    }
//                }
        }
    }


    private void bonfireStuff() {
        if (bonfireFireSpread) {
            Random rand = this.world.rand;
            if (rand.nextInt(25) != 0) return;
            Direction dir1 = Direction.byHorizontalIndex(rand.nextInt(4));
            Direction dir2 = Direction.getRandomDirection(rand);
            if ((dir2.getOpposite() == dir1) || (dir2.getHorizontalIndex() < 0)) {
                ignitePos(pos.offset(dir1), true);
            } else {
                ignitePos(pos.offset(dir1).offset(dir2), true);
            }
        }
    }

    private boolean ignitePos(BlockPos blockPos, boolean ignoreFlammable) {
        Material material = this.world.getBlockState(blockPos).getMaterial();
        if (material.isReplaceable() && !material.isLiquid()) {
            BlockState downState = this.world.getBlockState(blockPos.down());
            if (downState.isOpaqueCube(this.world, blockPos.down()) &&
                    (ignoreFlammable || downState.isFlammable(world, pos, Direction.UP))) {
                return this.world.setBlockState(blockPos, AbstractFireBlock.getFireForPlacement(this.world, blockPos));
            }
        }
        return false;
    }



    private void extinguishCampfire(boolean drops) {
        this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (drops) {
            CampfireBlock.extinguish(this.world, pos, this.getBlockState());
        }
        this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.LIT, false));
    }

    private void breakCampfire() {
        this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        this.dropAllItems();
        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
    }

    private boolean feelTheRainOnYourCampfire() {
        if (world.isRainingAt(this.pos.up())) {
            if (rainFuelLoss == -1) {
                extinguishCampfire(false);
                return true;
            } else {
                fuelTicks -= rainFuelLoss;
            }
        }
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
    public void addFuelTicks(int addTicks) {
        this.fuelTicks += addTicks;
    }

    @Override
    public boolean getEternal() {
        return this.isEternal;
    }

    @Override
    public void setEternal(boolean eternal) {
        this.isEternal = eternal;
    }


    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readFueled(BlockState state, CompoundNBT nbt, CallbackInfo ci) {
        if (nbt.contains("FuelTicks", 3)) {
            setFuelTicks(nbt.getInt("FuelTicks"));
        }
        if (nbt.contains("IsEternal", 99)) {
            setEternal(nbt.getBoolean("IsEternal"));
        }
    }

    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    private void writeFueled(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", this.fuelTicks);
            nbt.putBoolean("IsEternal", this.isEternal);
            cir.setReturnValue(nbt);
            cir.cancel();
        }
    }

}
