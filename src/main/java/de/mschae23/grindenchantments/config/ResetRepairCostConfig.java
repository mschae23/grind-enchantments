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

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.cost.AverageCountCostFunction;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;

public record ResetRepairCostConfig(boolean enabled, RegistryEntryList<Item> catalystItems, boolean requiresEnchantment, CostFunction costFunction) {
    public static final Codec<ResetRepairCostConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ResetRepairCostConfig::enabled),
        RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("catalyst_items").forGetter(ResetRepairCostConfig::catalystItems),
        Codec.BOOL.fieldOf("requires_enchantment").forGetter(ResetRepairCostConfig::requiresEnchantment),
        CostFunction.TYPE_CODEC.fieldOf("cost_function").forGetter(ResetRepairCostConfig::costFunction)
    ).apply(instance, instance.stable(ResetRepairCostConfig::new)));

    @SuppressWarnings("deprecation")
    public static final ResetRepairCostConfig DEFAULT = new ResetRepairCostConfig(false,
        RegistryEntryList.of(Items.DIAMOND.getRegistryEntry()), true,
        // Intentionally no filter function
        new TransformCostFunction(new AverageCountCostFunction(new CountLevelsCostFunction(1.0, 4.0)), 1.5, 4.0));
}
