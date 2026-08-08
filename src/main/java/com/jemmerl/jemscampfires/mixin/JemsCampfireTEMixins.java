package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import com.llamalad7.mixinextras.sugar.Local;
import net.createmod.ponder.api.level.PonderLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = CampfireBlockEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends BlockEntity implements IFueledCampfire {
    @Unique
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.box(-1.0D, 3.0D, -1.0D, 17.0D, 16.0D, 17.0D);

    // Properties
    @Unique
    private boolean jems_fueled_campfires$isSoul;
    @Unique
    private boolean jems_fueled_campfires$playerPlaced = false;
    @Unique
    private int jems_fueled_campfires$fuelTicks = -1;
    @Unique
    private boolean jems_fueled_campfires$isEternal = false;
    @Unique
    private boolean jems_fueled_campfires$isBonfire = false;
    @Unique
    private boolean jems_fueled_campfires$markChanged = false;
    @Unique
    private int jems_fueled_campfires$fuelLightLevel = -1;

    public JemsCampfireTEMixins(BlockPos pWorldPosition, BlockState pBlockState) {
        super(BlockEntityType.CAMPFIRE, pWorldPosition, pBlockState);
    }

    @Final
    @Shadow
    private int[] cookingProgress;

    @Final
    @Shadow
    private NonNullList<ItemStack> items;

    @Shadow
    public abstract NonNullList<ItemStack> getItems();

    @Override
    public void onLoad() {
        if (level != null) {
            jems_fueled_campfires$isSoul = (BuiltInRegistries.BLOCK.getKey(getBlockState().getBlock()).toString().contains("soul"));
            if (!level.isClientSide()) {
                // This is the first load of the campfire TE
                // Get settings/properties that only matter or are needed when the campfire is first placed
                if (jems_fueled_campfires$fuelTicks < 0) {

                    //  If the player check compat. fix is enabled, this checks if a player is nearby but did
                    //  not place it physically. This will fix an issue where campfires that are player made but not
                    //  placed directly, such as with build-in-world campfire mods, are not properly handled.
                    //  <!> It could cause issues when spawning in near a world-genned campfire, but it's rare.
                    if (ServerConfig.PLAYER_CHECK_FIX.get() && level.hasNearbyAlivePlayer(worldPosition.getX()+0.5, worldPosition.getY()+0.5, worldPosition.getZ()+0.5, 5.5D)) {
                        jems_fueled_campfires$playerPlaced = true;
                    }

                    // Now "onLoad" in the BlockEntity runs before "setPlacedBy" in the Block
                    if (jems_fueled_campfires$playerPlaced) {
                        jems_fueled_campfires$isEternal = jems_fueled_campfires$isSoul ?
                                ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.PLACE_CAMPFIRE_ETERNAL.get();
                    } else {
                        jems_fueled_campfires$isEternal = jems_fueled_campfires$isSoul ?
                                ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();

                    }

                    jems_fueled_campfires$fuelTicks = Math.min((jems_fueled_campfires$isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get()), jems_fueled_campfires$getStandardMaxFuelTicks(jems_fueled_campfires$isSoul));
                }

                if (!ServerConfig.FUEL_BASED_LIGHTING.get() || (jems_fueled_campfires$isEternal && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) {
                    // Ensures the campfire is reset to the correct, non-dynamic lighting
                    jems_fueled_campfires$updateLighting();
                    return;
                }
                jems_fueled_campfires$dynamicLightLevelUpdate();
            }
        }
    }

    @Override
    public void jems_fueled_campfires$setPlayerPlaced() {
        jems_fueled_campfires$playerPlaced = true;
    }

    @Inject(at = @At("HEAD"), method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V")
    private static void cookTick(Level pLevel, BlockPos pPos, BlockState pState, CampfireBlockEntity pBlockEntity, CallbackInfo ci) {
        // cookTick only fires if the campfire is lit and on the server side
        if (pLevel == null) return;
        IFueledCampfire fueledCampfire = (IFueledCampfire) pBlockEntity;
        fueledCampfire.jems_fueled_campfires$clearChanged();
        fueledCampfire.jems_fueled_campfires$getFuel();
        fueledCampfire.jems_fueled_campfires$normalStuff();
        if (fueledCampfire.jems_fueled_campfires$getBonfire()) fueledCampfire.jems_fueled_campfires$bonfireStuff();
        if (fueledCampfire.jems_fueled_campfires$getChanged()) pBlockEntity.setChanged();
    }

    @Inject(at = @At(value = "FIELD", target = "net/minecraft/world/level/block/entity/CampfireBlockEntity.cookingProgress:[I",
            opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2),
            method = "cookTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;)V")
    private static void cookAndDrop(Level arg0, BlockPos arg1, BlockState arg2, CampfireBlockEntity pBlockEntity, CallbackInfo ci, @Local int i) {
        IFueledCampfire fueledCampfire = (IFueledCampfire) pBlockEntity;
        if (fueledCampfire.jems_fueled_campfires$getEternal() && jems_fueled_campfires$getLoseEternalCook(fueledCampfire.jems_fueled_campfires$isSoul())) {
            fueledCampfire.jems_fueled_campfires$setEternal(false);
        }
        if (fueledCampfire.jems_fueled_campfires$getBonfire()) {
            fueledCampfire.jems_fueled_campfires$fetchCookingVariable()[i] += (jems_fueled_campfires$getBonfireCookMult(fueledCampfire.jems_fueled_campfires$isSoul()) - 1);
            //j = cookingTimes[i];
        }
    }

    @Override
    // It annoys me that this is the only solution I could come up with that doesn't use an AT.
    // Would love to hear of an alternative please and thank you.
    public int[] jems_fueled_campfires$fetchCookingVariable() {
        return cookingProgress;
    }

    public void jems_fueled_campfires$getFuel() {
        // This will ensure fuel is distributed to each campfire equally when lit, if items touch multiple campfires
        // It however will not do the 1/4 tick check if it is freshly lit (lit, but no fuel) to ensure it will get fuel
        if (jems_fueled_campfires$fuelTicks > 0) {
            int mod = Util.mod(worldPosition.getX(), 2) + ((Util.mod(worldPosition.getZ(), 2) + 1) * 2) - 2;
            if ((level.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for (ItemEntity itemEntity : jems_fueled_campfires$getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            if (Util.failsFuelFilter(jems_fueled_campfires$isSoul, itemStack)) continue;

            int baseBurnTicks = Util.getItemFuelVal(itemStack);
            boolean eternalItem = jems_fueled_campfires$getAllowEternalItems(jems_fueled_campfires$isSoul) && itemStack.is(ModTags.JC_ETERNAL) && (!jems_fueled_campfires$isEternal);

            if ((baseBurnTicks > 0) || eternalItem) {
                int itemCount = itemStack.getCount();
                if (jems_fueled_campfires$burnFuelItem(baseBurnTicks, eternalItem)) {
                    itemEntity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.random.nextFloat() * 0.4F);
                    jems_fueled_campfires$doFuelInContainer(itemStack.getItem());

                    int newCount = itemCount - 1;
                    if (newCount <= 0) {
                        itemEntity.discard();
                    } else {
                        ItemStack stackCopy = itemStack.copy();
                        stackCopy.setCount(newCount);
                        itemEntity.setItem(stackCopy);
                    }
                }
                jems_fueled_campfires$markChanged = true;
            }
        }
    }

    @Unique
    private List<ItemEntity> jems_fueled_campfires$getCaptureItems() {
        return level.getEntitiesOfClass(ItemEntity.class, COLLECTION_AREA_SHAPE.bounds()
                .move(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ()), EntitySelector.ENTITY_STILL_ALIVE);
    }

    @Unique
    private boolean jems_fueled_campfires$burnFuelItem(int baseBurnTicks, boolean eternalItem) {
        if (eternalItem) {
            jems_fueled_campfires$setEternal(true);
            if (!jems_fueled_campfires$getEternalBonfire(jems_fueled_campfires$isSoul)) jems_fueled_campfires$setBonfire(false);
            if (baseBurnTicks <= 0) return true;
        }

        int newCurrFuelTicks = jems_fueled_campfires$fuelTicks + (int) Math.ceil(baseBurnTicks * jems_fueled_campfires$getFuelMult(jems_fueled_campfires$isSoul));
        int maxFuel = jems_fueled_campfires$getTrueMaxFuelTicks(jems_fueled_campfires$isSoul);
        if (newCurrFuelTicks < maxFuel) {
            jems_fueled_campfires$setFuelTicks(newCurrFuelTicks);
            return true;
        } else if (jems_fueled_campfires$getAlwaysBurnFuel(jems_fueled_campfires$isSoul)) {
            jems_fueled_campfires$setFuelTicks(maxFuel);
            return true;
        }
        return false;
    }

    @Unique
    private void jems_fueled_campfires$doFuelInContainer(Item item) {
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

    public void jems_fueled_campfires$normalStuff() {
        // If the campfire is already out of fuel (was lit without refueling) or it is raining, try to extinguish
        if ((!jems_fueled_campfires$isEternal && (jems_fueled_campfires$fuelTicks <= 0)) || ((!jems_fueled_campfires$isEternal || jems_fueled_campfires$getRainEternal(jems_fueled_campfires$isSoul)) && jems_fueled_campfires$feelTheRainOnYourCampfire())) {
            jems_fueled_campfires$extinguishCampfire(true);
            return;
        }

        jems_fueled_campfires$setBonfire(jems_fueled_campfires$getCanBonfire(jems_fueled_campfires$isSoul) && (!jems_fueled_campfires$isEternal || jems_fueled_campfires$getEternalBonfire(jems_fueled_campfires$isSoul)) && (jems_fueled_campfires$fuelTicks > jems_fueled_campfires$getStandardMaxFuelTicks(jems_fueled_campfires$isSoul)));
        if (!jems_fueled_campfires$isBonfire) {
            if (jems_fueled_campfires$getNormalFirespread(jems_fueled_campfires$isSoul) && (level.random.nextInt(70) == 0)) {
                Direction dir = Direction.from2DDataValue(level.random.nextInt(4));

                BlockPos ignPos = worldPosition.relative(dir);
                if (jems_fueled_campfires$canIgnitePos(ignPos, false)) {
                    level.setBlockAndUpdate(ignPos, BaseFireBlock.getState(level, ignPos));
                }
            }
        }

        if (jems_fueled_campfires$isEternal) {
            // The only time markChanged is true here is if fuel was added, which is when this update may be needed.
            if (jems_fueled_campfires$markChanged && ServerConfig.FUEL_BASED_LIGHTING.get() && ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get()) {
                jems_fueled_campfires$dynamicLightLevelUpdate();
            }
            return;
        }
        jems_fueled_campfires$fuelTicks -= jems_fueled_campfires$isBonfire ? jems_fueled_campfires$getBonfireFuelUse(jems_fueled_campfires$isSoul) : 1;
        if (jems_fueled_campfires$fuelTicks <= 0) {
            jems_fueled_campfires$fuelTicks = 0;
            jems_fueled_campfires$outOfFuel();
        }
        if (ServerConfig.FUEL_BASED_LIGHTING.get()) jems_fueled_campfires$dynamicLightLevelUpdate();
        jems_fueled_campfires$markChanged = true;
    }

    public void jems_fueled_campfires$bonfireStuff() {
        RandomSource randomsource = level.random;

        if (jems_fueled_campfires$getBonfireFirespread(jems_fueled_campfires$isSoul)) {
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
                if (jems_fueled_campfires$canIgnitePos(ignPos, true)) {
                    level.setBlockAndUpdate(ignPos, BaseFireBlock.getState(level, ignPos));
                    break;
                }
                ignPos = ignPos.below();
            }
        }
    }

    @Override
    public int jems_fueled_campfires$getBonfireLimit() {
        if (jems_fueled_campfires$getCanBonfire(jems_fueled_campfires$isSoul)) {
            return jems_fueled_campfires$getStandardMaxFuelTicks(jems_fueled_campfires$isSoul);
        }
        return -1;
    }

    @Unique
    private boolean jems_fueled_campfires$canIgnitePos(BlockPos blockPos, boolean ignoreFlammable) {
        BlockState state = level.getBlockState(blockPos);
        if (state.canBeReplaced() && !state.liquid()) {
            BlockState downState = level.getBlockState(blockPos.below());
            return (downState.isSolidRender(level, blockPos.below()) &&
                    (ignoreFlammable || downState.isFlammable(level, worldPosition, Direction.UP)));
        }
        return false;
    }

    // Returns true if the rain extinguishes the campfire
    @Unique
    private boolean jems_fueled_campfires$feelTheRainOnYourCampfire() {
        if (level.isRainingAt(worldPosition.above())) {
            if (jems_fueled_campfires$getRainFuelLoss(jems_fueled_campfires$isSoul) == -1) {
                return true;
            } else {
                jems_fueled_campfires$fuelTicks = Math.max(jems_fueled_campfires$fuelTicks - jems_fueled_campfires$getRainFuelLoss(jems_fueled_campfires$isSoul), 0);
                jems_fueled_campfires$markChanged = true;
                return (jems_fueled_campfires$fuelTicks <= 0);
            }
        }
        return false;
    }

    @Unique
    private void jems_fueled_campfires$outOfFuel() {
        if (jems_fueled_campfires$getBreakUnlit(jems_fueled_campfires$isSoul)) {
            jems_fueled_campfires$breakCampfire();
        } else {
            jems_fueled_campfires$extinguishCampfire(false);
        }
    }

    @Unique
    private void jems_fueled_campfires$extinguishCampfire(boolean preventDrops) {
        level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (preventDrops) {
            level.setBlockAndUpdate(worldPosition, getBlockState().setValue(CampfireBlock.LIT, false));
            jems_fueled_campfires$doExtinguishChecks();
        } else {
            CampfireBlock.dowse(null, level, worldPosition, getBlockState());
            // doExtinguishDrops is mixin'd into the campfire block dowse method to handle other extinguishing factors,
            // like shovels and water bottles, so it is not called from here.
        }

    }

    public void jems_fueled_campfires$doExtinguishDrops() {
        if (ServerConfig.EXTINGUISHED_DROP_ITEMS.get()) {
            Containers.dropContents(level, worldPosition, getItems());
        }
        jems_fueled_campfires$doExtinguishChecks();
    }

    // Can be called directly to bypass drop check
    @Unique
    public void jems_fueled_campfires$doExtinguishChecks() {
        if (jems_fueled_campfires$isEternal && jems_fueled_campfires$getLoseEternalExtinguish(jems_fueled_campfires$isSoul)) {
            jems_fueled_campfires$isEternal = false;
        }
        if (jems_fueled_campfires$isBonfire && jems_fueled_campfires$getLoseBonfireFuelExtinguish(jems_fueled_campfires$isSoul)) {
            jems_fueled_campfires$setFuelTicks(Math.min(jems_fueled_campfires$fuelTicks, jems_fueled_campfires$getStandardMaxFuelTicks(jems_fueled_campfires$isSoul)));
        }
    }

    @Unique
    private void jems_fueled_campfires$breakCampfire() {
        level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.setBlockAndUpdate(worldPosition, Blocks.AIR.defaultBlockState());
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Getters and Setters                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // General
    @Unique
    private static int jems_fueled_campfires$getStandardMaxFuelTicks(boolean soul) {
        // Maximum fuel assuming without bonfire
        return soul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
    }
    @Unique
    private static int jems_fueled_campfires$getTrueMaxFuelTicks(boolean soul) {
        // Maximum fuel BASED ON if it can become a bonfire. Trust me this simplifies stuff.
        int trueMaxFuelTicks = jems_fueled_campfires$getStandardMaxFuelTicks(soul);
        if (jems_fueled_campfires$getCanBonfire(soul)) {
            trueMaxFuelTicks += (soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_BONFIRE_FUEL_TICKS.get());
        }
        return trueMaxFuelTicks;
    }
    @Unique
    private static double jems_fueled_campfires$getFuelMult(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getAlwaysBurnFuel(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get() : ServerConfig.CAMPFIRE_ALWAYS_BURN_FUEL_ITEMS.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getBreakUnlit(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BREAK_UNLIT.get() : ServerConfig.CAMPFIRE_BREAK_UNLIT.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getNormalFirespread(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_FIRESPREAD.get();
    }
    @Unique
    private static int jems_fueled_campfires$getRainFuelLoss(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_RAIN_FUEL_TICK_LOSS.get() : ServerConfig.CAMPFIRE_RAIN_FUEL_TICK_LOSS.get();
    }


    // Decor
    @Unique
    private static boolean jems_fueled_campfires$getAllowEternalItems(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ALLOW_ETERNAL_ITEMS.get() : ServerConfig.CAMPFIRE_ALLOW_ETERNAL_ITEMS.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getLoseEternalCook(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get() : ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_COOKING.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getLoseEternalExtinguish(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH.get() : ServerConfig.CAMPFIRE_LOSE_ETERNAL_WHEN_EXTINGUISH.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getRainEternal(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_RAIN_AFFECT_ETERNAL.get() : ServerConfig.CAMPFIRE_RAIN_AFFECT_ETERNAL.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getEternalBonfire(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_ETERNAL_BONFIRE.get() : ServerConfig.CAMPFIRE_ETERNAL_BONFIRE.get();
    }

    // Bonfire
    @Unique
    private static boolean jems_fueled_campfires$getCanBonfire(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_CAN_BONFIRE.get() : ServerConfig.CAMPFIRE_CAN_BONFIRE.get();
    }
    @Unique
    private static int jems_fueled_campfires$getBonfireFuelUse(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_BURN_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_BURN_MULT.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getLoseBonfireFuelExtinguish(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH.get() : ServerConfig.CAMPFIRE_BONFIRE_LOSE_FUEL_EXTINGUISH.get();
    }
    @Unique
    private static int jems_fueled_campfires$getBonfireCookMult(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_COOKING_MULT.get() : ServerConfig.CAMPFIRE_BONFIRE_COOKING_MULT.get();
    }
    @Unique
    private static boolean jems_fueled_campfires$getBonfireFirespread(boolean soul) {
        return soul ? ServerConfig.SOUL_CAMPFIRE_BONFIRE_FIRESPREAD.get() : ServerConfig.CAMPFIRE_BONFIRE_FIRESPREAD.get();
    }

    @Override
    public boolean jems_fueled_campfires$isSoul() {
        return jems_fueled_campfires$isSoul;
    }

    @Override
    public int jems_fueled_campfires$getFuelTicks() {
        return jems_fueled_campfires$fuelTicks;
    }

    @Override
    public void jems_fueled_campfires$setFuelTicks(int setTicks) {
        jems_fueled_campfires$fuelTicks = setTicks;
    }

    @Override
    public boolean jems_fueled_campfires$getEternal() {
        return jems_fueled_campfires$isEternal;
    }

    @Override
    public void jems_fueled_campfires$setEternal(boolean eternal) {
        jems_fueled_campfires$isEternal = eternal;
        setChanged();
        if ((level == null) || !ServerConfig.FUEL_BASED_LIGHTING.get() || (jems_fueled_campfires$isEternal && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) {
            jems_fueled_campfires$setFuelLightLevel(0);
        } else {
            jems_fueled_campfires$dynamicLightLevelUpdate();
        }
    }

    @Override
    public boolean jems_fueled_campfires$getBonfire() {
        return jems_fueled_campfires$isBonfire;
    }

    @Override
    public void jems_fueled_campfires$setBonfire(boolean bonfire) {
        if (jems_fueled_campfires$isBonfire != bonfire) {
            jems_fueled_campfires$isBonfire = bonfire;
            if ((level != null) && !level.isClientSide) {
                BlockState state = getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 26); // Uses 2 client updates, 8 forces main render thread, and 16 no observers
            }
        }
    }

    // TODO add config for this formula? Not unless someone asks.
    @Unique
    private void jems_fueled_campfires$dynamicLightLevelUpdate() {
        int rampPeak = Math.min((int)(jems_fueled_campfires$getStandardMaxFuelTicks(jems_fueled_campfires$isSoul) * 0.34f), 3600);
        if (jems_fueled_campfires$fuelTicks < rampPeak) {
            float perc = jems_fueled_campfires$fuelTicks / (float)rampPeak;
            int val = (int)(jems_fueled_campfires$isSoul ? (6 + 3 * perc) : (8 + 7 * perc));
            jems_fueled_campfires$setFuelLightLevel(val);
            return;
        }
        jems_fueled_campfires$setFuelLightLevel(0);
    }

    @Unique
    @Override
    public void jems_fueled_campfires$setFuelLightLevel(int fuelLightLevel) {
        if (this.jems_fueled_campfires$fuelLightLevel != fuelLightLevel) {
            if (fuelLightLevel <= 0) {
                jems_fueled_campfires$fuelLightLevel = jems_fueled_campfires$isSoul ? 10 : 15;
            } else {
                jems_fueled_campfires$fuelLightLevel = fuelLightLevel;
            }

            if (level == null) return;
            jems_fueled_campfires$updateLighting();
        }
    }


    @Override
    public int jems_fueled_campfires$getFuelLightLevel() {
        return jems_fueled_campfires$fuelLightLevel;
    }

    @Override
    public void jems_fueled_campfires$updateLighting() {
        if (level instanceof PonderLevel) return;
        AuxiliaryLightManager lightManager = level.getAuxLightManager(worldPosition);

        if (lightManager != null) {
            lightManager.setLightAt(worldPosition, jems_fueled_campfires$fuelLightLevel);
        }

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 26); // Uses 2 client updates, 8 forces main render thread, and 16 no observers
    }

    @Override
    public void jems_fueled_campfires$clearChanged() {
        jems_fueled_campfires$markChanged = false;
    }

    @Override
    public boolean jems_fueled_campfires$getChanged() {
        return jems_fueled_campfires$markChanged;
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Data Handling Stuff                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putBoolean("IsBonfire", jems_fueled_campfires$isBonfire);
        if (ServerConfig.FUEL_BASED_LIGHTING.get() && (jems_fueled_campfires$fuelLightLevel > 0)) {
            compoundtag.putByte("FuelLight", (byte)jems_fueled_campfires$fuelLightLevel);
        }
        ContainerHelper.saveAllItems(compoundtag, items, true, registries);
        return compoundtag;
    }

    @Inject(at = @At("RETURN"), method = "saveAdditional")
    private void saveFueled(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag != null) {
            tag.putInt("FuelTicks", jems_fueled_campfires$fuelTicks);
            tag.putBoolean("IsEternal", jems_fueled_campfires$isEternal);
            tag.putBoolean("IsBonfire", jems_fueled_campfires$isBonfire);
            if (ServerConfig.FUEL_BASED_LIGHTING.get()) {
                tag.putByte("FuelLight", (byte) jems_fueled_campfires$fuelLightLevel);
            }
        }
    }

    @Inject(at = @At("RETURN"), method = "loadAdditional")
    private void loadFueled(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains("FuelTicks", 3)) {
            jems_fueled_campfires$setFuelTicks(tag.getInt("FuelTicks"));
        }
        if (tag.contains("IsEternal", 99)) {
            jems_fueled_campfires$setEternal(tag.getBoolean("IsEternal"));
        }
        if (tag.contains("IsBonfire", 99)) {
            jems_fueled_campfires$setBonfire(tag.getBoolean("IsBonfire"));
        }

        if (!ServerConfig.FUEL_BASED_LIGHTING.get() ||
                (jems_fueled_campfires$isEternal && !ServerConfig.FUEL_BASED_LIGHTING_ETERNAL.get())) {
            jems_fueled_campfires$setFuelLightLevel(0);
            return;
        }

        if (tag.contains("FuelLight", 1)) {
            jems_fueled_campfires$setFuelLightLevel(tag.getByte("FuelLight"));
        } else {
            jems_fueled_campfires$setFuelLightLevel(0);
        }
    }
}
