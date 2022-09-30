package com.worldedit1234.hunt.setup;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;

public class Setup {
    public static final int DEFAULT_BORDER_SIZE = 500;
    public static final double FIREBALL_SPEED = 1.4D;
    public static final int FIREBALL_POWER = 3;
    public static final float MAX_FIREBALL_TRAVEL = 200.0F;
    public static final float EXPLOSION_DAMAGE = 0.2F;
    public static final int OUTDOOR_HUNGER_LEVEL = 2;
    public static final int[] HUNGER_FREE_RANGE = {20, 120};

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void server(MinecraftServer server) {
        // Gamerules
        var gamerules = server.getGameRules();
        gamerules.getRule(GameRules.RULE_RANDOMTICKING).set(20, server);          // randomTickSpeed : 20
        gamerules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);        // keepInventory : true
        gamerules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);            // doDayLightCycle : false
        gamerules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);       // doWeatherCycle : false
        gamerules.getRule(GameRules.RULE_COMMANDBLOCKOUTPUT).set(false, server);  // commandBlockOutput: false
        LOGGER.info("Gamerule setup.");

        // World border
        worldBorder(server, DEFAULT_BORDER_SIZE);

        LOGGER.info("Server setup is finished.");
    }

    public static void worldBorder(MinecraftServer server, int size) {
        for (var i: server.getAllLevels()) {
            var border = i.getWorldBorder();
            var spawn = i.getSharedSpawnPos();
            border.setCenter(spawn.getX(), spawn.getZ());
            border.setSize(size);
        }

        LOGGER.info("World border setup.");
    }
}
