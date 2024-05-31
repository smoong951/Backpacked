package com.mrcrayfish.backpacked.common.challenge.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrcrayfish.backpacked.Constants;
import com.mrcrayfish.backpacked.common.MovementType;
import com.mrcrayfish.backpacked.common.challenge.Challenge;
import com.mrcrayfish.backpacked.common.challenge.ChallengeSerializer;
import com.mrcrayfish.backpacked.common.tracker.IProgressTracker;
import com.mrcrayfish.backpacked.common.tracker.ProgressFormatter;
import com.mrcrayfish.backpacked.common.tracker.impl.CountProgressTracker;
import com.mrcrayfish.backpacked.data.unlock.UnlockManager;
import com.mrcrayfish.backpacked.event.BackpackedEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TravelDistanceChallenge extends Challenge
{
    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "travel_distance");
    public static final Serializer SERIALIZER = new Serializer();
    public static final Codec<TravelDistanceChallenge> CODEC = RecordCodecBuilder.create(builder -> {
        return builder.group(ProgressFormatter.CODEC.fieldOf("formatter").orElse(ProgressFormatter.INT_PERCENT).forGetter(challenge -> {
            return challenge.formatter;
        }), ExtraCodecs.strictOptionalField(MovementType.LIST_CODEC.xmap(EnumSet::copyOf, List::copyOf), "movement").forGetter(challenge -> {
            return challenge.movementTypes;
        }), ExtraCodecs.POSITIVE_INT.fieldOf("total_distance").forGetter(challenge -> {
            return challenge.totalDistanceInCm;
        })).apply(builder, TravelDistanceChallenge::new);
    });

    private final ProgressFormatter formatter;
    private final Optional<EnumSet<MovementType>> movementTypes;
    private final int totalDistanceInCm;

    protected TravelDistanceChallenge(ProgressFormatter formatter, Optional<EnumSet<MovementType>> movementTypes, int totalDistanceInCm)
    {
        super(ID);
        this.formatter = formatter;
        this.movementTypes = movementTypes;
        this.totalDistanceInCm = totalDistanceInCm;
    }

    @Override
    public ChallengeSerializer<?> getSerializer()
    {
        return SERIALIZER;
    }

    @Override
    public IProgressTracker createProgressTracker(ResourceLocation backpackId)
    {
        return new Tracker(this.formatter, this.movementTypes, this.totalDistanceInCm);
    }

    public static class Serializer extends ChallengeSerializer<TravelDistanceChallenge>
    {

        @Override
        public Codec<TravelDistanceChallenge> codec()
        {
            return TravelDistanceChallenge.CODEC;
        }
    }

    public static class Tracker extends CountProgressTracker
    {
        private final Optional<EnumSet<MovementType>> movementTypes;

        public Tracker(ProgressFormatter formatter, Optional<EnumSet<MovementType>> movementTypes, int maxCount)
        {
            super(maxCount, formatter);
            this.movementTypes = movementTypes;
        }

        public static void registerEvent()
        {
            BackpackedEvents.PLAYER_TRAVEL.register((player, distance, type) -> {
                int distanceInCm = Math.round((float) Math.sqrt(distance) * 100);
                UnlockManager.getTrackers(player, Tracker.class).forEach(tracker -> {
                    if(tracker.isComplete())
                        return;
                    if(tracker.movementTypes.map(types -> types.contains(type)).orElse(true)) {
                        tracker.increment(distanceInCm, player);
                    }
                });
            });
        }
    }
}
