package com.worldedit1234.hunt;

import com.mojang.logging.LogUtils;
import com.worldedit1234.hunt.command.Actionbar;
import com.worldedit1234.hunt.command.CommandRegistry;
import com.worldedit1234.hunt.drop.Drop;
import com.worldedit1234.hunt.entity.OwnedFireball;
import com.worldedit1234.hunt.setup.Setup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.minecraftforge.server.command.ConfigCommand;
import org.slf4j.Logger;

import java.util.Random;

@Mod.EventBusSubscriber(modid = BetterHunt.MOD_ID)
public class EventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        new CommandRegistry(dispatcher);
        ConfigCommand.register(dispatcher);
        LOGGER.info("Registering commands");
    }

    @SubscribeEvent
    public static void onServerTickEvent(TickEvent.ServerTickEvent event) {
        if (event.side.isClient()) {
            return;
        }

        var server = event.getServer();
        var actionbar = Actionbar.getActionbar();
        actionbar.tickTimer();
        actionbar.sendResultAll(server);
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient()) {
            return;
        }

        var player = event.player;
        var y = player.getY();
        if (y < Setup.HUNGER_FREE_RANGE[0] || y > Setup.HUNGER_FREE_RANGE[1]) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER,
                    0, Setup.OUTDOOR_HUNGER_LEVEL, false, false));
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getSide().isClient()) {
            return;
        }

        var level = event.getLevel();
        var player = event.getEntity();
        var itemStack = event.getItemStack();
        var count = itemStack.getCount();

        // 불쏘시개
        if (itemStack.is(Items.FIRE_CHARGE)  && count > 0) {
            Vec3 aim = player.getLookAngle();
            Vec3 pos = player.position();
            OwnedFireball fireball = new OwnedFireball(level, player, aim);
            fireball.setPos(pos.x, pos.y + 1.0D, pos.z);
            level.addFreshEntity(fireball);  // spawn
            LOGGER.info("{} spawns fireball {}",
                    fireball.getOwner().getName().getString(), fireball.getUUID());

            if (!player.isCreative()) {
                itemStack.setCount(count - 1);
            }
        }
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        Level level = (Level) event.getLevel();
        var pos = event.getPos();

        // 참나무 잎
        if (level.getBlockState(pos).is(Blocks.OAK_LEAVES)) {
            int rand = RANDOM.nextInt(100);
            if (rand < Setup.APPLE_DROP_CHANCE) {
                level.addFreshEntity(new ItemEntity(
                        level,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        Items.APPLE.getDefaultInstance()
                ));
                LOGGER.info("New Apple.");
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        var entity = event.getEntity();

        if (entity instanceof Player player) {
            LOGGER.info("Player is dead and drops items.");
            var inventory = player.getInventory();

            for (ItemStack i : inventory.items) {
                Drop.dropItem(player, i);
            }
            for (ItemStack i : inventory.armor) {
                Drop.dropItem(player, i);
            }
            for (ItemStack i : inventory.offhand) {
                Drop.dropItem(player, i);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamageEvent(LivingDamageEvent event) {
        if (event.getSource().isExplosion()) {  // 엔티티가 폭발 데미지를 입었을 때
            event.setAmount(event.getAmount() * Setup.EXPLOSION_DAMAGE);

            if (event.getEntity() instanceof Player player) {
                LOGGER.info("{} gets explosion damage.", player.getName().getString());
            }
        }
    }
}
