package com.worldedit1234.hunt.command;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class Position {
    private static Position position = null;
    private PosType posDisplayType;

    private Position() {
        this.posDisplayType = PosType.HIDDEN;
    }

    public static Position getPosition() {
        if (position == null) {
            position = new Position();
        }
        return position;
    }

    public int setDisplayType(PosType posType) {
        this.posDisplayType = posType;
        return 1;
    }

    public PosType getPosType() {
        return this.posDisplayType;
    }

    public void showPosition(MinecraftServer server, ServerPlayer reciever) {
        switch (this.posDisplayType) {
            case HIDDEN -> {}
            case ME -> showPosition(reciever, reciever);
            case NEAREST -> showPosition(reciever, getNearestPlayer(server, reciever));
            case FURTHEST -> showPosition(reciever, getFurthestPlayer(server, reciever));
        }
    }

    private void showPosition(ServerPlayer reciever, @Nullable Player target) {
        if (target == null) {
            reciever.connection.send(
                    new ClientboundSetActionBarTextPacket(Component.literal("??? : ? ? ?")));
            return;
        }

        var text = String.format(
                "%s : %d %d %d",
                target.getName().getString(),
                Math.round(target.getX() - 0.5D),
                Math.round(target.getY() - 0.5D),
                Math.round(target.getZ() - 0.5D)
        );
        var textPacket = new ClientboundSetActionBarTextPacket(Component.literal(text));
        reciever.connection.send(textPacket);
    }

    public void showPositionAll(MinecraftServer server) {
        for (ServerPlayer i: server.getPlayerList().getPlayers()) {
            showPosition(server, i);
        }
    }

    public void storePosAll(MinecraftServer server) {

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
}
