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

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;
import de.mschae23.grindenchantments.GrindEnchantmentsMod;

public record ClientConfig(boolean showLevelCost, boolean useLocalIfUnsynced) implements ModConfig<ClientConfig> {
    public static final MapCodec<ClientConfig> TYPE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BOOL.fieldOf("show_enchantment_cost").forGetter(ClientConfig::showLevelCost),
        Codec.BOOL.fieldOf("use_local_server_config_if_unsynced").forGetter(ClientConfig::useLocalIfUnsynced)
    ).apply(instance, instance.stable(ClientConfig::new)));

    public static final ModConfig.Type<ClientConfig, ClientConfig> TYPE = new ModConfig.Type<>(4, TYPE_CODEC);
    public static final ClientConfig DEFAULT = new ClientConfig(true, true);
    @SuppressWarnings("unchecked")
    public static final ModConfig.Type<ClientConfig, ? extends ModConfig<ClientConfig>>[] VERSIONS = new ModConfig.Type[] { TYPE, };

    public static final Codec<ModConfig<ClientConfig>> CODEC = ModConfig.createCodec(TYPE.version(), version ->
        GrindEnchantmentsMod.getConfigType(VERSIONS, version));

    @Override
    public Type<ClientConfig, ?> type() {
        return TYPE;
    }

    @Override
    public ClientConfig latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public String toString() {
        return "ClientConfig{" +
            "showLevelCost=" + this.showLevelCost +
            ", useLocalIfUnsynced=" + this.useLocalIfUnsynced +
            '}';
    }
}
