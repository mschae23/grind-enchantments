package de.martenschaefer.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DisenchantConfig(boolean enabled) {
    public static final Codec<DisenchantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(DisenchantConfig::enabled)
    ).apply(instance, instance.stable(DisenchantConfig::new)));

    public static final DisenchantConfig DEFAULT = new DisenchantConfig(true);
}
