package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends ContainerBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }

    @Shadow
    protected static boolean isLit(BlockState state) { return false; }


//    @Inject(at = @At("RETURN"), method = "<init>*")
//    protected void initEdit(CallbackInfo ci) {
//        //this.setDefaultState(this.getDefaultState().with(CampfireBlock.LIT, !ServerConfig.SPAWN_ALL_CF_UNLIT.get()));
//    }

    @Inject(at = @At("HEAD"), method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn, CallbackInfo callbackInfo) {

    }

//    private boolean burnFuel(IFueledCampfire cfTileEntity, int baseBurnTicks, int maxFuelTicks, int maxOverFuelTicks, boolean canBonfire, boolean isSoul) {
//        int currFuelTicks = cfTileEntity.getFuelTicks();
//        int adjBurnTicks = (int) Math.ceil(baseBurnTicks * (isSoul ? ServerConfig.SOUL_CAMPFIRE_FUEL_MULT.get() : ServerConfig.CAMPFIRE_FUEL_MULT.get()));
//        int newCurrFuelTicks = currFuelTicks + adjBurnTicks;
//        if (newCurrFuelTicks > maxFuelTicks) {
//            if (canBonfire) {
//                System.out.println("but bonfire!");
//                cfTileEntity.setFuelTicks(Math.min(newCurrFuelTicks, maxOverFuelTicks));
//                //cfTileEntity.setBonfire(true);
//                return true;
//            } else {
//                //System.out.println("return");
//                return false;
//            }
//        } else {
//            System.out.println("under normal max");
//            cfTileEntity.setFuelTicks(newCurrFuelTicks);
//            return true;
//        }
//    }

    //            if (worldIn.isRemote && isLit(state) && rawBurnTime > 0)
//                worldIn.addParticle(ParticleTypes.SMOKE, entityIn.getPosX(), entityIn.getPosY() + 0.25D, entityIn.getPosZ(), 0, 0.05D, 0);
//
//            if (rawBurnTime > 0) {
//                if (((ItemEntity) entityIn).getThrowerId() != null && ((ICampfireExtra) worldIn.getTileEntity(pos)).getLifeTime() != -1337) {
//                    if (!worldIn.isRemote) {
//                        int burnTime = rawBurnTime * CampfireOverhaulConfig.CAMPFIRE_FUEL_MULTIPLIER.get() * ((ItemEntity) entityIn).getItem().getCount();
//                        CampfireTileEntity tileEntity = (CampfireTileEntity) worldIn.getTileEntity(pos);
//                        ICampfireExtra lifeTime = ((ICampfireExtra) tileEntity);
//
//                        if (lifeTime.getLifeTime() < (CampfireHelper.isSoul(state) ? CampfireOverhaulConfig.SOUL_CAMPFIRE_MAX_LIFE_TIME.get() : CampfireOverhaulConfig.CAMPFIRE_MAX_LIFE_TIME.get()) && lifeTime.getLifeTime() >= 0) {
//                            lifeTime.addLifeTime(burnTime);
//                            if (((ItemEntity) entityIn).getItem().getItem() == Items.LAVA_BUCKET)
//                                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(Items.BUCKET));
//                            entityIn.remove();
//                        }
//                    }
//                }
//            }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(!worldIn.isRemote()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof IFueledCampfire) {
                IFueledCampfire cfTileEntity = (IFueledCampfire) tileentity;
                // Override world-genned eternal status when placed by a player
                if (state.getBlock() == Blocks.SOUL_CAMPFIRE) {
                    cfTileEntity.setEternal(ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get());
                } else {
                    cfTileEntity.setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
                }
            }
        }
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }


    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/item/BlockItemUseContext;)Lnet/minecraft/block/BlockState;", cancellable = true)
    public void placementState(BlockItemUseContext context, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(cir.getReturnValue()
                .with(CampfireBlock.LIT, ((this.getDefaultState().getBlock() == Blocks.SOUL_CAMPFIRE) ?
                        ServerConfig.PLACE_SOUL_CAMPFIRE_LIT.get() : ServerConfig.PLACE_CAMPFIRE_LIT.get())));
    }




    // Detect fuel dropped



    //    @Override
    //    // ...Never mind. Can't access the TE from the client-side, packets would just be expensive for no reason.
    //    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
    //        boolean isSoul = this.getDefaultState().getBlock() == Blocks.SOUL_CAMPFIRE;
    //        if (isSoul ? ServerConfig.SOUL_CAMPFIRE_FUEL_BASED_LIGHT.get() : ServerConfig.CAMPFIRE_FUEL_BASED_LIGHT.get()) {
    //            if (state.get(CampfireBlock.LIT)) {
    //                CampfireTileEntity tileEntity = (CampfireTileEntity) world.getTileEntity(pos);
    //                if (tileEntity instanceof IFueledCampfire) {
    //                    IFueledCampfire cfTileEntity = (IFueledCampfire) tileEntity;
    //                    float fuel = cfTileEntity.getFuelTicks();
    //                    float maxFuel = isSoul ? ServerConfig.SOUL_CAMPFIRE_MAX_FUEL_TICKS.get() : ServerConfig.CAMPFIRE_MAX_FUEL_TICKS.get();
    //                    return (int)(15f * (fuel/maxFuel));
    //                }
    //            }
    //        }
    //        return super.getLightValue(state, world, pos);
    //    }

}
