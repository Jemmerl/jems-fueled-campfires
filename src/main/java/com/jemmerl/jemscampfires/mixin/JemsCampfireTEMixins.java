package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.JCTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Random;

@Mixin(value = CampfireTileEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends TileEntity implements IFueledCampfire {
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.makeCuboidShape(0.0D, 7.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    private boolean isSoul;
    private boolean isBonfire = false;

    private int stdMaxFuelTicks; // Maximum fuel assuming without bonfire
    private int trueMaxFuelTicks; // Maximum fuel BASED ON if it can become a bonfire. Trust me this simplifies stuff.
    private double fuelMult;
    private boolean alwaysBurnFuelItems;
    private boolean igniteAround;

    private boolean breakWhenUnlit;
    private boolean rainAffectEternal;
    private boolean loseEternalWhenCook;
    private int rainFuelLoss;
    private boolean allowEternalFuels;
    //private long prevDayTime = 0L;

    private boolean canBonfire;
    private int bonfireFuelUse;
    private int bonfireCookMult;
    private boolean bonfireFireSpread;
    private boolean bonfireExtraParticles;

    // Properties
    private int fuelTicks = -1;
    private boolean isEternal = false;

    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    private int[] cookingTimes;

    @Shadow
    public abstract void dropAllItems();

    @Override
    public void onLoad() {
        //super.onLoad();
        if (!this.world.isRemote()) {
            isSoul = (this.getBlockState().getBlock() == Blocks.SOUL_CAMPFIRE.getBlock());
            //prevDayTime = world.getDayTime();

            // This is the first load of the campfire TE
            // Get settings that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get();
            }

            // Get setting that can change if the server config is changed
            rainAffectEternal = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get() : ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get();
            rainFuelLoss = isSoul ? ServerConfig.SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS.get() : ServerConfig.CAMPFIRE_RAIN_FUEL_TICK_LOSS.get();
            loseEternalWhenCook = isSoul ? ServerConfig.SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get() : ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get();
            allowEternalFuels = isSoul ? ServerConfig.SOUL_CAMPFIRE_ALLOW_ETERNAL_ITEMS.get() : ServerConfig.CAMPFIRE_ALLOW_ETERNAL_ITEMS.get();
            igniteAround = isSoul ? ServerConfig.SOUL_CAMPFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_FIRESPREAD.get();
            breakWhenUnlit = isSoul ? ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get() : ServerConfig.CAMPFIRE_BREAK_UNLIT.get();
            alwaysBurnFuelItems = isSoul ? ServerConfig.SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get() : ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get();
            fuelMult = isSoul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get();
            canBonfire = isSoul ? ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get() : ServerConfig.CAMPFIRE_CAN_BONFIRE.get();
            bonfireFuelUse = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_BURN_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_BURN_MULT.get();
            bonfireCookMult = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_COOKING_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_COOKING_MULT.get();
            bonfireFireSpread = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_BONFIRE_FIRESPREAD.get();
            bonfireExtraParticles = isSoul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_EXTRA_PARTICLES.get() : ServerConfig.CAMPFIRE_BONFIRE_EXTRA_PARTICLES.get();
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
        if (!this.getBlockState().get(CampfireBlock.LIT)) {
            return;
        }

//        long dayTime = world.getDayTime();
//        System.out.println(prevDayTime + " " + dayTime);
//        if ((fuelTicks > 0) && ((dayTime - prevDayTime) != 1)) {
//            long diff = 24000L - prevDayTime + dayTime;
//            fuelTicks = Math.max(fuelTicks-(int)diff, 0);
//            if (fuelTicks == 0) {
//                outOfFuel(false); // No sounds, like it happened in your sleep
//                prevDayTime = dayTime;
//                return;
//            }
//        }

        getFuel();
        normalStuff();
        if (isBonfire) bonfireStuff();
        //prevDayTime = dayTime;
    }

    @Inject(at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPLT, ordinal = 0), locals = LocalCapture.CAPTURE_FAILHARD, method = "cookAndDrop()V")
    private void cookAndDrop(CallbackInfo ci, int i, ItemStack itemstack, int j) {
        if (isEternal && loseEternalWhenCook) {
            this.isEternal = false;
        }

        if (isBonfire) {
            this.cookingTimes[i] += (bonfireCookMult - 1);
            j = cookingTimes[i];
        }
    }

    private void getFuel() {
        if (!this.world.isRemote) {
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
                boolean eternalItem = allowEternalFuels && itemStack.getItem().isIn(JCTags.JC_ETERNAL) && (!isEternal);

                if ((baseBurnTicks > 0) || eternalItem) {
                    int itemCount = itemStack.getCount();
                    if (burnFuelItem(baseBurnTicks, eternalItem)) {
                        itemEntity.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
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
    private boolean burnFuelItem(int baseBurnTicks, boolean eternalItem) {
        if (eternalItem) {
            isEternal = true;
            if (baseBurnTicks <= 0) return true;
        }

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
        if (!this.world.isRemote()) {
            if (isEternal && !rainAffectEternal) {
                return;
            } else if (fuelTicks == 0) {
                extinguishCampfire(true, false);
                return;
            }

            // Returns true if the rain extinguishes the campfire
            if (feelTheRainOnYourCampfire()) {
                return;
            }

            //todo add option to remove excess bonfire fuel when extinguished?

            isBonfire = canBonfire && (fuelTicks > stdMaxFuelTicks);
            if (!isBonfire) {
                if (bonfireExtraParticles && this.getBlockState().get(CampfireBlock.SIGNAL_FIRE) && !this.world.getBlockState(pos.down()).matchesBlock(Blocks.HAY_BLOCK)) {
                    this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.SIGNAL_FIRE, false));
                }

                if (igniteAround && (this.world.rand.nextInt(70) == 0)) {
                    Direction dir = Direction.byHorizontalIndex(world.rand.nextInt(4));
                    ignitePos(pos.offset(dir), false);
                }
            }

            fuelTicks -= isBonfire ? bonfireFuelUse : 1;
            if (fuelTicks <= 0) {
                fuelTicks = 0;
                outOfFuel(true);
            }

//                if (!isEternal) {
//                    if ((ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get()) && (fuelTicks > ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get())) {
//                    } else {
//                    }
//                }
        }
    }

    private void bonfireStuff() {
        if (!this.world.isRemote()) {
            Random rand = this.world.rand;
            if (bonfireExtraParticles) {
                this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.SIGNAL_FIRE, true));
            }

            if (bonfireFireSpread) {
                if (rand.nextInt(30) != 0) return;
                Direction dir1 = Direction.byHorizontalIndex(rand.nextInt(4));
                Direction dir2 = Direction.getRandomDirection(rand);
                if ((dir2.getOpposite() == dir1) || (dir2.getHorizontalIndex() < 0)) {
                    ignitePos(pos.offset(dir1), true);
                } else {
                    ignitePos(pos.offset(dir1).offset(dir2), true);
                }
            }
        }
    }

    private void ignitePos(BlockPos blockPos, boolean ignoreFlammable) {
        Material material = this.world.getBlockState(blockPos).getMaterial();
        if (material.isReplaceable() && !material.isLiquid()) {
            BlockState downState = this.world.getBlockState(blockPos.down());
            if (downState.isOpaqueCube(this.world, blockPos.down()) &&
                    (ignoreFlammable || downState.isFlammable(world, pos, Direction.UP))) {
                this.world.setBlockState(blockPos, AbstractFireBlock.getFireForPlacement(this.world, blockPos));
            }
        }
        //return false;
    }


    private void extinguishCampfire(boolean sound, boolean drops) {
        if (sound) {
            this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        if (drops) {
            CampfireBlock.extinguish(this.world, pos, this.getBlockState());
        }
        this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.LIT, false));
    }

    private void breakCampfire(boolean sound) {
        if (sound) {
            this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        this.dropAllItems();
        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
    }

    private boolean feelTheRainOnYourCampfire() {
        if (world.isRainingAt(this.pos.up())) {
            if (rainFuelLoss == -1) {
                extinguishCampfire(true, false);
                return true;
            } else {
                fuelTicks -= rainFuelLoss;
            }
        }
        return false;
    }

    private void outOfFuel(boolean sound) {
        if (breakWhenUnlit) {
            breakCampfire(sound);
        } else {
            extinguishCampfire(sound, true);
        }
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
