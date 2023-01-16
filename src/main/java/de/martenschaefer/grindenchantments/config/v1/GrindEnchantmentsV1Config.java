package de.martenschaefer.grindenchantments.config.v1;

import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.grindenchantments.config.ClientConfig;
import de.martenschaefer.grindenchantments.config.DedicatedServerConfig;
import de.martenschaefer.grindenchantments.config.DisenchantConfig;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsV2Config;
import de.martenschaefer.grindenchantments.config.MoveConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record GrindEnchantmentsV1Config(DisenchantConfig disenchant, MoveConfig move, boolean allowCurses,
                                        DedicatedServerConfig dedicatedServerConfig, ClientConfig clientConfig) implements ModConfig<GrindEnchantmentsV2Config> {
    public static final Codec<GrindEnchantmentsV1Config> TYPE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsV1Config::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsV1Config::move),
        Codec.BOOL.orElse(Boolean.FALSE).fieldOf("allow_removing_curses").forGetter(GrindEnchantmentsV1Config::allowCurses),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(GrindEnchantmentsV1Config::dedicatedServerConfig),
        ClientConfig.CODEC.fieldOf("client_options").forGetter(GrindEnchantmentsV1Config::clientConfig)
    ).apply(instance, instance.stable(GrindEnchantmentsV1Config::new)));

    public static final Type<GrindEnchantmentsV2Config, GrindEnchantmentsV1Config> TYPE = new Type<>(1, TYPE_CODEC);

    public static final GrindEnchantmentsV1Config DEFAULT =
        new GrindEnchantmentsV1Config(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, false, DedicatedServerConfig.DEFAULT, ClientConfig.DEFAULT);

    @Override
    public Type<GrindEnchantmentsV2Config, ?> type() {
        return TYPE;
    }

    @Override
    public GrindEnchantmentsV2Config latest() {
        return new GrindEnchantmentsV2Config(this.disenchant, this.move, this.allowCurses, this.dedicatedServerConfig, this.clientConfig);
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }
}
