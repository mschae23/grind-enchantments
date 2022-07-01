package de.martenschaefer.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GrindEnchantmentsConfig(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                      DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) {
    public static final Codec<GrindEnchantmentsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsConfig::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsConfig::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsConfig::allowCurses),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsConfig::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsConfig::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsConfig::new)));

    public static final GrindEnchantmentsConfig DEFAULT =
        new GrindEnchantmentsConfig(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);
}
