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

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EnchantmentTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FilterConfig(boolean enabled, ItemConfig item, EnchantmentConfig enchantment, FilterAction curses) {
    public static final Codec<FilterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(FilterConfig::enabled),
        ItemConfig.CODEC.fieldOf("item").forGetter(FilterConfig::item),
        EnchantmentConfig.CODEC.fieldOf("enchantment").forGetter(FilterConfig::enchantment),
        FilterAction.CODEC.fieldOf("cursed_enchantments").forGetter(FilterConfig::curses)
    ).apply(instance, instance.stable(FilterConfig::new)));

    public static final FilterConfig DEFAULT = new FilterConfig(true, ItemConfig.DEFAULT, EnchantmentConfig.DEFAULT, FilterAction.IGNORE);

    public ItemEnchantmentsComponent filter(ItemEnchantmentsComponent enchantments) {
        if (!this.enabled) {
            return enchantments;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);

        if (this.curses == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (entry.isIn(EnchantmentTags.CURSE)) {
                    return ItemEnchantmentsComponent.DEFAULT;
                }
            }
        }

        if (this.enchantment.action == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (this.enchantment.enchantments.contains(entry)) {
                    return ItemEnchantmentsComponent.DEFAULT;
                }
            }
        }

        builder.remove(enchantment ->
            ((this.curses == FilterAction.IGNORE) && enchantment.isIn(EnchantmentTags.CURSE))
            || ((this.enchantment.action == FilterAction.IGNORE) == this.enchantment.enchantments.contains(enchantment)));

        return builder.build();
    }

    public ItemEnchantmentsComponent filterReversed(ItemEnchantmentsComponent enchantments) {
        if (!this.enabled) {
            return ItemEnchantmentsComponent.DEFAULT;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);

        if (this.curses == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (entry.isIn(EnchantmentTags.CURSE)) {
                    return ItemEnchantmentsComponent.DEFAULT;
                }
            }
        }

        if (this.enchantment.action == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (this.enchantment.enchantments.contains(entry)) {
                    return ItemEnchantmentsComponent.DEFAULT;
                }
            }
        }

        builder.remove(enchantment ->
            ((this.curses == FilterAction.ALLOW) || !enchantment.isIn(EnchantmentTags.CURSE))
            && ((this.enchantment.action == FilterAction.ALLOW) == this.enchantment.enchantments.contains(enchantment)));

        return builder.build();
    }

    public record ItemConfig(RegistryEntryList<Item> items, FilterAction action) {
        public static final Codec<ItemConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.ITEM).fieldOf("enchantments").forGetter(ItemConfig::items),
            FilterAction.NON_IGNORE_CODEC.fieldOf("action").forGetter(ItemConfig::action)
        ).apply(instance, instance.stable(ItemConfig::new)));

        public static final ItemConfig DEFAULT = new ItemConfig(RegistryEntryList.empty(), FilterAction.DENY);
    }

    public record EnchantmentConfig(RegistryEntryList<Enchantment> enchantments, FilterAction action) {
        public static final Codec<EnchantmentConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.entryList(RegistryKeys.ENCHANTMENT).fieldOf("enchantments").forGetter(EnchantmentConfig::enchantments),
            FilterAction.CODEC.fieldOf("action").forGetter(EnchantmentConfig::action)
        ).apply(instance, instance.stable(EnchantmentConfig::new)));

        public static final EnchantmentConfig DEFAULT = new EnchantmentConfig(RegistryEntryList.empty(), FilterAction.IGNORE);
    }
}
