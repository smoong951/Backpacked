package com.mrcrayfish.backpacked;

import com.mrcrayfish.backpacked.common.BackpackEvents;
import com.mrcrayfish.backpacked.common.WanderingTraderEvents;
import com.mrcrayfish.backpacked.common.backpack.challenge.ChallengeManager;
import com.mrcrayfish.backpacked.core.ModCommands;
import com.mrcrayfish.backpacked.core.ModSyncedDataKeys;
import com.mrcrayfish.backpacked.data.tracker.UnlockManager;
import com.mrcrayfish.backpacked.enchantment.RepairmanEnchantment;
import com.mrcrayfish.backpacked.network.Network;
import com.mrcrayfish.framework.api.FrameworkAPI;

/**
 * Author: MrCrayfish
 */
public class Bootstrap
{
    public static void init()
    {
        FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.UNLOCK_TRACKER);
        FrameworkAPI.registerSyncedDataKey(ModSyncedDataKeys.PICKPOCKET_CHALLENGE);
        Network.init();
        UnlockManager.init();
        BackpackEvents.init();
        RepairmanEnchantment.init();
        WanderingTraderEvents.init();
        Config.init();
        ModCommands.init();
    }
}
