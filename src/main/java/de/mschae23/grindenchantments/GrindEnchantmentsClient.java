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

package de.mschae23.grindenchantments;

import java.util.stream.Stream;
import net.minecraft.registry.RegistryWrapper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import de.mschae23.grindenchantments.config.ClientConfig;
import de.mschae23.grindenchantments.config.sync.ServerConfigS2CPayload;
import de.mschae23.grindenchantments.registry.GrindEnchantmentsRegistries;
import org.apache.logging.log4j.Level;

public class GrindEnchantmentsClient implements ClientModInitializer {
    private static ClientConfig CLIENT_CONFIG = ClientConfig.DEFAULT;

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            CLIENT_CONFIG = GrindEnchantmentsMod.initializeClientConfig();
            GrindEnchantmentsMod.LOCAL_SERVER_CONFIG = GrindEnchantmentsMod.initializeServerConfig(RegistryWrapper.WrapperLookup.of(
                Stream.of(GrindEnchantmentsRegistries.COST_FUNCTION)));

            ClientConfigurationNetworking.registerGlobalReceiver(ServerConfigS2CPayload.ID, (payload, context) -> {
                //noinspection resource
                context.client().execute(() -> {
                    GrindEnchantmentsMod.SERVER_CONFIG = payload.config();

                    if (CLIENT_CONFIG.sync().logReceivedConfig()) {
                        GrindEnchantmentsMod.log(Level.INFO, "Received server config: " + GrindEnchantmentsMod.SERVER_CONFIG);
                    }
                });
            });
        });

        ClientPlayConnectionEvents.INIT.register((handler, client2) -> {
            if (GrindEnchantmentsMod.SERVER_CONFIG != null) {
                GrindEnchantmentsMod.SERVER_CONFIG.validateRegistryEntries(handler.getRegistryManager());
            }
        });

        // Set server config to null when joining a world, so that it is known whether the server sent its config
        ClientConfigurationConnectionEvents.INIT.register((handler, client2) -> GrindEnchantmentsMod.SERVER_CONFIG = null);
        // Set server config to null when leaving the world too, for the same reason
        ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client2) -> GrindEnchantmentsMod.SERVER_CONFIG = null);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client2) -> GrindEnchantmentsMod.SERVER_CONFIG = null);
    }

    public static ClientConfig getClientConfig() {
        return CLIENT_CONFIG;
    }
}
