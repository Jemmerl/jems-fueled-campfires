package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.JCTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraftforge.common.ForgeHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mixin(value = CampfireBlockEntity.class, priority = 0)
public abstract class JemsCampfireTEMixins extends BlockEntity implements IFueledCampfire {
    private static final VoxelShape COLLECTION_AREA_SHAPE = Block.box(-1.0D, 3.0D, -1.0D, 17.0D, 16.0D, 17.0D);

    // Properties
    private boolean isSoul;
    private int fuelTicks = -1;
    private boolean isEternal = false;
    private boolean isBonfire = false;

    // **TODO BOARD** //
    // TODO: Maybe add fuel-based lighting in the future as a resource-expensive optional setting.
    //  Would need to send packets between server and client.

    public JemsCampfireTEMixins(BlockEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Shadow
    private int[] cookingTimes;

    @Shadow
    public abstract void dropAllItems();

    @Override
    public void onLoad() {
        //super.onLoad();
        if (!this.level.isClientSide()) {
            isSoul = (this.getBlockState().getBlock().getRegistryName().toString().contains("soul"));

            // This is the first load of the campfire TE
            // Get settings/properties that only matter or are needed when the campfire is first placed
            if (fuelTicks < 0) {
                // This isEternal gets overridden if the block is placed by a player, else it has been world-genned
                isEternal = isSoul ? ServerConfig.SPAWN_SOUL_CAMPFIRE_ETERNAL.get() : ServerConfig.SPAWN_CAMPFIRE_ETERNAL.get();
                fuelTicks = Math.min((isSoul ? ServerConfig.SOUL_CAMPFIRE_INITIAL_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_INITIAL_FUEL_TICKS.get()), getStandardMaxFuelTicks(isSoul));
            }
        }
    }


    @Inject(at = @At("TAIL"), method = "tick()V")
    private void tick(CallbackInfo ci) {
        if (level == null) return;
        if (!level.isClientSide) {
            if (!this.getBlockState().getValue(CampfireBlock.LIT)) {
                return;
            }
            getFuel();
            normalStuff();
            if (isBonfire) bonfireStuff();
        }
    }

    //@Inject(at = @At(value = "JUMP", opcode = Opcodes.IF_ICMPLT, ordinal = 0), locals = LocalCapture.PRINT, method = "cookAndDrop()V")
    @Inject(at = @At(value = "FIELD", target = "net/minecraft/tileentity/CampfireTileEntity.cookingTimes:[I", opcode = Opcodes.GETFIELD, args = "array=get", ordinal = 0, shift = At.Shift.BY, by = -2), locals = LocalCapture.CAPTURE_FAILHARD, method = "cookAndDrop()V")
    private void cookAndDrop(CallbackInfo ci, int i, ItemStack itemstack) {
        if (isEternal && getLoseEternalCook(isSoul)) {
            this.isEternal = false;
        }
        if (isBonfire) {
            this.cookingTimes[i] += (getBonfireCookMult(isSoul) - 1);
            //j = cookingTimes[i];
        }
    }

    private void getFuel() {
        // This will ensure fuel is distributed to each campfire equally when lit, if items touch multiple campfires
        // It however will not do the 1/4 tick check if it is freshly lit (lit, but no fuel) to ensure it will get fuel
        if (fuelTicks > 0) {
            int mod = Util.mod(worldPosition.getX(), 2) + ((Util.mod(worldPosition.getZ(), 2) + 1) * 2) - 2;
            if ((this.level.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for(ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            int baseBurnTicks = ForgeHooks.getBurnTime(itemStack, null);
            boolean eternalItem = getAllowEternalItems(isSoul) && itemStack.getItem().is(JCTags.JC_ETERNAL) && (!isEternal);

            if ((baseBurnTicks > 0) || eternalItem) {
                int itemCount = itemStack.getCount();
                if (burnFuelItem(baseBurnTicks, eternalItem)) {
                    itemEntity.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + level.random.nextFloat() * 0.4F);
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
            setBonfire(false);
            if (baseBurnTicks <= 0) return true;
        }

        int newCurrFuelTicks = fuelTicks + (int) Math.ceil(baseBurnTicks * getFuelMult(isSoul));
        if (newCurrFuelTicks < getTrueMaxFuelTicks(isSoul)) {
            setFuelTicks(newCurrFuelTicks);
            return true;
        }
        if (getAlwaysBurnFuel(isSoul) && this.getBlockState().getValue(CampfireBlock.LIT)) {
            setFuelTicks(getTrueMaxFuelTicks(isSoul));
            return true;
        }
        return false;
    }

    // To-do use this separated class for mod compat-stuff with other fuels in containers (ex: lava buckets)
    // Modders can mixin to this class with ease, make sure to inject at RETURN and not include any early returns!
    private void doFuelInContainer(Item item) {
        if (item == Items.LAVA_BUCKET) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), new ItemStack(Items.BUCKET));
        }
    }

    private void normalStuff() {
        // If the campfire is already out of fuel (was lit without refueling) or it is raining, try to extinguish
        if ((!isEternal && (fuelTicks <= 0)) || ((!isEternal || getRainEternal(isSoul)) && feelTheRainOnYourCampfire())) {
            extinguishCampfire(false);
            return;
        }

        setBonfire(getCanBonfire(isSoul) && (!isEternal || getEternalBonfire(isSoul)) && (fuelTicks > getStandardMaxFuelTicks(isSoul)));
        if (!isBonfire) {
            if (getNormalFirespread(isSoul) && (this.level.random.nextInt(70) == 0)) {
                Direction dir = Direction.from2DDataValue(level.random.nextInt(4));
                ignitePos(worldPosition.relative(dir), false);
            }
        }

        if (isEternal) return;
        fuelTicks -= isBonfire ? getBonfireFuelUse(isSoul) : 1;
        if (fuelTicks <= 0) {
            fuelTicks = 0;
            outOfFuel();
        }
    }

    private void bonfireStuff() {
        Random rand = this.level.random;

        // Update clients once per second about bonfire status
        // AFAIK this is the only way I can ensure players see the correct bonfire behavior
        // Bonfire updates are still sent as normal through setBonfire, but this may change
        if (ServerConfig.ALLOW_CLIENT_PACKETS.get() && (level.getGameTime() % 20L == 0L)) {
            BlockState state = this.getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 18); // Uses 2 client updates, and 16 no observers
        }

        if (getBonfireFirespread(isSoul)) {
            if (rand.nextInt(20) != 0) return;
            Direction dir1 = Direction.from2DDataValue(rand.nextInt(4));
            Direction dir2 = Direction.getRandom(rand);
            int up = rand.nextInt(2);
            if ((dir2.getOpposite() == dir1) || (dir2.get2DDataValue() < 0)) {
                ignitePos(worldPosition.relative(dir1).above(up), true);
            } else {
                ignitePos(worldPosition.relative(dir1).relative(dir2).above(up), true);
            }
        }
    }

    private void ignitePos(BlockPos blockPos, boolean ignoreFlammable) {
        Material material = this.level.getBlockState(blockPos).getMaterial();
        if (material.isReplaceable() && !material.isLiquid()) {
            BlockState downState = this.level.getBlockState(blockPos.below());
            if (downState.isSolidRender(this.level, blockPos.below()) &&
                    (ignoreFlammable || downState.isFlammable(level, worldPosition, Direction.UP))) {
                this.level.setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level, blockPos));
            }
        }
        //return false;
    }

    // Returns true if the rain extinguishes the campfire
    private boolean feelTheRainOnYourCampfire() {
        if (level.isRainingAt(this.worldPosition.above())) {
            if (getRainFuelLoss(isSoul) == -1) {
                return true;
            } else {
                fuelTicks = Math.max(fuelTicks-getRainFuelLoss(isSoul), 0);
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
        this.level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (drops) {
            CampfireBlock.dowse(this.level, worldPosition, this.getBlockState());
            // doExtinguished is called from the campfire block normally to handle other extinguishing factors,
            // like shovels and water bottles, so it is not called here.
        } else {
            this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(CampfireBlock.LIT, false));
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
        this.level.playSound(null, worldPosition, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0F, 1.0F);
        this.dropAllItems();
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
    public boolean getBonfire() {
        return this.isBonfire;
    }

    @Override
    public void setBonfire(boolean bonfire) {
        if (this.isBonfire != bonfire) {
            this.isBonfire = bonfire;
            if (ServerConfig.ALLOW_CLIENT_PACKETS.get() && (level != null) && (!level.isClientSide)) {
                BlockState state = this.getBlockState();
                level.sendBlockUpdated(worldPosition, state, state, 18); // Uses 2 client updates, and 16 no observers
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                            Data Handling Stuff                                              //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

//    /**
//     * @author Jemmerl
//     * @reason Overriding was not working. <-- It was I was just being dumb.
//     */
    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        if (ServerConfig.ALLOW_CLIENT_PACKETS.get()) {
            CompoundTag nbtTag = this.getUpdateTag();
            //nbtTag.putInt("FuelTicks", this.fuelTicks);
            //nbtTag.putBoolean("IsEternal", this.isEternal);
            nbtTag.putBoolean("IsBonfire", this.isBonfire);
            return new ClientboundBlockEntityDataPacket(worldPosition, 13, nbtTag);
        }
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 13, this.getUpdateTag());
    }

    @Override
    // Receive packet from client
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt){
        CompoundTag nbtTag = pkt.getTag();
        if (nbtTag.contains("IsBonfire", 99)) {
            setBonfire(nbtTag.getBoolean("IsBonfire"));
        }
        super.onDataPacket(net, pkt);
    }

    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readFueled(BlockState state, CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("FuelTicks", 3)) {
            setFuelTicks(nbt.getInt("FuelTicks"));
        }
        if (nbt.contains("IsEternal", 99)) {
            setEternal(nbt.getBoolean("IsEternal"));
        }
        if (nbt.contains("IsBonfire", 99)) {
            setBonfire(nbt.getBoolean("IsBonfire"));
        }
    }

    @Inject(at = @At("RETURN"), method = "write(Lnet/minecraft/nbt/CompoundNBT;)Lnet/minecraft/nbt/CompoundNBT;", cancellable = true)
    private void writeFueled(CompoundTag compound, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", this.fuelTicks);
            nbt.putBoolean("IsEternal", this.isEternal);
            nbt.putBoolean("IsBonfire", this.isBonfire);
            cir.setReturnValue(nbt);
            cir.cancel();
        }
    }

}
