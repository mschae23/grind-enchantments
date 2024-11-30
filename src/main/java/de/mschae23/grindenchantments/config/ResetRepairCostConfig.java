/*
 * Copyright (C) 2024  mschae23
 *
 * This file is part of Grind enchantments.
 *
 * Grind enchantments is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.mschae23.grindenchantments.config;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.cost.AverageCountCostFunction;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;
import org.apache.logging.log4j.Level;

public record ResetRepairCostConfig(boolean enabled, List<Identifier> catalystItems, boolean requiresEnchantment, CostFunction costFunction) {
    public static final Codec<ResetRepairCostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ResetRepairCostConfig::enabled),
        Codecs.listOrSingle(Identifier.CODEC).fieldOf("catalyst_items").forGetter(ResetRepairCostConfig::catalystItems),
        Codec.BOOL.fieldOf("requires_enchantment").forGetter(ResetRepairCostConfig::requiresEnchantment),
        CostFunction.CODEC.fieldOf("cost_function").forGetter(ResetRepairCostConfig::costFunction)
    ).apply(instance, instance.stable(ResetRepairCostConfig::new)));

    public static final ResetRepairCostConfig DEFAULT = new ResetRepairCostConfig(false,
        List.of(Identifier.ofVanilla("diamond")), true,
        // Intentionally no filter function
        new TransformCostFunction(new AverageCountCostFunction(new CountLevelsCostFunction(1.0, 4.0)), 1.5, 4.0));

    public static final ResetRepairCostConfig DISABLED = new ResetRepairCostConfig(false,
        List.of(), false,
        new CountLevelsCostFunction(1.0, 1.0));

    public static PacketCodec<PacketByteBuf, ResetRepairCostConfig> createPacketCodec(PacketCodec<PacketByteBuf, CostFunction> costFunctionCodec) {
        return PacketCodec.tuple(
            PacketCodecs.BOOLEAN, ResetRepairCostConfig::enabled,
            Identifier.PACKET_CODEC.collect(PacketCodecs.toList()), ResetRepairCostConfig::catalystItems,
            PacketCodecs.BOOLEAN, ResetRepairCostConfig::requiresEnchantment,
            costFunctionCodec, ResetRepairCostConfig::costFunction,
            ResetRepairCostConfig::new
        );
    }

    public void validateRegistryEntries(RegistryWrapper.WrapperLookup wrapperLookup) {
        Optional<? extends RegistryWrapper.Impl<Item>> registryWrapperOpt = wrapperLookup.getOptional(RegistryKeys.ITEM);

        if (registryWrapperOpt.isEmpty()) {
            GrindEnchantmentsMod.log(Level.WARN, "Item registry is not present");
            return;
        }

        RegistryWrapper.Impl<Item> registryWrapper = registryWrapperOpt.get();

        this.catalystItems.stream()
            .map(item -> Pair.of(item, registryWrapper.getOptional(RegistryKey.of(RegistryKeys.ITEM, item))))
            .flatMap(result -> result.getSecond().isEmpty() ? Stream.of(result.getFirst()) : Stream.empty())
            .map(Identifier::toString)
            .forEach(item -> GrindEnchantmentsMod.log(Level.WARN, "Reset repair cost config contains unknown catalyst item: " + item));
    }

    @Override
    public String toString() {
        return "ResetRepairCostConfig{" +
            "enabled=" + this.enabled +
            ", catalystItems=" + this.catalystItems +
            ", requiresEnchantment=" + this.requiresEnchantment +
            ", costConfig=" + this.costFunction +
            '}';
    }
}
