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
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClientSyncConfig(boolean useLocalIfUnsynced, boolean logReceivedConfig) {
    public static final Codec<ClientSyncConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.fieldOf("use_local_server_config_if_unsynced").forGetter(ClientSyncConfig::useLocalIfUnsynced),
        Codec.BOOL.fieldOf("log_received_config").forGetter(ClientSyncConfig::logReceivedConfig)
    ).apply(instance, instance.stable(ClientSyncConfig::new)));

    public static final ClientSyncConfig DEFAULT = new ClientSyncConfig(true, false);

    @Override
    public String toString() {
        return "ClientSyncConfig{" +
            "useLocalIfUnsynced=" + this.useLocalIfUnsynced +
            ", logReceivedConfig=" + this.logReceivedConfig +
            '}';
    }
}
