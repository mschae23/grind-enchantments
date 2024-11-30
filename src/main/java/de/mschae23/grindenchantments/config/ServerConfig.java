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

import net.minecraft.registry.RegistryWrapper;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.mschae23.config.api.ModConfig;

public record ServerConfig(DisenchantConfig disenchant, MoveConfig move, ResetRepairCostConfig resetRepairCost,
                           FilterConfig filter,
                           DedicatedServerConfig dedicatedServerConfig) implements ModConfig<ServerConfig> {
    public static final MapCodec<ServerConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        DisenchantConfig.CODEC.fieldOf("disenchant_to_book").forGetter(ServerConfig::disenchant),
        MoveConfig.CODEC.fieldOf("move_enchantments").forGetter(ServerConfig::move),
        ResetRepairCostConfig.CODEC.fieldOf("reset_repair_cost").forGetter(ServerConfig::resetRepairCost),
        FilterConfig.CODEC.fieldOf("filter").forGetter(ServerConfig::filter),
        DedicatedServerConfig.CODEC.orElse(DedicatedServerConfig.DEFAULT).fieldOf("dedicated_server_options").forGetter(ServerConfig::dedicatedServerConfig)
        ).apply(instance, instance.stable(ServerConfig::new)));

    public static final ModConfig.Type<ServerConfig, ServerConfig> TYPE = new ModConfig.Type<>(4, CODEC);
    public static final ServerConfig DEFAULT = new ServerConfig(DisenchantConfig.DEFAULT, MoveConfig.DEFAULT,
        ResetRepairCostConfig.DEFAULT, FilterConfig.DEFAULT, DedicatedServerConfig.DEFAULT);

    @SuppressWarnings("unchecked")
    public static final ModConfig.Type<ServerConfig, ? extends ModConfig<ServerConfig>>[] VERSIONS = new ModConfig.Type[] { TYPE, };

    @Override
    public Type<ServerConfig, ?> type() {
        return TYPE;
    }

    @Override
    public ServerConfig latest() {
        return this;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    public void validateRegistryEntries(RegistryWrapper.WrapperLookup wrapperLookup) {
        this.filter.validateRegistryEntries(wrapperLookup);
        this.resetRepairCost.validateRegistryEntries(wrapperLookup);
    }
}
