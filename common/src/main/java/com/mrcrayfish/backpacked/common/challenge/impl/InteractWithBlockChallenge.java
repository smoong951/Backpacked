package com.mrcrayfish.backpacked.common.challenge.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.Constants;
import com.mrcrayfish.backpacked.common.challenge.Challenge;
import com.mrcrayfish.backpacked.common.challenge.ChallengeSerializer;
import com.mrcrayfish.backpacked.common.challenge.ChallengeUtils;
import com.mrcrayfish.backpacked.common.tracker.IProgressTracker;
import com.mrcrayfish.backpacked.common.tracker.ProgressFormatter;
import com.mrcrayfish.backpacked.common.tracker.impl.CountProgressTracker;
import com.mrcrayfish.backpacked.data.unlock.UnlockManager;
import com.mrcrayfish.backpacked.event.BackpackedEvents;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class InteractWithBlockChallenge extends Challenge
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "interact_with_block");
    public static final Serializer SERIALIZER = new Serializer();
    public static final Codec<InteractWithBlockChallenge> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(ProgressFormatter.CODEC.fieldOf("formatter").orElse(ProgressFormatter.USED_X_TIMES).forGetter(challenge -> {
            return challenge.formatter;
        }), ExtraCodecs.strictOptionalField(BlockPredicate.CODEC, "block").forGetter(challenge -> {
            return challenge.block;
        }), ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(challenge -> {
            return challenge.item;
        }), ExtraCodecs.strictOptionalField(ExtraCodecs.POSITIVE_INT, "count", 1).forGetter(challenge -> {
            return challenge.count;
        })).apply(builder, InteractWithBlockChallenge::new);
    });

    private final ProgressFormatter formatter;
    private final Optional<BlockPredicate> block;
    private final Optional<ItemPredicate> item;
    private final int count;

    public InteractWithBlockChallenge(ProgressFormatter formatter, Optional<BlockPredicate> block, Optional<ItemPredicate> item, int count)
    {
        super(ID);
        this.formatter = formatter;
        this.block = block;
        this.item = item;
        this.count = count;
    }

    @Override
    public ChallengeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @Override
    public IProgressTracker createProgressTracker(ResourceLocation backpackId)
    {
        return new Tracker(this.count, this.formatter, this.block, this.item);
    }

    public static class Serializer extends ChallengeSerializer<InteractWithBlockChallenge>
    {

        @Override
        public Codec<InteractWithBlockChallenge> codec()
        {
            return InteractWithBlockChallenge.CODEC;
        }
    }

    public static class Tracker extends CountProgressTracker
    {
        private final Optional<BlockPredicate> block;
        private final Optional<ItemPredicate> item;

        private Tracker(int maxCount, ProgressFormatter formatter, Optional<BlockPredicate> block, Optional<ItemPredicate> item)
        {
            super(maxCount, formatter);
            this.block = block;
            this.item = item;
        }

        private boolean test(BlockState state, ItemStack stack)
        {
            return ChallengeUtils.testPredicate(this.block, state, null) && ChallengeUtils.testPredicate(this.item, stack);
        }

        public static void registerEvent()
        {
            // Only capture the compound tag of the block entity if we need to
            BackpackedEvents.INTERACTED_WITH_BLOCK_CAPTURE_TAG.register((state, stack, player) -> {
                return UnlockManager.getTrackers(player, Tracker.class).stream().anyMatch(tracker -> {
                    return !tracker.isComplete() && tracker.test(state, stack);
                });
            });

            // If this event is called, we have successfully interacted with block. Now update tracker
            BackpackedEvents.INTERACTED_WITH_BLOCK.register((state, stack, tag, player) -> {
                UnlockManager.getTrackers(player, Tracker.class).forEach(tracker -> {
                    if(!tracker.isComplete() && tracker.test(state, stack)) {
                        tracker.increment(player);
                    }
                });
            });
        }
    }
}
