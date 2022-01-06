package com.mrcrayfish.backpacked.common;

import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.Config;
import com.mrcrayfish.backpacked.Reference;
import com.mrcrayfish.backpacked.item.BackpackItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID)
public class CommonEvents
{
    @SubscribeEvent
    public static void onPickupItem(EntityItemPickupEvent event)
    {
        if(Config.SERVER.lockBackpackIntoSlot.get() && event.getEntityLiving() instanceof ServerPlayer player)
        {
            ItemEntity entity = event.getItem();
            ItemStack stack = entity.getItem();
            if(!(stack.getItem() instanceof BackpackItem))
                return;

            CompoundTag tag = stack.getTag();
            if(tag == null || tag.getList("Items", Tag.TAG_COMPOUND).isEmpty())
                return;

            event.setCanceled(true);

            if(Backpacked.getBackpackStack(player).isEmpty())
            {
                if(Backpacked.setBackpackStack(player, stack))
                {
                    ((ServerLevel) entity.level).getChunkSource().broadcast(entity, new ClientboundTakeItemEntityPacket(entity.getId(), player.getId(), stack.getCount()));
                    event.setCanceled(true);
                    event.getItem().discard();
                }
            }
        }
    }
}
