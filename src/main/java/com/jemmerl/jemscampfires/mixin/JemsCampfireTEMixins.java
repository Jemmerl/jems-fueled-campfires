package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import org.spongepowered.asm.mixin.Mixin;
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

    @Inject(at = @At("RETURN"), method = "<init>*")
    protected void initEdit(CallbackInfo ci) {
        isEternal = ServerConfig.SPAWN_CAMPFIRES_ETERNAL.get();
    }

    @Override
    public int getFuelTicks() {
        return fuelTicks;
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
        return isEternal;
    }

    @Override
    public void setEternal(boolean eternal) {
        isEternal = eternal;
    }


    @Override
    public void onLoad() {
        //super.onLoad();
        if(!this.world.isRemote()) {
            isSoul = (this.getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE.getBlock());
        }
    }

    @Inject(at = @At("RETURN"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if(this.world != null) {
            // This is the first load of the campfire TE
            if(!this.world.isRemote()) {
                if (fuelTicks < 0) {
                    fuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get();
                }
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
