package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(value = CampfireBlockEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends BlockEntity implements IFueledCampfire {
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.box(-1.0D, 3.0D, -1.0D, 17.0D, 16.0D, 17.0D);

    // Properties
    private boolean isSoul;
    private int fuelTicks = -1;
    private boolean isEternal = false;
    private boolean isBonfire = false;
    private boolean markChanged = false;
    private int fuelLightLevel = -1;

    public JemsCampfireTEMixins(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityType.CAMPFIRE, pWorldPosition, pBlockState);
    }

    @Shadow
    private int[] cookingProgress;

    @Shadow
    private NonNullList<ItemStack> items;

    @Shadow
    public abstract NonNullList<ItemStack> getItems();

    @Override
    public void onLoad() {
        super.onLoad();
        if (!this.level.isClientSide()) {
            isSoul = (ForgeRegistries.BLOCKS.getKey(this.getBlockState().getBlock()).toString().contains("soul"));

            // This is the first load of the campfire TE
            // Get settings/properties that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();

                // ... UNLESS the player check compat. fix is enabled, at which the above is overridden if a player is
                //  near but did not place it directly. This will fix an issue where campfires are player made but not
                //  directly. It could cause issues when spawning in near a world-genned campfire, but it's rare.
                if (ServerConfig.PLAYER_CHECK_FIX.get() && level.hasNearbyAlivePlayer(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 5.5D)) {
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

    @Inject(at = @At("HEAD"), method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V")
    private static void cookTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci) {
        // cookTick only fires if the campfire is lit and on the server side
        if (pLevel == null) return;
        IFueledCampfire fueledCampfire = (IFueledCampfire) pBlockEntity;
        fueledCampfire.clearChanged();
        fueledCampfire.getFuel();
        fueledCampfire.normalStuff();
        if (fueledCampfire.getBonfire()) fueledCampfire.bonfireStuff();
        if (fueledCampfire.getChanged()) pBlockEntity.setChanged();
    }

    @Inject(at = @At(value = "FIELD", target = "net/minecraft/world/level/block/entity/CampfireBlockEntity.cookingProgress:[I",
            opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2), locals = LocalCapture.CAPTURE_FAILHARD,
            method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V")
    private static void cookAndDrop(Level arg0, BlockPos arg1, BlockState arg2, CampfireBlockEntity pBlockEntity, CallbackInfo ci, boolean flag, int i, ItemStack itemstack) {
        IFueledCampfire fueledCampfire = (IFueledCampfire) pBlockEntity;
        if (fueledCampfire.getEternal() && getLoseEternalCook(fueledCampfire.isSoul())) {
            fueledCampfire.setEternal(false);
        }
        if (fueledCampfire.getBonfire()) {
            fueledCampfire.fetchCookingVariable()[i] += (getBonfireCookMult(fueledCampfire.isSoul()) - 1);
            //j = cookingTimes[i];
        }
    }

    @Override
    // It annoys me that this is the only solution I could come up with that doesn't use an AT.
    // Would love to hear of an alternative please and thank you.
    public int[] fetchCookingVariable() {
        return this.cookingProgress;
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
            if (Util.failsFuelFilter(isSoul, itemStack)) continue;

            int baseBurnTicks = Util.getItemFuelVal(itemStack);
            boolean eternalItem = getAllowEternalItems(isSoul) && itemStack.is(ModTags.JC_ETERNAL) && (!isEternal);

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
                markChanged = true;
            }
        }
    }

    private List<ItemEntity> getCaptureItems() {
        return this.level.getEntitiesOfClass(ItemEntity.class, COLLECTION_AREA_SHAPE.bounds()
                .move(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), EntitySelector.ENTITY_STILL_ALIVE);
    }

    private boolean burnFuelItem(int baseBurnTicks, boolean eternalItem) {
        if (eternalItem) {
            isEternal = true;
            setBonfire(false); //if (!getEternalBonfire(isSoul)) setBonfire(false); //TODO ?
            if (baseBurnTicks <= 0) return true;
        }

        int newCurrFuelTicks = fuelTicks + (int) Math.ceil(baseBurnTicks * getFuelMult(isSoul));
        int maxFuel = getTrueMaxFuelTicks(isSoul);
        if (newCurrFuelTicks < maxFuel) {
            setFuelTicks(newCurrFuelTicks);
            return true;
        }
        if (getAlwaysBurnFuel(isSoul) && this.getBlockState().getValue(CampfireBlock.LIT)) {
            setFuelTicks(maxFuel);
            return true;
        }
        return false;
    }

    // To-do use this separated class for mod compat-stuff with other fuels in containers (ex: lava buckets)
    // Modders can mixin to this class with ease, make sure to inject at RETURN and not include any early returns!
    private void doFuelInContainer(Item item) {
        Item containerItem = Util.fuelContainers.getOrDefault(item, null);
        if (containerItem == null) return;

        double d0 = worldPosition.getX()+0.5;
        double d1 = worldPosition.getY()+0.9;
        double d2 = worldPosition.getZ()+0.5;

        double degree = Math.toRadians(level.random.nextInt(360));
        double sin = Math.sin(degree);
        double cos = Math.cos(degree);

        ItemEntity itementity = new ItemEntity(level, d0, d1, d2, new ItemStack(containerItem));
        itementity.setDeltaMovement(sin * 0.2D, 0.01, cos * 0.2D);
        level.addFreshEntity(itementity);
    }

    public void normalStuff() {
        // If the campfire is already out of fuel (was lit without refueling) or it is raining, try to extinguish
        if ((!isEternal && (fuelTicks <= 0)) || ((!isEternal || getRainEternal(isSoul)) && feelTheRainOnYourCampfire())) {
            extinguishCampfire(true);
            return;
        }

        setBonfire(getCanBonfire(isSoul) && (!isEternal || getEternalBonfire(isSoul)) && (fuelTicks > getStandardMaxFuelTicks(isSoul)));
        if ((!isBonfire) && getNormalFirespread(isSoul) && (level.random.nextInt(70) == 0)) {
            Direction dir = Direction.from2DDataValue(level.random.nextInt(4));

            BlockPos ignPos = worldPosition.relative(dir);
            if (canIgnitePos(ignPos, false)) {
                level.setBlockAndUpdate(ignPos, BaseFireBlock.getState(level, ignPos));
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

    public void bonfireStuff() {
        RandomSource randomsource = level.random;

        if (getBonfireFirespread(isSoul)) {
            if (randomsource.nextInt(40) != 0) return;
            Direction dir1 = Direction.from2DDataValue(randomsource.nextInt(4));
            Direction dir2 = Direction.getRandom(randomsource);

            // If dir2 points opposite of dir1 or is UP/DOWN, then light directly adjacent to the campfire.
            // Else, light one block away from the campfire.
            BlockPos ignPos = worldPosition.relative(dir1).above();
            if ((dir2.getOpposite() != dir1) && (dir2.get2DDataValue() > 0)) {
                ignPos = ignPos.relative(dir2);
            }

            for (int down = 0; down <= 2; down++) {
                if (canIgnitePos(ignPos, true)) {
                    level.setBlockAndUpdate(ignPos, BaseFireBlock.getState(level, ignPos));
                    break;
                }
                ignPos = ignPos.below();
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
        Material material = level.getBlockState(blockPos).getMaterial();
        if (material.isReplaceable() && !material.isLiquid()) {
            BlockState downState = level.getBlockState(blockPos.below());
            return (downState.isSolidRender(level, blockPos.below()) &&
                    (ignoreFlammable || downState.isFlammable(level, worldPosition, Direction.UP)));
        }
        return false;
    }

    // Returns true if the rain extinguishes the campfire
    private boolean feelTheRainOnYourCampfire() {
        if (level.isRainingAt(this.worldPosition.above())) {
            if (getRainFuelLoss(isSoul) == -1) {
                return true;
            } else {
                fuelTicks = Math.max(fuelTicks-getRainFuelLoss(isSoul), 0);
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
            extinguishCampfire(false);
        }
    }

    private void extinguishCampfire(boolean preventDrops) {
        this.level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (preventDrops) {
            this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(CampfireBlock.LIT, false));
            doExtinguishChecks();
        } else {
            CampfireBlock.dowse(null, this.level, worldPosition, this.getBlockState());
            // doExtinguishDrops is mixin'd into the campfire block dowse method to handle other extinguishing factors,
            // like shovels and water bottles, so it is not called from here.
        }
    }

    public void doExtinguishDrops() {
        if (ServerConfig.EXTINGUISHED_DROP_ITEMS.get()) {
            Containers.dropContents(this.level, worldPosition, getItems());
        }
        doExtinguishChecks();
    }

    // Can be called directly to bypass drop check
    public void doExtinguishChecks() {
        if (isEternal && getLoseEternalExtinguish(isSoul)) {
            isEternal = false;
        }
        if (isBonfire && getLoseBonfireFuelExtinguish(isSoul)) {
            setFuelTicks(Math.min(fuelTicks, getStandardMaxFuelTicks(isSoul)));
        }
    }

    private void breakCampfire() {
        this.level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        //this.dropAllItems(); TODO may no longer be needed
        this.level.setBlockAndUpdate(this.worldPosition, Blocks.AIR.defaultBlockState());
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
    public boolean isSoul() {
        return isSoul;
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
        if ((level == null) || !ServerConfig.FUEL_BASED_LIGHTING.get() || !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get()) {
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
            if ((level != null) && (!level.isClientSide)) {
                BlockState state = this.getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 26); // Uses 2 client updates, and 16 no observers
            }
        }
    }

    // TODO add config for this formula? Not unless someone asks.
    private void dynamicLightLevelUpdate() {
        int rampPeak = Math.min((int)(getStandardMaxFuelTicks(isSoul) * 0.34f), 3600);
        if (fuelTicks < rampPeak) {
            float perc = fuelTicks / (float)rampPeak;
            int val = (int)(isSoul ? (6 + 3 * perc) : (8 + 7 * perc));
            setFuelLightLevel(val);
            return;
        }
        setFuelLightLevel(0);
    }

    @Override
    public void setFuelLightLevel(int fuelLightLevel) {
        if (this.fuelLightLevel != fuelLightLevel) {
            this.fuelLightLevel = fuelLightLevel;
            if (level == null) {
                return;
            }
            updateLighting();
        }
    }

    @Override
    public int getFuelLightLevel() {
        return fuelLightLevel;
    }

    @Override
    public void updateLighting() {
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 26); // Uses 2 client updates, 8 forces main render thread, and 16 no observers
        setChanged();
        level.getChunkSource().getLightEngine().checkBlock(worldPosition);
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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("IsBonfire", isBonfire);
        if (ServerConfig.FUEL_BASED_LIGHTING.get() && (fuelLightLevel != -1)) {
            compoundtag.putByte("FuelLight", (byte)fuelLightLevel);
        }
        ContainerHelper.saveAllItems(compoundtag, items, true);
        return compoundtag;
    }

    @Inject(at = @At("RETURN"), method = "saveAdditional(Lnet/minecraft/nbt/CompoundTag;)V", cancellable = true)
    private void saveFueled(CompoundTag nbtTag, CallbackInfo ci) {
        if (nbtTag != null) {
            nbtTag.putInt("FuelTicks", fuelTicks);
            nbtTag.putBoolean("IsEternal", isEternal);
            nbtTag.putBoolean("IsBonfire", isBonfire);
            if (ServerConfig.FUEL_BASED_LIGHTING.get()) {
                nbtTag.putByte("FuelLight", (byte) fuelLightLevel);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "load(Lnet/minecraft/nbt/CompoundTag;)V")
    private void loadFueled(CompoundTag nbtTag, CallbackInfo ci) {
        if (nbtTag.contains("FuelTicks", 3)) {
            setFuelTicks(nbtTag.getInt("FuelTicks"));
        }
        if (nbtTag.contains("IsEternal", 99)) {
            setEternal(nbtTag.getBoolean("IsEternal"));
        } else {
            setEternal(false); // Ensure eternal is false if failed to load NBT
        }
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

}
