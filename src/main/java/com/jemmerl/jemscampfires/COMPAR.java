//package com.jemmerl.jemscampfires;
//
//import net.minecraft.core.BlockPos;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.level.Level;
//import net.minecraft.world.level.LevelAccessor;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.entity.CampfireBlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraft.world.level.gameevent.GameEvent;
//
//import javax.annotation.Nullable;
//
//public class COMPAR {
//
//    public static void extinguish(IWorld world, BlockPos pos, BlockState state) {
//        if (world.isRemote()) {
//            for(int i = 0; i < 20; ++i) {
//                spawnSmokeParticles((World)world, pos, state.get(SIGNAL_FIRE), true);
//            }
//        }
//
//        TileEntity tileentity = world.getTileEntity(pos);
//        if (tileentity instanceof CampfireTileEntity) {
//            ((CampfireTileEntity)tileentity).dropAllItems();
//        }
//
//    }
//
//    public static void dowse(@Nullable Entity pEntity, LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
//        if (pLevel.isClientSide()) {
//            for(int i = 0; i < 20; ++i) {
//                makeParticles((Level)pLevel, pPos, pState.getValue(SIGNAL_FIRE), true);
//            }
//        }
//
//        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
//        if (blockentity instanceof CampfireBlockEntity) {
//            ((CampfireBlockEntity)blockentity).dowse();
//        }
//
//        pLevel.gameEvent(pEntity, GameEvent.BLOCK_CHANGE, pPos);
//    }
//}
