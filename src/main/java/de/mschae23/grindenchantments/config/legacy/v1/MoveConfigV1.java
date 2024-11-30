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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.MoveConfig;

@Deprecated
public record MoveConfigV1(boolean enabled, CostConfigV1 costConfig) {
    public static final Codec<MoveConfigV1> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(MoveConfigV1::enabled),
        CostConfigV1.CODEC.fieldOf("cost_config").forGetter(MoveConfigV1::costConfig)
    ).apply(instance, instance.stable(MoveConfigV1::new)));

    public MoveConfig latest() {
        return new MoveConfig(this.enabled, this.costConfig.latest());
    }
}
