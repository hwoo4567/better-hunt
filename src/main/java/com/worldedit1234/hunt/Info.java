package com.worldedit1234.hunt;

import java.util.ArrayList;
import java.util.Arrays;

public class Info {
    public static final String description =
            "This mod is for better survival PVP play.\nThere are several extra recipes and tools.\nAnd it provides optimum game system for competition.\nEnjoy :)";

    public static final ArrayList<String> recipes = new ArrayList<>(Arrays.asList(
            "apple * 2: apple + bone_meal",
            "golden_apple: apple + gold_ingot * 4",
            "potion haste: golden_pickaxe + glass_bottle",
            "potion instant_health: gold_ingot + redstone + glass_bottle",
            "potion speed: gold_ingot + sugar + glass_bottle"
    ));
}
