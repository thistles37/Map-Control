package com.thistles.mapcontrol.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TabCompletion implements TabCompleter {

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            if (player.hasPermission("mapcontrol.use")) {
                arguments.add("clear");
                arguments.add("unlock");
                arguments.add("tp");
                arguments.add("get");
        }
        return arguments;
        } else if (args.length > 1) {
            return new ArrayList<>();
        }
        return null;
    }
}