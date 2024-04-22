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

package de.mschae23.grindenchantments.item;

import net.minecraft.component.DataComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record GrindEnchantmentsDataComponent(int cost, boolean canTake) {
    public static final Codec<GrindEnchantmentsDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("cost").forGetter(GrindEnchantmentsDataComponent::cost),
        Codec.BOOL.fieldOf("can_take").forGetter(GrindEnchantmentsDataComponent::canTake)
    ).apply(instance, instance.stable(GrindEnchantmentsDataComponent::new)));

    public static final DataComponentType<GrindEnchantmentsDataComponent> TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE,
        GrindEnchantmentsMod.id("grind_enchantments"), DataComponentType.<GrindEnchantmentsDataComponent>builder().codec(CODEC).build());

    public static void init() {
    }
}
