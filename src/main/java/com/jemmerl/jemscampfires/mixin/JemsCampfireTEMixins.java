package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.util.IFueledCampfire;
import net.minecraft.block.BlockState;
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
    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    private int fuelTicks = 0;
    private boolean isEternal = false;

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
