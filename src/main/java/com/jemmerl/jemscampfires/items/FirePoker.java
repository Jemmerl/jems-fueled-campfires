package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

public class FirePoker extends Item {
    public FirePoker(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level world = context.getLevel();
        BlockState state = world.getBlockState(context.getClickedPos());
        IFueledCampfire cfTileEntity = Util.getCFTE(context.getLevel(), context.getClickedPos());
        if (cfTileEntity != null) {
            Player player = context.getPlayer();
            if ((player == null) || (player.getCooldowns().isOnCooldown(this))) {
                return super.onItemUseFirst(stack, context);
            }
            player.getCooldowns().addCooldown(this, 10);
            Util.dispatchCampfireInfo(world, context.getClickedPos(), state, player, cfTileEntity);
            return InteractionResult.sidedSuccess(world.isClientSide());
        }
        return super.onItemUseFirst(stack, context);
    }
}
