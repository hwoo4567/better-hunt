package com.worldedit1234.hunt.drop;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/* 도구 (검, 곡괭이) : 10%의 확률로 떨굼, 내구도 대폭 감소
 * 자원 (다이아몬드, 철 ...) : 10 ~ 40%를 떨굼
 * 갑옷 : 도구와 같음
 * 설커상자는 확정적으로 떨굼
 * 식량은 자원과 같음
 */

public class Drop {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();
    private static final List<Item> MINERAL_SOURCES = Arrays.asList(
            Items.NETHERITE_INGOT,  // 네더라이트 주괴
            Items.NETHERITE_BLOCK,  // 네더라이트 블록
            Items.DIAMOND,          // 다이아몬드
            Items.DIAMOND_BLOCK,    // 다이아몬드 블록
            Items.GOLD_INGOT,       // 금 주괴
            Items.GOLD_BLOCK,       // 금 블록
            Items.IRON_INGOT,       // 철 주괴
            Items.IRON_BLOCK,       // 철 블록
            Items.COAL,             // 석탄
            Items.COAL_BLOCK,       // 석탄 블록
            Items.EMERALD,          // 에메랄드
            Items.EMERALD_BLOCK,    // 에메랄드 블록
            Items.REDSTONE,         // 레드스톤
            Items.REDSTONE_BLOCK,   // 레드스톤 블록
            Items.LAPIS_LAZULI,     // 청금석
            Items.LAPIS_BLOCK       // 청금석 블록
    );

    public static void dropItem(Player player, ItemStack itemStack) {
        var item = itemStack.getItem();
        var tab = item.getItemCategory();

        if (MINERAL_SOURCES.contains(item)) {
            dropItem(player, itemStack, DropType.SOURCE);         // 광물

        } else if (tab == CreativeModeTab.TAB_COMBAT || tab == CreativeModeTab.TAB_TOOLS) {
            if (item.isDamageable(item.getDefaultInstance())) {
                dropItem(player, itemStack, DropType.TOOL);       // 도구
            } else {
                dropItem(player, itemStack, DropType.SOURCE);     // 화살, 포션 등
            }
        } else if (tab == CreativeModeTab.TAB_FOOD) {
            dropItem(player, itemStack, DropType.SOURCE);         // 음식

        } else if (Block.byItem(item) instanceof ShulkerBoxBlock) {
            dropItem(player, itemStack, DropType.ALL);            // 설커 상자
        }
    }

    public static void dropItem(Player player, ItemStack itemStack, DropType type) {
        switch (type) {
            case SOURCE -> dropSource(player, itemStack);
            case TOOL -> dropTool(player, itemStack);
            case ALL -> dropAll(player, itemStack);
        }
    }

    private static void dropSource(Player player, ItemStack itemStack) {
        // Drops "Source" type items
        // 10 ~ 40% of itemStacks are lost

        int size = itemStack.getCount();
        int dropRatio = getRandom(10, 40);  // 10 ~ 40 (%)
        int dropSize = size * dropRatio / 100;  // count * ratio %

        itemStack.setCount(size - dropSize);

        var dropItem = itemStack.copy();
        dropItem.setCount(dropSize);
        spawnItemEntity(player, dropItem);

        LOGGER.info("Source Drop: {} drops {}% of item {}.",
                player.getName().getString(), dropRatio, itemStack.getDisplayName().getString());
    }

    private static void dropTool(Player player, ItemStack itemStack) {
        // Drops "Tool" type items
        // Drop: 10%
        // Damage: 60% (10% ~ 70% damage)
        // Tool item must be damageable

        if (!itemStack.getItem().isDamageable(itemStack)) {
            LOGGER.warn("dropTool is called when the item is not damageable.");
            return;
        }

        int rand = getRandom(1, 100);

        if (rand <= 10) {  // 10%
            spawnItemEntity(player, itemStack.copy());
            itemStack.setCount(0);
            LOGGER.info("Tool Drop: {} drops item {}.",
                    player.getName().getString(), itemStack.getDisplayName().getString());
        }
        else if (rand <= 70) {  // 60%
            int damageRatio = getRandom(10, 70);  // 10% ~ 70% damage
            int damage = itemStack.getDamageValue();  // 아이템이 원래 입은 데미지
            int total = itemStack.getMaxDamage() - damage;  // 남은 내구도
            itemStack.setDamageValue(damage + (total * damageRatio / 100));

            LOGGER.info("Tool Damage: {}'s item {} gets {}% damage.",
                    player.getName().getString(), itemStack.getDisplayName().getString(), damageRatio);
        }
    }

    private static void dropAll(Player player, ItemStack itemStack) {
        spawnItemEntity(player, itemStack.copy());
        itemStack.setCount(0);
    }

    private static void spawnItemEntity(Player player, ItemStack itemStack) {
        var level = player.getLevel();
        level.addFreshEntity(new ItemEntity(
                level,
                player.getX(),
                player.getY() - 0.5D,
                player.getZ(),
                itemStack
        ));
    }

    private static int getRandom(int from, int to) {
        return RANDOM.nextInt(to) + from + 1;
    }
}
