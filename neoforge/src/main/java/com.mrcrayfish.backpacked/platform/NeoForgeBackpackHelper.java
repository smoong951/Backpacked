package com.mrcrayfish.backpacked.platform;

import com.mrcrayfish.backpacked.Backpacked;
import com.mrcrayfish.backpacked.blockentity.ShelfBlockEntity;
import com.mrcrayfish.backpacked.integration.CuriosHelper;
import com.mrcrayfish.backpacked.inventory.ExtendedPlayerInventory;
import com.mrcrayfish.backpacked.inventory.container.BackpackContainerMenu;
import com.mrcrayfish.backpacked.item.BackpackItem;
import com.mrcrayfish.backpacked.platform.services.IBackpackHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Author: MrCrayfish
 */
public class NeoForgeBackpackHelper implements IBackpackHelper
{
    @Override
    public ItemStack getBackpackStack(Player player)
    {
        AtomicReference<ItemStack> backpack = new AtomicReference<>(ItemStack.EMPTY);
        if(Backpacked.isCuriosLoaded())
        {
            backpack.set(CuriosHelper.getBackpackStack(player));
        }
        else if(player.getInventory() instanceof ExtendedPlayerInventory inventory)
        {
            ItemStack stack = inventory.getBackpackItems().get(0);
            if(stack.getItem() instanceof BackpackItem)
            {
                backpack.set(stack);
            }
        }
        return backpack.get();
    }

    @Override
    public boolean setBackpackStack(Player player, ItemStack stack)
    {
        if(!(stack.getItem() instanceof BackpackItem) && !stack.isEmpty())
            return false;

        if(Backpacked.isCuriosLoaded())
        {
            CuriosHelper.setBackpackStack(player, stack);
            return true;
        }
        else if(player.getInventory() instanceof ExtendedPlayerInventory inventory)
        {
            inventory.getBackpackItems().set(0, stack.copy());
            return true;
        }
        return false;
    }

    @Override
    public EnchantmentCategory getEnchantmentCategory()
    {
        return Backpacked.ENCHANTMENT_TYPE;
    }

    @Override
    public boolean isUsingThirdPartySlot()
    {
        return Backpacked.isCuriosLoaded();
    }

    @Override
    public boolean isBackpackVisible(Player player)
    {
        if(Backpacked.isCuriosLoaded())
        {
            return CuriosHelper.isBackpackVisible(player);
        }
        return true;
    }

    @Override
    public ShelfBlockEntity createShelfBlockEntityType(BlockPos pos, BlockState state)
    {
        return new ShelfBlockEntity(pos, state);
    }

    @Override
    public void openBackpackScreen(ServerPlayer openingPlayer, Container inventory, int cols, int rows, boolean owner, Component title)
    {
        openingPlayer.openMenu(new SimpleMenuProvider((id, playerInventory, entity) -> {
            return new BackpackContainerMenu(id, openingPlayer.getInventory(), inventory, cols, rows, owner);
        }, title), buffer -> {
            buffer.writeVarInt(cols);
            buffer.writeVarInt(rows);
            buffer.writeBoolean(owner);
        });
    }

    @Override
    public BackpackItem createBackpackItem(Item.Properties properties)
    {
        return new BackpackItem(properties);
    }
}