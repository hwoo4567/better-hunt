package com.worldedit1234.hunt;

import com.mojang.logging.LogUtils;
import com.worldedit1234.hunt.command.Position;
import com.worldedit1234.hunt.command.CommandRegistry;
import com.worldedit1234.hunt.drop.Drop;
import com.worldedit1234.hunt.entity.OwnedFireball;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import net.minecraftforge.server.command.ConfigCommand;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = BetterHunt.MOD_ID)
public class EventHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

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
        var position = Position.getPosition();

        position.showPositionAll(server);
    }

    @SubscribeEvent
    public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        // y좌표 20 ~ 120 안에 있지 않으면 허기
        if (event.side.isClient()) {
            return;
        }

        var player = event.player;
        var y = player.getY();
        if (y < 20 || y > 120) {
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
