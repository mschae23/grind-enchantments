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

package de.mschae23.grindenchantments.config.legacy.v1;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;

@Deprecated
public record CostConfigV1(CostFunction function, double factor, double offset) {
    public static final MapCodec<CostConfigV1> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CostFunction.CODEC.fieldOf("count_mode").forGetter(CostConfigV1::function),
        Codec.DOUBLE.fieldOf("cost_factor").orElse(1.0).forGetter(CostConfigV1::factor),
        Codec.DOUBLE.fieldOf("cost_offset").orElse(0.0).forGetter(CostConfigV1::offset)
    ).apply(instance, instance.stable(CostConfigV1::new)));

    public CostFunction latest() {
        return new TransformCostFunction(this.function, this.factor, this.offset);
    }
}
