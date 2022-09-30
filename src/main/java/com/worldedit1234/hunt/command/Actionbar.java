package com.worldedit1234.hunt.command;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.Array;
import java.sql.Time;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

// TODO: 화살표 방향 표시 구현하기

public class Actionbar {
    private static Actionbar actionbar = null;
    private final Random RANDOM = new Random();
    private PosType posDisplayType;
    private int timer;  // minecraft tick (= 0.05s)
    private boolean ticking;
    private long prevTick;

    private MinecraftServer server;
    private Level level;
    private BlockPos home;

    private Actionbar() {
        this.posDisplayType = PosType.HIDDEN;
        this.timer = 0;
        this.ticking = false;
        this.prevTick = 0L;
    }

    public static Actionbar getActionbar() {
        if (actionbar == null) {
            actionbar = new Actionbar();
        }
        return actionbar;
    }

    public PosType getPosType() {
        return this.posDisplayType;
    }
    public int setPosType(PosType posType) {
        this.posDisplayType = posType;
        return 1;
    }
    public int getTimerTick() {
        return this.timer;
    }

    public void startTimer(int second) {
        this.timer = second;
        this.ticking = true;
    }
    public int stopTimer() {
        this.ticking = false;
        return 1;
    }

    public void setHome(MinecraftServer server, Level level, BlockPos home) {
        this.server = server;
        this.level = level;
        this.home = home;
    }

    public String getTimerStr() {
        int seconds = this.timer % 60;
        int minutes = (this.timer / 60) % 60;
        int hours = this.timer / 3600;

        if (hours == 0) {
            if (minutes == 0) {
                return String.format("%02d", seconds);
            }
            return String.format("%02d:%02d", minutes, seconds);
        } else {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    private String getPosStr(Player receiver, @Nullable Player target, boolean direction) {
        if (target == null) {
            return "???";
        }
        return String.format(
                "%s%s : %d, %d, %d",
                target.getName().getString(),
                direction? String.format(" (%c)", getDirection(receiver, target)) : "",
                Math.round(target.getX() - 0.5),
                Math.round(target.getY() - 0.5),
                Math.round(target.getZ() - 0.5)
        );
    }

    public boolean isTicking() {
        return this.ticking;
    }

    public void tickTimer() {
        if (!this.ticking) {
            return;
        }

        long tick = System.currentTimeMillis();

        if (tick - this.prevTick >= 1000L) {
            this.prevTick = tick;
            this.timer--;

            if (this.timer <= 0) {
                this.timer = 0;
                this.ticking = false;

                for (var i: this.server.getPlayerList().getPlayers()) {
                    // home 좌표에 ±5
                    int x, z;
                    int y = this.home.getY();
                    int try_ = 0;
                    final int MAX = 5;

                    do {
                        x = this.home.getX() + getRandom(-5, 5);
                        z = this.home.getZ() + getRandom(-5, 5);
                        try_++;
                        if (try_ > MAX) {
                            x = this.home.getX();
                            z = this.home.getZ();
                            break;
                        }
                    } while (!level.getBlockState(new BlockPos(x, y, z)).isAir());

                    i.teleportTo(x, y, z);
                }
            }
        }
    }

    public void sendResultAll(MinecraftServer server) {
        for (ServerPlayer i: server.getPlayerList().getPlayers()) {
            sendResult(i);
        }
    }

    @Nullable
    public String getPosResult(MinecraftServer server, ServerPlayer receiver) {
        String result = null;
        switch (this.posDisplayType) {
            case HIDDEN -> {}
            case ME -> result = getPosStr(receiver, receiver, false);
            case NEAREST -> result = getPosStr(receiver, getNearestPlayer(server, receiver), true);
            case FURTHEST -> result = getPosStr(receiver, getFurthestPlayer(server, receiver), true);
        }
        return result;  // null: hidden
    }

    @Nullable
    public String getTimerResult() {
        return this.timer == 0 ? null : this.getTimerStr();  // null: timer 0
    }

    private void sendResult(ServerPlayer receiver) {
        var posResult = this.getPosResult(server, receiver);
        var timerResult = this.getTimerResult();

        var result = resultJoin("  |  ", posResult, timerResult);
        receiver.connection.send(
                new ClientboundSetActionBarTextPacket(Component.literal(result)));
    }

    @Nullable
    private static Player getNearestPlayer(MinecraftServer server, Player player) {
        Player nearest = null;
        for (var i : server.getPlayerList().getPlayers()) {
            if (player.getUUID() == i.getUUID()) {
                continue;
            }

            if (nearest == null || player.distanceToSqr(i) < player.distanceToSqr(nearest)) {
                nearest = i;
            }
        }

        return nearest;  // 플레이어가 한 명 있을 땐 null이다.
    }

    @Nullable
    private static Player getFurthestPlayer(MinecraftServer server, Player player) {
        Player furthest = null;
        for (var i : server.getPlayerList().getPlayers()) {
            if (player.getUUID() == i.getUUID()) {
                continue;
            }

            if (furthest == null || player.distanceToSqr(i) > player.distanceToSqr(furthest)) {
                furthest = i;
            }
        }

        return furthest;  // 플레이어가 한 명 있을 땐 null이다.
    }

    private static char getDirection(Player receiver, Player target) {
        String arrows = "↑↗→↘↓↙←↖";

        var v1 = receiver.getLookAngle();
        var lookAt = new Vec2((float) v1.x, (float) v1.z);
        var posVec = new Vec2((float) (target.getX() - receiver.getX()),
                (float) (target.getZ() - receiver.getZ()));

        var angle = Math.asin(lookAt.y / lookAt.length());
        var lookAngle = lookAt.x >= 0 ? angle : Math.PI * 2 - angle;  // 오른쪽 각도

        return arrows.charAt(1);
    }

    private static String resultJoin(String str, String ...args) {
        ArrayList<String> result = new ArrayList<>();
        for (var i : args) {
            if (i != null) {
                result.add(i);
            }
        }

        return String.join(str, result);
    }

    private int getRandom(int from, int to) {
        return this.RANDOM.nextInt(to) + from + 1;
    }
}
