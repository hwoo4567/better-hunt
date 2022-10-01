package com.worldedit1234.hunt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.worldedit1234.hunt.setup.Info;
import com.worldedit1234.hunt.setup.Setup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CommandRegistry {
    private final Actionbar actionbar;
    private int result;

    public CommandRegistry(CommandDispatcher<CommandSourceStack> dispatcher) {
        actionbar = Actionbar.getActionbar();

        // ****************************** command ******************************
        LiteralArgumentBuilder<CommandSourceStack> cmd =
        Commands.literal("betterHunt")
        .then(Commands.literal("position")
                .then(Commands.literal("hide")
                        .executes(context -> actionbar.setPosType(PosType.HIDDEN)))
                .then(Commands.literal("me")
                        .executes(context -> actionbar.setPosType(PosType.ME)))
                .then(Commands.literal("nearest")
                        .executes(context -> actionbar.setPosType(PosType.NEAREST)))
                .then(Commands.literal("furthest")
                        .executes(context -> actionbar.setPosType(PosType.FURTHEST)))
                // position + no arguments
                .executes(context -> sendResult(context, "Position Display Type: %s",
                        actionbar.getPosType().name().toLowerCase())))

        .then(Commands.literal("timer")
                .then(Commands.literal("start")
                        .then(Commands.argument("second", IntegerArgumentType.integer(1))
                                .executes(this::setAndStartTimer)))
                .then(Commands.literal("stop")
                        .executes(context -> actionbar.stopTimer()))
                // timer + no arguments
                .executes(this::getTimerInfo))

        .then(Commands.literal("border")
                .then(Commands.literal("set")
                        .then(Commands.argument("size", IntegerArgumentType.integer(10))
                                .executes(this::setBorderSize)))
                // border + no arguments
                .executes(this::sendBorderSize))

        .then(Commands.literal("info")
                .then(Commands.literal("recipes")
                        .executes(context -> sendResultAll(context, String.join("\n", Info.recipes))))
                // info + no arguments
                .executes(context -> sendResultAll(context, Info.description)));
        // ****************************** commands ******************************
        dispatcher.register(cmd);
        result = 1;
    }

    public int getResult() {
        return this.result;
    }

    private int sendResult(CommandContext<CommandSourceStack> context, String message, Object... args) {
        var entity = context.getSource().getEntity();
        if (entity instanceof Player player) {
            sendMessage(player, message, args);
        }

        return 1;
    }

    private int sendResultAll(CommandContext<CommandSourceStack> context, String message, Object... args) {
        var server = context.getSource().getServer();
        for (var i: server.getPlayerList().getPlayers()) {
            sendMessage(i, message, args);
        }

        return 1;
    }

    private void sendMessage(Player player, String message, Object... args) {
        player.sendSystemMessage(Component.literal(String.format(message, args)));
    }

    private int setAndStartTimer(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        actionbar.setHome(source.getServer(), source.getLevel(),
                new BlockPos(source.getPosition()));
        actionbar.startTimer(IntegerArgumentType.getInteger(context, "second"));
        return 1;
    }

    private int getTimerInfo(CommandContext<CommandSourceStack> context) {
        var home = actionbar.getHome();
        sendResultAll(
                context,
                "second: %d\nTicking: %b\nHome: %s",
                actionbar.getTimer(),
                actionbar.isTicking(),
                home == null ? "???" : home.toString()
        );

        return 1;
    }

    private int sendBorderSize(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "size");
        return sendResult(context, "Border size is %d*%d.", value, value);
    }

    private int setBorderSize(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "size");
        Setup.worldBorder(context.getSource().getServer(), value);

        return 1;
    }
}
