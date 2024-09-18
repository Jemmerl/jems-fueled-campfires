package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.init.JCTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
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

import javax.annotation.Nullable;
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

    // **TODO BOARD** //
    // TODO: Maybe add fuel-based lighting in the future as a resource-expensive optional setting.
    //  Would need to send packets between server and client.

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
        if (world == null) return;
        if (!world.isRemote) {
            if (!this.getBlockState().get(CampfireBlock.LIT)) {
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
            int mod = Util.mod(pos.getX(), 2) + ((Util.mod(pos.getZ(), 2) + 1) * 2) - 2;
            if ((this.world.getGameTime() % 4L) != mod) {
                return;
            }
        }

        for(ItemEntity itemEntity : getCaptureItems()) {
            ItemStack itemStack = itemEntity.getItem();
            int baseBurnTicks = ForgeHooks.getBurnTime(itemStack, null);
            boolean eternalItem = getAllowEternalItems(isSoul) && itemStack.getItem().isIn(JCTags.JC_ETERNAL) && (!isEternal);

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
        if (getAlwaysBurnFuel(isSoul) && this.getBlockState().get(CampfireBlock.LIT)) {
            setFuelTicks(getTrueMaxFuelTicks(isSoul));
            return true;
        }
        return false;
    }

    // To-do use this separated class for mod compat-stuff with other fuels in containers (ex: lava buckets)
    // Modders can mixin to this class with ease, make sure to inject at RETURN and not include any early returns!
    private void doFuelInContainer(Item item) {
        if (item == Items.LAVA_BUCKET) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BUCKET));
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
            if (getNormalFirespread(isSoul) && (this.world.rand.nextInt(70) == 0)) {
                Direction dir = Direction.byHorizontalIndex(world.rand.nextInt(4));
                ignitePos(pos.offset(dir), false);
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
        Random rand = this.world.rand;

        // Update clients once per second about bonfire status
        // AFAIK this is the only way I can ensure players see the correct bonfire behavior
        // Bonfire updates are still sent as normal through setBonfire, but this may change
        if (ServerConfig.ALLOW_CLIENT_PACKETS.get() && (world.getGameTime() % 20L == 0L)) {
            BlockState state = this.getBlockState();
            world.notifyBlockUpdate(pos, state, state, 18); // Uses 2 client updates, and 16 no observers
        }

        if (getBonfireFirespread(isSoul)) {
            if (rand.nextInt(20) != 0) return;
            Direction dir1 = Direction.byHorizontalIndex(rand.nextInt(4));
            Direction dir2 = Direction.getRandomDirection(rand);
            int up = rand.nextInt(2);
            if ((dir2.getOpposite() == dir1) || (dir2.getHorizontalIndex() < 0)) {
                ignitePos(pos.offset(dir1).up(up), true);
            } else {
                ignitePos(pos.offset(dir1).offset(dir2).up(up), true);
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

    // Returns true if the rain extinguishes the campfire
    private boolean feelTheRainOnYourCampfire() {
        if (world.isRainingAt(this.pos.up())) {
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
            if (ServerConfig.ALLOW_CLIENT_PACKETS.get() && (world != null) && (!world.isRemote)) {
                BlockState state = this.getBlockState();
                world.notifyBlockUpdate(pos, state, state, 18); // Uses 2 client updates, and 16 no observers
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
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (ServerConfig.ALLOW_CLIENT_PACKETS.get()) {
            CompoundNBT nbtTag = this.getUpdateTag();
            //nbtTag.putInt("FuelTicks", this.fuelTicks);
            //nbtTag.putBoolean("IsEternal", this.isEternal);
            nbtTag.putBoolean("IsBonfire", this.isBonfire);
            return new SUpdateTileEntityPacket(pos, 13, nbtTag);
        }
        return new SUpdateTileEntityPacket(this.pos, 13, this.getUpdateTag());
    }

    @Override
    // Receive packet from client
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt){
        CompoundNBT nbtTag = pkt.getNbtCompound();
        if (nbtTag.contains("IsBonfire", 99)) {
            setBonfire(nbtTag.getBoolean("IsBonfire"));
        }
        super.onDataPacket(net, pkt);
    }

    @Inject(at = @At("RETURN"), method = "read(Lnet/minecraft/block/BlockState;Lnet/minecraft/nbt/CompoundNBT;)V")
    private void readFueled(BlockState state, CompoundNBT nbt, CallbackInfo ci) {
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
    private void writeFueled(CompoundNBT compound, CallbackInfoReturnable<CompoundNBT> cir) {
        CompoundNBT nbt = cir.getReturnValue();
        if (nbt != null) {
            nbt.putInt("FuelTicks", this.fuelTicks);
            nbt.putBoolean("IsEternal", this.isEternal);
            nbt.putBoolean("IsBonfire", this.isBonfire);
            cir.setReturnValue(nbt);
            cir.cancel();
        }
    }

}
