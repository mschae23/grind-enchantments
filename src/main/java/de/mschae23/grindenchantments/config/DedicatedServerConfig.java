package de.mschae23.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DedicatedServerConfig(boolean alternativeCostDisplay) {
    public static final Codec<DedicatedServerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("alternative_cost_display_enabled").forGetter(DedicatedServerConfig::alternativeCostDisplay)
    ).apply(instance, instance.stable(DedicatedServerConfig::new)));

    public static final DedicatedServerConfig DEFAULT = new DedicatedServerConfig(false);
}
