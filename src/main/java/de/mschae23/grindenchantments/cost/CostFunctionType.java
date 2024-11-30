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

import java.util.function.Function;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import com.mojang.serialization.MapCodec;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import io.netty.buffer.ByteBuf;

public interface CostFunctionType<M extends CostFunction> {
    CostFunctionType<CountEnchantmentsCostFunction> COUNT_ENCHANTMENTS = register("count_enchantments", CountEnchantmentsCostFunction.TYPE);
    CostFunctionType<CountLevelsCostFunction> COUNT_LEVELS = register("count_levels", CountLevelsCostFunction.TYPE);
    CostFunctionType<CountMinPowerCostFunction> COUNT_MIN_POWER = register("count_min_power", CountMinPowerCostFunction.TYPE);
    CostFunctionType<AverageCountCostFunction> AVERAGE_COUNT = register("average_count", AverageCountCostFunction.TYPE);
    CostFunctionType<FirstEnchantmentCostFunction> FIRST_ENCHANTMENT = register("first_enchantment", FirstEnchantmentCostFunction.TYPE);
    CostFunctionType<TransformCostFunction> TRANSFORM = register("transform", TransformCostFunction.TYPE);
    CostFunctionType<FilterCostFunction> FILTER = register("filter", FilterCostFunction.TYPE);

    MapCodec<M> codec();
    PacketCodec<PacketByteBuf, M> packetCodec(PacketCodec<PacketByteBuf, CostFunction> delegateCodec);

    static <M extends CostFunction> CostFunctionType<M> register(String id, CostFunctionType<M> type) {
        return Registry.register(GrindEnchantmentsRegistries.COST_FUNCTION, GrindEnchantmentsMod.id(id), type);
    }

    static PacketCodec<ByteBuf, CostFunctionType<?>> createPacketCodec() {
        return RegistryKey.createPacketCodec(GrindEnchantmentsRegistries.COST_FUNCTION_KEY).xmap(
            key -> GrindEnchantmentsRegistries.COST_FUNCTION.getOptionalValue(key).orElseThrow(
                () -> new IllegalStateException("Can't decode '" + key.getValue() + "', unregistered value")),
            type -> GrindEnchantmentsRegistries.COST_FUNCTION.getKey(type).orElseThrow(
                () -> new IllegalStateException("Can't encode '" + type + "', unregistered value"))
        );
    }

    static void init() {
    }

    record Impl<M extends CostFunction>(MapCodec<M> codec, Function<PacketCodec<PacketByteBuf, CostFunction>, PacketCodec<PacketByteBuf, M>> packetCodec) implements CostFunctionType<M> {
        @Override
        public MapCodec<M> codec() {
            return this.codec;
        }

        @Override
        public PacketCodec<PacketByteBuf, M> packetCodec(PacketCodec<PacketByteBuf, CostFunction> delegateCodec) {
            return this.packetCodec.apply(delegateCodec);
        }
    }
}
