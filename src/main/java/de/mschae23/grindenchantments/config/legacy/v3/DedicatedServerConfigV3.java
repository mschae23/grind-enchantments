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

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.DedicatedServerConfig;

@Deprecated
public record DedicatedServerConfigV3(boolean alternativeCostDisplay) {
    public static final Codec<DedicatedServerConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("alternative_cost_display_enabled").forGetter(DedicatedServerConfigV3::alternativeCostDisplay)
    ).apply(instance, instance.stable(DedicatedServerConfigV3::new)));
    public static final DedicatedServerConfigV3 DEFAULT = new DedicatedServerConfigV3(false);

    public DedicatedServerConfig latest() {
        return new DedicatedServerConfig(this.alternativeCostDisplay);
    }
}
