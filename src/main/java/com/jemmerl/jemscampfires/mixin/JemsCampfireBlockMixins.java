package com.jemmerl.jemscampfires.mixin;

import com.jemmerl.jemscampfires.config.ServerConfig;
import com.jemmerl.jemscampfires.util.IFueledCampfire;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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



}
