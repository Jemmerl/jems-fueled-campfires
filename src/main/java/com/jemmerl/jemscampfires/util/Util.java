package com.jemmerl.jemscampfires.util;

import com.jemmerl.jemscampfires.JemsCampfires;
import com.jemmerl.jemscampfires.init.ModTags;
import com.jemmerl.jemscampfires.init.ServerConfig;
import com.jemmerl.jemscampfires.init.fueloverrides.FuelOverrideDataManager;
import com.jemmerl.jemscampfires.network.JCPacketHandler;
import com.jemmerl.jemscampfires.network.S2C_CFInfoPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Random;

public class Util {

    public static int mod(int a, int b) {
        return (a % b + b) % b;
    }

    public static IFueledCampfire getCFTE(IBlockReader worldIn, BlockPos posIn) {
        TileEntity tileentity = worldIn.getTileEntity(posIn);
        if (tileentity instanceof IFueledCampfire) {
            return (IFueledCampfire) tileentity;
        }
        return null;
    }

    public static boolean failsFuelFilter(boolean soul, Item item) {
        if (soul) {
            return (item.isIn(ModTags.SOUL_CF_FILTERED_FUELS) != ServerConfig.SOUL_CAMPFIRE_USE_WHITELIST.get());
        }
        return (item.isIn(ModTags.CF_FILTERED_FUELS) != ServerConfig.CAMPFIRE_USE_WHITELIST.get());
    }

    public static int getItemFuelVal(ItemStack itemStack, Item item) {
        int val = FuelOverrideDataManager.getCustomFuelVal(item);
        return (val > 0) ? val : ForgeHooks.getBurnTime(itemStack, null);
    }

    // TODO: This hashmap is for fuels that are in containers (ex: lava buckets)
    //       Modders/pack-devs can use mixin injects (or any other way, idk) to add new items, make sure not to
    //       overwrite/clear the map unless you know what you are doing!
    // If anyone genuinely uses this feature and does not like this method, just ask! I will do an API for it.
    // But I don't feel like it right now, because I don't know how to do an API and do not expect ppl to use this -Jem
    public static HashMap<Item, Item> fuelContainers = new HashMap<>();
    static {
        fuelContainers.put(Items.LAVA_BUCKET, Items.BUCKET);
    }

    public static void dispatchCampfireInfo(World world, BlockPos pos, BlockState state, PlayerEntity player, IFueledCampfire cfTileEntity) {
        if(world.isRemote) {
            if (!state.get(BlockStateProperties.LIT)) return;

            if (!ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString().contains("soul")) {
                Random random = world.getRandom();
                int n = random.nextInt(4) + 1;
                for (int i = 0; i < n; i++) {
                    world.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D,
                            (random.nextFloat() / 2.0F), 3.0E-5D, (random.nextFloat() / 2.0F));
                }
            }
        } else {
            TextFormatting color = TextFormatting.WHITE;
            float bonfireLimit = cfTileEntity.getFuelTicks() / (float)cfTileEntity.getBonfireLimit();
            if (bonfireLimit > 0.90f) {
                color = TextFormatting.RED;
            } else if (bonfireLimit > 0.75f) {
                color = TextFormatting.YELLOW;
            }
            boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED) && state.get(BlockStateProperties.WATERLOGGED);

            if (!(player instanceof ServerPlayerEntity)) {
                JemsCampfires.LOGGER.error("Player not instance of ServerPlayer, failed to send campfire info packet.");
                return;
            }

            JCPacketHandler.sendToClient(new S2C_CFInfoPacket(state.get(CampfireBlock.LIT), waterlogged,
                            cfTileEntity.getBonfire(), cfTileEntity.getEternal(), color, cfTileEntity.getFuelTicks()),
                    (ServerPlayerEntity) player);
        }
    }

}
