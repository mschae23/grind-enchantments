package de.mschae23.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClientConfig(boolean showLevelCost) {
    public static final Codec<ClientConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("show_enchantment_cost").forGetter(ClientConfig::showLevelCost)
    ).apply(instance, instance.stable(ClientConfig::new)));

    public static final ClientConfig DEFAULT = new ClientConfig(true);
}
