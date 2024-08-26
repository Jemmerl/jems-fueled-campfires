package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CampfireTileEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends TileEntity implements IFueledCampfire {
    private boolean isSoul;
    private int fuelTicks = -1;
    private boolean isEternal = false;

    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    public abstract void dropAllItems();


    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {



        if ((this.world != null) && (!this.world.isRemote()) && (this.getBlockState().get(CampfireBlock.LIT))) {
            if (isSoul) {
                if (isEternal && !ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get()) {
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
                    if (ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get()) {
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



            } else {
                if (isEternal && !ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get()) {
                    return;
                } else if (fuelTicks == 0) {
                    extinguishCampfire(false);
                    return;
                }

                if (feelTheRainOnYourCampfire(false)) {
                    return;
                }

                fuelTicks--;
                if (fuelTicks <= 0) {
                    fuelTicks = 0;
                    if (ServerConfig.CAMPFIRE_BREAK_UNLIT.get()) {
                        breakCampfire();
                    } else {
                        extinguishCampfire(true);
                    }
                }
            }


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


    @Override
    public void onLoad() {
        //super.onLoad();
        if (!this.world.isRemote()) {
            isSoul = (this.getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE.getBlock());

            // This is the first load of the campfire TE
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get();
            }
        }
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
