package com.worldedit1234.hunt.command;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class Actionbar {
    private static Actionbar actionbar = null;
    private final Random RANDOM = new Random();
    private PosType posDisplayType;
    private int timer;  // second
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
                direction ? String.format(" (%s)", getDirectionStr(receiver, target)) : "",
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

        return nearest;  // 플레이어가 한 명 있을 땐 null
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

        return furthest;  // 플레이어가 한 명 있을 땐 null
    }

    private static String getDirectionStr(Player receiver, Player target) {
        return getDirectionStr(receiver, target.getX(), target.getZ());
    }

    private static String getDirectionStr(Player receiver, double x, double z) {
        final String[] ARROWS = {"↑", "⬉", "←", "⬋", "↓", "⬊", "→", "⬈"};
        float look = receiver.getYRot();  // 가로 회전

        // vector.x : X좌표, vector.y : Z좌표
        var posVec = new Vec2((float) (x - receiver.getX()), (float) (z - receiver.getZ()));

        var length = posVec.length();
        if (length == 0) {
            return "HERE";
        }

        var angle0 = Math.acos(posVec.y / length);
        float posAngle = (float) Math.toDegrees(posVec.x >= 0 ? -angle0 : angle0);

        var angle = new Angle(look - posAngle);
        int index = (int) (angle.rotate(22.5F).getDegree() / 45F);
        return ARROWS[index];
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
