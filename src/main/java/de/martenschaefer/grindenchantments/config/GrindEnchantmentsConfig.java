package de.martenschaefer.grindenchantments.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GrindEnchantmentsConfig(DisenchantConfig disenchant, MoveConfig move, boolean showCost) {
    public static final Codec<GrindEnchantmentsConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(GrindEnchantmentsConfig::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(GrindEnchantmentsConfig::move),
        Codec.BOOL.fieldOf("show_enchantment_cost").forGetter(GrindEnchantmentsConfig::showCost)
    ).apply(instance, instance.stable(GrindEnchantmentsConfig::new)));

    public static final GrindEnchantmentsConfig DEFAULT =
        new GrindEnchantmentsConfig(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT, true);
}
