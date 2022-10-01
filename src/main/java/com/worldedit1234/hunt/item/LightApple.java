package com.worldedit1234.hunt.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

public class LightApple extends Item {
    public static final String NAME = "light_apple";

    // Items.GOLDEN_APPLE
    private static final FoodProperties FOOD = (new FoodProperties.Builder())
            .nutrition(4)
            .saturationMod(1.2F)
            // 재생1 4초
            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 80, 0), 1.0F)
            // 흡수1 30초
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 600, 0), 1.0F)
            .alwaysEat().build();

    public LightApple() {
        super((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).rarity(Rarity.RARE).food(FOOD));
    }
}
