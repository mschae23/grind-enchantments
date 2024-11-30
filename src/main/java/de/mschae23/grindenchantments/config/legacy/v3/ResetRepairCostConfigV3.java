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

package de.mschae23.grindenchantments.config.legacy.v3;

import java.util.List;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.ResetRepairCostConfig;
import de.mschae23.grindenchantments.cost.AverageCountCostFunction;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;

@Deprecated
public record ResetRepairCostConfigV3(boolean enabled, List<Identifier> catalystItems, boolean requiresEnchantment, CostFunction costFunction) {
    public static final Codec<ResetRepairCostConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(ResetRepairCostConfigV3::enabled),
        Codecs.listOrSingle(Identifier.CODEC).fieldOf("catalyst_items").forGetter(ResetRepairCostConfigV3::catalystItems),
        Codec.BOOL.fieldOf("requires_enchantment").forGetter(ResetRepairCostConfigV3::requiresEnchantment),
        CostFunction.CODEC.fieldOf("cost_function").forGetter(ResetRepairCostConfigV3::costFunction)
    ).apply(instance, instance.stable(ResetRepairCostConfigV3::new)));

    public static final ResetRepairCostConfigV3 DEFAULT = new ResetRepairCostConfigV3(false,
        List.of(Identifier.ofVanilla("diamond")), true,
        // Intentionally no filter function
        new TransformCostFunction(new AverageCountCostFunction(new CountLevelsCostFunction(1.0, 4.0)), 1.5, 4.0));

    public ResetRepairCostConfig latest() {
        return new ResetRepairCostConfig(this.enabled, this.catalystItems, this.requiresEnchantment, this.costFunction);
    }
}
