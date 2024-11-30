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
import de.mschae23.grindenchantments.config.FilterAction;
import de.mschae23.grindenchantments.config.FilterConfig;

@Deprecated
public record FilterConfigV3(boolean enabled, ItemConfig item, EnchantmentConfig enchantment, FilterAction curses) {
    public static final Codec<FilterConfigV3> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(FilterConfigV3::enabled),
        ItemConfig.CODEC.fieldOf("item").forGetter(FilterConfigV3::item),
        EnchantmentConfig.CODEC.fieldOf("enchantment").forGetter(FilterConfigV3::enchantment),
        FilterAction.CODEC.fieldOf("cursed_enchantments").forGetter(FilterConfigV3::curses)
    ).apply(instance, instance.stable(FilterConfigV3::new)));

    public static final FilterConfigV3 DEFAULT = new FilterConfigV3(true, ItemConfig.DEFAULT, EnchantmentConfig.DEFAULT, FilterAction.IGNORE);

    public FilterConfig latest() {
        return new FilterConfig(this.enabled, new FilterConfig.ItemConfig(this.item.items, this.item.action),
            new FilterConfig.EnchantmentConfig(this.enchantment.enchantments, this.enchantment.action), this.curses);
    }

    public record ItemConfig(List<Identifier> items, FilterAction action) {
        public static final Codec<ItemConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.listOrSingle(Identifier.CODEC).fieldOf("enchantments").forGetter(ItemConfig::items),
            FilterAction.NON_IGNORE_CODEC.fieldOf("action").forGetter(ItemConfig::action)
        ).apply(instance, instance.stable(ItemConfig::new)));

        public static final ItemConfig DEFAULT = new ItemConfig(List.of(), FilterAction.DENY);
    }

    public record EnchantmentConfig(List<Identifier> enchantments, FilterAction action) {
        public static final Codec<EnchantmentConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.listOrSingle(Identifier.CODEC).fieldOf("enchantments").forGetter(EnchantmentConfig::enchantments),
            FilterAction.CODEC.fieldOf("action").forGetter(EnchantmentConfig::action)
        ).apply(instance, instance.stable(EnchantmentConfig::new)));

        public static final EnchantmentConfig DEFAULT = new EnchantmentConfig(List.of(), FilterAction.IGNORE);
    }
}
