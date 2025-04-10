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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import de.mschae23.grindenchantments.cost.CostFunction;
import de.mschae23.grindenchantments.cost.CountLevelsCostFunction;
import de.mschae23.grindenchantments.cost.CountMinPowerCostFunction;
import de.mschae23.grindenchantments.cost.FilterCostFunction;
import de.mschae23.grindenchantments.cost.TransformCostFunction;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DisenchantConfig(boolean enabled, boolean consumeItem, CostFunction costFunction) {
    public static final Codec<DisenchantConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(DisenchantConfig::enabled),
        Codec.BOOL.fieldOf("consume_enchanted_item").forGetter(DisenchantConfig::consumeItem),
        CostFunction.CODEC.fieldOf("cost_function").forGetter(DisenchantConfig::costFunction)
    ).apply(instance, instance.stable(DisenchantConfig::new)));

    public static final DisenchantConfig DEFAULT = new DisenchantConfig(true, false,
        new FilterCostFunction(new TransformCostFunction(new CountMinPowerCostFunction(), 0.3, 8.0)));

    public static final DisenchantConfig DISABLED = new DisenchantConfig(false, false,
        new CountLevelsCostFunction(1.0, 1.0));

    public static PacketCodec<PacketByteBuf, DisenchantConfig> createPacketCodec(PacketCodec<PacketByteBuf, CostFunction> costFunctionCodec) {
        return PacketCodec.tuple(
            PacketCodecs.BOOLEAN, DisenchantConfig::enabled,
            PacketCodecs.BOOLEAN, DisenchantConfig::consumeItem,
            costFunctionCodec, DisenchantConfig::costFunction,
            DisenchantConfig::new
        );
    }

    @Override
    public String toString() {
        return "DisenchantConfig{" +
            "enabled=" + this.enabled +
            ", consumeItem=" + this.consumeItem +
            ", costConfig=" + this.costFunction +
            '}';
    }
}
