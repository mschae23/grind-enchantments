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

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;
import org.apache.logging.log4j.Level;

public record FilterConfig(boolean enabled, ItemConfig item, EnchantmentConfig enchantment, FilterAction curses) {
    public static final Codec<FilterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("enabled").forGetter(FilterConfig::enabled),
        ItemConfig.CODEC.fieldOf("item").forGetter(FilterConfig::item),
        EnchantmentConfig.CODEC.fieldOf("enchantment").forGetter(FilterConfig::enchantment),
        FilterAction.CODEC.fieldOf("cursed_enchantments").forGetter(FilterConfig::curses)
    ).apply(instance, instance.stable(FilterConfig::new)));

    public static final FilterConfig DEFAULT = new FilterConfig(true, ItemConfig.DEFAULT, EnchantmentConfig.DEFAULT, FilterAction.IGNORE);
    public static final FilterConfig DISABLED = new FilterConfig(false, ItemConfig.DEFAULT, EnchantmentConfig.DEFAULT, FilterAction.IGNORE);

    public static PacketCodec<PacketByteBuf, FilterConfig> createPacketCodec() {
        return PacketCodec.tuple(
            PacketCodecs.BOOLEAN, FilterConfig::enabled,
            ItemConfig.createPacketCodec(), FilterConfig::item,
            EnchantmentConfig.createPacketCodec(), FilterConfig::enchantment,
            FilterAction.PACKET_CODEC, FilterConfig::curses,
            FilterConfig::new
        );
    }

    private boolean shouldDeny(ItemEnchantmentsComponent.Builder builder) {
        if (this.curses == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (entry.isIn(EnchantmentTags.CURSE)) {
                    return true;
                }
            }
        }

        if (this.enchantment.action == FilterAction.DENY) {
            for (RegistryEntry<Enchantment> entry : builder.getEnchantments()) {
                if (entry.getKey().map(key -> this.enchantment.enchantments.contains(key.getValue())).orElse(false)) {
                    return true;
                }
            }
        }

        return false;
    }

    public ItemEnchantmentsComponent filter(ItemEnchantmentsComponent enchantments) {
        if (!this.enabled) {
            return enchantments;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);

        if (this.shouldDeny(builder)) {
            return ItemEnchantmentsComponent.DEFAULT;
        }

        builder.remove(enchantment ->
            ((this.curses == FilterAction.IGNORE) && enchantment.isIn(EnchantmentTags.CURSE))
            || ((this.enchantment.action == FilterAction.IGNORE) == enchantment.getKey().map(key ->
                this.enchantment.enchantments.contains(key.getValue())).orElse(false)));

        return builder.build();
    }

    public ItemEnchantmentsComponent filterReversed(ItemEnchantmentsComponent enchantments) {
        if (!this.enabled) {
            return ItemEnchantmentsComponent.DEFAULT;
        }

        ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(enchantments);

        if (this.shouldDeny(builder)) {
            return ItemEnchantmentsComponent.DEFAULT;
        }

        builder.remove(enchantment ->
            ((this.curses == FilterAction.ALLOW) || !enchantment.isIn(EnchantmentTags.CURSE))
            && ((this.enchantment.action == FilterAction.ALLOW) == enchantment.getKey().map(key ->
                this.enchantment.enchantments.contains(key.getValue())).orElse(false)));

        return builder.build();
    }

    public void validateRegistryEntries(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.item.validateRegistryEntries(wrapperLookup);
        this.enchantment.validateRegistryEntries(wrapperLookup);
    }

    @Override
    public String toString() {
        return "FilterConfig{" +
            "enabled=" + this.enabled +
            ", item=" + this.item +
            ", enchantment=" + this.enchantment +
            ", curses=" + this.curses +
            '}';
    }

    public record ItemConfig(List<Identifier> items, FilterAction action) {
        public static final Codec<ItemConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.listOrSingle(Identifier.CODEC).fieldOf("enchantments").forGetter(ItemConfig::items),
            FilterAction.NON_IGNORE_CODEC.fieldOf("action").forGetter(ItemConfig::action)
        ).apply(instance, instance.stable(ItemConfig::new)));

        public static final ItemConfig DEFAULT = new ItemConfig(List.of(), FilterAction.DENY);

        public static PacketCodec<PacketByteBuf, ItemConfig> createPacketCodec() {
            return PacketCodec.tuple(
                Identifier.PACKET_CODEC.collect(PacketCodecs.toList()), ItemConfig::items,
                FilterAction.PACKET_CODEC, ItemConfig::action,
                ItemConfig::new
            );
        }

        public void validateRegistryEntries(RegistryWrapper.WrapperLookup wrapperLookup) {
            Optional<? extends RegistryWrapper.Impl<Item>> registryWrapperOpt = wrapperLookup.getOptional(RegistryKeys.ITEM);

            if (registryWrapperOpt.isEmpty()) {
                GrindEnchantmentsMod.log(Level.WARN, "Item registry is not present");
                return;
            }

            RegistryWrapper.Impl<Item> registryWrapper = registryWrapperOpt.get();

            this.items.stream()
                .map(item -> Pair.of(item, registryWrapper.getOptional(RegistryKey.of(RegistryKeys.ITEM, item))))
                .flatMap(result -> result.getSecond().isEmpty() ? Stream.of(result.getFirst()) : Stream.empty())
                .map(Identifier::toString)
                .forEach(item -> GrindEnchantmentsMod.log(Level.WARN, "Filter config contains unknown item: " + item));
        }

        @Override
        public String toString() {
            return "ItemConfig{" +
                "items=" + this.items +
                ", action=" + this.action +
                '}';
        }
    }

    public record EnchantmentConfig(List<Identifier> enchantments, FilterAction action) {
        public static final Codec<EnchantmentConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codecs.listOrSingle(Identifier.CODEC).fieldOf("enchantments").forGetter(EnchantmentConfig::enchantments),
            FilterAction.CODEC.fieldOf("action").forGetter(EnchantmentConfig::action)
        ).apply(instance, instance.stable(EnchantmentConfig::new)));

        public static final EnchantmentConfig DEFAULT = new EnchantmentConfig(List.of(), FilterAction.IGNORE);

        public static PacketCodec<PacketByteBuf, EnchantmentConfig> createPacketCodec() {
            return PacketCodec.tuple(
                Identifier.PACKET_CODEC.collect(PacketCodecs.toList()), EnchantmentConfig::enchantments,
                FilterAction.PACKET_CODEC, EnchantmentConfig::action,
                EnchantmentConfig::new
            );
        }

        public void validateRegistryEntries(RegistryWrapper.WrapperLookup wrapperLookup) {
            Optional<? extends RegistryWrapper.Impl<Enchantment>> registryWrapperOpt = wrapperLookup.getOptional(RegistryKeys.ENCHANTMENT);

            if (registryWrapperOpt.isEmpty()) {
                GrindEnchantmentsMod.log(Level.WARN, "Enchantment registry is not present");
                return;
            }

            RegistryWrapper.Impl<Enchantment> registryWrapper = registryWrapperOpt.get();

            this.enchantments.stream()
                .map(enchantment -> Pair.of(enchantment, registryWrapper.getOptional(RegistryKey.of(RegistryKeys.ENCHANTMENT, enchantment))))
                .flatMap(result -> result.getSecond().isEmpty() ? Stream.of(result.getFirst()) : Stream.empty())
                .map(Identifier::toString)
                .forEach(item -> GrindEnchantmentsMod.log(Level.WARN, "Filter config contains unknown enchantment: " + item));
        }

        @Override
        public String toString() {
            return "EnchantmentConfig{" +
                "enchantments=" + this.enchantments +
                ", action=" + this.action +
                '}';
        }
    }
}
