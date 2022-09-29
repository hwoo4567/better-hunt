package com.worldedit1234.hunt.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.worldedit1234.hunt.setup.Info;
import com.worldedit1234.hunt.setup.Setup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CommandRegistry {
    public CommandRegistry(CommandDispatcher<CommandSourceStack> dispatcher) {
        var actionbar = Position.getPosition();

        LiteralArgumentBuilder<CommandSourceStack> cmd =
        Commands.literal("betterHunt")
        .then(Commands.literal("position")
                .then(Commands.literal("hide")
                        .executes(context -> actionbar.setDisplayType(PosType.HIDDEN)))
                .then(Commands.literal("me")
                        .executes(context -> actionbar.setDisplayType(PosType.ME)))
                .then(Commands.literal("nearest")
                        .executes(context -> actionbar.setDisplayType(PosType.NEAREST)))
                .then(Commands.literal("furthest")
                        .executes(context -> actionbar.setDisplayType(PosType.FURTHEST)))
                // position + no argument
                .executes(context -> sendMessage(context, "Position Display Type: %s",
                        actionbar.getPosType().name().toLowerCase())))

        .then(Commands.literal("border")
                .then(Commands.literal("set")
                        .then(Commands.argument("size", IntegerArgumentType.integer(10))
                                .executes(CommandRegistry::setBorderSize)))
                // border + no argument
                .executes(CommandRegistry::getBorderSize))
        .then(Commands.literal("info")
                .then(Commands.literal("recipes")
                        .executes(context -> sendMessage(context, String.join("\n", Info.recipes))))
                // info + no argument
                .executes(context -> sendMessage(context, Info.description)));

        dispatcher.register(cmd);
    }

    // throws CommandSyntaxException
    private static int sendMessage(CommandContext<CommandSourceStack> commandContext, String message, Object... args)  {
        var entity = commandContext.getSource().getEntity();
        if (entity instanceof Player player) {
            player.sendSystemMessage(Component.literal(String.format(message, args)));
        }

        return 1;
    }

    private static int getBorderSize(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "size");
        return sendMessage(context, "Border size is %d*%d.", value, value);
    }

    private static int setBorderSize(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "size");
        Setup.worldBorder(context.getSource().getServer(), value);

        return 1;
    }
}
