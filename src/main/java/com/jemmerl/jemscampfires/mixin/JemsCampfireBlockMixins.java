package com.jemmerl.jemscampfires.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.item.BlockItemUseContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
