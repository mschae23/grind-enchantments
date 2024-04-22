package de.mschae23.grindenchantments.config;

import de.mschae23.config.api.ModConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GrindEnchantmentsV2Config(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                        DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) implements ModConfig<GrindEnchantmentsV2Config> {
    public static final MapCodec<GrindEnchantmentsV2Config> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsV2Config::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsV2Config::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsV2Config::allowCurses),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsV2Config::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsV2Config::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsV2Config::new)));

    public static final ModConfig.Type<GrindEnchantmentsV2Config, GrindEnchantmentsV2Config> TYPE = new ModConfig.Type<>(2, TYPE_CODEC);

    public static final GrindEnchantmentsV2Config DEFAULT =
        new GrindEnchantmentsV2Config(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);

    @Override
    public Type<GrindEnchantmentsV2Config, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsV2Config latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
