package com.jemmerl.jemscampfires.util;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.init.fueloverrides.FuelOverrideDataManager;
import com.jemmerl.jemscampfires.network.S2C_CFInfoPkt;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;

public class Util {

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static IFueledCampfire getCFTE(BlockGetter worldIn, BlockPos posIn) {
        BlockEntity blockEntity = worldIn.getBlockEntity(posIn);
        if ((blockEntity instanceof IFueledCampfire)) {
            return (IFueledCampfire) blockEntity;
        }
        return null;
    }

    public static boolean failsFuelFilter(boolean soul, ItemStack itemStack) {
        if (soul) {
            return (itemStack.is(ModTags.SOUL_CF_FILTERED_FUELS) != ServerConfig.SOUL_CAMPFIRE_USE_WHITELIST.get());
        }
        return (itemStack.is(ModTags.CF_FILTERED_FUELS) != ServerConfig.CAMPFIRE_USE_WHITELIST.get());
    }

    public static int getItemFuelVal(ItemStack itemStack) {
        int val = FuelOverrideDataManager.getCustomFuelVal(itemStack.getItem());
        return (val > 0) ? val : itemStack.getBurnTime(RecipeType.SMELTING);
    }

    // TODO: This hashmap is for fuels that are in containers (ex: lava buckets)
    //       Modders/pack-devs can use mixin injects (or any other way, idk) to add new items, make sure not to
    //       overwrite/clear the map unless you know what you are doing!
    // If anyone genuinely uses this feature and does not like this method, just ask! I will do an API or datapack for it.
    // But I don't feel like it right now, because I don't know how to do an API and do not expect ppl to use this -Jem
    public static HashMap<Item, Item> fuelContainers = new HashMap<>();
    static {
        fuelContainers.put(Items.LAVA_BUCKET, Items.BUCKET);
    }

    public static void dispatchCampfireInfo(Level level, BlockPos pos, BlockState state, Player player, IFueledCampfire cfTileEntity) {
        if(level.isClientSide) {
            if (!state.getValue(CampfireBlock.LIT)) return;

            if (!BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString().contains("soul")) {
                RandomSource randomSource = level.getRandom();
                int n = randomSource.nextInt(4) + 1;
                for (int i = 0; i < n; i++) {
                    level.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                            (randomSource.nextFloat() / 2.0F), 3.0E-5D, (randomSource.nextFloat() / 2.0F));
                }
            }
        } else {
            ChatFormatting color = ChatFormatting.WHITE;
            float bonfireLimit = cfTileEntity.jems_fueled_campfires$getFuelTicks() / (float)cfTileEntity.jems_fueled_campfires$getBonfireLimit();
            if (bonfireLimit > 0.90f) {
                color = ChatFormatting.RED;
            } else if (bonfireLimit > 0.75f) {
                color = ChatFormatting.YELLOW;
            }
            boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED);

            if (!(player instanceof ServerPlayer serverPlayer)) {
                JemsCampfires.LOGGER.error("Player not instance of ServerPlayer, failed to send campfire info packet.");
                return;
            }

            PacketDistributor.sendToPlayer(serverPlayer, S2C_CFInfoPkt.createPktFromData(
                    state.getValue(CampfireBlock.LIT), waterlogged, cfTileEntity.jems_fueled_campfires$getBonfire(),
                    cfTileEntity.jems_fueled_campfires$getEternal(), color,
                    cfTileEntity.jems_fueled_campfires$getFuelTicks()));
        }
    }
}