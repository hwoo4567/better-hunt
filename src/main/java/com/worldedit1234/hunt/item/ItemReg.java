package com.worldedit1234.hunt.item;

import com.mojang.logging.LogUtils;
import com.worldedit1234.hunt.BetterHunt;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

public class ItemReg {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BetterHunt.MOD_ID);

    public static final RegistryObject<Item> LIGHT_APPLE = ITEMS.register(LightApple.NAME, LightApple::new);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        LOGGER.info("Registering items.");
    }
}
