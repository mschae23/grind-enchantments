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

package de.mschae23.grindenchantments.cost;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.EnchantmentTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.config.FilterConfig;

public record CountLevelsCostFunction(double normalFactor, double treasureFactor) implements CostFunction {
    public static final MapCodec<CountLevelsCostFunction> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.DOUBLE.fieldOf("normal_factor").forGetter(CountLevelsCostFunction::normalFactor),
        Codec.DOUBLE.fieldOf("treasure_factor").forGetter(CountLevelsCostFunction::treasureFactor)
    ).apply(instance, instance.stable(CountLevelsCostFunction::new)));
    public static final CostFunctionType<CountLevelsCostFunction> TYPE = new CostFunctionType.Impl<>(TYPE_CODEC, CountLevelsCostFunction::packetCodec);

    @Override
    public double getCost(ItemEnchantmentsComponent enchantments, FilterConfig filter, RegistryWrapper.WrapperLookup wrapperLookup) {
        return enchantments.getEnchantmentEntries().stream()
            .mapToDouble(entry -> (double) entry.getIntValue() * (entry.getKey().isIn(EnchantmentTags.TREASURE) ? this.treasureFactor : this.normalFactor))
            .sum();
    }

    @Override
    public CostFunctionType<?> getType() {
        return CostFunctionType.COUNT_LEVELS;
    }

    public static PacketCodec<PacketByteBuf, CountLevelsCostFunction> packetCodec(PacketCodec<PacketByteBuf, CostFunction> delegateCodec) {
        return PacketCodec.tuple(PacketCodecs.DOUBLE, CountLevelsCostFunction::normalFactor, PacketCodecs.DOUBLE, CountLevelsCostFunction::treasureFactor, CountLevelsCostFunction::new);
    }

    @Override
    public String toString() {
        return "CountLevelsCostFunction{" +
            "normalFactor=" + this.normalFactor +
            ", treasureFactor=" + this.treasureFactor +
            '}';
    }
}
