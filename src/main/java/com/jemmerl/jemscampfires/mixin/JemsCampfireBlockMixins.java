package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.CampfireTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(value = CampfireBlock.class, priority = 0)
public abstract class JemsCampfireBlockMixins extends ContainerBlock {
    protected JemsCampfireBlockMixins(Properties builder) {
        super(builder);
    }



//    @Shadow
//    public static boolean isLit(BlockState state) {
//        return false;
//    }


//    @Inject(at = @At("RETURN"), method = "<init>*")
//    protected void initEdit(CallbackInfo ci) {
//        //this.setDefaultState(this.getDefaultState().with(CampfireBlock.LIT, !ServerConfig.SPAWN_ALL_CF_UNLIT.get()));
//    }

    // pretty sure this will still do the trick only for player activiation. wont cover projectiles
//    @Inject(at = @At("INVOKE_ASSIGN"), target = "Lnet.minecraft.tileentity.CampfireTileEntity;onBlockActivated(Lnet.minecraft.block.BlockState;Lnet.minecraft.world.World;Lnet.minecraft.util.math.BlockPos;Lnet.minecraft.entity.player.PlayerEntity;Lnet.minecraft.util.Hand;Lnet.minecraft.util.math.BlockRayTraceResult)LActionResultType;")
//    public void e() {
//    }


//    @Override
//    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
//        super.onBlockClicked(state, worldIn, pos, player);
//    }


    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(!worldIn.isRemote()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof JemsCampfireTEMixins) {
                JemsCampfireTEMixins cfTileEntity = (JemsCampfireTEMixins) tileentity;
                if (state.getBlock() == Blocks.SOUL_CAMPFIRE) {
                    cfTileEntity.setEternal(ServerConfig.PLACE_SOUL_CAMPFIRE_ETERNAL.get());
                } else {
                    cfTileEntity.setEternal(ServerConfig.PLACE_CAMPFIRE_ETERNAL.get());
                }
            }

        }




        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
    }

//    @Override
//    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
//        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
//    }

    @Inject(at = @At("RETURN"), method = "getStateForPlacement(Lnet/minecraft/item/BlockItemUseContext;)Lnet/minecraft/block/BlockState;", cancellable = true)
    public void placementState(BlockItemUseContext context, CallbackInfoReturnable<BlockState> cir) {
        if(context.getWorld().isRemote()) {
            //System.out.println("only if a player places...");

        }

                //cir.getReturnValue().get(CampfireBlock.LIT) & !CampfireOverhaulConfig.CAMPFIRE_CREATED_UNLIT.get()
        cir.setReturnValue(cir.getReturnValue()
                .with(CampfireBlock.LIT, false)
        );
    }

    // ON ACTIVATED because assume all player placed CF MUST be activated first
    //             TileEntity tileentity = context.getWorld().getTileEntity(context.getPos());
    //            System.out.println(tileentity==null);
    //            if (tileentity instanceof CampfireTileEntity) {
    //                CampfireTileEntity campfiretileentity = (CampfireTileEntity)tileentity;
    //                System.out.println(campfiretileentity);
    //            }

//    @Nullable
//    public BlockState getStateForPlacement(BlockItemUseContext context) {
//        IWorld iworld = context.getWorld();
//        BlockPos blockpos = context.getPos();
//        boolean flag = iworld.getFluidState(blockpos).getFluid() == Fluids.WATER;
//        return this.getDefaultState().with(WATERLOGGED, Boolean.valueOf(flag)).with(SIGNAL_FIRE, Boolean.valueOf(this.isHayBlock(iworld.getBlockState(blockpos.down())))).with(LIT, Boolean.valueOf(!flag)).with(FACING, context.getPlacementHorizontalFacing());
//    }

    // Detect fuel dropped



}
