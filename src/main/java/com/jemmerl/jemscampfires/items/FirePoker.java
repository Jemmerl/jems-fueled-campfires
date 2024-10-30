package com.jemmerl.jemscampfires.items;

import com.jemmerl.jemscampfires.util.IFueledCampfire;
import com.jemmerl.jemscampfires.util.Util;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.world.World;

public class FirePoker extends Item {
    public FirePoker(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        World world = context.getWorld();
        BlockState state = world.getBlockState(context.getPos());
        if (state.getBlock() instanceof CampfireBlock) {
            IFueledCampfire cfTileEntity = Util.getCFTE(context.getWorld(), context.getPos());
            if (cfTileEntity != null) {
                PlayerEntity player = context.getPlayer();
                if ((player == null) || (player.getCooldownTracker().hasCooldown(this))) {
                    return super.onItemUseFirst(stack, context);
                }
                player.getCooldownTracker().setCooldown(this, 10);
                Util.displayCampfireInfo(world, context.getPos(), state, player, cfTileEntity);
                return ActionResultType.func_233537_a_(world.isRemote());
            }
        }
        return super.onItemUseFirst(stack, context);
    }
}
