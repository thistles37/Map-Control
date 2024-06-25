package com.thistles.common.commands;


import com.thistles.common.console.Messages;
import com.thistles.common.console.PluginInfo;
import com.thistles.nmshandler.NMSHandler;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.thistles.common.utils.MapUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

public class Commands implements CommandExecutor {
    public Commands() {
    }

    private final HashMap<Player, Stack<HashMap<Integer, CompoundTag>>> mapActions = new HashMap<>();

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may execute this command!");
            return true;
        } else {
            Player p = (Player) sender;

            if (args.length == 0) {
                p.sendMessage(PluginInfo.getPluginName() + " " + PluginInfo.getPluginVersion() + " by " + PluginInfo.getPluginAuthor());
                return false;
            }

            final String argument = args[0].toLowerCase();
            ItemStack heldItem = p.getInventory().getItemInMainHand();
            World world = p.getWorld();
            NamedTag mapNamedTag;
            File mapFile;

            /*
            Create nms handler
             */
            NMSHandler nms;
            try {
                String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3].substring(1);
                nms = (NMSHandler) Class.forName("com.thistles.nms.NMSHandler_" + version).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            if (argument.equals("help") && p.hasPermission("mapcontrol.help")) {
                p.sendMessage(ChatColor.GOLD + "/map help: " + ChatColor.RESET + "command information");
                p.sendMessage(ChatColor.GOLD + "/map clear: " + ChatColor.RESET + "clear map data");
                p.sendMessage(ChatColor.GOLD + "/map unlock: " + ChatColor.RESET + "unlock map");
                p.sendMessage(ChatColor.GOLD + "/map undo: " + ChatColor.RESET + "revert map actions");
                p.sendMessage(ChatColor.GOLD + "/map tp: " + ChatColor.RESET + "teleport to map origin");
                p.sendMessage(ChatColor.GOLD + "/map get: " + ChatColor.RESET + "receive map on an item frame");
                return true;
            }

            if (argument.equals("get") && p.hasPermission("mapcontrol.get")) {
                ItemStack map = MapUtils.getMap(world, p);
                if (map != null) {
                    p.getInventory().addItem(map);
                    p.sendMessage(Messages.FRAMED_MAP_FOUND);
                } else {
                    p.sendMessage(Messages.NO_FRAMED_MAP_FOUND);
                }
                return true;
            }

            if (argument.equals("undo") && p.hasPermission("mapcontrol.undo")) {
                try {
                    if (!MapUtils.actionExists(p)) {
                        p.sendMessage(Messages.NO_UNDO_AVAILABLE);
                        return true;
                    }
                    HashMap<Integer, CompoundTag> action = MapUtils.retrieveAction(p);
                    int key = action.keySet().iterator().next();
                    File actionFile = MapUtils.getMapData(world, key);
                    NamedTag actionNamedTag = MapUtils.readMapData(actionFile);
                    MapUtils.undoAction(world, actionNamedTag, action);
                    nms.removeCache(key);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                p.sendMessage(Messages.UNDO_SUCCESS);
                return true;
            }

            if (argument.equals("clear") && p.hasPermission("mapcontrol.clear")) {
                if (!MapUtils.isMap(heldItem)) {
                    p.sendMessage(Messages.NO_MAP_IN_HAND);
                    return true;
                }
                int id = MapUtils.getMapId(heldItem);
                try {
                    mapFile = MapUtils.getMapData(world, id);
                    if (!mapFile.exists()) {
                        p.sendMessage(Messages.MAP_DATA_NOT_FOUND);
                        return true;
                    }
                    mapNamedTag = MapUtils.readMapData(mapFile);
                    MapUtils.storeAction(p, mapNamedTag, id);
                    MapUtils.clearMap(mapNamedTag);
                    MapUtils.saveMapData(world, mapNamedTag, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                nms.removeCache(id);
                p.sendMessage(Messages.CLEAR_MAP_SUCCESS);
                return true;
            }

            if (argument.equals("unlock") && p.hasPermission("mapcontrol.unlock")) {
                if (!MapUtils.isMap(heldItem)) {
                    p.sendMessage(Messages.NO_MAP_IN_HAND);
                    return true;
                }
                int id = MapUtils.getMapId(heldItem);
                try {
                    mapFile = MapUtils.getMapData(world, id);
                    if (!mapFile.exists()) {
                        p.sendMessage(Messages.MAP_DATA_NOT_FOUND);
                        return true;
                    }
                    mapNamedTag = MapUtils.readMapData(mapFile);
                    MapUtils.storeAction(p, mapNamedTag, id);
                    MapUtils.unlockMap(mapNamedTag);
                    MapUtils.saveMapData(world, mapNamedTag, id);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                nms.removeCache(id);
                p.sendMessage(Messages.UNLOCK_MAP_SUCCESS);
                return true;
            }

            if (argument.equals("tp") && p.hasPermission("mapcontrol.tp")) {
                if (!MapUtils.isMap(heldItem)) {
                    p.sendMessage(Messages.NO_MAP_IN_HAND);
                    return true;
                }
                int id = MapUtils.getMapId(heldItem);
                try {
                    mapFile = MapUtils.getMapData(world, id);
                    if (!mapFile.exists()) {
                        p.sendMessage(Messages.MAP_DATA_NOT_FOUND);
                        return true;
                    }
                    mapNamedTag = MapUtils.readMapData(mapFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Location coordinates = MapUtils.getMapCoordinates(world, MapUtils.getMapCompound(mapNamedTag));
                MapUtils.tpToMap(p, coordinates);
                p.sendMessage(Messages.TELEPORTING_TO_DESTINATION);
                return true;
            }

            p.sendMessage(Messages.NO_PERMISSIONS);
            return true;
        }
    }
}
