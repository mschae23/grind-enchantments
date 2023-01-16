package de.martenschaefer.grindenchantments;

import java.nio.file.Path;
import java.nio.file.Paths;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.Identifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import de.martenschaefer.config.api.ConfigIo;
import de.martenschaefer.config.api.ModConfig;
import de.martenschaefer.grindenchantments.config.GrindEnchantmentsV2Config;
import de.martenschaefer.grindenchantments.config.v1.GrindEnchantmentsV1Config;
import de.martenschaefer.grindenchantments.cost.CostFunctionType;
import de.martenschaefer.grindenchantments.event.ApplyLevelCostEvent;
import de.martenschaefer.grindenchantments.event.GrindstoneEvents;
import de.martenschaefer.grindenchantments.impl.DisenchantOperation;
import de.martenschaefer.grindenchantments.impl.MoveOperation;
import de.martenschaefer.grindenchantments.registry.GrindEnchantmentsRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GrindEnchantmentsMod implements ModInitializer {
    public static final String MODID = "grindenchantments";
    public static final Logger LOGGER = LogManager.getLogger("Grind Enchantments");

    public static final Path CONFIG_PATH = Paths.get(MODID + ".json");

    private static final GrindEnchantmentsV2Config LATEST_CONFIG_DEFAULT = GrindEnchantmentsV2Config.DEFAULT;
    private static final int LATEST_CONFIG_VERSION = LATEST_CONFIG_DEFAULT.version();
    private static final Codec<ModConfig<GrindEnchantmentsV2Config>> CONFIG_CODEC = ModConfig.createCodec(LATEST_CONFIG_VERSION, GrindEnchantmentsMod::getConfigType);

    private static GrindEnchantmentsV2Config CONFIG = LATEST_CONFIG_DEFAULT;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server ->
            CONFIG = ConfigIo.initializeConfig(Paths.get(MODID + ".json"), LATEST_CONFIG_VERSION, LATEST_CONFIG_DEFAULT, CONFIG_CODEC,
                RegistryOps.of(JsonOps.INSTANCE, server.getRegistryManager()), LOGGER::info, LOGGER::error)
        );

        GrindEnchantmentsRegistries.init();
        CostFunctionType.init();

        DisenchantOperation disenchant = new DisenchantOperation();
        MoveOperation move = new MoveOperation();

        GrindstoneEvents.registerAll(disenchant);
        GrindstoneEvents.registerAll(move);

        ApplyLevelCostEvent.EVENT.register(ApplyLevelCostEvent.DEFAULT, (cost, player) -> {
            player.addExperienceLevels(-cost);
            return true;
        });

        // Mod compatibility with Tax Free Levels
        /* if (FabricLoader.getInstance().isModLoaded("taxfreelevels")) {
            ApplyLevelCostEvent.EVENT.register(ApplyLevelCostEvent.MOD_COMPATIBILITY, (cost, player) -> {
                TaxFreeLevels.applyFlattenedXpCost(player, cost);
                return true;
            });
        } */
    }

    @SuppressWarnings("deprecation")
    private static ModConfig.Type<GrindEnchantmentsV2Config, ?> getConfigType(int version) {
        //noinspection SwitchStatementWithTooFewBranches
        return new ModConfig.Type<>(version, switch (version) {
            case 1 -> GrindEnchantmentsV1Config.TYPE_CODEC;
            default -> GrindEnchantmentsV2Config.TYPE_CODEC;
        });
    }

    public static GrindEnchantmentsV2Config getConfig() {
        return CONFIG;
    }

    public static void log(Level level, Object message) {
        LOGGER.log(level, "[Grind Enchantments] " + message);
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
