package com.worldedit1234.hunt;

import com.mojang.logging.LogUtils;
import com.worldedit1234.hunt.item.ItemReg;
import com.worldedit1234.hunt.setup.Setup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// Main Forge mod class
@Mod(BetterHunt.MOD_ID)
public class BetterHunt {
    public static final String MOD_ID = "hunt";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BetterHunt() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ItemReg.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LOGGER.info("Server Started...");
        Setup.server(event.getServer());
    }
}