package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
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
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.makeCuboidShape(-1.0D, 3.0D, -1.0D, 17.0D, 16.0D, 17.0D);

    // Properties
    private boolean isSoul;
    private int fuelTicks = -1;
    private boolean isEternal = false;
    private boolean isBonfire = false;
    private boolean markChanged = false;
    private int fuelLightLevel = -1;

    public JemsCampfireTEMixins(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    private int[] cookingTimes;

    @Shadow
    private NonNullList<ItemStack> inventory;

    @Shadow
    public abstract void dropAllItems();

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.world.isRemote()) {
            isSoul = (this.getBlockState().getBlock().getRegistryName().toString().contains("soul"));

            // This is the first load of the campfire TE
            // Get settings/properties that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();

                // ... UNLESS the player check compat. fix is enabled, at which the above is overridden if a player is
                //  near but did not place it directly. This will fix an issue where campfires are player made but not
                //  directly. It could cause issues when spawning in near a world-genned campfire, but it's rare.
                if (ServerConfig.PLAYER_CHECK_FIX.get() && world.isPlayerWithin(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5.5D)) {
                    isEternal = isSoul ? ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.PLACE_CAMPFIRE_ETERNAL.get();
                }

                fuelTicks = Math.min((isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get()), getStandardMaxFuelTicks(isSoul));
            }

            if (!ServerConfig.FUEL_BASED_LIGHTING.get() || (isEternal && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) {
                updateLighting();
                return;
            }
            dynamicLightLevelUpdate();
        }
    }


    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (world == null) return;
        if (world.isRemote) return;
        if (!this.getBlockState().get(CampfireBlock.LIT)) return;

        markChanged = false;
        getFuel();
        normalStuff();
        if (isBonfire) bonfireStuff();
        if (markChanged) this.markDirty();
    }

    @Inject(at = @At(value = "FIELD", target = "net/minecraft/tileentity/CampfireTileEntity.cookingTimes:[I", opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2), locals = LocalCapture.CAPTURE_FAILHARD, method = "cookAndDrop()V")
    private void cookAndDrop(CallbackInfo ci, int i, ItemStack itemstack) {
        if (isEternal && getLoseEternalCook(isSoul)) {
            isEternal = false;
        }
        if (isBonfire) {
            cookingTimes[i] += (getBonfireCookMult(isSoul) - 1);
            //j = cookingTimes[i];
        }
    }

    private void getFuel() {
        // This will ensure fuel is distributed to each campfire equally when lit, if items touch multiple campfires
        // It however will not do the 1/4 tick check if it is freshly lit (lit, but no fuel) to ensure it will get fuel
        if (fuelTicks > 0) {
            int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
            if ((this.world.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for (ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            if (Util.failsFuelFilter(isSoul, item)) continue;

            int baseBurnTicks = Util.getItemFuelVal(itemStack, item);
            boolean eternalItem = getAllowEternalItems(isSoul) && item.isIn(ModTags.JC_ETERNAL) && (!isEternal);

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
                markChanged = true;
            }
        }
    }

    private List<ItemEntity> getCaptureItems() {
        return this.world.getEntitiesWithinAABB(ItemEntity.class, COLLECTION_AREA_SHAPE.getBoundingBox()
                .offset(pos.getX(), pos.getY(), pos.getZ()), EntityPredicates.IS_ALIVE);
    }

    private boolean burnFuelItem(int baseBurnTicks, boolean eternalItem) {
        if (eternalItem) {
            isEternal = true;
            setBonfire(false);
            if (baseBurnTicks <= 0) return true;
        }

        int newCurrFuelTicks = fuelTicks + (int) Math.ceil(baseBurnTicks * getFuelMult(isSoul));
        if (newCurrFuelTicks < getTrueMaxFuelTicks(isSoul)) {
            setFuelTicks(newCurrFuelTicks);
            return true;
        }
        if (getAlwaysBurnFuel(isSoul)) {
            setFuelTicks(getTrueMaxFuelTicks(isSoul));
            return true;
        }
        return false;
    }

    private void doFuelInContainer(Item item) {
        Item containerItem = Util.fuelContainers.getOrDefault(item, null);
        if (containerItem == null) return;

        double d0 = pos.getX() + 0.5;
        double d1 = pos.getY() + 0.9;
        double d2 = pos.getZ() + 0.5;

        double degree = Math.toRadians(world.rand.nextInt(360));
        double sin = Math.sin(degree);
        double cos = Math.cos(degree);

        ItemEntity itementity = new ItemEntity(world, d0, d1, d2, new ItemStack(containerItem));
        itementity.addVelocity(sin * 0.2D, 0.01, cos * 0.2D);
        world.addEntity(itementity);
    }

    private void normalStuff() {
        // If the campfire is already out of fuel (was lit without refueling) or it is raining, try to extinguish
        if ((!isEternal && (fuelTicks <= 0)) || ((!isEternal || getRainEternal(isSoul)) && feelTheRainOnYourCampfire())) {
            extinguishCampfire(false);
            return;
        }

        setBonfire(getCanBonfire(isSoul) && (!isEternal || getEternalBonfire(isSoul)) && (fuelTicks > getStandardMaxFuelTicks(isSoul)));
        if ((!isBonfire) && getNormalFirespread(isSoul) && (world.rand.nextInt(70) == 0)) {
            Direction dir = Direction.byHorizontalIndex(world.rand.nextInt(4));

            BlockPos ignPos = pos.offset(dir);
            if (canIgnitePos(ignPos, false)) {
                this.world.setBlockState(ignPos, AbstractFireBlock.getFireForPlacement(this.world, pos));

            }
        }

        if (isEternal) {
            // The only time markChanged is true here is if fuel was added, which is when this update may be needed.
            if (markChanged && ServerConfig.FUEL_BASED_LIGHTING.get() && ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get()) {
                dynamicLightLevelUpdate();
            }
            return;
        }
        fuelTicks -= isBonfire ? getBonfireFuelUse(isSoul) : 1;
        if (fuelTicks <= 0) {
            fuelTicks = 0;
            outOfFuel();
        }
        if (ServerConfig.FUEL_BASED_LIGHTING.get()) dynamicLightLevelUpdate();
        markChanged = true;
    }

    private void bonfireStuff() {
        Random rand = this.world.rand;

        if (getBonfireFirespread(isSoul)) {
            if (rand.nextInt(40) != 0) return;
            Direction dir1 = Direction.byHorizontalIndex(rand.nextInt(4));
            Direction dir2 = Direction.getRandomDirection(rand);

            // If dir2 points opposite of dir1 or is UP/DOWN, then light directly adjacent to the campfire.
            // Else, light one block away from the campfire.
            BlockPos ignPos = pos.offset(dir1).up();
            if ((dir2.getOpposite() != dir1) && (dir2.getHorizontalIndex() > 0)) {
                ignPos = ignPos.offset(dir2);
            }

            for (int down = 0; down <= 2; down++) {
                if (canIgnitePos(ignPos, true)) {
                    world.setBlockState(ignPos, AbstractFireBlock.getFireForPlacement(world, ignPos));
                    break;
                }
                ignPos = ignPos.down();
            }
        }
    }

    @Override
    public int getBonfireLimit() {
        if (getCanBonfire(isSoul)) {
            return getStandardMaxFuelTicks(isSoul);
        }
        return -1;
    }

    private boolean canIgnitePos(BlockPos blockPos, boolean ignoreFlammable) {
        Material material = world.getBlockState(blockPos).getMaterial();
        if (material.isReplaceable() && !material.isLiquid()) {
            BlockState downState = world.getBlockState(blockPos.down());
            return (downState.isOpaqueCube(world, blockPos.down()) &&
                    (ignoreFlammable || downState.isFlammable(world, pos, Direction.UP)));
        }
        return false;
    }

    // Returns true if the rain extinguishes the campfire
    private boolean feelTheRainOnYourCampfire() {
        if (world.isRainingAt(this.pos.up())) {
            if (getRainFuelLoss(isSoul) == -1) {
                return true;
            } else {
                fuelTicks = Math.max(fuelTicks - getRainFuelLoss(isSoul), 0);
                markChanged = true;
                return (fuelTicks <= 0);
            }
        }
        return false;
    }

    private void outOfFuel() {
        if (getBreakUnlit(isSoul)) {
            breakCampfire();
        } else {
            extinguishCampfire(true);
        }
    }

    private void extinguishCampfire(boolean drops) {
        this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        if (drops) {
            CampfireBlock.extinguish(this.world, pos, this.getBlockState());
            // doExtinguished is called from the campfire block normally to handle other extinguishing factors,
            // like shovels and water bottles, so it is not called here.
        } else {
            this.world.setBlockState(this.pos, this.getBlockState().with(CampfireBlock.LIT, false));
            doExtinguished();
        }
    }

    public void doExtinguished() {
        if (isEternal && getLoseEternalExtinguish(isSoul)) {
            isEternal = false;
        }
        if (isBonfire && getLoseBonfireFuelExtinguish(isSoul)) {
            setFuelTicks(Math.min(fuelTicks, getStandardMaxFuelTicks(isSoul)));
        }
    }

    private void breakCampfire() {
        this.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F);
        this.dropAllItems();
        this.world.setBlockState(this.pos, Blocks.AIR.getDefaultState());
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Getters and Setters                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // General
    private static int getStandardMaxFuelTicks(boolean soul) {
        // Maximum fuel assuming without bonfire
        return soul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
    }

    private static int getTrueMaxFuelTicks(boolean soul) {
        // Maximum fuel BASED ON if it can become a bonfire. Trust me this simplifies stuff.
        int trueMaxFuelTicks = getStandardMaxFuelTicks(soul);
        if (getCanBonfire(soul)) {
            trueMaxFuelTicks += (soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_BONFIRE_FUEL_TICKS.get());
        }
        return trueMaxFuelTicks;
    }

    private static double getFuelMult(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get();
    }

    private static boolean getAlwaysBurnFuel(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get() : ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get();
    }

    private static boolean getBreakUnlit(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get() : ServerConfig.CAMPFIRE_BREAK_UNLIT.get();
    }

    private static boolean getNormalFirespread(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_FIRESPREAD.get();
    }

    private static int getRainFuelLoss(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS.get() : ServerConfig.CAMPFIRE_RAIN_FUEL_TICK_LOSS.get();
    }

    // Decor
    private static boolean getAllowEternalItems(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ALLOW_ETERNAL_ITEMS.get() : ServerConfig.CAMPFIRE_ALLOW_ETERNAL_ITEMS.get();
    }

    private static boolean getLoseEternalCook(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get() : ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get();
    }

    private static boolean getLoseEternalExtinguish(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH.get() : ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH.get();
    }

    private static boolean getRainEternal(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get() : ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get();
    }

    private static boolean getEternalBonfire(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ETERNAL_BONFIRE.get() : ServerConfig.CAMPFIRE_ETERNAL_BONFIRE.get();
    }

    // Bonfire
    private static boolean getCanBonfire(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get() : ServerConfig.CAMPFIRE_CAN_BONFIRE.get();
    }

    private static int getBonfireFuelUse(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_BURN_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_BURN_MULT.get();
    }

    private static boolean getLoseBonfireFuelExtinguish(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH.get() : ServerConfig.CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH.get();
    }

    private static int getBonfireCookMult(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_COOKING_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_COOKING_MULT.get();
    }

    private static boolean getBonfireFirespread(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_BONFIRE_FIRESPREAD.get();
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
        if ((world == null) || !ServerConfig.FUEL_BASED_LIGHTING.get() || !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get()) {
            setFuelLightLevel(isSoul ? 10 : 15);
        } else {
            dynamicLightLevelUpdate();
        }
        isEternal = eternal;
    }

    @Override
    public boolean getBonfire() {
        return this.isBonfire;
    }

    @Override
    public void setBonfire(boolean bonfire) {
        if (this.isBonfire != bonfire) {
            this.isBonfire = bonfire;
            markChanged = true;
            if ((world != null) && (!world.isRemote)) {
                BlockState state = this.getBlockState();
                world.notifyBlockUpdate(pos, state, state, 18); // Uses 2 client updates, and 16 no observers
            }
        }
    }

    // TODO add config for this formula? Not unless someone asks.
    @Override
    public void dynamicLightLevelUpdate(boolean lightingUpdate) {
        int rampPeak = Math.min((int) (getStandardMaxFuelTicks(isSoul) * 0.34f), 3600);
        if (fuelTicks < rampPeak) {
            float perc = fuelTicks / (float) rampPeak;
            int val = (int) (isSoul ? (6 + 3 * perc) : (8 + 7 * perc));
            setFuelLightLevel(val, lightingUpdate);
            return;
        }
        setFuelLightLevel(0, lightingUpdate);
    }

    @Override
    public void setFuelLightLevel(int fuelLightLevel, boolean lightingUpdate) {
        if (this.fuelLightLevel != fuelLightLevel) {
            this.fuelLightLevel = fuelLightLevel;
            markChanged = true;
            if (world == null) {
                return;
            }
            if (lightingUpdate) updateLighting();
        }
    }

    @Override
    public int getFuelLightLevel() {
        return fuelLightLevel;
    }

    @Override
    public void updateLighting() {
        BlockState state = getBlockState();
        world.notifyBlockUpdate(pos, state, state, 26); // Uses 2 client updates, 8 forces main render thread, and 16 no observers
        world.getChunkProvider().getLightManager().checkBlock(pos);
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Data Handling Stuff                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Don't need to override, because the Campfire version just calls "getUpdateTag" anyway.
//    @Override
//    @Nullable
//    // BLOCK UPDATES Send packet to client.
//    public SUpdateTileEntityPacket getUpdatePacket(){
//    }

    @Override
    // Receive BLOCK UPDATE packet on client
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbtTag = pkt.getNbtCompound();

        if (nbtTag.contains("IsBonfire", 99)) {
            setBonfire(nbtTag.getBoolean("IsBonfire"));
        }

        if (!ServerConfig.FUEL_BASED_LIGHTING.get() ||
                (isEternal && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) return;

        if (nbtTag.contains("FuelLight", 1)) {
            setFuelLightLevel(nbtTag.getByte("FuelLight"));
        } else {
            setFuelLightLevel(0);
        }
    }

    // Used for any sync to client, either block update or chunk load.
    @Inject(at = @At("RETURN"), method = "getUpdateTag()Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    public void getUpdateTag(CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbtTag = cir.getReturnValue();

        nbtTag.putBoolean("IsBonfire", this.isBonfire);

        if (ServerConfig.FUEL_BASED_LIGHTING.get() && (fuelLightLevel != -1)) {
            nbtTag.putByte("FuelLight", (byte) fuelLightLevel);
        }
        cir.setReturnValue(nbtTag);
    }

    // Override not needed, because the default just uses the methods in "read", which already handle all of this.
//    @Override
//    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
//    }

    // Save to disk
    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    private void writeFueled(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", fuelTicks);
            nbt.putBoolean("IsEternal", isEternal);
            nbt.putBoolean("IsBonfire", isBonfire);
            nbt.putByte("FuelLight", (byte) fuelLightLevel);
        }
        cir.setReturnValue(nbt);
    }

    // Used for all of: disk load, chunk load packet received, and block update packet received
    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readFueled(BlockState state, CompoundNBT compound, CallbackInfo ci) {
        if (compound.contains("FuelTicks", 3)) {
            setFuelTicks(compound.getInt("FuelTicks"));
        }

        if (compound.contains("IsEternal", 99)) {
            setEternal(compound.getBoolean("IsEternal"));
        } else {
            setEternal(false); // Ensure eternal is false if failed to load NBT
        }

        if (compound.contains("IsBonfire", 99)) {
            setBonfire(compound.getBoolean("IsBonfire"));
        }

        if (compound.contains("FuelLight", 1)) {
            setFuelLightLevel(compound.getByte("FuelLight"), (ServerConfig.FUEL_BASED_LIGHTING.get() &&
                    (!isEternal || ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())));
        } else {
            setFuelLightLevel(0, false);
        }
    }

}
