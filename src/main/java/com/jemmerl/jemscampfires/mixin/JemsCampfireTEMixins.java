package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = CampfireTileEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends TileEntity implements IFueledCampfire {
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.makeCuboidShape(0.0D, 7.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private boolean isSoul;

    // General settings
    private boolean rainAffectEternal;
    private boolean breakWhenUnlit;
    private boolean canBonfire;
    private int maxFuelTicks;
    private int maxOverFuelTicks;
    private boolean alwaysBurnFuelItems;
    private double fuelMult;

    // First-time only settings
    private int fuelTicks = -1;
    private boolean isEternal = false;

    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    public abstract void dropAllItems();

    @Override
    public void onLoad() {
        //super.onLoad();
        if (!this.world.isRemote()) {
            // Get setting that can change if the server config is changed
            isSoul = (this.getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE.getBlock());
            rainAffectEternal = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get() : ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get();
            breakWhenUnlit = isSoul ? ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get() : ServerConfig.CAMPFIRE_BREAK_UNLIT.get();
            canBonfire = isSoul ? ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get() : ServerConfig.CAMPFIRE_CAN_BONFIRE.get();
            maxFuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
            maxOverFuelTicks = maxFuelTicks + (isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_BONFIRE_EXTRA_MAX_FUEL_TICKS.get());
            alwaysBurnFuelItems = isSoul ? ServerConfig.SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get() : ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get();
            fuelMult = isSoul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get();

            // This is the first load of the campfire TE
            // Get settings that only matter when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (world == null) return;
        getFuel();
        useFuel();
    }

    private void getFuel() {
        // This will ensure fuel is distributed to each campfire equally, if fuel is touching multiple at once
        int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
        if ((this.world.getGameTime() % 4) != mod) {
            return;
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
    // TODO make the "max fuel" check based on bonfire extra ticks if the campfire can bonfire
    private boolean burnFuelItem(int baseBurnTicks) {
        int adjBurnTicks = (int) Math.ceil(baseBurnTicks * fuelMult);
        int newCurrFuelTicks = getFuelTicks() + adjBurnTicks;
        if (newCurrFuelTicks > maxFuelTicks) {
            if (alwaysBurnFuelItems && this.getBlockState().get(CampfireBlock.LIT)) {
                setFuelTicks(maxFuelTicks);
                return true;
            }
            if (canBonfire) {
                System.out.println("bonfire!");
                // Bonfires will *always* consume fuel on them. Maybe I'll configure this is someone complains.
                setFuelTicks(Math.min(newCurrFuelTicks, maxOverFuelTicks));
                //cfTileEntity.setBonfire(true);
                return true;
            }
            return false;
        }
        setFuelTicks(newCurrFuelTicks);
        return true;
    }

    private void useFuel() {
        if ((!this.world.isRemote()) && (this.getBlockState().get(CampfireBlock.LIT))) {
            if (isEternal && !rainAffectEternal) {
                return;
            } else if (fuelTicks == 0) {
                extinguishCampfire(false);
                return;
            }

            if (feelTheRainOnYourCampfire(true)) {
                return;
            }

            fuelTicks--;
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

    private boolean feelTheRainOnYourCampfire(boolean isSoul) {
        if (world.isRainingAt(this.pos.up())) {
            int fuelLoss = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS.get() : ServerConfig.CAMPFIRE_RAIN_FUEL_TICK_LOSS.get();
            if (fuelLoss == -1) {
                extinguishCampfire(false);
                return true;
            } else {
                fuelTicks -= fuelLoss;
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
    public void readFueled(BlockState state, CompoundNBT nbt, CallbackInfo ci) {
        if (nbt.contains("FuelTicks", 3)) {
            setFuelTicks(nbt.getInt("FuelTicks"));
        }
        if (nbt.contains("IsEternal", 99)) {
            setEternal(nbt.getBoolean("IsEternal"));
        }
    }

    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    public void writeFueled(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", this.fuelTicks);
            nbt.putBoolean("IsEternal", this.isEternal);
            cir.setReturnValue(nbt);
            cir.cancel();
        }
    }

}
