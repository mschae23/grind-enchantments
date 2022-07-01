package de.martenschaefer.grindenchantments.cost;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.enchantment.Enchantment;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FirstEnchantmentCostCountMode(CostCountMode delegate) implements CostCountMode {
    public static final Codec<FirstEnchantmentCostCountMode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CostCountMode.TYPE_CODEC.fieldOf("delegate").forGetter(FirstEnchantmentCostCountMode::delegate)
    ).apply(instance, instance.stable(FirstEnchantmentCostCountMode::new)));

    @Override
    public double getCost(Set<Map.Entry<Enchantment, Integer>> enchantments, boolean allowCurses) {
        Optional<Map.Entry<Enchantment, Integer>> firstEnchantment = enchantments.stream()
            .filter(entry -> allowCurses || !entry.getKey().isCursed()).limit(1).findFirst();

        return firstEnchantment.map(entry -> this.delegate.getCost(Set.of(entry), allowCurses)).orElse(1.0);
    }

    @Override
    public CostCountModeType<?> getType() {
        return CostCountModeType.FIRST_ENCHANTMENT;
    }
}
